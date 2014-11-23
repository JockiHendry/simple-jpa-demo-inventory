/*
 * Copyright 2014 Jocki Hendry.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package domain.retur

import domain.event.PerubahanStok
import domain.exception.BarangSelisih
import domain.exception.DataTidakBolehDiubah
import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.inventory.SebuahDaftarBarang
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import project.user.NomorService
import simplejpa.DomainClass
import griffon.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang')
abstract class ReturJual implements SebuahDaftarBarang {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200)
    String keterangan

    @NotNull
    Boolean sudahDiproses = false

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    PengeluaranBarang pengeluaranBarang

    @NotEmpty @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true) @OrderColumn @Valid
    List<ItemRetur> items = []

    void tambah(ItemRetur itemRetur) {
        // Periksa klaim untuk item retur ini
        if (itemRetur.jumlahBarangDitukar() > itemRetur.jumlah) {
            throw new BarangSelisih("${itemRetur.produk.nama}: jumlah yang ditukar (${itemRetur.jumlahBarangDitukar()}) melebihi jumlah yang diterima (${itemRetur.jumlah})!")
        }
        items << itemRetur
    }

    List<Klaim> getKlaims(Class clazz, boolean hanyaBelumDiproses = false) {
        def hasil = []
        items.each { hasil.addAll(it.getKlaims(clazz, hanyaBelumDiproses)) }
        hasil
    }

    List<KlaimTukar> getKlaimsTukar(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimTukar, hanyaBelumDiproses)
    }

    DaftarBarang getDaftarBarangServis(boolean hanyaBelumDiproses = false) {
        new DaftarBarangSementara(getKlaims(KlaimServis, hanyaBelumDiproses).collect {
            new ItemBarang(it.produk, it.jumlah)
        }, -1)
    }

    Integer jumlahDitukar() {
        items.sum { ItemRetur i -> i.jumlahBarangDitukar() + i.jumlahBarangDiservis() }?: 0
    }

    void proses(Klaim klaim) {
        klaim.proses()
        sudahDiproses = items.every { it.isSudahDiproses() }?: false
    }

    void prosesKlaimServis() {
        getKlaims(KlaimServis, true).each { proses(it) }
    }

    PengeluaranBarang tukar(Gudang gudang, String namaKonsumen, boolean pakaiYangSudahDipesan = true) {
        if (this.pengeluaranBarang != null) {
            throw new DataTidakBolehDiubah('Penukaran telah dilakukan!', this)
        }
        DaftarBarangSementara daftarYangHarusDitukar = yangHarusDitukar()
        if (daftarYangHarusDitukar.items.empty) {
            throw new UnsupportedOperationException("Tidak ada penukaran yang dapat dilakukan untuk retur jual [$nomor]")
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: gudang,
            keterangan: "Retur Jual [$nomor]"
        )
        daftarYangHarusDitukar.items.each { pengeluaranBarang.tambah(it) }
        pengeluaranBarang.diterima(LocalDate.now(), namaKonsumen, '[Retur Jual]')
        this.pengeluaranBarang = pengeluaranBarang

        // Semua klaim ditukar sudah diproses
        getKlaimsTukar(true).each { proses(it) }

        // Event perubahan stok
        ReferensiStok ref = new ReferensiStokBuilder(pengeluaranBarang, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, ref, false, pakaiYangSudahDipesan))

        pengeluaranBarang
    }

    void hapusPenukaran() {
        if (!pengeluaranBarang) {
            throw DataTidakBolehDiubah("Tidak ada pengeluaran barang yang bisa dihapus!")
        }
        pengeluaranBarang.items.each { ItemBarang i ->
            for (KlaimTukar k : getKlaimsTukar()) {
                if ((k.produk == i.produk) && (i.jumlah == i.jumlah) && (k.sudahDiproses)) {
                    k.sudahDiproses = false
                }
            }
        }
        sudahDiproses = false
        pengeluaranBarang = null
    }

    abstract PengeluaranBarang tukar()

    @Override
    DaftarBarang toDaftarBarang() {
        def itemBarangs = items.collect { new ItemBarang(it.produk, it.jumlah) }
        DaftarBarangSementara hasil = new DaftarBarangSementara(itemBarangs)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil.keterangan = keterangan
        hasil
    }

    List<ItemRetur> normalisasi() {
        List<ItemRetur> hasil = []
        items.groupBy { it.produk }.each { k, v ->
            ItemRetur itemRetur = new ItemRetur(v[0].produk, v[0].jumlah, [] as Set)
            v[0].klaims.each { itemRetur.tambahKlaim(it.clone()) }
            if (v.size() > 1) {
                for (int i=1; i<v.size(); i++) {
                    itemRetur.merge(v[i])
                }
            }
            hasil << itemRetur
        }
        hasil
    }

    DaftarBarang yangHarusDitukar() {
        new DaftarBarangSementara(getKlaimsTukar(true).collect { new ItemBarang(it.produk, it.jumlah) })
    }

}


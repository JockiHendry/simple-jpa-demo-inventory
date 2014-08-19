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
package domain.penjualan

import domain.exception.DataTidakBolehDiubah
import domain.faktur.Faktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.KewajibanPembayaran
import domain.faktur.Pembayaran
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import project.user.NomorService
import domain.validation.InputPenjualanOlehSales
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.joda.time.*
import griffon.util.*
import javax.validation.groups.Default

@NamedEntityGraphs([
    @NamedEntityGraph(name='FakturJualOlehSales.Complete', attributeNodes=[
        @NamedAttributeNode('listItemFaktur'),
        @NamedAttributeNode('piutang'),
        @NamedAttributeNode('bonusPenjualan'),
    ]),
    @NamedEntityGraph(name='FakturJualOlehSales.PengeluaranBarang', attributeNodes=[
        @NamedAttributeNode('listItemFaktur'),
        @NamedAttributeNode('pengeluaranBarang'),
        @NamedAttributeNode('bonusPenjualan'),
    ]),
    @NamedEntityGraph(name='FakturJualOlehSales.Items', attributeNodes = [
        @NamedAttributeNode('listItemFaktur')
    ])
])
@DomainClass @Entity @Canonical(excludes='piutang,bonusPenjualan') @EqualsAndHashCode(callSuper=true, excludes='piutang,bonusPenjualan')
class FakturJualOlehSales extends FakturJual {

    @NotNull(groups=[Default,InputPenjualanOlehSales]) @ManyToOne
    Konsumen konsumen

    @NotNull(groups=[Default]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate jatuhTempo

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    KewajibanPembayaran piutang

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    BonusPenjualan bonusPenjualan

    void kirim(String alamatTujuan, String namaSupir, LocalDate tanggal = LocalDate.now()) {
        if (status==StatusFakturJual.DIANTAR || !status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }

        // Buat PengeluaranBarang berdasarkan data yang ada di faktur
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(), gudang: konsumen.sales.gudang,
            alamatTujuan: alamatTujuan, namaSupir: namaSupir
        )
        pengeluaranBarang.items = barangYangHarusDikirim().items
        tambah(pengeluaranBarang)
    }

    void tambah(BuktiTerima buktiTerima) {
        super.tambah(buktiTerima)
        piutang = new KewajibanPembayaran(jumlah: total())

        // Menambahkan poin pada konsumen
        konsumen.tambahPoin(pengeluaranBarang)
    }

    void bayar(Pembayaran pembayaran) {
        if (status!=StatusFakturJual.DITERIMA || piutang==null) {
            throw new DataTidakBolehDiubah(this)
        }
        piutang.bayar(pembayaran)
        if (piutang.lunas) {
            status = StatusFakturJual.LUNAS
            konsumen.hapusFakturBelumLunas(this)
        }
    }

    void hapus(Pembayaran pembayaran) {
        if (status!=StatusFakturJual.DITERIMA || piutang==null) {
            throw new DataTidakBolehDiubah(this)
        }
        piutang.hapus(pembayaran)
    }

    @Override
    void hapusPengeluaranBarang() {
        if (!konsumen.sales.dalamKota()) {
            throw new DataTidakBolehDiubah(this)
        }
        super.hapusPengeluaranBarang()
    }

    @Override
    void hapusBuktiTerima() {
        if (piutang!=null && piutang.jumlahDibayar() > 0) {
            throw new DataTidakBolehDiubah(this)
        }
        super.hapusBuktiTerima()
        piutang = null

        // Menghapus poin pada konsumen
        konsumen.hapusPoin(pengeluaranBarang.toPoin())
    }

    BigDecimal sisaPiutang() {
        piutang.sisa()
    }

    BigDecimal jumlahPiutang() {
        piutang.jumlah
    }

    BigDecimal jumlahDibayar() {
        piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_POTONGAN)
    }

    BigDecimal potonganPiutang() {
        piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN)
    }

    void tambahBonus(DaftarBarangSementara daftarBarang) {
        tambahBonus(daftarBarang.items)
    }

    void tambahBonus(List<ItemBarang> listItemBarang) {
        if (!status.bolehDiubah || bonusPenjualan) {
            throw new DataTidakBolehDiubah(bonusPenjualan)
        }
        BonusPenjualan bonusPenjualan = new BonusPenjualan(
            tanggal: tanggal, gudang: konsumen.sales.gudang,
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BONUS)
        )
        listItemBarang.each { bonusPenjualan.tambah(it) }
        this.bonusPenjualan = bonusPenjualan
    }

    void hapusBonus() {
        if (!status.bolehDiubah) {
            throw new DataTidakBolehDiubah(this.bonusPenjualan)
        }
        if (!bonusPenjualan) {
            throw new IllegalStateException('Bonus penjualan tidak ditemukan!')
        }
        bonusPenjualan = null
    }

    boolean sudahJatuhTempo(LocalDate padaTanggal = LocalDate.now()) {
        padaTanggal.equals(jatuhTempo) || padaTanggal.isAfter(jatuhTempo)
    }

    DaftarBarangSementara barangYangHarusDikirim() {
        DaftarBarangSementara hasil = toDaftarBarangSementara()
        if (bonusPenjualan) {
            hasil += bonusPenjualan
        }
        hasil.items = hasil.normalisasi()
        hasil
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Faktur faktur = (Faktur) o

        if (nomor != faktur.nomor) return false

        return true
    }

    int hashCode() {
        if (nomor) {
            return nomor.hashCode()
        } else {
            return 1
        }
    }

}


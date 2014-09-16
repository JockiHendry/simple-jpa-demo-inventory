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

import domain.event.PerubahanStok
import domain.event.PesanStok
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.KewajibanPembayaran
import domain.faktur.Pembayaran
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.pembelian.PenerimaanBarang
import project.inventory.GudangRepository
import project.user.NomorService
import domain.validation.InputPenjualanOlehSales
import groovy.transform.*
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil

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
@DomainClass @Entity @Canonical(excludes='piutang,bonusPenjualan,retur') @EqualsAndHashCode(callSuper=true, excludes='piutang,bonusPenjualan,retur')
class FakturJualOlehSales extends FakturJual {

    @NotNull(groups=[Default,InputPenjualanOlehSales]) @ManyToOne
    Konsumen konsumen

    @NotNull(groups=[Default]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate jatuhTempo

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    KewajibanPembayaran piutang

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    BonusPenjualan bonusPenjualan

    Boolean kirimDariGudangUtama

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) @JoinTable(name='FakturJual_retur') @OrderColumn
    List<PenerimaanBarang> retur = []

    void kirim(String alamatTujuan, LocalDate tanggal = LocalDate.now(), String keterangan = null) {
        if (status==StatusFakturJual.DIANTAR || !status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }

        // Buat PengeluaranBarang berdasarkan data yang ada di faktur
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(), gudang: kirimDari(), keterangan: keterangan, alamatTujuan: alamatTujuan
        )
        pengeluaranBarang.items = barangYangHarusDikirim().items
        ApplicationHolder.application.event(new PesanStok(this, true))
        tambah(pengeluaranBarang)
    }

    Gudang kirimDari() {
        kirimDariGudangUtama? (SimpleJpaUtil.instance.repositoryManager.findRepository('GudangRepository') as GudangRepository).cariGudangUtama(): konsumen.sales.gudang
    }

    void tambah(BuktiTerima buktiTerima) {
        super.tambah(buktiTerima)

        // Menambah piutang
        piutang = new KewajibanPembayaran(jumlah: total() - totalRetur())

        // Menambah poin
        DaftarBarangSementara daftarBarang = new DaftarBarangSementara(pengeluaranBarang.items)
        retur.each { daftarBarang -= it}
        daftarBarang.nomor = pengeluaranBarang.nomor
        konsumen.tambahPoin(daftarBarang)
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
        super.hapusPengeluaranBarang()
        ApplicationHolder.application.event(new PesanStok(this))
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

    void tambahRetur(PenerimaanBarang penerimaanBarang) {
        // Periksa apakah barang yang dikembalikan adalah barang yang sudah dipesan sebelumnya.
        if (status == StatusFakturJual.LUNAS) {
            throw new DataTidakBolehDiubah('Faktur jual yang telah lunas tidak boleh di-retur!', this)
        }
        if (piutang && piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_POTONGAN) > 0) {
            throw new DataTidakBolehDiubah('Faktur jual yang telah dibayar tidak boleh di-retur!', this)
        }
        BigDecimal harga = 0
        penerimaanBarang.gudang = kirimDari()
        penerimaanBarang.normalisasi().each { ItemBarang barangRetur ->
            ItemFaktur itemFaktur = listItemFaktur.find { (it.produk == barangRetur.produk) && (it.jumlah >= barangRetur.jumlah) }
            if (itemFaktur) {
                BigDecimal hargaRetur = barangRetur.jumlah * itemFaktur.harga
                harga += itemFaktur.diskon? itemFaktur.diskon.hasil(hargaRetur): hargaRetur
            } else {
                throw new DataTidakKonsisten("Tidak ada penjualan ${barangRetur.produk.nama} sejumlah ${barangRetur.jumlah} di faktur jual ${nomor}!")
            }
        }

        // Tambahkan pada retur
        retur.add(penerimaanBarang)

        // Kurangi piutang bila ada
        if (piutang) {
            piutang.bayar(new Pembayaran(LocalDate.now(), harga, true))
        }

        // Kurangi bonus untuk konsumen tersebut
        if (status == StatusFakturJual.DITERIMA) {
            konsumen.hapusPoin(penerimaanBarang)
        }

        // Lakukan Perubahan stok (bertambah)
        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, this))
    }

    BigDecimal totalRetur() {
        BigDecimal total = 0
        retur.each { PenerimaanBarang penerimaanBarang ->
            penerimaanBarang.items.each { ItemBarang barangRetur ->
                ItemFaktur itemFaktur = listItemFaktur.find { (it.produk == barangRetur.produk) && (it.jumlah >= barangRetur.jumlah) }
                if (itemFaktur) {
                    BigDecimal hargaRetur = barangRetur.jumlah * itemFaktur.harga
                    total += itemFaktur.diskon? itemFaktur.diskon.hasil(hargaRetur): hargaRetur
                } else {
                    throw new DataTidakKonsisten('Barang retur melebihi batas yang ditentukan!', this)
                }
            }
        }
        total
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


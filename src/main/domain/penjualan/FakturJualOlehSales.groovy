/*
 * Copyright 2015 Jocki Hendry.
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
import domain.exception.BarangSelisih
import domain.exception.DataTidakBolehDiubah
import domain.exception.FakturTidakDitemukan
import domain.exception.HargaSelisih
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.KewajibanPembayaran
import domain.faktur.Pembayaran
import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.pengaturan.KeyPengaturan
import domain.penjualan.state.FakturJualOlehSalesDiantar
import domain.penjualan.state.FakturJualOlehSalesDibuat
import domain.penjualan.state.FakturJualOlehSalesDiterima
import domain.penjualan.state.FakturJualOlehSalesLunas
import domain.penjualan.state.FakturJualOlehSalesMulai
import domain.penjualan.state.FakturJualOlehSalesSingkatMulai
import domain.penjualan.state.OperasiFakturJual
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import project.inventory.GudangRepository
import project.pengaturan.PengaturanRepository
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
    @NamedEntityGraph(name='FakturJualOlehSales.Piutang', attributeNodes=[
        @NamedAttributeNode(value='konsumen', subgraph='konsumen'),
        @NamedAttributeNode('piutang')
    ], subgraphs = [
        @NamedSubgraph(name='konsumen', attributeNodes=[
            @NamedAttributeNode('region'),
            @NamedAttributeNode('sales')
        ])
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

    public static final String RETUR_FAKTUR = "ReturFaktur"

    @NotNull(groups=[Default,InputPenjualanOlehSales]) @ManyToOne
    Konsumen konsumen

    @NotNull(groups=[Default]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate jatuhTempo

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    KewajibanPembayaran piutang

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    BonusPenjualan bonusPenjualan

    Boolean kirimDariGudangUtama

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) @JoinTable(name='FakturJual_retur')
    @Fetch(FetchMode.SUBSELECT) @OrderColumn
    List<ReturFaktur> retur = []

    void buatSuratJalan(String alamatTujuan, LocalDate tanggal = LocalDate.now(), String keterangan = null) {
        (getOperasiFakturJual() as FakturJualOlehSalesDibuat).buatSuratJalan(this, alamatTujuan, tanggal, keterangan)
    }

    Gudang kirimDari() {
        kirimDariGudangUtama? (SimpleJpaUtil.instance.repositoryManager.findRepository('GudangRepository') as GudangRepository).cariGudangUtama(): konsumen.sales.gudang
    }

    @Override
    BigDecimal nilaiPenjualan() {
        piutang?.jumlah?: 0
    }

    void bayar(Pembayaran pembayaran) {
        getOperasiFakturJual().proses(this, [operasi: 'bayar', pembayaran: pembayaran])
    }

    void hapusPembayaran(Pembayaran pembayaran) {
        getOperasiFakturJual().proses(this, [operasi: 'hapusPembayaran', pembayaran: pembayaran])
    }

    void hapusPembayaran(String nomorReferensi, String jenisReferensi = null) {
        getOperasiFakturJual().proses(this, [operasi: 'hapusPembayaran', nomorReferensi: nomorReferensi, jenisReferensi: jenisReferensi])
    }

    BigDecimal sisaPiutang() {
        piutang.sisa()
    }

    BigDecimal jumlahPiutang() {
        piutang?.jumlah?: 0
    }

    BigDecimal jumlahDibayar() {
        piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_POTONGAN)
    }

    BigDecimal potonganPiutang() {
        piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    void tambahBonus(DaftarBarangSementara daftarBarang) {
        tambahBonus(daftarBarang.items)
    }

    void tambahBonus(List<ItemBarang> listItemBarang) {
        if (bonusPenjualan) {
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
        if (!bonusPenjualan) {
            throw new DataTidakBolehDiubah('Bonus penjualan tidak ditemukan!')
        }
        bonusPenjualan = null
    }

    boolean sudahJatuhTempo(LocalDate padaTanggal = LocalDate.now()) {
        padaTanggal.equals(jatuhTempo) || padaTanggal.isAfter(jatuhTempo)
    }

    DaftarBarangSementara barangYangHarusDikirim() {
        DaftarBarang hasil = toDaftarBarang()
        if (bonusPenjualan) {
            hasil += bonusPenjualan
        }
        retur.each {
            hasil -= it
        }
        hasil.items = hasil.normalisasi()
        hasil
    }

    BigDecimal prosesTambahRetur(ReturFaktur returFaktur) {
        BigDecimal harga = 0
        returFaktur.gudang = kirimDari()
        returFaktur.normalisasi().each { ItemBarang barangRetur ->
            ItemFaktur itemFaktur = listItemFaktur.find { (it.produk == barangRetur.produk) && (it.jumlah >= barangRetur.jumlah) }
            if (itemFaktur) {
                harga += (barangRetur.jumlah * (itemFaktur.diskon? itemFaktur.diskon.hasil(itemFaktur.harga): itemFaktur.harga))
            } else {
                throw new BarangSelisih("Tidak ada penjualan ${barangRetur.produk.nama} sejumlah ${barangRetur.jumlah} di faktur jual ${nomor}!")
            }
        }
        retur.add(returFaktur)
        ReferensiStok ref = new ReferensiStokBuilder(returFaktur, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(returFaktur, ref))
        harga
    }

    ReturFaktur prosesHapusRetur(String nomor) {
        // Periksa apakah barang yang dikembalikan adalah barang yang sudah dipesan sebelumnya.
        if (status == StatusFakturJual.LUNAS) {
            throw new DataTidakBolehDiubah('Faktur jual yang telah lunas tidak boleh di-retur!', this)
        }
        if (piutang && piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_POTONGAN) > 0) {
            throw new DataTidakBolehDiubah('Faktur jual yang telah dibayar tidak boleh di-retur!', this)
        }
        if (piutang && !piutang.listPembayaran.find {(it.referensi.namaClass == RETUR_FAKTUR) && (it.referensi.nomor == nomor)}) {
            throw new DataTidakBolehDiubah('Retur akan mempengaruhi integritas data! Penghapusan retur ini akan menyebabkan piutang bertambah!\n' +
                'Solusi: Hapus faktur ini dan buat faktur baru dengan nilai yang aktual.', this)
        }

        ReturFaktur returFaktur = retur.find { it.nomor == nomor }
        if (!returFaktur) {
            throw new FakturTidakDitemukan(nomor)
        }
        retur.remove(returFaktur)

        // Lakukan Perubahan stok (berkurang akibat invers)
        ReferensiStok ref = new ReferensiStokBuilder(returFaktur, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(returFaktur, ref, true))
        returFaktur
    }

    BigDecimal totalRetur() {
        BigDecimal total = 0
        retur.each { ReturFaktur returFaktur ->
            returFaktur.items.each { ItemBarang barangRetur ->
                ItemFaktur itemFaktur = listItemFaktur.find { (it.produk == barangRetur.produk) && (it.jumlah >= barangRetur.jumlah) }
                if (itemFaktur) {
                    BigDecimal hargaRetur = barangRetur.jumlah * itemFaktur.harga
                    total += itemFaktur.diskon? itemFaktur.diskon.hasil(hargaRetur): hargaRetur
                } else {
                    throw new HargaSelisih('Barang retur melebihi batas yang ditentukan!')
                }
            }
        }
        total
    }

    BigDecimal totalSetelahRetur() {
        total() - totalRetur()
    }

    @Override
    boolean isBolehPesanStok() {
        kirimDari().utama
    }

    @Override
    List<ItemBarang> yangDipesan() {
        barangYangHarusDikirim().items
    }

    @Override
    OperasiFakturJual getOperasiFakturJual() {
        PengaturanRepository pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan') as PengaturanRepository
        switch (status) {
            case null:
                if (pengaturanRepository.getValue(KeyPengaturan.WORKFLOW_GUDANG)) {
                    return new FakturJualOlehSalesMulai()
                } else {
                    return new FakturJualOlehSalesSingkatMulai()
                }
            case StatusFakturJual.DIBUAT: return new FakturJualOlehSalesDibuat()
            case StatusFakturJual.DIANTAR: return new FakturJualOlehSalesDiantar()
            case StatusFakturJual.DITERIMA: return new FakturJualOlehSalesDiterima()
            case StatusFakturJual.LUNAS: return new FakturJualOlehSalesLunas()
        }
        null
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


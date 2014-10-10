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
package domain.pembelian

import domain.event.PerubahanStok
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.faktur.Faktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.Pembayaran
import domain.retur.ReturBeli
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import domain.validation.InputPurchaseOrder
import groovy.transform.Canonical
import simplejpa.DomainClass
import griffon.util.*
import simplejpa.SimpleJpaUtil
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedEntityGraphs
import javax.persistence.NamedSubgraph
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderColumn
import javax.validation.constraints.NotNull
import javax.validation.groups.Default
import java.text.NumberFormat

@NamedEntityGraphs([
    @NamedEntityGraph(name='PurchaseOrder.Complete', attributeNodes = [
        @NamedAttributeNode(value='listItemFaktur'),
        @NamedAttributeNode(value='fakturBeli', subgraph='faktur'),
        @NamedAttributeNode(value='listPenerimaanBarang'),
    ], subgraphs = [
        @NamedSubgraph(name='faktur', attributeNodes = [
            @NamedAttributeNode(value = 'listItemFaktur')
        ])
    ])
])
@DomainClass @Entity @Canonical(excludes='fakturBeli,listPenerimaanBarang')
class PurchaseOrder extends Faktur {

    @NotNull(groups=[Default,InputPurchaseOrder]) @ManyToOne
    Supplier supplier

    @Enumerated
    StatusPurchaseOrder status = StatusPurchaseOrder.DIBUAT

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    FakturBeli fakturBeli

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name='PO_ID') @OrderColumn(name='PO_ORDER')
    List<PenerimaanBarang> listPenerimaanBarang = []

    void tambah(PenerimaanBarang penerimaanBarang) {
        if (!status.penerimaanBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }

        // Melakukan pemeriksaan apakah barang yang ditambahkan adalah bagian dari yang dipesan.
        List<ItemBarang> sisaBarang = sisaBelumDiterima()
        penerimaanBarang.normalisasi().each { ItemBarang i ->
            if (!sisaBarang.find { i.produk == it.produk && i.jumlah <= it.jumlah }) {
                throw new DataTidakKonsisten("Tidak pemesanan ${i.produk.nama} sejumlah ${i.jumlah} yang dipesan untuk PO ${nomor}!", penerimaanBarang)
            }
        }

        penerimaanBarang.gudang = (SimpleJpaUtil.instance.repositoryManager.findRepository('GudangRepository') as GudangRepository).cariGudangUtama()

        listPenerimaanBarang << penerimaanBarang
        if (diterimaPenuh() && fakturBeli) {
            status = StatusPurchaseOrder.OK
            if (!fakturBeli.hutang) {
                fakturBeli.buatHutang()
            }
        } else if (diterimaPenuh()) {
            status = StatusPurchaseOrder.BARANG_DITERIMA
        }

        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, this))
    }

    void hapus(PenerimaanBarang penerimaanBarang) {
        if (!status.penerimaanBolehDiubah || penerimaanBarang.deleted == 'Y') {
            throw new DataTidakBolehDiubah(this)
        }
        penerimaanBarang.deleted = 'Y'
        if (isPenerimaanKosong()) {
            if (fakturBeli) {
                status = StatusPurchaseOrder.FAKTUR_DITERIMA
            } else {
                status = StatusPurchaseOrder.DIBUAT
            }
        }
        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, this, true))
    }

    void hapus(Pembayaran pembayaranHutang) {
        if (status!=StatusPurchaseOrder.OK || fakturBeli==null) {
            throw new DataTidakBolehDiubah(this)
        }
        if (!fakturBeli.hutang) {
            throw new IllegalStateException('Pembayaran hutang tidak dapat dibayar untuk purchase order ini')
        }
        fakturBeli.hutang.hapus(pembayaranHutang)
    }


    void tambah(FakturBeli f, boolean strictMode = true) {
        if (!status.fakturBolehDiubah || fakturBeli) {
            throw new DataTidakBolehDiubah(this)
        }

        // Memeriksa apakah isi faktur baru sama dengan isi barang yang dipesan
        if (!toDaftarBarang().isiSamaDengan(f.toDaftarBarang())) {
            throw new DataTidakKonsisten("Faktur ${f.nomor} berbeda dengan yang dipesan untuk PO ${nomor}!", f)
        }
        if (strictMode) {
            if (f.total().compareTo(this.total()) != 0) {
                throw new DataTidakKonsisten("Total untuk faktur ${f.nomor} sebesar ${NumberFormat.currencyInstance.format(f.total())} " +
                    "berbeda dengan yang dipesan untuk PO ${nomor} sebesar " +
                    "${NumberFormat.currencyInstance.format(total())}!", f)
            }
        }

        fakturBeli = f
        if (diterimaPenuh()) {
            status = StatusPurchaseOrder.OK
            fakturBeli.buatHutang()
        } else {
            status = StatusPurchaseOrder.FAKTUR_DITERIMA
        }
    }

    void hapusFaktur() {
        if (!status.fakturBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        fakturBeli = null
        if (diterimaPenuh()) {
            status = StatusPurchaseOrder.BARANG_DITERIMA
        } else {
            status = StatusPurchaseOrder.DIBUAT
        }
    }

    void bayar(Pembayaran pembayaranHutang) {
        if (status!=StatusPurchaseOrder.OK || fakturBeli==null) {
            throw new DataTidakBolehDiubah(this)
        }
        if (!fakturBeli.hutang) {
            throw new IllegalStateException('Hutang belum dapat dibayar untuk purchase order ini')
        }
        fakturBeli.hutang.bayar(pembayaranHutang)
        if (fakturBeli.hutang.lunas) {
            status = StatusPurchaseOrder.LUNAS
        }
    }

    void bayar(ReturBeli returBeli) {
        if (returBeli.supplier != supplier) {
            throw new IllegalArgumentException("Supplier retur [${returBeli.supplier}] tidak sama dengan di PO [$supplier].")
        }
        if (returBeli.sudahDiterima) {
            throw new IllegalArgumentException("Retur [${returBeli.nomor} sudah pernah diproses dan tidak dapat dipakai lagi.")
        }
        Pembayaran pembayaran = new Pembayaran(LocalDate.now(), returBeli.nilaiPotonganHutang, true)
        returBeli.sudahDiterima = true
        bayar(pembayaran)
    }

    BigDecimal sisaHutang() {
        fakturBeli?.hutang?.sisa()?: 0
    }

    BigDecimal jumlahHutang() {
        fakturBeli?.hutang?.jumlah?: 0
    }

    BigDecimal jumlahDibayar() {
        fakturBeli?.hutang?.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_POTONGAN)?: 0
    }

    boolean diterimaPenuh() {
        if (listPenerimaanBarang.isEmpty() || listItemFaktur.isEmpty()) return false
        PenerimaanBarang sudahDiterima
        listPenerimaanBarang.each {
            if (it.deleted == 'Y') return
            sudahDiterima = (sudahDiterima==null)? it: (sudahDiterima + it)
        }
        sudahDiterima? sudahDiterima.isiSamaDengan(this): false
    }

    boolean isPenerimaanKosong() {
        boolean kosong = true
        listPenerimaanBarang.each {
            if (it.deleted != 'Y') kosong = false
        }
        kosong
    }

    List<ItemBarang> sisaBelumDiterima() {
        List<ItemBarang> daftarBarang = toDaftarBarang().items

        PenerimaanBarang p
        listPenerimaanBarang.each {
            if (it.deleted == 'Y') return
            p = (!p? it: (p + it))
        }
        if (!p) return daftarBarang
        List diterima = p.normalisasi()

        diterima.each { ItemBarang d ->
            def i = daftarBarang.findIndexOf { ItemBarang it -> it.produk == d.produk }
            if (i>=0) {
                if (daftarBarang[i].jumlah == d.jumlah) {
                    daftarBarang.remove(i)
                } else {
                    daftarBarang[i].jumlah -= d.jumlah
                }
            }
        }

        daftarBarang
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Faktur faktur = (Faktur) o

        if (nomor != faktur.nomor) return false

        return true
    }

    int hashCode() {
        return nomor.hashCode()
    }

}

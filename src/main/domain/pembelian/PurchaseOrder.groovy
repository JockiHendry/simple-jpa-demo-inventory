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

import domain.Container
import domain.event.PerubahanStok
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.faktur.Faktur
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.validation.InputPurchaseOrder
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import org.slf4j.LoggerFactory
import simplejpa.DomainClass
import griffon.util.*
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
import javax.validation.constraints.Size
import javax.validation.groups.Default

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
@DomainClass @Entity @Canonical(excludes='fakturBeli,listPenerimaanBarang') @EqualsAndHashCode(callSuper=true)
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

        // Memeriksa apakah produk yang diterima adalah salah satu bagian dari produk yang dipesan
        penerimaanBarang.listItemBarang.each { ItemBarang i ->
            if (listItemFaktur.find { it.produk == i.produk } == null) {
                throw new DataTidakKonsisten("Tidak ada ${i.produk.nama} yang dipesan untuk PO ${nomor}!", penerimaanBarang)
            }
        }

        penerimaanBarang.gudang = Container.app.gudangRepository.cariGudangUtama()

        listPenerimaanBarang << penerimaanBarang
        if (diterimaPenuh() && fakturBeli) {
            status = StatusPurchaseOrder.OK
            if (!fakturBeli.hutang) {
                fakturBeli.buatHutang()
            }
        } else {
            status = StatusPurchaseOrder.DIPROSES
        }

        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, this))
    }

    void hapus(PenerimaanBarang penerimaanBarang) {
        if (!status.penerimaanBolehDiubah || penerimaanBarang.deleted == 'Y') {
            throw new DataTidakBolehDiubah(this)
        }
        penerimaanBarang.deleted = 'Y'
        if (isPenerimaanKosong() && !fakturBeli) {
            status = StatusPurchaseOrder.DIBUAT
        } else {
            status = StatusPurchaseOrder.DIPROSES
        }
        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, this, true))
    }

    void hapus(PembayaranHutang pembayaranHutang) {
        if (status!=StatusPurchaseOrder.OK || fakturBeli==null) {
            throw new DataTidakBolehDiubah(this)
        }
        if (!fakturBeli.hutang) {
            throw new IllegalStateException('Pembayaran hutang tidak dapat dibayar untuk purchase order ini')
        }
        fakturBeli.hutang.hapus(pembayaranHutang)
    }


    void tambah(FakturBeli f) {
        if (!status.fakturBolehDiubah || fakturBeli) {
            throw new DataTidakBolehDiubah(this)
        }

        // Memeriksa apakah isi faktur baru sama dengan isi barang yang dipesan
        if (normalisasi().toSet() != f.normalisasi().toSet()) {
            throw new DataTidakKonsisten("Faktur ${f.nomor} berbeda dengan yang dipesan untuk PO ${nomor}!", f)
        }

        fakturBeli = f
        if (diterimaPenuh()) {
            status = StatusPurchaseOrder.OK
            fakturBeli.buatHutang()
        } else {
            status = StatusPurchaseOrder.DIPROSES
        }
    }

    void hapusFaktur() {
        if (!status.fakturBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        fakturBeli = null
        if (isPenerimaanKosong() && !fakturBeli) {
            status = StatusPurchaseOrder.DIBUAT
        } else {
            status = StatusPurchaseOrder.DIPROSES
        }
    }

    void bayar(PembayaranHutang pembayaranHutang) {
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

    BigDecimal sisaHutang() {
        fakturBeli.hutang.sisa()
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
        List<ItemBarang> daftarBarang = normalisasi()

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
}

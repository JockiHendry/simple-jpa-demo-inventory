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
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakLengkap
import domain.inventory.ItemBarang
import domain.penjualan.NomorService
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class PurchaseOrderRepository {

    public List<PurchaseOrder> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch, def statusSearch) {
        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
        }
    }

    public List<FakturBeli> cariHutang(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch,
           String supplierSearch, LocalDate tanggalJatuhTempo = null,
           StatusHutangSearch statusHutangSearch = StatusHutangSearch.SEMUA) {

        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (statusHutangSearch != StatusHutangSearch.BELUM_LUNAS) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            }
            if (nomorSearch) {
                and()
                fakturBeli__nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
            if (tanggalJatuhTempo) {
                and()
                fakturBeli__jatuhTempo eq(tanggalJatuhTempo)
            }
            List statusSearch = []
            if ((statusHutangSearch == StatusHutangSearch.LUNAS) || (statusHutangSearch == StatusHutangSearch.SEMUA) ) {
                statusSearch << StatusPurchaseOrder.LUNAS
            }
            if ((statusHutangSearch == StatusHutangSearch.BELUM_LUNAS) || (statusHutangSearch == StatusHutangSearch.SEMUA)) {
                statusSearch << StatusPurchaseOrder.OK
            }
            and()
            status isIn(statusSearch)
        }
    }

    public PurchaseOrder buat(PurchaseOrder purchaseOrder) {
        purchaseOrder.nomor = Container.app.nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER)
        if (findPurchaseOrderByNomor(purchaseOrder.nomor)) {
            throw new DataDuplikat(purchaseOrder)
        }
        persist(purchaseOrder)
        purchaseOrder
    }

    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        PurchaseOrder mergedPurchaseOrder = findPurchaseOrderById(purchaseOrder.id)
        if (!mergedPurchaseOrder.status.bolehDiubah) {
            throw new DataTidakBolehDiubah(purchaseOrder)
        }
        mergedPurchaseOrder.with {
            tanggal = purchaseOrder.tanggal
            diskon = purchaseOrder.diskon
            supplier = purchaseOrder.supplier
            keterangan = purchaseOrder.keterangan
        }
        mergedPurchaseOrder
    }

    public PurchaseOrder tambah(PurchaseOrder purchaseOrder, PenerimaanBarang penerimaanBarang, List<ItemBarang> listItemBarang) {
        if (findPenerimaanBarangByNomor(penerimaanBarang.nomor)) {
            throw new DataDuplikat(penerimaanBarang)
        }
        if (listItemBarang.isEmpty()) {
            throw new DataTidakLengkap('Daftar barang yang diterima tidak boleh kosong!')
        }
        purchaseOrder = merge(purchaseOrder)
        listItemBarang.each {
            it.produk = merge(it.produk)
            penerimaanBarang.tambah(it)
        }
        purchaseOrder.tambah(penerimaanBarang)
        purchaseOrder
    }

    public PurchaseOrder tambah(PurchaseOrder purchaseOrder, FakturBeli fakturBeli) {
        if (findFakturBeliByNomor(fakturBeli.nomor)) {
            throw new DataDuplikat(fakturBeli)
        }
        if (fakturBeli.id != null) {
            throw new DataTidakBolehDiubah(fakturBeli)
        }
        purchaseOrder = merge(purchaseOrder)
        persist(fakturBeli)
        purchaseOrder.tambah(fakturBeli)
        purchaseOrder
    }

    public List hapus(PurchaseOrder purchaseOrder, PenerimaanBarang penerimaanBarang) {
        purchaseOrder = merge(purchaseOrder)
        penerimaanBarang = merge(penerimaanBarang)
        purchaseOrder.hapus(penerimaanBarang)
        [purchaseOrder, penerimaanBarang]
    }

    public PurchaseOrder hapus(PurchaseOrder purchaseOrder, FakturBeli fakturBeli) {
        purchaseOrder = merge(purchaseOrder)
        fakturBeli = merge(fakturBeli)
        purchaseOrder.hapusFaktur()
        purchaseOrder
    }

    public PurchaseOrder hapus(PurchaseOrder purchaseOrder) {
        purchaseOrder = findPurchaseOrderById(purchaseOrder.id)
        if (!purchaseOrder.status.bolehDiubah) {
            throw new DataTidakBolehDiubah(purchaseOrder)
        }
        purchaseOrder.deleted = 'Y'
        purchaseOrder
    }

    public enum StatusHutangSearch {
        SEMUA('Semua'), BELUM_LUNAS('Belum Lunas'), LUNAS('Lunas')

        String description

        public StatusHutangSearch(String description) {
            this.description = description
        }

        @Override
        String toString() {
            description
        }
    }

}

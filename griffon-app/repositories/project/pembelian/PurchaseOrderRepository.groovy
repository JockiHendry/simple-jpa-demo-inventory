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
package project.pembelian

import domain.faktur.BilyetGiro
import domain.faktur.Pembayaran
import util.SwingHelper
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.inventory.ItemBarang
import domain.pembelian.FakturBeli
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PurchaseOrder
import domain.pembelian.StatusPurchaseOrder
import project.user.NomorService
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class PurchaseOrderRepository {

    NomorService nomorService

    public List<PurchaseOrder> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorPOSearch,
            String nomorFakturSearch, String supplierSearch, def statusSearch) {
        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorPOSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorPOSearch}%")
            }

            if (statusSearch != SwingHelper.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorFakturSearch) {
                and()
                fakturBeli__nomor like("%${nomorFakturSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
        }
    }

    public List<PurchaseOrder> cariPenerimaan(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorPOSearch,
                                              String nomorFakturSearch, String supplierSearch, def statusSearch) {
        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorPOSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorPOSearch}%")
            }

            if (statusSearch == SwingHelper.SEMUA) {
                and()
                status isIn([StatusPurchaseOrder.DIBUAT, StatusPurchaseOrder.FAKTUR_DITERIMA])
            } else {
                and()
                status eq(statusSearch)
            }
            if (nomorFakturSearch) {
                and()
                fakturBeli__nomor like("%${nomorFakturSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
        }
    }

    public List<PurchaseOrder> cariFakturBeli(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorPOSearch,
                                              String nomorFakturSearch, String supplierSearch, def statusSearch) {
        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorPOSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorPOSearch}%")
            }
            if (statusSearch != SwingHelper.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorFakturSearch) {
                and()
                fakturBeli__nomor like("%${nomorFakturSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
        }
    }


    public List<PurchaseOrder> cariHutang(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch,
           String supplierSearch, LocalDate tanggalJatuhTempo = null,
           StatusHutangSearch statusHutangSearch = StatusHutangSearch.SEMUA) {

        findAllPurchaseOrderByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorSearch && (statusHutangSearch != StatusHutangSearch.BELUM_LUNAS)) {
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
                statusSearch << StatusPurchaseOrder.FAKTUR_DITERIMA
            }
            and()
            status isIn(statusSearch)
        }
    }

    public PurchaseOrder buat(PurchaseOrder purchaseOrder) {
        purchaseOrder.nomor = nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER)
        if (findPurchaseOrderByNomor(purchaseOrder.nomor)) {
            throw new DataDuplikat(purchaseOrder)
        }
        persist(purchaseOrder)
        purchaseOrder
    }

    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        PurchaseOrder mergedPurchaseOrder = findPurchaseOrderById(purchaseOrder.id)
        if (!mergedPurchaseOrder || !mergedPurchaseOrder.status.bolehDiubah) {
            mergedPurchaseOrder.keterangan = purchaseOrder.keterangan
        } else {
            mergedPurchaseOrder.with {
                tanggal = purchaseOrder.tanggal
                diskon = purchaseOrder.diskon
                supplier = purchaseOrder.supplier
                keterangan = purchaseOrder.keterangan
            }
        }
        purchaseOrder
    }

    public PurchaseOrder tambah(PurchaseOrder purchaseOrder, PenerimaanBarang penerimaanBarang, List<ItemBarang> listItemBarang) {
        if (findPenerimaanBarangByNomor(penerimaanBarang.nomor)) {
            throw new DataDuplikat(penerimaanBarang)
        }
        if (listItemBarang.isEmpty()) {
            throw new DataTidakBolehDiubah('Daftar barang yang diterima tidak boleh kosong!', this)
        }
        purchaseOrder = findPurchaseOrderByIdFetchItems(purchaseOrder.id)
        listItemBarang.each {
            it.produk = findProdukById(it.produk.id)
            it.produk.supplier = purchaseOrder.supplier
            penerimaanBarang.tambah(it)
        }
        purchaseOrder.tambah(penerimaanBarang)
        purchaseOrder
    }

    public PurchaseOrder tambah(PurchaseOrder purchaseOrder, FakturBeli fakturBeli, boolean strictMode = true) {
        if (findFakturBeliByNomor(fakturBeli.nomor)) {
            throw new DataDuplikat(fakturBeli)
        }
        if (fakturBeli.id != null) {
            throw new DataTidakBolehDiubah(fakturBeli)
        }
        purchaseOrder = findPurchaseOrderById(purchaseOrder.id)
        purchaseOrder.tambah(fakturBeli, strictMode)
        persist(fakturBeli)
        purchaseOrder
    }

    public List hapus(PurchaseOrder purchaseOrder, PenerimaanBarang penerimaanBarang) {
        purchaseOrder = findPurchaseOrderByIdFetchComplete(purchaseOrder.id)
        penerimaanBarang = findPenerimaanBarangById(penerimaanBarang.id)
        purchaseOrder.hapus(penerimaanBarang)
        [purchaseOrder, penerimaanBarang]
    }

    public PurchaseOrder hapusFaktur(PurchaseOrder purchaseOrder) {
        purchaseOrder = findPurchaseOrderByIdFetchComplete(purchaseOrder.id)
        purchaseOrder.hapusFaktur()
        purchaseOrder
    }

    public PurchaseOrder hapus(PurchaseOrder purchaseOrder) {
        purchaseOrder = findPurchaseOrderByIdFetchComplete(purchaseOrder.id)
        if (!purchaseOrder || !purchaseOrder.status.bolehDiubah) {
            throw new DataTidakBolehDiubah(purchaseOrder)
        }
        purchaseOrder.deleted = 'Y'
        purchaseOrder
    }

    public PurchaseOrder hapus(PurchaseOrder purchaseOrder, Pembayaran pembayaran) {
        purchaseOrder = findPurchaseOrderByIdFetchComplete(purchaseOrder.id)
        purchaseOrder.hapus(pembayaran)
        purchaseOrder
    }

    public PurchaseOrder bayar(PurchaseOrder purchaseOrder, Pembayaran pembayaran, BilyetGiro bilyetGiro = null) {
        purchaseOrder = findPurchaseOrderByIdFetchComplete(purchaseOrder.id)
        if (bilyetGiro) {
            if (bilyetGiro.id == null) {
                persist(bilyetGiro)
            } else {
                bilyetGiro = findBilyetGiroById(bilyetGiro.id)
            }
            pembayaran.bilyetGiro = bilyetGiro
        }
        purchaseOrder.bayar(pembayaran)
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

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
package domain.labarugi

import domain.inventory.PenyesuaianStok
import domain.inventory.ReferensiStok
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import groovy.transform.Canonical

@Canonical
class HargaPokokPenjualan {

    NilaiInventory nilaiInventory

    long qtyPenjualanOlehSales = 0
    long qtyPenjualanEceran = 0
    long qtyReturSales = 0
    long qtyReturEceran = 0
    long qtyPenyesuaianStok = 0
    long qtyLain = 0

    BigDecimal nilaiPenjualanOlehSales = 0
    BigDecimal nilaiPenjualanEceran = 0
    BigDecimal nilaiReturSales = 0
    BigDecimal nilaiReturEceran = 0
    BigDecimal nilaiPenyesuaianStok = 0
    BigDecimal nilaiLain = 0

    public HargaPokokPenjualan(NilaiInventory nilaiInventory) {
        this.nilaiInventory = nilaiInventory
    }

    void tambah(ReferensiStok referensiStok, long jumlah) {
        if (referensiStok?.classGudang == PenyesuaianStok.simpleName) {
            qtyPenyesuaianStok += jumlah
            nilaiPenyesuaianStok += nilaiInventory.kurang(jumlah)
        } else if (referensiStok?.classFinance == FakturJualOlehSales.simpleName) {
            qtyPenjualanOlehSales += jumlah
            nilaiPenjualanOlehSales += nilaiInventory.kurang(jumlah)
        } else if (referensiStok?.classFinance == FakturJualEceran.simpleName) {
            qtyPenjualanEceran += jumlah
            nilaiPenjualanEceran += nilaiInventory.kurang(jumlah)
        } else if (referensiStok?.classFinance == ReturJualOlehSales.simpleName) {
            qtyReturSales += jumlah
            nilaiReturSales += nilaiInventory.kurang(jumlah)
        } else if (referensiStok?.classFinance == ReturJualEceran.simpleName) {
            qtyReturEceran += jumlah
            nilaiReturEceran += nilaiInventory.kurang(jumlah)
        } else {
            qtyLain += jumlah
            nilaiLain += nilaiInventory.kurang(jumlah)
        }
    }

    long totalQty() {
        qtyPenyesuaianStok + qtyPenjualanOlehSales + qtyPenjualanEceran + qtyReturEceran + qtyReturSales + qtyLain
    }

    BigDecimal totalNilai() {
        nilaiPenyesuaianStok + nilaiPenjualanOlehSales + nilaiPenjualanEceran + nilaiReturEceran + nilaiReturSales + nilaiLain
    }

    void tambah(HargaPokokPenjualan lain) {
        qtyPenyesuaianStok += lain.qtyPenyesuaianStok
        qtyPenjualanOlehSales += lain.qtyPenjualanOlehSales
        qtyPenjualanEceran += lain.qtyPenjualanEceran
        qtyReturEceran += lain.qtyReturEceran
        qtyReturSales += lain.qtyReturSales
        qtyLain += lain.qtyLain

        nilaiPenyesuaianStok += lain.nilaiPenyesuaianStok
        nilaiPenjualanOlehSales += lain.nilaiPenjualanOlehSales
        nilaiPenjualanEceran += lain.nilaiPenjualanEceran
        nilaiReturEceran += lain.nilaiReturEceran
        nilaiReturSales += lain.nilaiReturSales
        nilaiLain += lain.nilaiLain
    }

}

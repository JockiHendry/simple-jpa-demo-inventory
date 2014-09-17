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
package project.user

import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Sales
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class NomorService {

    private final Logger log = LoggerFactory.getLogger(NomorService)

    public void serviceInit() {
        refreshAll()
    }

    public enum TIPE {
        FAKTUR_JUAL('FakturJual', '%06d/%s/%s'),
        PENGELUARAN_BARANG('PengeluaranBarang', '%06d-SJ-KB-%s'),
        PENGELUARAN_BONUS('BonusPenjualan', '%06d-BONUS-%s'),
        PURCHASE_ORDER('PurchaseOrder', '%06d-PO-KB-%s'),
        TRANSFER('Transfer', '%06d-TR-KB-%s'),
        PENYESUAIAN_STOK('PenyesuaianStok', '%06d-PS-KB-%s'),
        PENCAIRAN_POIN('PencairanPoin', '%06d-PP-KB-%s'),
        RETUR_BELI('ReturBeli', '%06d-RB-KB-%s'),
        RETUR_JUAL('ReturJual', '%06d-RJ-KB-%s'),
        PENERIMAAN_BARANG('PenerimaanBarang', '%06d-RV-KB-%s')

        String format;
        String desc;

        TIPE(String desc, String format) {
            this.desc = desc
            this.format = format
        }
    }

    Map<TIPE, Long> nomorUrutTerakhir = [:]

    public void refreshAll() {
        TIPE.values().each { TIPE tipe ->
            List l = "findAll${tipe.desc}ByDsl"([pageSize: 1, orderBy: 'nomor', orderDirection: 'desc']) {
                tanggal gt(LocalDate.now().withDayOfYear(1).minusDays(1))
            }
            if (!l.empty) {
                try {
                    nomorUrutTerakhir[tipe] = Integer.valueOf(l[0].nomor.substring(0,6).trim())
                } catch (NumberFormatException nfe) {
                    log.warn "Tidak dapat membaca nomor faktur terakhir!"
                    nomorUrutTerakhir[tipe] = 0
                }
            }
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    public String buatNomor(TIPE tipe) {
        String hasil = getCalonNomor(tipe)
        nomorUrutTerakhir[tipe]++
        hasil
    }

    public String buatNomorFakturJual(Sales sales = null) {
        getCalonNomorFakturJual(sales)
    }

    public String buatNomorFakturJual(FakturJual fakturJual) {
        if (fakturJual instanceof FakturJualOlehSales) {
            return buatNomorFakturJual(fakturJual.konsumen.sales)
        } else {
            return buatNomorFakturJual()
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    public String getCalonNomor(TIPE tipe) {
        String.format(tipe.format, getNomorTerakhir(tipe)+1, LocalDate.now().toString('MMyyyy'))
    }

    public String getCalonNomorFakturJual(Sales sales = null) {
        String.format(TIPE.FAKTUR_JUAL.format, getNomorFakturJualTerakhir(sales)+1, LocalDate.now().toString('MMyyyy'),
            sales? sales.kode: 'ECERAN')
    }

    @Transaction(Transaction.Policy.SKIP)
    public long getNomorTerakhir(TIPE tipe) {
        if (!nomorUrutTerakhir[tipe]) {
            nomorUrutTerakhir[tipe] = 0
        }
        nomorUrutTerakhir[tipe]
    }

    public long getNomorFakturJualTerakhir(Sales salesSearch = null) {
        List result
        if (salesSearch) {
            result = findAllFakturJualOlehSalesByDsl([pageSize: 1, orderBy: 'nomor', orderDirection: 'desc']) {
                konsumen__sales eq(salesSearch)
                and()
                tanggal gt(LocalDate.now().withDayOfYear(1).minusDays(1))
            }
        } else {
            result = findAllFakturJualEceranByDsl([pageSize: 1, orderBy: 'nomor', orderDirection: 'desc']) {
                tanggal gt(LocalDate.now().withDayOfYear(1).minusDays(1))
            }
        }
        if (!result?.empty) {
            try {
                return Integer.valueOf(result[0].nomor.substring(0, 6))
            } catch (NumberFormatException nfe) {
                return 0
            }
        }
        0
    }
}
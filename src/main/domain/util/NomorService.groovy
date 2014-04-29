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
package domain.util

import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class NomorService {

    private final Logger log = LoggerFactory.getLogger(NomorService)

    public enum TIPE {
        FAKTUR_JUAL('FakturJual', '%06d-FJ-KB-%s'),
        PENGELUARAN_BARANG('PengeluaranBarang', '%06d-SJ-KB-%s'),
        PENGELUARAN_BONUS('BonusPenjualan', '%06d-BONUS-%s'),
        PURCHASE_ORDER('PurchaseOrder', '%06d-PO-KB-%s')

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

    @Transaction(Transaction.Policy.SKIP)
    public String getCalonNomor(TIPE tipe) {
        String.format(tipe.format, getNomorTerakhir(tipe)+1, LocalDate.now().toString('MMyyyy'))
    }

    @Transaction(Transaction.Policy.SKIP)
    public long getNomorTerakhir(TIPE tipe) {
        if (!nomorUrutTerakhir[tipe]) {
            nomorUrutTerakhir[tipe] = 0
        }
        nomorUrutTerakhir[tipe]
    }
}
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
package listener

import domain.event.BilyetGiroCleared
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import simplejpa.transaction.Transaction

@Transaction
class BilyetGiroEventListenerService {

    void onBilyetGiroCleared(BilyetGiroCleared bilyetGiroCleared) {
        log.info "Event onBilyetGiroCleared mulai dikerjakan..."

        // Cari seluruh faktur jual oleh sales yang berhubungan dengan giro ini dan set status
        // menjadi lunas bila perlu.

        List<FakturJualOlehSales> fakturBelumLunas = executeQuery('SELECT DISTINCT f FROM FakturJualOlehSales f, ' +
                'IN(f.piutang.listPembayaran) p WHERE f.status <> domain.penjualan.StatusFakturJual.LUNAS ' +
                'AND p.bilyetGiro = :bilyetGiro ',
                [:], [bilyetGiro: bilyetGiroCleared.source])
        fakturBelumLunas.each {
            if (it.piutang.lunas) {
                it.status = StatusFakturJual.LUNAS
            }
        }

        log.info "Event onBilyetGiroCleared selesai dikerjakan!"
    }

}

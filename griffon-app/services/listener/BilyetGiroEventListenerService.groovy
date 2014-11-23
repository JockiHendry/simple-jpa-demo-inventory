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
import domain.faktur.BilyetGiro
import domain.pembelian.PurchaseOrder
import domain.pembelian.StatusPurchaseOrder
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import project.penjualan.BilyetGiroService
import simplejpa.transaction.Transaction

@Transaction
class BilyetGiroEventListenerService {

    BilyetGiroService bilyetGiroService

    @SuppressWarnings("GroovyUnusedDeclaration")
    void onBilyetGiroCleared(BilyetGiroCleared bilyetGiroCleared) {
        log.info "Event onBilyetGiroCleared mulai dikerjakan..."

        BilyetGiro bilyetGiro = bilyetGiroCleared.source

        // Cari seluruh faktur jual oleh sales yang berhubungan dengan giro ini dan set status
        // menjadi lunas bila perlu.
        bilyetGiroService.cariFakturJualYangDibayarDengan(bilyetGiro).each { FakturJualOlehSales f ->
            if ((f.status != StatusFakturJual.LUNAS) && f.piutang.lunas) {
                f.status = StatusFakturJual.LUNAS
                f.konsumen.hapusFakturBelumLunas(f)
            }
        }

        // Cari seluruh purchase order yang berhubungan dengan giro ini dan set status menjadi lunas bila perlu.
        bilyetGiroService.cariPurchaseOrderYangDibayarDengan(bilyetGiro).each { PurchaseOrder p ->
            if ((p.status != StatusPurchaseOrder.LUNAS) && p.fakturBeli.hutang.lunas) {
                p.status = StatusPurchaseOrder.LUNAS
            }
        }

        log.info "Event onBilyetGiroCleared selesai dikerjakan!"
    }

}

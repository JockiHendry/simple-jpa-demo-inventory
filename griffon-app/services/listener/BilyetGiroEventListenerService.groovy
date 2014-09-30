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
import domain.pembelian.PurchaseOrder
import domain.pembelian.StatusPurchaseOrder
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
            'IN(f.piutang.listPembayaran) b WHERE f.status <> domain.penjualan.StatusFakturJual.LUNAS ' +
            'AND b.bilyetGiro = :bilyetGiro ',
            [:], [bilyetGiro: bilyetGiroCleared.source])
        fakturBelumLunas.each {
            if (it.piutang.lunas) {
                it.status = StatusFakturJual.LUNAS
                it.konsumen.hapusFakturBelumLunas(it)
            }
        }

        // Cari seluruh purchase order yang berhubungan dengan giro ini dan set status menjadi lunas bila perlu.
        List<PurchaseOrder> purchaseOrderBelumLunas = executeQuery('SELECT DISTINCT p FROM PurchaseOrder p, ' +
            'IN(p.fakturBeli.hutang.listPembayaran) b WHERE p.status <> domain.pembelian.StatusPurchaseOrder.LUNAS ' +
            'AND b.bilyetGiro = :bilyetGiro ',
            [:], [bilyetGiro: bilyetGiroCleared.source])
        purchaseOrderBelumLunas.each {
            if (it.fakturBeli.hutang.lunas) {
                it.status = StatusPurchaseOrder.LUNAS
            }
        }

        log.info "Event onBilyetGiroCleared selesai dikerjakan!"
    }

}

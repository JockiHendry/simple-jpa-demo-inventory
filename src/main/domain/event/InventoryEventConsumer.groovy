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
package domain.event

import domain.faktur.Faktur
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.inventory.Transfer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class InventoryEventConsumer {

    private final Log log = LogFactory.getLog(InventoryEventConsumer)

    void onPerubahanStok(PerubahanStok perubahanStok) {
        log.info "Event onPerubahanStok mulai dikerjakan..."

        DaftarBarang daftarBarang = perubahanStok.source
        Faktur faktur = perubahanStok.faktur

        daftarBarang.normalisasi().each { ItemBarang i ->
            log.info "Memproses item $i..."
            int pengali = daftarBarang.faktor()
            String keterangan = null
            if (perubahanStok.invers) {
                pengali *= -1
                keterangan = 'Invers akibat penghapusan'
                log.info "Item ini adalah item negasi dengan pengali [${pengali}]"
            }

            if (keterangan==null) keterangan = faktur.keterangan
            i.produk.perubahanStok(pengali * i.jumlah, faktur, daftarBarang.gudang, keterangan)

            log.info "Selesai memproses item $i!"
        }

        log.info "Event onPerubahanStok selesai dikerjakan!"
    }

    void onTransferStok(TransferStok transferStok) {
        log.info "Event onTransferStok mulai dikerjakan..."

        Transfer transfer = transferStok.source
        transfer.normalisasi().each { ItemBarang i ->
            log.info "Memproses item $i..."

            int pengali = transferStok.invers? 1: -1

            // Mengurangi gudang asal
            i.produk.perubahanStok(pengali * i.jumlah, transfer, transfer.gudang, transfer.keterangan)

            // Menambah gudang tujuan
            i.produk.perubahanStok(-pengali * i.jumlah, transfer, transfer.tujuan, transfer.keterangan)

            log.info "Selesai memproses item $i!"
        }

        log.info "Event onTransferStok selesai dikerjakan!"
    }

}

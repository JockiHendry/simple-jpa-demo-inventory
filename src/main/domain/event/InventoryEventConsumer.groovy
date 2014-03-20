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

import domain.Container
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.inventory.ProdukRepository
import domain.pembelian.PenerimaanBarang
import groovy.transform.Canonical
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import simplejpa.transaction.Transaction

@Transaction
class InventoryEventConsumer {

    private final Log log = LogFactory.getLog(InventoryEventConsumer)

    void onPerubahanStok(PerubahanStok perubahanStok) {
        log.info "Event onPerubahanStok mulai dikerjakan..."

        ProdukRepository produkRepository = Container.app.produkRepository

        if (perubahanStok.source instanceof PenerimaanBarang) {

            PenerimaanBarang penerimaanBarang = (PenerimaanBarang) perubahanStok.source
            penerimaanBarang.normalisasi().each { ItemBarang i ->
                log.info "Memproses item $i..."
                produkRepository.perubahanStok(i.produk, i.jumlah, penerimaanBarang)
                log.info "Selesai memproses item $i!"
            }

        }

        log.info "Event onPerubahanStok selesai dikerjakan!"
    }

    void onDaftarBarangDihapus(DaftarBarangDihapus daftarBarangDihapus) {
        log.info "Event onDaftarBarangDihapus mulai dikerjakan..."

        ProdukRepository produkRepository = Container.app.produkRepository
        DaftarBarang daftarBarang = daftarBarangDihapus.source

        daftarBarang.normalisasi().each { ItemBarang i ->
            log.info "Memproses negasi untuk item $i..."
            produkRepository.perubahanStok(i.produk, -i.jumlah, daftarBarang, 'Invers Akibat Penghapusan')
            log.info "Selesai memperoses item $i!"
        }

        log.info "Event onDaftarBarangDihapus selesai dikerjakan!"
    }

}

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

import domain.event.PerubahanRetur
import domain.event.PerubahanStok
import domain.event.PerubahanStokTukar
import domain.event.PesanStok
import domain.event.TransferStok
import domain.inventory.BolehPesanStok
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.inventory.ItemStok
import domain.inventory.Produk
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.inventory.Transfer
import domain.user.PesanLevelMinimum
import org.joda.time.LocalDate
import project.user.PesanRepository
import simplejpa.transaction.Transaction

class InventoryEventListenerService {

    PesanRepository pesanRepository

    void onPerubahanStokTukar(PerubahanStokTukar perubahanStokTukar) {
        log.info "Event onPerubahanStokTukar mulai dikerjakan..."

        DaftarBarang daftarBarang = (DaftarBarang) perubahanStokTukar.source
        int pengali = daftarBarang.faktor() * (perubahanStokTukar.invers? -1: 1)
        daftarBarang.items.each { ItemBarang itemBarang ->
            int jumlahTukar = pengali * itemBarang.jumlah
            if (itemBarang.produk.jumlahTukar == null) {
                itemBarang.produk.jumlahTukar = jumlahTukar
            } else {
                itemBarang.produk.jumlahTukar += jumlahTukar
            }
        }

        log.info "Event onPerubahanStokTukar selesai dikerjakan!"
    }

    void onPerubahanRetur(PerubahanRetur perubahanRetur) {
        log.info "Event onPerubahanRetur mulai dikerjakan..."

        DaftarBarang daftarBarang = perubahanRetur.nilai()
        daftarBarang.items.each { ItemBarang itemBarang ->
            int pengali = (perubahanRetur.invers? -1: 1) * daftarBarang.faktor()
            int jumlahRetur = pengali * itemBarang.jumlah
            if (itemBarang.produk.jumlahRetur == null) {
                itemBarang.produk.jumlahRetur = jumlahRetur
            } else {
                itemBarang.produk.jumlahRetur += jumlahRetur
            }
        }

        log.info "Event onPerubahanRetur selesai dikerjakan!"
    }

    @Transaction
    void onPesanStok(PesanStok pesanStok) {
        log.info "Event onPesanStok mulai dikerjakan..."

        BolehPesanStok source = pesanStok.source
        if (!source.bolehPesanStok) return

        int pengali = pesanStok.invers? -1: 1
        source.yangDipesan().each {
            Produk produk = findProdukById(it.produk.id)
            produk.jumlahAkanDikirim = (produk.jumlahAkanDikirim?:0) + (pengali * (it.jumlah?:0))
        }

        log.info "Event onPesanStok selesai dikerjakan!"
    }

    void onPerubahanStok(PerubahanStok perubahanStok) {
        log.info "Event onPerubahanStok mulai dikerjakan..."

        DaftarBarang daftarBarang = perubahanStok.source
        ReferensiStok referensiStok = perubahanStok.referensiStok

        daftarBarang.normalisasi().each { ItemBarang i ->
            log.info "Memproses item $i..."
            int pengali = daftarBarang.faktor()
            String keterangan = null
            if (perubahanStok.invers) {
                pengali *= -1
                keterangan = 'Invers Hapus'
                log.info "Item ini adalah item negasi dengan pengali [${pengali}]"
            }
            ItemStok itemStok = new ItemStok(LocalDate.now(), referensiStok, pengali * i.jumlah, keterangan)
            i.produk.perubahanStok(daftarBarang.gudang, itemStok)
            if (perubahanStok.pakaiYangSudahDipesan) {
                i.produk.jumlahAkanDikirim += (pengali * i.jumlah)
            }

            periksaLevelMinimum(i.produk)

            log.info "Selesai memproses item $i!"
        }

        log.info "Event onPerubahanStok selesai dikerjakan!"
    }

    void onTransferStok(TransferStok transferStok) {
        log.info "Event onTransferStok mulai dikerjakan..."

        Transfer transfer = transferStok.source
        transfer.normalisasi().each { ItemBarang i ->
            log.info "Memproses item $i..."

            int pengali = -1
            String keterangan
            if (transferStok.invers) {
                pengali = 1
                keterangan = 'Invers Hapus'
            }

            // Mengurangi gudang asal
            ItemStok itemStokAsal = new ItemStok(LocalDate.now(), new ReferensiStokBuilder(transfer).buat(),
                pengali * i.jumlah, keterangan)
            i.produk.perubahanStok(transfer.gudang, itemStokAsal)

            // Menambah gudang tujuan
            ItemStok itemStokTujuan = new ItemStok(LocalDate.now(), new ReferensiStokBuilder(transfer).buat(),
                -pengali * i.jumlah, keterangan)
            i.produk.perubahanStok(transfer.tujuan, itemStokTujuan)

            log.info "Selesai memproses item $i!"
        }

        log.info "Event onTransferStok selesai dikerjakan!"
    }

    void periksaLevelMinimum(Produk produk) {
        if (!produk.periksaLevel()) {
            PesanLevelMinimum pesan = new PesanLevelMinimum(produk, produk.jumlah, produk.levelMinimum)
            pesanRepository.buat(pesan)
        }
    }

}

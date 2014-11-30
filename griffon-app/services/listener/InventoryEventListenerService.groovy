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

@SuppressWarnings("GroovyUnusedDeclaration")
class InventoryEventListenerService {

    PesanRepository pesanRepository

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Transaction
    void onPerubahanStokTukar(PerubahanStokTukar perubahanStokTukar) {
        DaftarBarang daftarBarang = (DaftarBarang) perubahanStokTukar.source
        int pengali = daftarBarang.faktor() * (perubahanStokTukar.invers? -1: 1)
        daftarBarang.items.each { ItemBarang itemBarang ->
            int jumlahTukar = pengali * itemBarang.jumlah
            Produk produk = findProdukById(itemBarang.produk.id)
            if (produk.jumlahTukar == null) {
                produk.jumlahTukar = jumlahTukar
            } else {
                produk.jumlahTukar += jumlahTukar
            }
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    void onPerubahanRetur(PerubahanRetur perubahanRetur) {
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
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Transaction
    void onPesanStok(PesanStok pesanStok) {
        BolehPesanStok source = pesanStok.source
        if (!source.bolehPesanStok) return

        int pengali = pesanStok.invers? -1: 1
        source.yangDipesan().each {
            Produk produk = findProdukById(it.produk.id)
            produk.jumlahAkanDikirim = (produk.jumlahAkanDikirim?:0) + (pengali * (it.jumlah?:0))
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    void onPerubahanStok(PerubahanStok perubahanStok) {
        DaftarBarang daftarBarang = perubahanStok.source
        ReferensiStok referensiStok = perubahanStok.referensiStok

        daftarBarang.normalisasi().each { ItemBarang i ->
            int pengali = daftarBarang.faktor()
            String keterangan = null
            if (perubahanStok.invers) {
                pengali *= -1
                keterangan = 'Invers Hapus'
            }
            ItemStok itemStok = new ItemStok(LocalDate.now(), referensiStok, pengali * i.jumlah, keterangan)
            i.produk.perubahanStok(daftarBarang.gudang, itemStok)
            if (perubahanStok.pakaiYangSudahDipesan) {
                i.produk.jumlahAkanDikirim += (pengali * i.jumlah)
            }
            periksaLevelMinimum(i.produk)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    void onTransferStok(TransferStok transferStok) {
        Transfer transfer = transferStok.source
        transfer.normalisasi().each { ItemBarang i ->
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
        }
    }

    void periksaLevelMinimum(Produk produk) {
        if (!produk.periksaLevel()) {
            PesanLevelMinimum pesan = new PesanLevelMinimum(produk, produk.jumlah, produk.levelMinimum)
            pesanRepository.buat(pesan)
        }
    }

}

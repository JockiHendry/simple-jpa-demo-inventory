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
import domain.event.PesanStok
import domain.event.TransferStok
import domain.faktur.Faktur
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.inventory.Transfer
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJualOlehSales
import domain.user.PesanLevelMinimum
import project.user.PesanRepository

class InventoryEventListenerService {

    PesanRepository pesanRepository

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

    void onPesanStok(PesanStok pesanStok) {
        log.info "Event onPesanStok mulai dikerjakan..."

        // Pemesanan luar kota akan langsung mengurangi stok sehingga tidak perlu di-'pesan' terlebih dahulu.
        if (pesanStok.faktur instanceof FakturJualOlehSales && !pesanStok.faktur.konsumen.sales.dalamKota() && !pesanStok.faktur.kirimDariGudangUtama) {
            return
        }

        int pengali = pesanStok.invers? -1: 1

        pesanStok.faktur.listItemFaktur.each {
            Produk produk = it.produk
            produk.jumlahAkanDikirim = (produk.jumlahAkanDikirim?:0) + (pengali * (it.jumlah?:0))
        }

        if (pesanStok.faktur instanceof FakturJualOlehSales && pesanStok.faktur.bonusPenjualan != null) {
            (pesanStok.faktur as FakturJualOlehSales).bonusPenjualan.items.each {
                Produk produk = it.produk
                produk.jumlahAkanDikirim += pengali * it.jumlah
            }
        }

        log.info "Event onPesanStok selesai dikerjakan!"
    }

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

            if (keterangan==null) {
                keterangan = faktur?.keterangan?: daftarBarang.keterangan
            }
            i.produk.perubahanStok(pengali * i.jumlah, faktur, daftarBarang.gudang, keterangan)

            // Mengisi supplier untuk produk (dengan asumsi bahwa 1 produk 1 supplier)
            if (faktur instanceof PurchaseOrder) {
                i.produk.supplier = faktur.supplier
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

            int pengali = transferStok.invers? 1: -1

            // Mengurangi gudang asal
            i.produk.perubahanStok(pengali * i.jumlah, transfer, transfer.gudang, transfer.keterangan)

            // Menambah gudang tujuan
            i.produk.perubahanStok(-pengali * i.jumlah, transfer, transfer.tujuan, transfer.keterangan)

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

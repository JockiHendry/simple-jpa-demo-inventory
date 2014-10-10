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
package project.retur

import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.Konsumen
import domain.retur.ItemRetur
import domain.retur.Klaim
import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimTukar
import domain.retur.ReturJual
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class ReturJualService {

    final Logger log = LoggerFactory.getLogger(ReturJualService)

    void potongPiutang(ReturJual returJual) {
        returJual.items.each { ItemRetur i ->
            int sisaBelumDitukar = i.jumlah - i.jumlahBarangDitukar(true)
            if (sisaBelumDitukar > 0) {
                Konsumen konsumen = findKonsumenById(returJual.konsumen.id)
                BigDecimal jumlahPiutang = sisaBelumDitukar * konsumen.hargaTerakhir(i.produk)
                i.tambahKlaim(new KlaimPotongPiutang(jumlahPiutang))
            }
        }
    }

    void autoKlaim(ReturJual returJual) {
        // Tentukan produk yang bisa ditukar (semaksimal mungkin)
        returJual.items.each { ItemRetur i ->
            Produk produk = findProdukById(i.produk.id)
            int jumlahTersedia = produk.stok(returJual.gudang).jumlah
            int jumlahPerluDitukar = i.jumlah - i.jumlahBarangDitukar(true)
            if (jumlahTersedia > 0) {
                int jumlahDitukar = (jumlahTersedia >= jumlahPerluDitukar)? jumlahPerluDitukar: jumlahTersedia
                i.tambahKlaim(new KlaimTukar(produk, jumlahDitukar))
            }
        }
        // Potong piutang untuk sisa-nya
        potongPiutang(returJual)
    }

}

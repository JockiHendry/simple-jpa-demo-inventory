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

import domain.exception.HargaSelisih
import domain.exception.StokTidakCukup
import domain.inventory.Produk
import domain.penjualan.Konsumen
import domain.retur.ItemRetur
import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimServis
import domain.retur.KlaimTukar
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class ReturJualService {

    final Logger log = LoggerFactory.getLogger(ReturJualService)

    void potongPiutang(ReturJualOlehSales returJual) {
        returJual.items.each { ItemRetur i ->
            int sisaBelumDitukar = i.jumlah - i.jumlahBarangDitukar(true) - i.jumlahBarangDiservis(true)
            if (sisaBelumDitukar > 0) {
                Konsumen konsumen = findKonsumenById(returJual.konsumen.id)
                BigDecimal jumlahPiutang = sisaBelumDitukar * konsumen.hargaTerakhir(i.produk)
                if (jumlahPiutang > konsumen.jumlahPiutang()) {
                    throw new HargaSelisih('Jumlah piutang konsumen tidak cukup untuk potongan', konsumen.jumlahPiutang(), jumlahPiutang)
                }
                i.hapusSemuaKlaimPotongPiutang()
                i.tambahKlaim(new KlaimPotongPiutang(jumlahPiutang))
            }
        }
    }

    void autoKlaim(ReturJualOlehSales returJual, boolean tukarServis = false) {
        // Tentukan produk yang bisa ditukar (semaksimal mungkin)
        returJual.items.each { ItemRetur i ->
            i.klaims.clear()
            Produk produk = findProdukById(i.produk.id)
            int sisa = i.jumlah - i.jumlahBarangDitukar(true) - i.jumlahBarangDiservis(true)
            int jumlahTersedia = returJual.gudang.utama? produk.jumlahReadyGudangUtama(): produk.stok(returJual.gudang).jumlah
            if (jumlahTersedia > 0) {
                int jumlahDitukar = (jumlahTersedia >= sisa)? sisa: jumlahTersedia
                i.tambahKlaim(new KlaimTukar(produk, jumlahDitukar))
                sisa -= jumlahDitukar
            }
            if ((sisa > 0) && (produk.jumlahTukar > 0) && tukarServis) {
                int jumlahDitukar = (produk.jumlahTukar >= sisa)? sisa: produk.jumlahTukar
                i.tambahKlaim(new KlaimServis(produk, jumlahDitukar))
            }
        }
        // Potong piutang untuk sisa-nya
        potongPiutang(returJual)
    }

    void autoKlaim(ReturJualEceran returJual, boolean tukarServis = false) {
        returJual.items.each { ItemRetur i ->
            i.klaims.clear()
            Produk produk = findProdukById(i.produk.id)
            int jumlahPerluDitukar = i.jumlah - i.jumlahBarangDitukar(true) - i.jumlahBarangDiservis(true)
            if (produk.jumlahReadyGudangUtama() >= jumlahPerluDitukar) {
                i.tambahKlaim(new KlaimTukar(produk, jumlahPerluDitukar))
            } else if (tukarServis && ((produk.jumlahReadyGudangUtama() + produk.jumlahTukar) >= jumlahPerluDitukar)) {
                i.tambahKlaim(new KlaimTukar(produk, produk.jumlahReadyGudangUtama()))
                i.tambahKlaim(new KlaimServis(produk, jumlahPerluDitukar - produk.jumlahReadyGudangUtama()))
            } else {
                throw new StokTidakCukup(produk.nama, jumlahPerluDitukar, produk.jumlahReadyGudangUtama(), null)
            }
        }
    }

}

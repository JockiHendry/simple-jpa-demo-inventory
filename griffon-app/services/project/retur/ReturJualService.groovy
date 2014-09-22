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
import domain.retur.KlaimRetur
import domain.retur.KlaimTukar
import domain.retur.ReturJual
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class ReturJualService {

    final Logger log = LoggerFactory.getLogger(ReturJualService)

    BigDecimal hitungPotonganPiutang(ReturJual returJual) {
        hitungPotonganPiutang(returJual.listKlaimRetur, returJual.getKlaimTukar(true), returJual.konsumen)
    }

    BigDecimal hitungPotonganPiutang(List daftarKlaimRetur, List barangYangDitukar, Konsumen konsumen) {
        DaftarBarangSementara daftarKlaimPotongPiutang = (new DaftarBarangSementara(daftarKlaimRetur)) -
            (new DaftarBarangSementara(barangYangDitukar))
        konsumen = findKonsumenById(konsumen.id)
        daftarKlaimPotongPiutang.items.sum { ItemBarang itemBarang ->
            if (!itemBarang.produk || !itemBarang.jumlah) {
                return 0
            } else {
                return (konsumen.hargaTerakhir(itemBarang.produk) * itemBarang.jumlah) ?: 0
            }
        }?: 0
    }

    List<KlaimRetur> cariBarangYangBisaDitukar(ReturJual returJual) {
        cariBarangYangBisaDitukar(returJual.items, returJual.gudang)
    }

    List<KlaimRetur> cariBarangYangBisaDitukar(List<ItemBarang> items, Gudang gudang) {
        List<KlaimRetur> hasil = []
        for (ItemBarang itemBarang: items) {
            Produk produk = findProdukById(itemBarang.produk.id)
            int jumlahTersedia = produk.stok(gudang).jumlah
            if (jumlahTersedia > 0) {
                hasil << new KlaimTukar(itemBarang.produk, (jumlahTersedia >= itemBarang.jumlah)? itemBarang.jumlah: jumlahTersedia)
            }
        }
        hasil
    }

}

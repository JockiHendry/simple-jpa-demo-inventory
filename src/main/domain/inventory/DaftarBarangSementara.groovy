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
package domain.inventory

import groovy.transform.Canonical

@Canonical
class DaftarBarangSementara extends DaftarBarang {

    int nilaiFaktor

    public DaftarBarangSementara(List<ItemBarang> items, int nilaiFaktor = 1) {
        this.items = items
        this.nilaiFaktor = nilaiFaktor
    }

    DaftarBarangSementara plus(DaftarBarang daftarBarangLain) {
        plus(daftarBarangLain.items)
    }

    DaftarBarangSementara plus(List<ItemBarang> daftarLain) {
        new DaftarBarangSementara(this.items + daftarLain)
    }

    DaftarBarangSementara minus(DaftarBarang daftarBarangLain) {
        minus(daftarBarangLain.items)
    }

    DaftarBarangSementara minus(List<ItemBarang> daftarLain) {
        List<ItemBarang> hasil = []
        normalisasi().each { ItemBarang itemBarang ->
            ItemBarang itemBarangLain = daftarLain.find { it.produk == itemBarang.produk }
            if (itemBarangLain) {
                if (itemBarangLain.jumlah < itemBarang.jumlah) {
                    hasil << new ItemBarang(itemBarang.produk, itemBarang.jumlah - itemBarangLain.jumlah)
                } else {
                    // item dihapus sehingga tidak perlu ditambahkan
                }
            } else {
                hasil << itemBarang
            }
        }
        new DaftarBarangSementara(hasil, nilaiFaktor)
    }

    @Override
    int faktor() {
        nilaiFaktor
    }

}

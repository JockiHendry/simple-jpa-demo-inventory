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

    public DaftarBarangSementara() {
        this.nilaiFaktor = 1
    }

    public DaftarBarangSementara(List items, int nilaiFaktor = 1) {
        if (!items.empty) {
            this.items = items.collect { it as ItemBarang }
        }
        this.nilaiFaktor = nilaiFaktor
    }

    DaftarBarangSementara plus(DaftarBarang daftarBarangLain) {
        plus(daftarBarangLain.items)
    }

    DaftarBarangSementara plus(Collection<ItemBarang> daftarLain) {
        List<ItemBarang> hasil = new ArrayList<>(items.collect { new ItemBarang(it.produk, it.jumlah)})
        daftarLain.each { ItemBarang itemLain ->
            ItemBarang itemBarang = hasil.find {it.produk == itemLain.produk}
            if (itemBarang) {
                itemBarang.jumlah += itemLain.jumlah
            } else {
                hasil << itemLain
            }
        }
        new DaftarBarangSementara(hasil)
    }

    DaftarBarangSementara minus(DaftarBarang daftarBarangLain) {
        minus(daftarBarangLain.items)
    }

    DaftarBarangSementara minus(Collection<ItemBarang> daftarLain) {
        List<ItemBarang> hasil = new ArrayList<>(normalisasi())
        daftarLain.each { ItemBarang itemBarang ->
            ItemBarang lhs = hasil.find { it.produk == itemBarang.produk }
            if (lhs) {
                lhs.jumlah -= itemBarang.jumlah
                if (lhs.jumlah == 0) {
                    hasil.remove(lhs)
                }
            }
        }
        new DaftarBarangSementara(hasil, nilaiFaktor)
    }

    @Override
    int faktor() {
        nilaiFaktor
    }

}

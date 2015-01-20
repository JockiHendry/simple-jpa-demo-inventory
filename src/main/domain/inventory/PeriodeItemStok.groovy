/*
 * Copyright 2015 Jocki Hendry.
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

import domain.general.NilaiPeriodik
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*

@DomainClass @Entity @Canonical(excludes="listItem") @TupleConstructor(includeSuperProperties=true)
class PeriodeItemStok extends NilaiPeriodik {

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
    @OrderColumn
    List<ItemStok> listItem = []

    @Override
    List getListItemPeriodik() {
        listItem
    }
/**
     * Mengembalikan seluruh item stok yang mempengaruhi penambahan pada nilai inventory, yaitu pembelian dan
     * penyesuaian tambah.  Jenis lain seperti penambahan akibat transfer tidak akan disertakan disini.
     *
     * @return item stok yang menyebabkan nilai inventory bertambah.
     */
    List<ItemStok> cariPenambahanInventory() {
        List<ItemStok> hasil = []
        for (ItemStok itemStok: listItem) {
            if ((itemStok.jumlah > 0) && (itemStok.referensiStok?.classGudang != Transfer.simpleName)) {
                hasil << itemStok
            }
        }
        hasil
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PeriodeItemStok that = (PeriodeItemStok) o

        if (tanggalMulai != that.tanggalMulai) return false
        if (tanggalSelesai != that.tanggalSelesai) return false

        return true
    }

    int hashCode() {
        int result
        result = tanggalMulai.hashCode()
        result = 31 * result + tanggalSelesai.hashCode()
        return result
    }

}


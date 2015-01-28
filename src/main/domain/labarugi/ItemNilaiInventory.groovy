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
package domain.labarugi

import groovy.transform.Canonical
import org.joda.time.LocalDate

@Canonical
class ItemNilaiInventory implements Comparable {

    LocalDate tanggal

    String nama

    Long qty

    BigDecimal harga

    String faktur

    BigDecimal total() {
        (qty * (harga?:0))?: 0
    }

    boolean hapus(int qtyHapus) {
        if (qty > qtyHapus) {
            qty -= qtyHapus
            return false
        } else {
            qty = 0
            return true
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ItemNilaiInventory)) return false

        ItemNilaiInventory that = (ItemNilaiInventory) o

        if (faktur != that.faktur) return false
        if (harga != that.harga) return false
        if (nama != that.nama) return false
        if (qty != that.qty) return false
        if (tanggal != that.tanggal) return false

        return true
    }

    int hashCode() {
        int result
        result = (tanggal != null ? tanggal.hashCode() : 0)
        result = 31 * result + (nama != null ? nama.hashCode() : 0)
        result = 31 * result + (qty != null ? qty.hashCode() : 0)
        result = 31 * result + (harga != null ? harga.hashCode() : 0)
        result = 31 * result + (faktur != null ? faktur.hashCode() : 0)
        return result
    }

    @Override
    int compareTo(Object o) {
        if (o && (o instanceof ItemNilaiInventory)) {
            if (this.equals(o)) {
                return 0
            }
            if ((tanggal && o.tanggal) && (tanggal.compareTo(o.tanggal) != 0)) {
                return tanggal.compareTo(o.tanggal)
            }
            if ((faktur && o.faktur) && !faktur.equals(o.faktur)) {
                return faktur.compareTo(o.faktur)
            }
            if ((qty && o.qty) && (qty != o.qty)) {
                return qty.compareTo(o.qty)
            }
        }
        -1
    }

}

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
package domain.labarugi

import groovy.transform.Canonical
import org.joda.time.LocalDate

@Canonical
class NilaiInventory {

    List<ItemNilaiInventory> items = []

    BigDecimal nilai() {
        items.sum { it.total() }?: 0
    }

    Integer qty() {
        items.sum { it.qty?: 0}?: 0
    }

    void tambah(LocalDate tanggal, String nama, Integer qty, BigDecimal harga) {
        items << new ItemNilaiInventory(tanggal, nama, qty, harga)
    }

}

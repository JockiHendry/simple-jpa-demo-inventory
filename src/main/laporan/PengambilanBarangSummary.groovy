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
package laporan

import domain.inventory.Produk
import groovy.transform.Canonical
import org.joda.time.LocalDate

@Canonical
class PengambilanBarangSummary {

    LocalDate tanggal

    Produk produk

    Long qtyJualSales = 0

    Long qtyJualEceran = 0

    Long qtyRetur = 0

    Long qtyPenyesuaian = 0

    Long qtyTransfer = 0

    Long total() {
        qtyJualSales + qtyJualEceran + qtyRetur + qtyPenyesuaian + qtyTransfer
    }

}

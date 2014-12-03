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
package project

import domain.inventory.Produk
import domain.labarugi.NilaiInventory
import org.joda.time.LocalDate
import project.labarugi.LabaRugiService
import simplejpa.testing.DbUnitTestCase
import static org.junit.Assert.*

class LabaRugiServiceTests extends DbUnitTestCase {

    LabaRugiService labaRugiService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_laba_rugi_1.xlsx")
        labaRugiService = app.serviceManager.findService('LabaRugi')
    }

    void testHitungInventory() {
        Produk produk = labaRugiService.findProdukById(1l)
        NilaiInventory nilaiInventory = labaRugiService.hitungInventory(LocalDate.parse('2014-12-31'), produk)

        assertEquals(2, nilaiInventory.items.size())
        assertEquals(LocalDate.parse('2014-12-19'), nilaiInventory.items[0].tanggal)
        assertEquals('supplier', nilaiInventory.items[0].nama)
        assertEquals(15, nilaiInventory.items[0].harga.toInteger())
        assertEquals(200, nilaiInventory.items[0].qty)
        assertEquals(LocalDate.parse('2014-12-15'), nilaiInventory.items[1].tanggal)
        assertEquals('supplier', nilaiInventory.items[1].nama)
        assertEquals(14, nilaiInventory.items[1].harga.toInteger())
        assertEquals(400, nilaiInventory.items[1].qty)
        assertEquals(8600, nilaiInventory.nilai().toInteger())
    }

}

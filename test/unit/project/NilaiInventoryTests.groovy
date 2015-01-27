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
package project

import domain.inventory.Produk
import domain.labarugi.ItemNilaiInventory
import domain.labarugi.NilaiInventory
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class NilaiInventoryTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testHargaKosong() {
        Produk p = new Produk('Produk A')
        NilaiInventory nilaiInventory = new NilaiInventory()
        nilaiInventory.produk = p
        nilaiInventory.tambah(LocalDate.parse('2015-01-10'), 'Supplier1', 100, 10000)
        nilaiInventory.tambah(LocalDate.parse('2015-01-20'), null, 50, null)
        nilaiInventory.tambah(LocalDate.parse('2015-01-25'), 'Supplier2', 200, 15000)
        assertEquals(3, nilaiInventory.toList().size())
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-10'), 'Supplier1', 100, 10000), nilaiInventory.toList()[0])
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-20'), null, 50, 10000), nilaiInventory.toList()[1])
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-25'), 'Supplier2', 200, 15000), nilaiInventory.toList()[2])
        assertEquals(4500000, nilaiInventory.nilai())
    }

    void testTanggalTidakBerurut() {
        Produk p = new Produk('Produk A')
        NilaiInventory nilaiInventory = new NilaiInventory()
        nilaiInventory.produk = p
        nilaiInventory.tambah(LocalDate.parse('2015-01-25'), 'Supplier2', 200, 15000)
        nilaiInventory.tambah(LocalDate.parse('2015-01-10'), 'Supplier1', 100, 10000)
        nilaiInventory.tambah(LocalDate.parse('2015-01-20'), null, 50, null)
        assertEquals(3, nilaiInventory.toList().size())
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-10'), 'Supplier1', 100, 10000), nilaiInventory.toList()[0])
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-20'), null, 50, 10000), nilaiInventory.toList()[1])
        assertEquals(new ItemNilaiInventory(LocalDate.parse('2015-01-25'), 'Supplier2', 200, 15000), nilaiInventory.toList()[2])
        assertEquals(4500000, nilaiInventory.nilai())
    }

}

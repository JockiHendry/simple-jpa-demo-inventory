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

package simple.jpa.demo.inventory

import domain.Diskon
import domain.Faktur
import domain.ItemFaktur
import griffon.test.*

class FakturTests extends GriffonUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testTotal() {

        // Tanpa diskon
        Faktur faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(jumlah: 10, harga: 10000))
        faktur.tambah(new ItemFaktur(jumlah: 5,  harga: 4000))
        faktur.tambah(new ItemFaktur(jumlah: 3,  harga: 1000))
        assertEquals(123000, faktur.total())

        // Dengan diskon pada ItemFaktur
        faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(jumlah: 10, harga: 10000, diskon: new Diskon(5)))
        faktur.tambah(new ItemFaktur(jumlah: 5,  harga: 4000, diskon: new Diskon(3)))
        faktur.tambah(new ItemFaktur(jumlah: 3,  harga: 1000, diskon: new Diskon(2)))
        assertEquals(117340, faktur.total())

        // Dengan diskon pada Faktur
        faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(jumlah: 10, harga: 10000))
        faktur.tambah(new ItemFaktur(jumlah: 5,  harga: 4000))
        faktur.tambah(new ItemFaktur(jumlah: 3,  harga: 1000))
        faktur.diskon = new Diskon(5)
        assertEquals(116850, faktur.total())

        // Dengan diskon pada Faktur + ItemFaktur
        faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(jumlah: 10, harga: 10000, diskon: new Diskon(5)))
        faktur.tambah(new ItemFaktur(jumlah: 5,  harga: 4000, diskon: new Diskon(3)))
        faktur.tambah(new ItemFaktur(jumlah: 3,  harga: 1000, diskon: new Diskon(2)))
        faktur.diskon = new Diskon(5)
        assertEquals(111473, faktur.total())
    }
}

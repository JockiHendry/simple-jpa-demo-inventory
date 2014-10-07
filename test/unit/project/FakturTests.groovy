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

import domain.faktur.Diskon
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.inventory.Produk
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

    void testNormalisasi() {
        Produk produkA = new Produk('Produk A', 12000)
        Produk produkB = new Produk('Produk B', 11000)
        Produk produkC = new Produk('Produk C',  9000)

        Faktur faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(produkA, 10))
        faktur.tambah(new ItemFaktur(produkB, 4))
        faktur.tambah(new ItemFaktur(produkC, 5))
        faktur.tambah(new ItemFaktur(produkA, 2))
        faktur.tambah(new ItemFaktur(produkB, 6))

        Set hasil = [new ItemBarang(produkA, 12), new ItemBarang(produkC, 5), new ItemBarang(produkB, 10)]
        assertEquals(hasil, faktur.toDaftarBarang().items.toSet())
    }

    void testDiskon() {
        Produk produkA = new Produk('Produk A', 1000)
        Produk produkB = new Produk('Produk B', 1500)
        Produk produkC = new Produk('Produk C', 900)
        Produk produkD = new Produk('Produk D', 13000)

        Faktur faktur1 = new Faktur() {}
        faktur1.tambah(new ItemFaktur(produkA, 50, 1000, null, new Diskon(3)))
        faktur1.tambah(new ItemFaktur(produkB, 50, 1500, null, new Diskon(5)))
        assertEquals(5250, faktur1.jumlahDiskon())

        Faktur faktur2 = new Faktur() {}
        faktur2.tambah(new ItemFaktur(produkC, 50, 900, null, new Diskon(2)))
        faktur2.tambah(new ItemFaktur(produkD, 70, 13000, null, new Diskon(3)))
        faktur2.diskon = new Diskon(1)
        assertEquals(37750, faktur2.jumlahDiskon())
    }

    void testToDaftarBarangSementara() {
        Produk produkA = new Produk('Produk A', 12000)
        Produk produkB = new Produk('Produk B', 11000)
        Produk produkC = new Produk('Produk C',  9000)

        Faktur faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(produkA, 10))
        faktur.tambah(new ItemFaktur(produkB, 4))
        faktur.tambah(new ItemFaktur(produkC, 5))
        faktur.tambah(new ItemFaktur(produkA, 2))
        faktur.tambah(new ItemFaktur(produkB, 6))

        Set hasil = [new ItemBarang(produkA, 12), new ItemBarang(produkC, 5), new ItemBarang(produkB, 10)]
        DaftarBarangSementara d = faktur.toDaftarBarang()
        assertEquals(hasil, d.items.toSet())
    }

}

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

import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import griffon.test.*

class PenerimaanBarangTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPlus() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')

        Supplier s = new Supplier()
        PenerimaanBarang penerimaanBarang1 = new PenerimaanBarang(nomor: 'P1')
        penerimaanBarang1.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang1.tambah(new ItemBarang(produkB, 20))

        PenerimaanBarang penerimaanBarang2 = new PenerimaanBarang(nomor: 'P2')
        penerimaanBarang2.tambah(new ItemBarang(produkA, 3))
        penerimaanBarang2.tambah(new ItemBarang(produkB, 5))
        penerimaanBarang2.tambah(new ItemBarang(produkC, 30))

        PenerimaanBarang hasil = penerimaanBarang1 + penerimaanBarang2
        assertEquals('P1', hasil.nomor)
        assertEquals(68, hasil.jumlah())
        assertEquals(13, hasil.jumlah(produkA))
        assertEquals(25, hasil.jumlah(produkB))
        assertEquals(30, hasil.jumlah(produkC))
        assertEquals(3, hasil.items.size())
        assertNotNull(hasil.items.find { it.produk == produkA})
        assertNotNull(hasil.items.find { it.produk == produkB})
        assertNotNull(hasil.items.find { it.produk == produkC})
    }

    void testIsiSamaDenganDaftarBarang() {
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 15000)
        Produk produkC = new Produk('Produk C',  7000)

        PenerimaanBarang penerimaanBarang1 = new PenerimaanBarang(nomor: 'P1')
        penerimaanBarang1.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang1.tambah(new ItemBarang(produkB, 20))
        penerimaanBarang1.tambah(new ItemBarang(produkC,  5))

        PenerimaanBarang penerimaanBarang2 = new PenerimaanBarang(nomor: 'P2')
        penerimaanBarang2.tambah(new ItemBarang(produkA, 3))
        penerimaanBarang2.tambah(new ItemBarang(produkC, 2))
        penerimaanBarang2.tambah(new ItemBarang(produkA, 7))
        penerimaanBarang2.tambah(new ItemBarang(produkB, 20))
        penerimaanBarang2.tambah(new ItemBarang(produkC, 3))

        assertTrue(penerimaanBarang1.isiSamaDengan(penerimaanBarang2))
    }

    void testIsiSamaDenganFaktur() {
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 15000)
        Produk produkC = new Produk('Produk C',  7000)

        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(nomor: 'P1')
        penerimaanBarang.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang.tambah(new ItemBarang(produkB, 20))
        penerimaanBarang.tambah(new ItemBarang(produkC,  5))

        Faktur faktur = new Faktur() {}
        faktur.tambah(new ItemFaktur(produkA, 3))
        faktur.tambah(new ItemFaktur(produkC, 3))
        faktur.tambah(new ItemFaktur(produkC, 2))
        faktur.tambah(new ItemFaktur(produkB, 10))
        faktur.tambah(new ItemFaktur(produkA, 7))
        faktur.tambah(new ItemFaktur(produkB, 10))

        assertTrue(penerimaanBarang.isiSamaDengan(faktur))
    }

}

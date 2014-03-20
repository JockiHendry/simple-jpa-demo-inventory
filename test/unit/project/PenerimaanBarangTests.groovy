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

import domain.inventory.DaftarBarang
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
        PenerimaanBarang penerimaanBarang1 = new PenerimaanBarang(nomor: 'P1', supplier: s)
        penerimaanBarang1.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang1.tambah(new ItemBarang(produkB, 20))

        PenerimaanBarang penerimaanBarang2 = new PenerimaanBarang(nomor: 'P2', supplier: s)
        penerimaanBarang2.tambah(new ItemBarang(produkA, 3))
        penerimaanBarang2.tambah(new ItemBarang(produkB, 5))
        penerimaanBarang2.tambah(new ItemBarang(produkC, 30))

        PenerimaanBarang hasil = penerimaanBarang1 + penerimaanBarang2
        assertEquals('P1', hasil.nomor)
        assertEquals(s, hasil.supplier)
        assertEquals(68, hasil.jumlah())
        assertEquals(13, hasil.jumlah(produkA))
        assertEquals(25, hasil.jumlah(produkB))
        assertEquals(30, hasil.jumlah(produkC))
        assertEquals(3, hasil.listItemBarang.size())
        assertNotNull(hasil.listItemBarang.find { it.produk == produkA})
        assertNotNull(hasil.listItemBarang.find { it.produk == produkB})
        assertNotNull(hasil.listItemBarang.find { it.produk == produkC})
    }


}

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
import domain.inventory.DaftarBarang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import griffon.test.*

class DaftarBarangTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testJumlah() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')

        DaftarBarang daftarBarang = new DaftarBarang() {
            @Override
            int faktor() {
                1
            }
        }
        daftarBarang.tambah(new ItemBarang(produkA, 10))
        daftarBarang.tambah(new ItemBarang(produkB, 20))
        daftarBarang.tambah(new ItemBarang(produkC, 30))
        daftarBarang.tambah(new ItemBarang(produkA, 3))
        daftarBarang.tambah(new ItemBarang(produkB, 5))

        assertEquals(68, daftarBarang.jumlah())
    }

    void testJumlahProduk() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')

        DaftarBarang daftarBarang = new DaftarBarang() {
            @Override
            int faktor() {
                1
            }
        }
        daftarBarang.tambah(new ItemBarang(produkA, 10))
        daftarBarang.tambah(new ItemBarang(produkB, 20))
        daftarBarang.tambah(new ItemBarang(produkC, 30))
        daftarBarang.tambah(new ItemBarang(produkA, 3))
        daftarBarang.tambah(new ItemBarang(produkB, 5))

        assertEquals(13, daftarBarang.jumlah(produkA))
        assertEquals(25, daftarBarang.jumlah(produkB))
        assertEquals(30, daftarBarang.jumlah(produkC))
    }

    void testNormalisasi() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')

        DaftarBarang daftarBarang = new DaftarBarang() {
            @Override
            int faktor() {
                1
            }
        }
        daftarBarang.tambah(new ItemBarang(produkA, 10))
        daftarBarang.tambah(new ItemBarang(produkB, 20))
        daftarBarang.tambah(new ItemBarang(produkC, 30))
        daftarBarang.tambah(new ItemBarang(produkA, 3))
        daftarBarang.tambah(new ItemBarang(produkB, 5))

        List hasil = daftarBarang.normalisasi()

        assertEquals(3, hasil.size())
        assertEquals(13, hasil.find { it.produk == produkA}.jumlah)
        assertEquals(25, hasil.find { it.produk == produkB}.jumlah)
        assertEquals(30, hasil.find { it.produk == produkC}.jumlah)
    }

}

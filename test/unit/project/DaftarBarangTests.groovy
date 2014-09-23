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
import domain.inventory.DaftarBarangSementara
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

    void testToPoin() {
        Produk produkA = new Produk(nama: 'Produk A', poin: 10)
        Produk produkB = new Produk(nama: 'Produk B', poin: 0)
        Produk produkC = new Produk(nama: 'Produk C', poin: 5)

        DaftarBarang daftarBarang = new DaftarBarang() {
            @Override
            int faktor() {
                1
            }
        }
        daftarBarang.tambah(new ItemBarang(produkA, 10))  // Poin: 10 * 10 = 100
        daftarBarang.tambah(new ItemBarang(produkB, 20))  // Poin:  0 * 20 =   0
        daftarBarang.tambah(new ItemBarang(produkC, 30))  // Poin: 30 * 5  = 150
        daftarBarang.tambah(new ItemBarang(produkA, 3))   // Poin: 10 * 3  =  30
        daftarBarang.tambah(new ItemBarang(produkB, 5))   // Poin:  0 * 5  =   0

        assertEquals(280, daftarBarang.toPoin())
    }

    void testPlus() {
        Produk produkA = new Produk(nama: 'Produk A')
        Produk produkB = new Produk(nama: 'Produk B')
        Produk produkC = new Produk(nama: 'Produk C')

        DaftarBarangSementara d1 = new DaftarBarangSementara([
            new ItemBarang(produkA, 10),
            new ItemBarang(produkB, 20)
        ])

        DaftarBarangSementara d2 = new DaftarBarangSementara([
            new ItemBarang(produkA, 5),
            new ItemBarang(produkB, 10),
            new ItemBarang(produkC, 20)
        ])

        DaftarBarangSementara hasil = d1 + d2
        assertEquals(3, hasil.items.size())
        assertEquals(new ItemBarang(produkA, 15), hasil.items[0])
        assertEquals(new ItemBarang(produkB, 30), hasil.items[1])
        assertEquals(new ItemBarang(produkC, 20), hasil.items[2])

        // Pastikan bahwa lhs tidak berubah
        assertEquals(2, d1.items.size())
        assertEquals(new ItemBarang(produkA, 10), d1.items[0])
        assertEquals(new ItemBarang(produkB, 20), d1.items[1])
    }

    void testMinus() {
        Produk produkA = new Produk(nama: 'Produk A')
        Produk produkB = new Produk(nama: 'Produk B')
        Produk produkC = new Produk(nama: 'Produk C')

        DaftarBarangSementara d1 = new DaftarBarangSementara([
            new ItemBarang(produkA, 10),
            new ItemBarang(produkB, 20),
            new ItemBarang(produkC, 30)
        ])

        DaftarBarangSementara d2 = new DaftarBarangSementara([
            new ItemBarang(produkA, 5),
            new ItemBarang(produkB, 20)
        ])

        DaftarBarangSementara d3 = d1 - d2
        assertEquals(2, d3.items.size())
        assertEquals(produkA, d3.items[0].produk)
        assertEquals(5, d3.items[0].jumlah)
        assertEquals(produkC, d3.items[1].produk)
        assertEquals(30, d3.items[1].jumlah)

        // Pastikan bahwa lhs tidak berubah
        assertEquals(3, d1.items.size())
        assertEquals(new ItemBarang(produkA, 10), d1.items[0])
        assertEquals(new ItemBarang(produkB, 20), d1.items[1])
        assertEquals(new ItemBarang(produkC, 30), d1.items[2])

        DaftarBarangSementara d4 = d1 - new DaftarBarangSementara([], 1)
        assertEquals(3, d4.items.size())
        assertEquals(produkA, d4.items[0].produk)
        assertEquals(10, d4.items[0].jumlah)
        assertEquals(produkB, d4.items[1].produk)
        assertEquals(20, d4.items[1].jumlah)
        assertEquals(produkC, d4.items[2].produk)
        assertEquals(30, d4.items[2].jumlah)

        // Pastikan bahwa lhs tidak berubah
        assertEquals(3, d1.items.size())
        assertEquals(new ItemBarang(produkA, 10), d1.items[0])
        assertEquals(new ItemBarang(produkB, 20), d1.items[1])
        assertEquals(new ItemBarang(produkC, 30), d1.items[2])

        DaftarBarangSementara d5 = d1 - new DaftarBarangSementara([new ItemBarang(produkA, 3), new ItemBarang(produkA, 2), new ItemBarang(produkB, 20)])
        assertEquals(2, d5.items.size())
        assertEquals(produkA, d5.items[0].produk)
        assertEquals(5, d5.items[0].jumlah)
        assertEquals(produkC, d5.items[1].produk)
        assertEquals(30, d5.items[1].jumlah)

        // Pastikan bahwa lhs tidak berubah
        assertEquals(3, d1.items.size())
        assertEquals(new ItemBarang(produkA, 10), d1.items[0])
        assertEquals(new ItemBarang(produkB, 20), d1.items[1])
        assertEquals(new ItemBarang(produkC, 30), d1.items[2])
    }

}

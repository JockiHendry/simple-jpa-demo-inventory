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
import domain.labarugi.CacheGlobal
import domain.labarugi.NilaiInventory
import org.joda.time.LocalDate
import project.labarugi.LabaRugiService
import simplejpa.testing.DbUnitTestCase

class NilaiInventoryTest extends DbUnitTestCase {

    LabaRugiService labaRugiService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_nilai_inventory.xlsx")
        labaRugiService = app.serviceManager.findService('LabaRugi')
    }

    void testHitungNilaiInventory() {
        // Qty 10: Kasus normal beli +5 -> beli +5
        Produk produk1 = labaRugiService.findProdukByNama('produk1')
        NilaiInventory nilaiInventory = labaRugiService.hitungInventory(produk1, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        List hasil = nilaiInventory.toList()
        assertEquals(2, hasil.size())
        assertEquals(LocalDate.parse('2015-01-28'), hasil[0].tanggal)
        assertEquals('supplier1', hasil[0].nama)
        assertEquals('000001-PO-KB-012015', hasil[0].faktur)
        assertEquals(5, hasil[0].qty)
        assertEquals(1000, hasil[0].harga)
        assertEquals(LocalDate.parse('2015-01-28'), hasil[1].tanggal)
        assertEquals('supplier1', hasil[1].nama)
        assertEquals('000002-PO-KB-012015', hasil[1].faktur)
        assertEquals(5, hasil[1].qty)
        assertEquals(1100, hasil[1].harga)
        assertEquals(10500, nilaiInventory.nilai())

        // Qty  6: Kasus normal beli +5 -> jual -4 -> beli +5
        Produk produk2 = labaRugiService.findProdukByNama('produk2')
        nilaiInventory = labaRugiService.hitungInventory(produk2, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        hasil = nilaiInventory.toList()
        assertEquals(2, hasil.size())
        assertEquals(LocalDate.parse('2015-01-28'), hasil[0].tanggal)
        assertEquals('supplier1', hasil[0].nama)
        assertEquals('000001-PO-KB-012015', hasil[0].faktur)
        assertEquals(1, hasil[0].qty)
        assertEquals(1000, hasil[0].harga)
        assertEquals(LocalDate.parse('2015-01-28'), hasil[1].tanggal)
        assertEquals('supplier1', hasil[1].nama)
        assertEquals('000003-PO-KB-012015', hasil[1].faktur)
        assertEquals(5, hasil[1].qty)
        assertEquals(1000, hasil[1].harga)
        assertEquals(6000, nilaiInventory.nilai())

        // Qty  1: Kasus beli +5 -> jual -5 -> retur faktur +1 (harga harus sesuai harga beli)
        Produk produk3 = labaRugiService.findProdukByNama('produk3')
        nilaiInventory = labaRugiService.hitungInventory(produk3, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        hasil = nilaiInventory.toList()
        assertEquals(1, hasil.size())
        assertEquals(LocalDate.parse('2015-01-29'), hasil[0].tanggal)
        assertEquals('konsumen', hasil[0].nama)
        assertEquals('Retur Faktur: 000001/012015/sales', hasil[0].faktur)
        assertEquals(1, hasil[0].qty)
        assertEquals(1000, hasil[0].harga)
        assertEquals(1000, nilaiInventory.nilai())

        // Qty  1: Kasus beli +5 -> jual -5 -> penyesuaian +1 (harga harus sesuai harga beli)
        Produk produk4 = labaRugiService.findProdukByNama('produk4')
        nilaiInventory = labaRugiService.hitungInventory(produk4, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        hasil = nilaiInventory.toList()
        assertEquals(1, hasil.size())
        assertEquals(LocalDate.parse('2015-01-29'), hasil[0].tanggal)
        assertEquals('[Internal]', hasil[0].nama)
        assertEquals('000001-PS-KB-012015', hasil[0].faktur)
        assertEquals(1, hasil[0].qty)
        assertEquals(900, hasil[0].harga)
        assertEquals(900, nilaiInventory.nilai())

        // Qty  5: Kasus beli +5 -> jual -5 -> hapus jual +5  (harga harus sesuai harga beli)
        Produk produk5 = labaRugiService.findProdukByNama('produk5')
        nilaiInventory = labaRugiService.hitungInventory(produk5, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        hasil = nilaiInventory.toList()
        assertEquals(1, hasil.size())
        assertEquals(LocalDate.parse('2015-01-29'), hasil[0].tanggal)
        assertEquals('konsumen', hasil[0].nama)
        assertEquals('Penyeimbangan Hapus: 000003/012015/sales', hasil[0].faktur)
        assertEquals(5, hasil[0].qty)
        assertEquals(1000, hasil[0].harga)
        assertEquals(5000, nilaiInventory.nilai())

        // Qty  6: Kasus beli +5 -> jual -4 -> jual -1 -> beli +5 -> hapus jual +1
        Produk produk6 = labaRugiService.findProdukByNama('produk6')
        nilaiInventory = labaRugiService.hitungInventory(produk6, new CacheGlobal(tanggalMulai: LocalDate.parse('2015-01-31')))
        hasil = nilaiInventory.toList()
        assertEquals(2, hasil.size())
        assertEquals(LocalDate.parse('2015-01-29'), hasil[0].tanggal)
        assertEquals('supplier1', hasil[0].nama)
        assertEquals('000004-PO-KB-012015', hasil[0].faktur)
        assertEquals(5, hasil[0].qty)
        assertEquals(1200, hasil[0].harga)
        assertEquals(LocalDate.parse('2015-01-30'), hasil[1].tanggal)
        assertEquals('konsumen', hasil[1].nama)
        assertEquals('Penyeimbangan Hapus: 000004/012015/sales', hasil[1].faktur)
        assertEquals(1, hasil[1].qty)
        assertEquals(1200, hasil[1].harga)
        assertEquals(7200, nilaiInventory.nilai())
    }

}

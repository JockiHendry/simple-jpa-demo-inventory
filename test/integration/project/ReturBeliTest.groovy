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

import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.Supplier
import domain.retur.ReturBeli
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.retur.ReturBeliRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class ReturBeliTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ReturBeliTest)

    ReturBeliRepository returBeliRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("returBeli", "/project/data_pembelian.xls")
        returBeliRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('ReturBeli')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testJumlahReturDiProduk() {
        Produk p1 = returBeliRepository.findProdukById(-1l)
        Produk p2 = returBeliRepository.findProdukById(-2l)
        Produk p3 = returBeliRepository.findProdukById(-3l)
        Supplier s = returBeliRepository.findSupplierById(-1l)
        ReturBeli returBeli = new ReturBeli(tanggal: LocalDate.now(), nomor: 'TEST-1', supplier: s)
        returBeli.tambah(new ItemBarang(p1, 10))
        returBeli.tambah(new ItemBarang(p2, 20))
        returBeli.tambah(new ItemBarang(p3, 30))
        returBeli.tambahKlaimTukar(p1, 1)
        returBeliRepository.buat(returBeli)

        // Periksa nilai jumlah retur di produk
        p1 = returBeliRepository.findProdukById(-1l)
        assertEquals(40, p1.jumlahRetur)
        p2 = returBeliRepository.findProdukById(-2l)
        assertEquals(40, p2.jumlahRetur)
        p3 = returBeliRepository.findProdukById(-3l)
        assertEquals(10, p3.jumlahRetur)
    }

    public void testJumlahReturDiProdukSetelahHapus() {
        ReturBeli returBeli = returBeliRepository.findReturBeliById(-1l)
        returBeliRepository.hapus(returBeli)

        // Periksa nilai jumlah retur di produk
        Produk p1 = returBeliRepository.findProdukById(-1l)
        assertEquals(55, p1.jumlahRetur)
        Produk p2 = returBeliRepository.findProdukById(-2l)
        assertEquals(63, p2.jumlahRetur)
        Produk p3 = returBeliRepository.findProdukById(-3l)
        assertEquals(42, p3.jumlahRetur)
    }


    public void testTukarBaru() {
        returBeliRepository.withTransaction {
            ReturBeli returBeli = returBeliRepository.findReturBeliById(-1l)
            returBeli = returBeliRepository.tukarBaru(returBeli)

            assertTrue(returBeli.sudahDiproses)
            assertTrue(returBeli.getKlaimTukar(true).empty)
            assertNotNull(returBeli.penerimaanBarang)

            Gudang g = findGudangById(-1l)
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)

            assertEquals(2, returBeli.penerimaanBarang.items.size())
            assertEquals(p1, returBeli.penerimaanBarang.items[0].produk)
            assertEquals(5, returBeli.penerimaanBarang.items[0].jumlah)
            assertEquals(p2, returBeli.penerimaanBarang.items[1].produk)
            assertEquals(3, returBeli.penerimaanBarang.items[1].jumlah)

            assertEquals(15, p1.stok(g).jumlah)
            assertEquals(17, p2.stok(g).jumlah)
            assertEquals(15, p3.stok(g).jumlah)
        }

    }

}

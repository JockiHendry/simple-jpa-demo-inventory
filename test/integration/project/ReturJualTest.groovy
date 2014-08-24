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
import domain.inventory.Produk
import domain.retur.ReturJual
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.retur.ReturJualRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class ReturJualTest extends DbUnitTestCase {

	private static final Logger log = LoggerFactory.getLogger(ReturJualTest)

    ReturJualRepository returJualRepository

	protected void setUp() {
		super.setUp()
		setUpDatabase("returJual", "/project/data_penjualan.xls")
        returJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('ReturJual')
	}

	protected void tearDown() {
		super.tearDown()
		super.deleteAll()
	}

    public void testTukarBaru() {
        returJualRepository.withTransaction {
            ReturJual returJual = returJualRepository.findReturJualById(-1l)
            returJual = returJualRepository.tukarBaru(returJual)

            assertTrue(returJual.sudahDiklaim)
            assertTrue(returJual.getBelumDiklaim().empty)

            assertNotNull(returJual.pengeluaranBarang)
            assertTrue(returJual.pengeluaranBarang.sudahDiterima())

            Gudang g = findGudangById(-1l)
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)

            assertEquals('Mr. Nice Guy', returJual.pengeluaranBarang.buktiTerima.namaPenerima)
            assertEquals(LocalDate.now(), returJual.pengeluaranBarang.buktiTerima.tanggalTerima)
            assertEquals(2, returJual.pengeluaranBarang.items.size())
            assertEquals(p1, returJual.pengeluaranBarang.items[0].produk)
            assertEquals(5, returJual.pengeluaranBarang.items[0].jumlah)
            assertEquals(p2, returJual.pengeluaranBarang.items[1].produk)
            assertEquals(3, returJual.pengeluaranBarang.items[1].jumlah)

            assertEquals(5, p1.stok(g).jumlah)
            assertEquals(11, p2.stok(g).jumlah)
            assertEquals(15, p3.stok(g).jumlah)
        }
    }

}
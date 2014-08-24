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
import domain.retur.ReturBeli
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

    public void testTukarBaru() {
        returBeliRepository.withTransaction {
            ReturBeli returBeli = returBeliRepository.findReturBeliById(-1l)
            returBeli = returBeliRepository.tukarBaru(returBeli)

            assertTrue(returBeli.sudahDiklaim)
            assertTrue(returBeli.getBelumDiklaim().empty)
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

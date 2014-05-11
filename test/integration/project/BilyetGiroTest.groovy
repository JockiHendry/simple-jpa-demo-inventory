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

import domain.Container
import domain.faktur.BilyetGiro
import domain.penjualan.BilyetGiroRepository
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class BilyetGiroTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(BilyetGiroTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("fakturJual", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testPencairan() {
        BilyetGiroRepository repo = Container.app.bilyetGiroRepository
        repo.withTransaction {
            BilyetGiro bilyetGiro = repo.cari('AB-111')[0]
            bilyetGiro.cairkan(LocalDate.now())
            assertTrue(bilyetGiro.sudahDicairkan())
        }
        FakturJualOlehSales f = repo.findFakturJualOlehSalesById(-6l)
        assertEquals(StatusFakturJual.LUNAS, f.status)
    }

}

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
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Sales
import domain.util.NomorService
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class NomorServiceTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(NomorServiceTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("penjualan", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    void testGetNomorFakturJualTerakhir() {
        NomorService service = Container.app.nomorService
        assertEquals(2, service.getNomorFakturJualTerakhir())

        Sales sales1 = service.findSalesById(-1l)
        Sales sales2 = service.findSalesById(-2l)
        Sales sales3 = service.findSalesById(-3l)
        assertEquals(4, service.getNomorFakturJualTerakhir(sales1))
        assertEquals(0, service.getNomorFakturJualTerakhir(sales2))
        assertEquals(1, service.getNomorFakturJualTerakhir(sales3))
    }

    void testGetCalonNomorFakturJual() {
        NomorService service = Container.app.nomorService
        String bulanTahun = LocalDate.now().toString('MMyyyy')

        assertEquals("000003/$bulanTahun/ECERAN", service.getCalonNomorFakturJual())

        Sales sales1 = service.findSalesById(-1l)
        Sales sales2 = service.findSalesById(-2l)
        Sales sales3 = service.findSalesById(-3l)
        assertEquals("000005/$bulanTahun/SA", service.getCalonNomorFakturJual(sales1))
        assertEquals("000001/$bulanTahun/SB", service.getCalonNomorFakturJual(sales2))
        assertEquals("000002/$bulanTahun/SC", service.getCalonNomorFakturJual(sales3))
    }

}

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

import domain.penjualan.Sales
import project.user.NomorService
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class NomorServiceTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(NomorServiceTest)

    NomorService nomorService

    protected void setUp() {
        super.setUp()
        setUpDatabase("penjualan", "/project/data_penjualan.xls")
        nomorService = app.serviceManager.findService('Nomor')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    void testGetNomorFakturJualTerakhir() {
        assertEquals(2, nomorService.getNomorFakturJualTerakhir())

        Sales sales1 = nomorService.findSalesById(-1l)
        Sales sales2 = nomorService.findSalesById(-2l)
        Sales sales3 = nomorService.findSalesById(-3l)
        assertEquals(4, nomorService.getNomorFakturJualTerakhir(sales1))
        assertEquals(0, nomorService.getNomorFakturJualTerakhir(sales2))
        assertEquals(1, nomorService.getNomorFakturJualTerakhir(sales3))
    }

    void testGetCalonNomorFakturJual() {
        String bulanTahun = LocalDate.now().toString('MMyyyy')

        assertEquals("000003/$bulanTahun/ECERAN", nomorService.getCalonNomorFakturJual())

        Sales sales1 = nomorService.findSalesById(-1l)
        Sales sales2 = nomorService.findSalesById(-2l)
        Sales sales3 = nomorService.findSalesById(-3l)
        assertEquals("000005/$bulanTahun/SA", nomorService.getCalonNomorFakturJual(sales1))
        assertEquals("000001/$bulanTahun/SB", nomorService.getCalonNomorFakturJual(sales2))
        assertEquals("000002/$bulanTahun/SC", nomorService.getCalonNomorFakturJual(sales3))
    }

}

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
import domain.util.NomorService
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class NomorServiceTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testBuatNomor() {
        NomorService nomorService = Container.app.nomorService
        nomorService.nomorUrutTerakhir[NomorService.TIPE.PURCHASE_ORDER] = 0
        assertEquals(String.format('000001-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000002-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000003-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))

        nomorService.nomorUrutTerakhir[NomorService.TIPE.PURCHASE_ORDER] = 100
        assertEquals(String.format('000101-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000102-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000103-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.buatNomor(NomorService.TIPE.PURCHASE_ORDER))
    }

    void testCalonNomor() {
        NomorService nomorService = Container.app.nomorService
        nomorService.nomorUrutTerakhir[NomorService.TIPE.PURCHASE_ORDER] = 0
        assertEquals(String.format('000001-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.getCalonNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000001-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.getCalonNomor(NomorService.TIPE.PURCHASE_ORDER))
        assertEquals(String.format('000001-PO-KB-%s', LocalDate.now().toString('MMyyyy')), nomorService.getCalonNomor(NomorService.TIPE.PURCHASE_ORDER))
    }
}

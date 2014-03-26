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

import domain.inventory.Periode
import domain.pembelian.Hutang
import domain.pembelian.PembayaranHutang
import griffon.test.*
import org.joda.time.LocalDate

class HutangTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSudahJatuhTempo() {
        Hutang hutang = new Hutang(LocalDate.now())
        assertTrue(hutang.sudahJatuhTempo())
        hutang.jatuhTempo = LocalDate.now().minusDays(1)
        assertTrue(hutang.sudahJatuhTempo())
        hutang.jatuhTempo = LocalDate.now().plusDays(1)
        assertFalse(hutang.sudahJatuhTempo())

        hutang.jatuhTempo = Periode.format.parseLocalDate('01-01-2013')
        assertTrue(hutang.sudahJatuhTempo(Periode.format.parseLocalDate('01-01-2013')))
        assertTrue(hutang.sudahJatuhTempo(Periode.format.parseLocalDate('02-01-2013')))
        assertFalse(hutang.sudahJatuhTempo(Periode.format.parseLocalDate('31-12-2012')))
    }

    void testJumlahDibayar() {
        Hutang hutang = new Hutang(jumlah: 3500000)
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('01-01-2013'), 540000))
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('04-02-2013'), 320000))
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('14-03-2013'), 210000))

        assertEquals(1070000, hutang.jumlahDibayar())
    }

    void testSisa() {
        Hutang hutang = new Hutang(jumlah: 3500000)
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('01-01-2013'),  450000))
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('04-02-2013'),  820000))
        hutang.bayar(new PembayaranHutang(Periode.format.parseLocalDate('14-03-2013'), 1320000))

        assertEquals(910000, hutang.sisa())
    }
}

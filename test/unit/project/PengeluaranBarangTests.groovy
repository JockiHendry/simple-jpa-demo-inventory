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

import domain.penjualan.BuktiTerima
import domain.penjualan.PengeluaranBarang
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class PengeluaranBarangTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testDiterima1() {
        PengeluaranBarang p = new PengeluaranBarang(tanggal: LocalDate.now(), nomor: 'SJ001')
        p.diterima(new BuktiTerima(LocalDate.now(), 'Mr. Xu'))
        assertNotNull(p.buktiTerima)
        assertEquals(LocalDate.now(), p.buktiTerima.tanggalTerima)
        assertEquals('Mr. Xu', p.buktiTerima.namaPenerima)
    }

    public void testDiterima2() {
        PengeluaranBarang p = new PengeluaranBarang(tanggal: LocalDate.now(), nomor: 'SJ001')
        p.diterima(LocalDate.now(), 'Mr.Xu')
        assertNotNull(p.buktiTerima)
        assertEquals(LocalDate.now(), p.buktiTerima.tanggalTerima)
        assertEquals('Mr.Xu', p.buktiTerima.namaPenerima)
    }

    public void testBatalDiterima() {
        PengeluaranBarang p = new PengeluaranBarang(tanggal: LocalDate.now(), nomor: 'SJ001')
        p.diterima(LocalDate.now(), 'Mr.Xu')
        assertNotNull(p.buktiTerima)
        p.batalDiterima()
        assertNull(p.buktiTerima)
    }

    public void testSudahDiterima() {
        PengeluaranBarang p = new PengeluaranBarang(tanggal: LocalDate.now(), nomor: 'SJ001')
        assertFalse(p.sudahDiterima())
        p.diterima(LocalDate.now(), 'Mr.Xu')
        assertTrue(p.sudahDiterima())
    }

}

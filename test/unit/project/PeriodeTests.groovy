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
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class PeriodeTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testBulan() {
        Periode p

        p = Periode.bulan(LocalDate.parse('2014-01-15'))
        assertEquals(LocalDate.parse('2014-01-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-01-31'), p.tanggalSelesai)

        p = Periode.bulan(1, 2014)
        assertEquals(LocalDate.parse('2014-01-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-01-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-02-15'))
        assertEquals(LocalDate.parse('2014-02-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-02-28'), p.tanggalSelesai)

        p = Periode.bulan(2, 2014)
        assertEquals(LocalDate.parse('2014-02-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-02-28'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-03-15'))
        assertEquals(LocalDate.parse('2014-03-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-03-31'), p.tanggalSelesai)

        p = Periode.bulan(3, 2014)
        assertEquals(LocalDate.parse('2014-03-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-03-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-04-15'))
        assertEquals(LocalDate.parse('2014-04-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-04-30'), p.tanggalSelesai)

        p = Periode.bulan(4, 2014)
        assertEquals(LocalDate.parse('2014-04-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-04-30'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-05-15'))
        assertEquals(LocalDate.parse('2014-05-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-05-31'), p.tanggalSelesai)

        p = Periode.bulan(5, 2014)
        assertEquals(LocalDate.parse('2014-05-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-05-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-06-15'))
        assertEquals(LocalDate.parse('2014-06-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-06-30'), p.tanggalSelesai)

        p = Periode.bulan(6, 2014)
        assertEquals(LocalDate.parse('2014-06-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-06-30'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-07-15'))
        assertEquals(LocalDate.parse('2014-07-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-07-31'), p.tanggalSelesai)

        p = Periode.bulan(7, 2014)
        assertEquals(LocalDate.parse('2014-07-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-07-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-08-15'))
        assertEquals(LocalDate.parse('2014-08-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-08-31'), p.tanggalSelesai)

        p = Periode.bulan(8, 2014)
        assertEquals(LocalDate.parse('2014-08-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-08-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-09-15'))
        assertEquals(LocalDate.parse('2014-09-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-09-30'), p.tanggalSelesai)

        p = Periode.bulan(9, 2014)
        assertEquals(LocalDate.parse('2014-09-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-09-30'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-10-15'))
        assertEquals(LocalDate.parse('2014-10-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-10-31'), p.tanggalSelesai)

        p = Periode.bulan(10, 2014)
        assertEquals(LocalDate.parse('2014-10-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-10-31'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-11-15'))
        assertEquals(LocalDate.parse('2014-11-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-11-30'), p.tanggalSelesai)

        p = Periode.bulan(11, 2014)
        assertEquals(LocalDate.parse('2014-11-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-11-30'), p.tanggalSelesai)

        p = Periode.bulan(LocalDate.parse('2014-12-15'))
        assertEquals(LocalDate.parse('2014-12-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-12-31'), p.tanggalSelesai)

        p = Periode.bulan(12, 2014)
        assertEquals(LocalDate.parse('2014-12-01'), p.tanggalMulai)
        assertEquals(LocalDate.parse('2014-12-31'), p.tanggalSelesai)
    }

}

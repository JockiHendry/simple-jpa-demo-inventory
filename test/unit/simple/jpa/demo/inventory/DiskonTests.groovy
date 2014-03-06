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

package simple.jpa.demo.inventory

import domain.faktur.Diskon
import griffon.test.*

class DiskonTests extends GriffonUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testJumlahDiskon() {
        // Diskon 10%
        Diskon diskon = new Diskon(10, 0)
        assertEquals(1000, diskon.jumlahDiskon(10000))
        assertEquals(2000, diskon.jumlahDiskon(20000))

        // Diskon 10% + Rp 1.000
        diskon = new Diskon(10, 1000)
        assertEquals(2000, diskon.jumlahDiskon(10000))
        assertEquals(3000, diskon.jumlahDiskon(20000))

        // Diskon Rp.2000
        diskon = new Diskon(potonganLangsung: 2000)
        assertEquals(2000, diskon.jumlahDiskon(10000))
        assertEquals(2000, diskon.jumlahDiskon(20000))
    }

    void testHasilDiskon() {
        // Diskon 30%
        Diskon diskon = new Diskon(30, 0)
        assertEquals(7000, diskon.hasilDiskon(10000))
        assertEquals(14000, diskon.hasilDiskon(20000))

        // Diskon 5% + Rp 200
        diskon = new Diskon(5, 200)
        assertEquals(9300, diskon.hasilDiskon(10000))
        assertEquals(18800, diskon.hasilDiskon(20000))

        // Diskon Rp.1000
        diskon = new Diskon(potonganLangsung: 1000)
        assertEquals(9000, diskon.hasilDiskon(10000))
        assertEquals(19000, diskon.hasilDiskon(20000))
    }
}

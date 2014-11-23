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

import domain.exception.DataTidakBolehDiubah
import domain.faktur.ItemFaktur
import domain.inventory.Periode
import domain.inventory.Produk
import domain.pembelian.FakturBeli
import griffon.test.*

class FakturBeliTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testBuatHutang() {
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        Produk produkC = new Produk('Produk C',  8000)
        FakturBeli fakturBeli = new FakturBeli(tanggal: Periode.format.parseLocalDate('01-01-2010'))
        fakturBeli.setJatuhTempoPer(15)
        fakturBeli.tambah(new ItemFaktur(produkA, 10,  9000))
        fakturBeli.tambah(new ItemFaktur(produkB,  5, 11000))
        fakturBeli.tambah(new ItemFaktur(produkC,  8,  5000))

        fakturBeli.buatHutang()
        assertEquals(Periode.format.parseLocalDate('16-01-2010'), fakturBeli.jatuhTempo)
        assertEquals(185000, fakturBeli.hutang.jumlah)

        shouldFail(DataTidakBolehDiubah) {
            fakturBeli.buatHutang()
        }
    }

    void testSudahJatuhTempo() {
        FakturBeli fakturBeli = new FakturBeli(tanggal: Periode.format.parseLocalDate('01-01-2013'))
        fakturBeli.setJatuhTempoPer(15)
        assertTrue(fakturBeli.sudahJatuhTempo(Periode.format.parseLocalDate('16-01-2013')))
        assertTrue(fakturBeli.sudahJatuhTempo(Periode.format.parseLocalDate('17-01-2013')))
        assertFalse(fakturBeli.sudahJatuhTempo(Periode.format.parseLocalDate('15-01-2013')))
    }



}

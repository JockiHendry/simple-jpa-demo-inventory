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
import domain.pembelian.PembayaranHutang
import domain.pembelian.StatusFakturBeli
import griffon.test.*
import org.joda.time.LocalDate

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
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now())
        fakturBeli.status = StatusFakturBeli.BARANG_DITERIMA
        fakturBeli.tambah(new ItemFaktur(produkA, 10,  9000))
        fakturBeli.tambah(new ItemFaktur(produkB,  5, 11000))
        fakturBeli.tambah(new ItemFaktur(produkC,  8,  5000))

        fakturBeli.buatHutang()
        assertEquals(fakturBeli.tanggal.plusDays(30), fakturBeli.hutang.jatuhTempo)
        assertEquals(185000, fakturBeli.hutang.jumlah)

        fakturBeli.buatHutang(Periode.format.parseLocalDate('28-02-2014'))
        assertEquals(Periode.format.parseLocalDate('28-02-2014'), fakturBeli.hutang.jatuhTempo)
        assertEquals(185000, fakturBeli.hutang.jumlah)

        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            FakturBeli f = new FakturBeli()
            f.buatHutang()
        }
    }

    void testBayarHutang() {
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        Produk produkC = new Produk('Produk C',  8000)
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now())
        fakturBeli.status = StatusFakturBeli.BARANG_DITERIMA
        fakturBeli.tambah(new ItemFaktur(produkA, 10,  9000))
        fakturBeli.tambah(new ItemFaktur(produkB,  5, 11000))
        fakturBeli.tambah(new ItemFaktur(produkC,  8,  5000))
        fakturBeli.buatHutang()

        fakturBeli.bayarHutang(new PembayaranHutang(Periode.format.parseLocalDate('01-02-2013'), 50000))
        assertEquals(135000, fakturBeli.hutang.sisa())
        assertFalse(fakturBeli.hutang.lunas)

        fakturBeli.bayarHutang(new PembayaranHutang(Periode.format.parseLocalDate('01-03-2013'), 70000))
        assertEquals(65000, fakturBeli.hutang.sisa())
        assertFalse(fakturBeli.hutang.lunas)

        fakturBeli.bayarHutang(new PembayaranHutang(Periode.format.parseLocalDate('01-04-2013'), 50000))
        assertEquals(15000, fakturBeli.hutang.sisa())
        assertFalse(fakturBeli.hutang.lunas)

        fakturBeli.bayarHutang(new PembayaranHutang(Periode.format.parseLocalDate('01-05-2013'), 15000))
        assertEquals(0, fakturBeli.hutang.sisa())
        assertTrue(fakturBeli.hutang.lunas)
    }
}

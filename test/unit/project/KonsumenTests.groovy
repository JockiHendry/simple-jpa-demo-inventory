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

import domain.faktur.Diskon
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.Sales
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class KonsumenTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testSisaPiutang() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(sales: sales)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 20000, 20100, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(nomor: 'F1', konsumen: mrNiceGuy)
        f1.tambah(new ItemFaktur(produkA, 10, 10000))
        f1.tambah(new ItemFaktur(produkB, 5, 20000))
        FakturJualOlehSales f2 = new FakturJualOlehSales(nomor: 'F2', konsumen: mrNiceGuy)
        f2.tambah(new ItemFaktur(produkA, 3, 10000))
        f2.tambah(new ItemFaktur(produkB, 2, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        mrNiceGuy.tambahFakturBelumLunas(f2)
        assertEquals(270000, mrNiceGuy.jumlahPiutang())
        assertEquals(270000, mrNiceGuy.creditTerpakai)

        // Hapus
        mrNiceGuy.hapusFakturBelumLunas(f2)
        assertEquals(200000, mrNiceGuy.jumlahPiutang())
        assertEquals(200000, mrNiceGuy.creditTerpakai)
    }

    public void testAdaTagihanJatuhTempo() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(sales: sales)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 20000, 20100, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(nomor: 'FA1', konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().plusDays(5))
        f1.tambah(new ItemFaktur(produkA, 10, 10000))
        f1.tambah(new ItemFaktur(produkB, 5, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        assertFalse(mrNiceGuy.adaTagihanJatuhTempo())

        FakturJualOlehSales f2 = new FakturJualOlehSales(nomor: 'FA2', konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().minusDays(1))
        f2.tambah(new ItemFaktur(produkA, 3, 10000))
        f2.tambah(new ItemFaktur(produkB, 2, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f2)
        assertTrue(mrNiceGuy.adaTagihanJatuhTempo())
    }

    public void testBolehKredit() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(creditLimit: 50000, sales: sales)
        Produk produkA = new Produk('Produk A', 1000, 1010, 50)
        Produk produkB = new Produk('Produk B', 2000, 2010, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().plusDays(5))
        f1.tambah(new ItemFaktur(produkA, 3, 1000))
        f1.tambah(new ItemFaktur(produkB, 5, 2000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        assertFalse(mrNiceGuy.adaTagihanJatuhTempo())
        assertTrue(mrNiceGuy.bolehKredit(20000))
        assertTrue(mrNiceGuy.bolehKredit(37000))
        assertFalse(mrNiceGuy.bolehKredit(40000))
    }

    public void testGetHargaDiskon() {
        Sales sales = new Sales(gudang: new Gudang(utama: true))
        Konsumen konsumen = new Konsumen(sales: sales)
        Produk produk = new Produk('Produk A', 1000, 1100, 10)
        konsumen.diskon[produk] = new Diskon(2)
        assertEquals(980, konsumen.getHargaDiskon(produk))
    }

    public void testSetHargaDiskon() {
        Sales sales = new Sales(gudang: new Gudang(utama: true))
        Konsumen konsumen = new Konsumen(sales: sales)
        Produk produk = new Produk('Produk A', 1000, 1100, 10)
        konsumen.setDiskon(produk, 980)
        assertEquals(2, konsumen.diskon[produk].potonganPersen)

    }

}

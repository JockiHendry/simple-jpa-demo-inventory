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

import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.Konsumen
import domain.retur.KlaimRetur
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.retur.ReturJualService
import simplejpa.testing.DbUnitTestCase

class ReturJualServiceTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ReturJualServiceTest)

    ReturJualService returJualService

    protected void setUp() {
        super.setUp()
        setUpDatabase("returJual", "/project/data_penjualan.xls")
        returJualService = app.serviceManager.findService('ReturJual')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testHitungPotonganPiutang() {
        Produk produk1 = returJualService.findProdukById(-1l)
        Produk produk2 = returJualService.findProdukById(-2l)
        Produk produk3 = returJualService.findProdukById(-3l)
        def listKlaimRetur = [
            new KlaimRetur(produk1, 10),
            new KlaimRetur(produk2, 20),
            new KlaimRetur(produk3, 30),
            new KlaimRetur(potongan: 1000)
        ]
        def listTukar = []
        Konsumen konsumen = returJualService.findKonsumenById(-1l)
        assertEquals(1400000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))

        listTukar = [new KlaimRetur(produk1, 5)]
        assertEquals(1350000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))

        listTukar = [new KlaimRetur(produk1, 5), new KlaimRetur(produk1, 5)]
        assertEquals(1300000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))

        listTukar = [new KlaimRetur(produk1, 10)]
        assertEquals(1300000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))

        listTukar = [new KlaimRetur(produk1, 5), new KlaimRetur(produk2, 5), new KlaimRetur(produk3, 5)]
        assertEquals(1100000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))

        listTukar = [new KlaimRetur(produk1, 10), new KlaimRetur(produk2, 20)]
        assertEquals(900000, returJualService.hitungPotonganPiutang(listKlaimRetur, listTukar, konsumen))
    }

    public void testCariBarangYangBisaDitukar() {
        Produk produk1 = returJualService.findProdukById(-1l)
        Produk produk2 = returJualService.findProdukById(-2l)
        Produk produk3 = returJualService.findProdukById(-3l)
        Produk produk4 = returJualService.findProdukById(-4l)
        Produk produk5 = returJualService.findProdukById(-5l)
        def items = [
            new ItemBarang(produk1, 10),
            new ItemBarang(produk2, 20),
            new ItemBarang(produk3, 30)
        ]
        Gudang gudang = returJualService.findGudangById(-1l)
        List hasil = returJualService.cariBarangYangBisaDitukar(items, gudang)

        assertEquals(3, hasil.size())
        assertEquals(new KlaimRetur(produk1, 10), hasil[0])
        assertEquals(new KlaimRetur(produk2, 14), hasil[1])
        assertEquals(new KlaimRetur(produk3, 15), hasil[2])

        items = [
            new ItemBarang(produk3, 30),
            new ItemBarang(produk4, 40),
            new ItemBarang(produk5, 50)
        ]
        hasil = returJualService.cariBarangYangBisaDitukar(items, gudang)

        assertEquals(1, hasil.size())
        assertEquals(new KlaimRetur(produk3, 15), hasil[0])
    }

}

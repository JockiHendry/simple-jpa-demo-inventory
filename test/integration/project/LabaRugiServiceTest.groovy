/*
 * Copyright 2015 Jocki Hendry.
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

import domain.inventory.Produk
import domain.labarugi.CacheGlobal
import domain.labarugi.NilaiInventory
import org.joda.time.LocalDate
import project.labarugi.LabaRugiService
import simplejpa.testing.DbUnitTestCase
import static org.junit.Assert.*

class LabaRugiServiceTest extends DbUnitTestCase {

    LabaRugiService labaRugiService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_laba_rugi_1.xlsx")
        labaRugiService = app.serviceManager.findService('LabaRugi')
    }

    void testHitungInventory() {
        Produk produk = labaRugiService.findProdukById(1l)
        NilaiInventory nilaiInventory = labaRugiService.hitungInventory(produk, new CacheGlobal(tanggalMulai: LocalDate.parse('2014-12-31')))

        assertEquals(2, nilaiInventory.toList().size())
        assertEquals(LocalDate.parse('2014-12-19'), nilaiInventory.toList()[1].tanggal)
        assertEquals('supplier', nilaiInventory.toList()[1].nama)
        assertEquals(15, nilaiInventory.toList()[1].harga.toInteger())
        assertEquals(200, nilaiInventory.toList()[1].qty)
        assertEquals(LocalDate.parse('2014-12-15'), nilaiInventory.toList()[0].tanggal)
        assertEquals('supplier', nilaiInventory.toList()[0].nama)
        assertEquals(14, nilaiInventory.toList()[0].harga.toInteger())
        assertEquals(400, nilaiInventory.toList()[0].qty)
        assertEquals(8600, nilaiInventory.nilai().toInteger())
    }

    void testHitungPendapatanKotor() {
        def (penjualanSales, penjualanEceran, potonganPiutang) = labaRugiService.hitungPenjualan(LocalDate.parse('2014-12-01'), LocalDate.parse('2014-12-31'))
        assertEquals(20000, penjualanSales as Integer)
        assertEquals(5000, penjualanEceran as Integer)
        assertEquals(0, potonganPiutang as Integer)

        def (hpp, ongkosKirimBeli) = labaRugiService.hitungHPP(LocalDate.parse('2014-12-01'), LocalDate.parse('2014-12-31'))
        assertEquals(11000, hpp.toInteger())
        assertEquals(10000, ongkosKirimBeli.toInteger())
    }

    void testTotalPendapatanDanPengeluaran() {
        assertEquals(22000, labaRugiService.totalPendapatan(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31')).toInteger())
        assertEquals(0, labaRugiService.totalPengeluaran(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31')).toInteger())
    }

}

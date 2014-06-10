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
import domain.exception.DataTidakBolehDiubah
import domain.inventory.ItemBarang
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PurchaseOrder
import domain.pembelian.PurchaseOrderRepository
import domain.pembelian.StatusPurchaseOrder
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class PurchaseOrderTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderTest)

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("penerimaanBarang", "/project/data_pembelian.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testTidakBolehDiUpdate() {
        PurchaseOrderRepository repo = Container.app.purchaseOrderRepository
        PurchaseOrder p = repo.findPurchaseOrderById(-5l)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.update(p)
        }
    }

    public void testTidakBolehDiHapus() {
        PurchaseOrderRepository repo = Container.app.purchaseOrderRepository
        PurchaseOrder p = repo.findPurchaseOrderById(-5l)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.hapus(p)
        }
    }

    public void testPerubahanStokPadaTambahPenerimaanBarang() {
        PurchaseOrderRepository repo = Container.app.purchaseOrderRepository
        repo.withTransaction {
            PurchaseOrder p = repo.findPurchaseOrderById(-1l)
            assertEquals(37, p.listItemFaktur[0].produk.jumlah)
            assertEquals(27, p.listItemFaktur[1].produk.jumlah)
            assertEquals(28, p.listItemFaktur[2].produk.jumlah)

            PenerimaanBarang penerimaanBarang = new PenerimaanBarang(nomor: 'TESTING!!', tanggal: LocalDate.now(), gudang: repo.findGudangById(-1l))
            penerimaanBarang.tambah(new ItemBarang(repo.findProdukById(-1), 5))
            penerimaanBarang.tambah(new ItemBarang(repo.findProdukById(-2), 3))
            p.tambah(penerimaanBarang)
            assertEquals(42, repo.findProdukById(-1).jumlah)
            assertEquals(30, repo.findProdukById(-2).jumlah)
            penerimaanBarang = new PenerimaanBarang(nomor: 'TESTING 2!!', tanggal: LocalDate.now(), gudang: repo.findGudangById(-1l))
            penerimaanBarang.tambah(new ItemBarang(repo.findProdukById(-3), 4))
            p.tambah(penerimaanBarang)
            assertEquals(32, repo.findProdukById(-3).jumlah)
            assertTrue(p.diterimaPenuh())
        }

    }

    public void testPerubahanStokPadaHapusPenerimaanBarang() {
        PurchaseOrderRepository repo = Container.app.purchaseOrderRepository
        repo.withTransaction {
            PurchaseOrder p = repo.findPurchaseOrderById(-3l)
            assertEquals(1, p.listPenerimaanBarang.size())
            assertEquals(27, p.listItemFaktur[0].produk.jumlah)

            p.hapus(p.listPenerimaanBarang[0])
            assertEquals(25, repo.findProdukById(-2l).jumlah)
            assertEquals(StatusPurchaseOrder.DIBUAT, p.status)
        }
    }
}

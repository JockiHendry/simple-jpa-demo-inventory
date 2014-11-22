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
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PurchaseOrder
import project.pembelian.PurchaseOrderRepository
import domain.pembelian.StatusPurchaseOrder
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class PurchaseOrderTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderTest)

    PurchaseOrderRepository purchaseOrderRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_pembelian.xlsx")
        purchaseOrderRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('PurchaseOrder')
    }

    public void testTidakBolehDiHapus() {
        PurchaseOrder p = purchaseOrderRepository.findPurchaseOrderById(-5l)
        shouldFail(DataTidakBolehDiubah) {
            purchaseOrderRepository.hapus(p)
        }
    }

    public void testPerubahanStokPadaTambahPenerimaanBarang() {
        PurchaseOrder p = purchaseOrderRepository.findPurchaseOrderByIdFetchComplete(-1l)
        assertEquals(37, p.listItemFaktur[0].produk.jumlah)
        assertEquals(27, p.listItemFaktur[1].produk.jumlah)
        assertEquals(28, p.listItemFaktur[2].produk.jumlah)

        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(nomor: 'TESTING!!', tanggal: LocalDate.now(), gudang: purchaseOrderRepository.findGudangById(-1l))
        p = purchaseOrderRepository.tambah(p, penerimaanBarang, [new ItemBarang(purchaseOrderRepository.findProdukById(-1), 5), new ItemBarang(purchaseOrderRepository.findProdukById(-2), 3)])
        Produk produk1 = purchaseOrderRepository.findProdukById(-1l)
        Produk produk2 = purchaseOrderRepository.findProdukById(-2l)
        assertEquals(p.supplier, produk1.supplier)
        assertEquals(p.supplier, produk2.supplier)
        assertEquals(42, produk1.jumlah)
        assertEquals(30, produk2.jumlah)

        penerimaanBarang = new PenerimaanBarang(nomor: 'TESTING 2!!', tanggal: LocalDate.now(), gudang: purchaseOrderRepository.findGudangById(-1l))
        p = purchaseOrderRepository.tambah(p, penerimaanBarang, [new ItemBarang(purchaseOrderRepository.findProdukById(-3), 4)])
        Produk produk3 = purchaseOrderRepository.findProdukById(-3l)
        assertEquals(p.supplier, produk3.supplier)
        assertEquals(32, produk3.jumlah)
        assertTrue(p.diterimaPenuh())
    }

    public void testPerubahanStokPadaHapusPenerimaanBarang() {
        purchaseOrderRepository.withTransaction {
            PurchaseOrder p = purchaseOrderRepository.findPurchaseOrderById(-3l)
            assertEquals(1, p.listPenerimaanBarang.size())
            assertEquals(27, p.listItemFaktur[0].produk.jumlah)

            p.hapusFaktur()
            p.hapus(p.listPenerimaanBarang[0])
            assertEquals(25, purchaseOrderRepository.findProdukById(-2l).jumlah)
            assertEquals(StatusPurchaseOrder.DIBUAT, p.status)
        }
    }
}

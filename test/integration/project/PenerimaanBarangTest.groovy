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
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.ItemStok
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PenerimaanBarangRepository
import domain.pembelian.Supplier
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase
import domain.inventory.Periode

class PenerimaanBarangTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ProdukTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("penerimaanBarang", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testFindReceivedNotInvoiced() {
        List result = Container.app.penerimaanBarangRepository.cariReceivedNotInvoiced(Periode.format.parseLocalDate('01-01-1900'), Periode.format.parseLocalDate('01-01-2099'), null, null)
        assertEquals(3, result.size())
        assertEquals(-2, result[0].id)
        assertEquals('P2', result[0].nomor)
        assertEquals(-3, result[1].id)
        assertEquals('P3', result[1].nomor)
        assertEquals(-4, result[2].id)
        assertEquals('P4', result[2].nomor)
    }

    void testCreate() {
        PenerimaanBarangRepository repo = Container.app.penerimaanBarangRepository
        repo.withTransaction {
            Supplier s = repo.findSupplierById(-1)
            PenerimaanBarang p = new PenerimaanBarang(nomor: 'P5', tanggal: Periode.format.parseLocalDate('01-02-2013'), supplier: s)
            p.tambah(new ItemBarang(repo.findProdukById(-1), 10))
            p.tambah(new ItemBarang(repo.findProdukById(-2), 5))
            p.tambah(new ItemBarang(repo.findProdukById(-3), 3))
            repo.buat(p)

            Gudang gudang = Container.app.gudangRepository.cariGudangUtama()
            Produk produkA = repo.findProdukByIdFetchComplete(-1l)
            Produk produkB = repo.findProdukByIdFetchComplete(-2l)
            Produk produkC = repo.findProdukByIdFetchComplete(-3l)

            assertEquals(47, produkA.jumlah)
            assertEquals(20, produkA.stok(gudang).jumlah)
            assertEquals(10, produkA.stok(gudang).periode(LocalDate.now()).jumlah)
            assertTrue(produkA.stok(gudang).periode(LocalDate.now()).listItem.contains(new ItemStok(LocalDate.now(), p, 10)))

            assertEquals(32, produkB.jumlah)
            assertEquals(19, produkB.stok(gudang).jumlah)
            assertEquals(5, produkB.stok(gudang).periode(LocalDate.now()).jumlah)
            assertTrue(produkB.stok(gudang).periode(LocalDate.now()).listItem.contains(new ItemStok(LocalDate.now(), p, 5)))

            assertEquals(31, produkC.jumlah)
            assertEquals(18, produkC.stok(gudang).jumlah)
            assertEquals(3,  produkC.stok(gudang).periode(LocalDate.now()).jumlah)
            assertTrue(produkC.stok(gudang).periode(LocalDate.now()).listItem.contains(new ItemStok(LocalDate.now(), p, 3)))
        }
    }

    void testDelete() {
        PenerimaanBarangRepository repo = Container.app.penerimaanBarangRepository
        repo.withTransaction {
            PenerimaanBarang p = repo.findPenerimaanBarangById(-4l)
            p = repo.hapus(p)

            assertEquals('Y', p.deleted)

            Produk p1 = repo.findProdukByIdFetchComplete(-1l)
            Produk p2 = repo.findProdukByIdFetchComplete(-2l)
            Produk p3 = repo.findProdukByIdFetchComplete(-3l)
            Gudang gudangUtama = Container.app.gudangRepository.cariGudangUtama()


            assertEquals(32, p1.jumlah)
            assertEquals(5, p1.stok(gudangUtama).jumlah)
            List<ItemStok> listItemStok = p1.stok(gudangUtama).periode(LocalDate.now()).listItem
            assertTrue(listItemStok.contains(new ItemStok(LocalDate.now(), p, -5, 'Invers Akibat Penghapusan')))

            assertEquals(17, p2.jumlah)
            assertEquals(4, p2.stok(gudangUtama).jumlah)
            listItemStok = p2.stok(gudangUtama).periode(LocalDate.now()).listItem
            assertTrue(listItemStok.contains(new ItemStok(LocalDate.now(), p, -10, 'Invers Akibat Penghapusan')))

            assertEquals(21, p3.jumlah)
            assertEquals(8, p3.stok(gudangUtama).jumlah)
            listItemStok = p3.stok(gudangUtama).periode(LocalDate.now()).listItem
            assertTrue(listItemStok.contains(new ItemStok(LocalDate.now(), p, -7, 'Invers Akibat Penghapusan')))
        }
    }

    void testTidakBolehDelete() {
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            PenerimaanBarangRepository repo = Container.app.penerimaanBarangRepository
            PenerimaanBarang p = repo.findPenerimaanBarangById(-1l)
            repo.hapus(p)
        }
    }

    void testTidakBolehUpdate() {
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            PenerimaanBarangRepository repo = Container.app.penerimaanBarangRepository
            PenerimaanBarang p = repo.findPenerimaanBarangById(-1l)
            repo.update(p)
        }
    }
}

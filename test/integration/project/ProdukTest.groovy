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
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.inventory.ProdukRepository
import domain.inventory.PeriodeItemStok
import org.dbunit.dataset.ITable
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase
import domain.inventory.Periode

class ProdukTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ProdukTest)

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("produk", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testStokProduk() {
        ProdukRepository repo = Container.app.produkRepository

        Gudang gudang = repo.findGudangByNama("Gudang")
        Gudang warehouseA = repo.findGudangByNama("Warehouse A")
        Gudang warehouseB = repo.findGudangByNama("Warehouse B")
        Gudang warehouseC = repo.findGudangByNama("Warehouse C")
        Gudang warehouseD = repo.findGudangByNama("Warehouse D")
        Gudang warehouseE = repo.findGudangByNama("Warehouse E")
        Gudang warehouseF = repo.findGudangByNama("Warehouse F")

        Produk produk = repo.findProdukByNamaFetchComplete("Produk A")
        assertEquals(7, produk.daftarStok.size())
        assertEquals(10, produk.stok(gudang).jumlah)
        assertEquals(4, produk.stok(warehouseA).jumlah)
        assertEquals(10, produk.stok(warehouseB).jumlah)
        assertEquals(5, produk.stok(warehouseC).jumlah)
        assertEquals(3, produk.stok(warehouseD).jumlah)
        assertEquals(2, produk.stok(warehouseE).jumlah)
        assertEquals(3, produk.stok(warehouseF).jumlah)

        produk = repo.findProdukByNamaFetchComplete("Produk B")
        assertEquals(3, produk.daftarStok.size())
        assertEquals(14, produk.stok(gudang).jumlah)
        assertEquals(6, produk.stok(warehouseA).jumlah)
        assertEquals(7, produk.stok(warehouseB).jumlah)
    }

    public void testPeriodeItemStok() {
        ProdukRepository repo = Container.app.produkRepository

        Gudang gudang = repo.findGudangByNama("Gudang")
        Gudang warehouseC = repo.findGudangByNama("Warehouse C")

        Produk produk = repo.findProdukByNamaFetchComplete("Produk A")
        StokProduk stok = produk.stok(gudang)
        assertEquals(3, stok.listPeriodeRiwayat.size())
        assertEquals(3, stok.periode(Periode.format.parseLocalDate('15-12-2013')).jumlah)
        assertEquals(Periode.format.parseLocalDate('01-12-2013'), stok.periode(Periode.format.parseLocalDate('15-12-2013')).tanggalMulai)
        assertEquals(Periode.format.parseLocalDate('31-12-2013'), stok.periode(Periode.format.parseLocalDate('15-12-2013')).tanggalSelesai)
        assertEquals(2, stok.periode(Periode.dari('01-01-2014', '31-01-2014'))[0].jumlah)

        stok = produk.stok(warehouseC)
        assertEquals(5, stok.periode(Periode.dari('01-01-2014', '31-01-2014'))[0].jumlah)

        shouldFail {
            stok.periodeUntukArsip(2)
        }

        assertTrue(stok.periodeUntukArsip(3).isEmpty())

        LocalDate lampau = Periode.format.parseLocalDate('15-01-2010')
        PeriodeItemStok periodeLampau = stok.buatPeriode(lampau)
        assertEquals(Periode.format.parseLocalDate('01-01-2010'), periodeLampau.tanggalMulai)
        assertEquals(Periode.format.parseLocalDate('31-01-2010'), periodeLampau.tanggalSelesai)

        assertNotNull(periodeLampau)
        assertTrue(!stok.periodeUntukArsip(3).isEmpty())
    }

    public void testArsipItemStok() {
        log.debug "Mulai dari testArsipItemStok..."

        ProdukRepository repo = Container.app.produkRepository

        log.debug "Mencari produk Z..."
        Produk produk = repo.findProdukByNama("Produk Z", [fetchGraph: 'Produk.Complete'])
        log.debug "Produk ditemukan."

        log.debug "Mencari gudang Gudang"
        Gudang gudang = repo.findGudangByNama("Gudang")
        log.debug "Gudang ditemukan."

        log.debug "Mencari StokProduk untuk produk Z dan Gudang..."
        StokProduk stok = produk.stok(gudang)
        log.debug "Stok ditemukan."

        log.debug "Mencari periode item stok untuk tahun 2010..."
        PeriodeItemStok periodeLampau = stok.periode(Periode.format.parseLocalDate('01-01-2010'))
        log.debug "Periode item stok ditemukan."

        assertNotNull(periodeLampau)
        assertEquals(10, periodeLampau.jumlah)

        log.debug "Memulai proses pengarsipan..."
        repo.arsipItemStok(3)
        log.debug "Proses pengarsipan selesai."

        ITable aktualItemStok = getConnection().createQueryTable("AktualItemStok",
                "SELECT * FROM periodeitemstok_listitem WHERE periodeItemStok_Id <> -18")
        assertEquals(22, aktualItemStok.rowCount)

        repo.withTransaction {
            log.debug "Mencari produk Z..."
            produk = repo.findProdukByNama("Produk Z", [fetchGraph: 'Produk.Complete'])
            log.debug "Produk ditemukan."

            log.debug "Mencari gudang Gudang..."
            gudang = repo.findGudangByNama("Gudang")
            log.debug "Gudang ditemukan."

            log.debug "Mencari StokProduk untuk produk Z dan Gudang..."
            stok = produk.stok(gudang)
            log.debug "Stok ditemukan."

            log.debug "Mencari periode item stok untuk tahun 2010..."
            periodeLampau = stok.periode(Periode.format.parseLocalDate('01-01-2010'))
            log.debug "Periode item stok ditemukan."

            assertTrue(periodeLampau.arsip)
            assertEquals(10, periodeLampau.jumlah)

            assertTrue(periodeLampau.listItem.isEmpty())
        }
    }

}
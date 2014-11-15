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

import domain.exception.BarangSelisih
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.servis.PenerimaanServis
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.inventory.GudangRepository
import project.servis.PenerimaanServisRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class PenerimaanServisTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(PenerimaanServisTest)

    PenerimaanServisRepository penerimaanServisRepository
    GudangRepository gudangRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("penerimaanServis", "/project/data_retur_jual.xls")
        penerimaanServisRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('PenerimaanServisRepository')
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('GudangRepository')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuatDanHapus() {
        Produk p1 = penerimaanServisRepository.findProdukById(-1l)
        Produk p2 = penerimaanServisRepository.findProdukById(-2l)
        Produk p3 = penerimaanServisRepository.findProdukById(-3l)
        Gudang gudang = gudangRepository.cariGudangUtama()
        PenerimaanServis penerimaanServis = new PenerimaanServis(nomor: 'PS-001', tanggal: LocalDate.now(), gudang: gudang)
        penerimaanServis.tambah(new ItemBarang(p1, 5))
        penerimaanServis.tambah(new ItemBarang(p2, 2))
        penerimaanServis.tambah(new ItemBarang(p3, 3))
        penerimaanServis = penerimaanServisRepository.buat(penerimaanServis)

        // Periksa apakah qty retur berkurang
        p1 = penerimaanServisRepository.findProdukById(-1l)
        p2 = penerimaanServisRepository.findProdukById(-2l)
        p3 = penerimaanServisRepository.findProdukById(-3l)
        assertEquals(8, p1.jumlahTukar)
        assertEquals(2, p2.jumlahTukar)
        assertEquals(3, p3.jumlahTukar)
        assertEquals(5, p1.jumlahRetur)
        assertEquals(1, p2.jumlahRetur)
        assertEquals(2, p3.jumlahRetur)

        // Hapus
        penerimaanServis = penerimaanServisRepository.hapus(penerimaanServis)

        // Periksa apakah qty retur bertambah
        p1 = penerimaanServisRepository.findProdukById(-1l)
        p2 = penerimaanServisRepository.findProdukById(-2l)
        p3 = penerimaanServisRepository.findProdukById(-3l)
        assertEquals(3, p1.jumlahTukar)
        assertEquals(0, p2.jumlahTukar)
        assertEquals(0, p3.jumlahTukar)
        assertEquals(10, p1.jumlahRetur)
        assertEquals( 3, p2.jumlahRetur)
        assertEquals( 5, p3.jumlahRetur)
    }

    public void testBuatDenganJumlahSalah() {
        Produk p1 = penerimaanServisRepository.findProdukById(-1l)
        Produk p2 = penerimaanServisRepository.findProdukById(-2l)
        Produk p3 = penerimaanServisRepository.findProdukById(-3l)
        Gudang gudang = gudangRepository.cariGudangUtama()
        PenerimaanServis penerimaanServis = new PenerimaanServis(nomor: 'PS-001', tanggal: LocalDate.now(), gudang: gudang)
        penerimaanServis.tambah(new ItemBarang(p1, 10))
        penerimaanServis.tambah(new ItemBarang(p2, 20))
        penerimaanServis.tambah(new ItemBarang(p3, 30))
        shouldFail(BarangSelisih) {
            penerimaanServisRepository.buat(penerimaanServis)
        }
    }

}
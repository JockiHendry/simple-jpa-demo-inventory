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
import domain.inventory.ItemBarang
import domain.inventory.PenyesuaianStok
import domain.inventory.PenyesuaianStokRepository
import domain.inventory.Produk
import org.joda.time.LocalDate
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class PenyesuaianStokTest extends DbUnitTestCase {

    PenyesuaianStokRepository penyesuaianStokRepository = SimpleJpaUtil.container.penyesuaianStokRepository

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("gudang", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuatBertambahDanHapus() {
        Gudang gudang = penyesuaianStokRepository.findGudangById(-1l)
        Produk produkA = penyesuaianStokRepository.findProdukById(-1l)
        Produk produkB = penyesuaianStokRepository.findProdukById(-2l)
        Produk produkC = penyesuaianStokRepository.findProdukById(-3l)

        PenyesuaianStok penyesuaianStok = new PenyesuaianStok(tanggal: LocalDate.now(), gudang: gudang,
            bertambah: true, keterangan: 'Hasil Stock Opname Akhir Tahun 2013')
        penyesuaianStok.tambah(new ItemBarang(produk: produkA, jumlah: 6))
        penyesuaianStok.tambah(new ItemBarang(produk: produkB, jumlah: 7))
        penyesuaianStok.tambah(new ItemBarang(produk: produkC, jumlah: 3))
        penyesuaianStok = penyesuaianStokRepository.buat(penyesuaianStok)

        assertNotNull(penyesuaianStok.nomor)

        // Cek stok
        penyesuaianStokRepository.withTransaction {
            assertEquals(16, penyesuaianStokRepository.findProdukById(-1l).stok(gudang).jumlah)
            assertEquals(21, penyesuaianStokRepository.findProdukById(-2l).stok(gudang).jumlah)
            assertEquals(18, penyesuaianStokRepository.findProdukById(-3l).stok(gudang).jumlah)
        }

        penyesuaianStok = penyesuaianStokRepository.hapus(penyesuaianStok)
        assertEquals('Y', penyesuaianStok.deleted)

        // Cek stok
        penyesuaianStokRepository.withTransaction {
            assertEquals(10, penyesuaianStokRepository.findProdukById(-1l).stok(gudang).jumlah)
            assertEquals(14, penyesuaianStokRepository.findProdukById(-2l).stok(gudang).jumlah)
            assertEquals(15, penyesuaianStokRepository.findProdukById(-3l).stok(gudang).jumlah)
        }
    }

    public void testBuatBerkurangDanHapus() {
        Gudang gudang = penyesuaianStokRepository.findGudangById(-1l)
        Produk produkA = penyesuaianStokRepository.findProdukById(-1l)
        Produk produkB = penyesuaianStokRepository.findProdukById(-2l)
        Produk produkC = penyesuaianStokRepository.findProdukById(-3l)

        PenyesuaianStok penyesuaianStok = new PenyesuaianStok(tanggal: LocalDate.now(), gudang: gudang,
            bertambah: false, keterangan: 'Hasil Stock Opname Akhir Tahun 2013')
        penyesuaianStok.tambah(new ItemBarang(produk: produkA, jumlah: 6))
        penyesuaianStok.tambah(new ItemBarang(produk: produkB, jumlah: 7))
        penyesuaianStok.tambah(new ItemBarang(produk: produkC, jumlah: 3))
        penyesuaianStok = penyesuaianStokRepository.buat(penyesuaianStok)

        assertNotNull(penyesuaianStok.nomor)

        // Cek stok
        penyesuaianStokRepository.withTransaction {
            assertEquals(4, penyesuaianStokRepository.findProdukById(-1l).stok(gudang).jumlah)
            assertEquals(7, penyesuaianStokRepository.findProdukById(-2l).stok(gudang).jumlah)
            assertEquals(12, penyesuaianStokRepository.findProdukById(-3l).stok(gudang).jumlah)
        }

        penyesuaianStok = penyesuaianStokRepository.hapus(penyesuaianStok)
        assertEquals('Y', penyesuaianStok.deleted)

        // Cek stok
        penyesuaianStokRepository.withTransaction {
            assertEquals(10, penyesuaianStokRepository.findProdukById(-1l).stok(gudang).jumlah)
            assertEquals(14, penyesuaianStokRepository.findProdukById(-2l).stok(gudang).jumlah)
            assertEquals(15, penyesuaianStokRepository.findProdukById(-3l).stok(gudang).jumlah)
        }
    }

}

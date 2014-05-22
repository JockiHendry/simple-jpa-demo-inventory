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
import domain.inventory.Produk
import domain.inventory.Transfer
import domain.inventory.TransferRepository
import org.joda.time.LocalDate
import simplejpa.testing.DbUnitTestCase

class TransferTest extends DbUnitTestCase {

    protected void setUp() {
        super.setUp()
        setUpDatabase("gudang", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuatDanHapus() {
        TransferRepository repo = Container.app.transferRepository
        Gudang gudangAsal = repo.findGudangById(-1l)
        Gudang gudangTujuan = repo.findGudangById(-2l)
        Produk produkA = repo.findProdukById(-1l)
        Produk produkB = repo.findProdukById(-2l)
        Produk produkC = repo.findProdukById(-3l)

        Transfer transfer = new Transfer(tanggal: LocalDate.now(), gudang: gudangAsal, tujuan: gudangTujuan)
        transfer.tambah(new ItemBarang(produk: produkA, jumlah: 6))
        transfer.tambah(new ItemBarang(produk: produkB, jumlah: 7))
        transfer.tambah(new ItemBarang(produk: produkC, jumlah: 10))
        transfer = repo.buat(transfer)

        assertNotNull(transfer.nomor)

        // Cek stok gudang asal
        repo.withTransaction {
            assertEquals(4, repo.findProdukById(-1l).stok(gudangAsal).jumlah)
            assertEquals(7, repo.findProdukById(-2l).stok(gudangAsal).jumlah)
            assertEquals(5, repo.findProdukById(-3l).stok(gudangAsal).jumlah)
        }

        // Cek stok gudang tujuan
        repo.withTransaction {
            assertEquals(10, repo.findProdukById(-1l).stok(gudangTujuan).jumlah)
            assertEquals(13, repo.findProdukById(-2l).stok(gudangTujuan).jumlah)
            assertEquals(10, repo.findProdukById(-3l).stok(gudangTujuan).jumlah)
        }

        transfer = repo.hapus(transfer)
        assertEquals('Y', transfer.deleted)

        // Cek stok gudang asal
        repo.withTransaction {
            assertEquals(10, repo.findProdukById(-1l).stok(gudangAsal).jumlah)
            assertEquals(14, repo.findProdukById(-2l).stok(gudangAsal).jumlah)
            assertEquals(15, repo.findProdukById(-3l).stok(gudangAsal).jumlah)
        }

        // Cek stok gudang tujuan
        repo.withTransaction {
            assertEquals(4, repo.findProdukById(-1l).stok(gudangTujuan).jumlah)
            assertEquals(6, repo.findProdukById(-2l).stok(gudangTujuan).jumlah)
            assertEquals(0, repo.findProdukById(-3l).stok(gudangTujuan).jumlah)
        }

    }

}

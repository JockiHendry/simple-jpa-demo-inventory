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
import domain.inventory.Transfer
import project.inventory.TransferRepository
import org.joda.time.LocalDate
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class TransferTest extends DbUnitTestCase {

    TransferRepository transferRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_inventory.xlsx")
        transferRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Transfer')
    }

    public void testBuatDanHapus() {
        Gudang gudangAsal = transferRepository.findGudangById(-1l)
        Gudang gudangTujuan = transferRepository.findGudangById(-2l)
        Produk produkA = transferRepository.findProdukById(-1l)
        Produk produkB = transferRepository.findProdukById(-2l)
        Produk produkC = transferRepository.findProdukById(-3l)

        Transfer transfer = new Transfer(tanggal: LocalDate.now(), gudang: gudangAsal, tujuan: gudangTujuan)
        transfer.tambah(new ItemBarang(produk: produkA, jumlah: 6))
        transfer.tambah(new ItemBarang(produk: produkB, jumlah: 7))
        transfer.tambah(new ItemBarang(produk: produkC, jumlah: 10))
        transfer = transferRepository.buat(transfer)

        assertNotNull(transfer.nomor)

        // Cek stok gudang asal
        transferRepository.withTransaction {
            assertEquals(4, transferRepository.findProdukById(-1l).stok(gudangAsal).jumlah)
            assertEquals(7, transferRepository.findProdukById(-2l).stok(gudangAsal).jumlah)
            assertEquals(5, transferRepository.findProdukById(-3l).stok(gudangAsal).jumlah)
        }

        // Cek stok gudang tujuan
        transferRepository.withTransaction {
            assertEquals(10, transferRepository.findProdukById(-1l).stok(gudangTujuan).jumlah)
            assertEquals(13, transferRepository.findProdukById(-2l).stok(gudangTujuan).jumlah)
            assertEquals(10, transferRepository.findProdukById(-3l).stok(gudangTujuan).jumlah)
        }

        transfer = transferRepository.hapus(transfer)
        assertEquals('Y', transfer.deleted)

        // Cek stok gudang asal
        transferRepository.withTransaction {
            assertEquals(10, transferRepository.findProdukById(-1l).stok(gudangAsal).jumlah)
            assertEquals(14, transferRepository.findProdukById(-2l).stok(gudangAsal).jumlah)
            assertEquals(15, transferRepository.findProdukById(-3l).stok(gudangAsal).jumlah)
        }

        // Cek stok gudang tujuan
        transferRepository.withTransaction {
            assertEquals(4, transferRepository.findProdukById(-1l).stok(gudangTujuan).jumlah)
            assertEquals(6, transferRepository.findProdukById(-2l).stok(gudangTujuan).jumlah)
            assertEquals(0, transferRepository.findProdukById(-3l).stok(gudangTujuan).jumlah)
        }

    }

}

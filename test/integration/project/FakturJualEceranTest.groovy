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
import domain.faktur.ItemFaktur
import domain.inventory.GudangRepository
import domain.inventory.Produk
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualRepository
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class FakturJualEceranTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(FakturJualEceranTest)

    GudangRepository gudangRepository = SimpleJpaUtil.container.gudangRepository
    FakturJualRepository fakturJualRepository = SimpleJpaUtil.container.fakturJualRepository

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("fakturJual", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuat() {
        Container.app.nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1)
        Produk produkB = fakturJualRepository.findProdukById(-2)
        FakturJualEceran fakturJualEceran = new FakturJualEceran(tanggal: LocalDate.now(), namaPembeli: 'Mr. Wu')
        fakturJualEceran.tambah(new ItemFaktur(produkA, 10, 10000))
        fakturJualEceran.tambah(new ItemFaktur(produkB, 5,   5000))
        fakturJualEceran = fakturJualRepository.buat(fakturJualEceran)
        assertNotNull(fakturJualEceran.nomor)
        assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)
        assertEquals(125000, fakturJualEceran.total())
    }

    public void testAntar() {
        FakturJualEceran fakturJualEceran
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-1l)
            assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)
            fakturJualEceran.antar()
            assertEquals(StatusFakturJual.DIANTAR, fakturJualEceran.status)
        }

        // Periksa apakah jumlah produk berkurang
        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1)
            Produk produkB = fakturJualRepository.findProdukById(-2)
            assertEquals(29, produkA.jumlah)
            assertEquals(19, produkB.jumlah)
            assertEquals(2, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(6, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
        }

        // Pastikan bahwa faktur tidak dapat di-edit setelah diantar
        fakturJualEceran.namaPembeli = 'A correction'
        shouldFail(DataTidakBolehDiubah) {
            fakturJualRepository.update(fakturJualEceran)
        }
    }

    public void testBatalkanAntar() {
        FakturJualEceran fakturJualEceran
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-2l)
            fakturJualEceran.hapusPengeluaranBarang()
        }
        assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)

        // Periksa apakah jumlah produk bertambah
        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1)
            Produk produkB = fakturJualRepository.findProdukById(-2)
            assertEquals(45, produkA.jumlah)
            assertEquals(35, produkB.jumlah)
            assertEquals(18, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(22, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
        }

        // Pastikan bahwa pengantaran tidak dapat dibatalkan setelah diterima
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-2l)
            fakturJualEceran.antar()
            fakturJualEceran.bayar()
            shouldFail(DataTidakBolehDiubah) {
                fakturJualEceran.hapusPengeluaranBarang()
            }
        }
    }

    public void testBayar() {
        FakturJualEceran fakturJualEceran
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-2l)
            fakturJualEceran.bayar()
        }
        assertEquals(StatusFakturJual.LUNAS, fakturJualEceran.status)

        // Belum boleh bayar
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-1l)
            shouldFail(DataTidakBolehDiubah) {
                fakturJualEceran.bayar()
            }
        }
    }

}

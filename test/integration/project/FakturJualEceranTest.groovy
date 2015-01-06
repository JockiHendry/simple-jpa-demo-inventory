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

import domain.exception.DataTidakBolehDiubah
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.pengaturan.KeyPengaturan
import domain.penjualan.FakturJualOlehSales
import project.inventory.GudangRepository
import domain.inventory.Produk
import domain.penjualan.FakturJualEceran
import project.pengaturan.PengaturanRepository
import project.penjualan.FakturJualRepository
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class FakturJualEceranTest extends DbUnitTestCase {

    GudangRepository gudangRepository
    FakturJualRepository fakturJualRepository
    PengaturanRepository pengaturanRepository
    NomorService nomorService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_penjualan.xlsx")
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')
        fakturJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('FakturJual')
        pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan')
        nomorService = app.serviceManager.findService('Nomor')
    }

    public void testBuat() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1)
        Produk produkB = fakturJualRepository.findProdukById(-2)
        FakturJualEceran fakturJualEceran = new FakturJualEceran(tanggal: LocalDate.now(), namaPembeli: 'Mr. Wu')
        fakturJualEceran.tambah(new ItemFaktur(produkA, 10, 10000))
        fakturJualEceran.tambah(new ItemFaktur(produkB, 5,   5000))
        fakturJualEceran = fakturJualRepository.buat(fakturJualEceran)
        assertNotNull(fakturJualEceran.nomor)
        assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)
        assertEquals(125000, fakturJualEceran.total())

        // Periksa jumlah pemesanan di produk
        produkA = fakturJualRepository.findProdukById(-1)
        produkB = fakturJualRepository.findProdukById(-2)
        assertEquals(10, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        // Hapus
        fakturJualRepository.hapus(fakturJualEceran)
        produkA = fakturJualRepository.findProdukById(-1)
        produkB = fakturJualRepository.findProdukById(-2)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testAntar() {
        FakturJualEceran fakturJualEceran
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-1l)
            assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)
            Produk produkA = fakturJualRepository.findProdukById(-1)
            Produk produkB = fakturJualRepository.findProdukById(-2)
            produkA.jumlahAkanDikirim = 10
            produkB.jumlahAkanDikirim = 10
            fakturJualEceran = fakturJualRepository.proses(fakturJualEceran)
            assertEquals(StatusFakturJual.DIANTAR, fakturJualEceran.status)
        }

        // Periksa apakah jumlah produk berkurang
        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1)
            Produk produkB = fakturJualRepository.findProdukById(-2)
            assertEquals(2, produkA.jumlahAkanDikirim)
            assertEquals(2, produkB.jumlahAkanDikirim)
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
            fakturJualEceran = fakturJualRepository.hapus(fakturJualEceran)
        }
        assertEquals(StatusFakturJual.DIBUAT, fakturJualEceran.status)

        // Periksa apakah jumlah produk bertambah
        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1)
            Produk produkB = fakturJualRepository.findProdukById(-2)
            assertEquals(8, produkA.jumlahAkanDikirim)
            assertEquals(8, produkB.jumlahAkanDikirim)
            assertEquals(45, produkA.jumlah)
            assertEquals(35, produkB.jumlah)
            assertEquals(18, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(22, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
        }

        // Pastikan bahwa pengantaran tidak dapat dibatalkan setelah diterima
//        fakturJualRepository.withTransaction {
//            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-2l)
//            fakturJualEceran = fakturJualRepository.proses(fakturJualEceran) // antar
//            fakturJualEceran = fakturJualRepository.proses(fakturJualEceran) // bayar
//            shouldFail {
//                fakturJualRepository.hapus(fakturJualEceran)
//            }
//        }
    }

    public void testBayar() {
        FakturJualEceran fakturJualEceran
        fakturJualRepository.withTransaction {
            fakturJualEceran = fakturJualRepository.findFakturJualEceranById(-2l)
            fakturJualEceran.proses()
        }
        assertEquals(StatusFakturJual.LUNAS, fakturJualEceran.status)
    }

    public void testProsesSemuaFakturJualEceran() {
        fakturJualRepository.prosesSemuaFakturJualEceran()
        List<FakturJualEceran> hasil = fakturJualRepository.findAllFakturJualEceran()
        assertEquals(2, hasil.size())
        assertEquals(StatusFakturJual.DIANTAR, hasil[0].status)
        assertEquals(StatusFakturJual.DIANTAR, hasil[1].status)
    }

    public void testLunasiSemuaFakturJualEceran() {
        fakturJualRepository.lunasiSemuaFakturJualEceran()
        List<FakturJualEceran> hasil = fakturJualRepository.findAllFakturJualEceran()
        assertEquals(2, hasil.size())
        assertEquals(StatusFakturJual.LUNAS, hasil[0].status)
        assertEquals(StatusFakturJual.LUNAS, hasil[1].status)
    }

    public void testProsesSemuaFakturJualSales() {
        fakturJualRepository.prosesSemuaFakturJualSales()
        fakturJualRepository.withTransaction {
            List<FakturJualOlehSales> hasil = fakturJualRepository.findAllFakturJualOlehSales()
            assertEquals(5, hasil.size())
            (0..4).each { i ->
                assertEquals(StatusFakturJual.DITERIMA, hasil[i].status)
                assertNotNull(hasil[i].pengeluaranBarang)
                assertNotNull(hasil[i].pengeluaranBarang.buktiTerima.namaPenerima)
                assertNotNull(hasil[i].pengeluaranBarang.buktiTerima.namaSupir)
                assertNotNull(hasil[i].pengeluaranBarang.buktiTerima.tanggalTerima)
            }
        }
    }

    public void testBuatFakturTanpaWorkflow() {
        Gudang gudang = gudangRepository.cariGudangUtama()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        FakturJualEceran faktur = new FakturJualEceran(namaPembeli: 'Mr. Xu', tanggal: LocalDate.now())
        faktur.tambah(new ItemFaktur(produkA,  5, 1000))
        faktur.tambah(new ItemFaktur(produkB, 10, 1000))
        pengaturanRepository.cache[KeyPengaturan.WORKFLOW_GUDANG] = false
        faktur = fakturJualRepository.buat(faktur)

        // Periksa status faktur
        assertEquals(StatusFakturJual.LUNAS, faktur.status)
        assertNotNull(faktur.pengeluaranBarang)
        assertEquals(2, faktur.pengeluaranBarang.items.size())
        assertEquals(produkA, faktur.pengeluaranBarang.items[0].produk)
        assertEquals(5, faktur.pengeluaranBarang.items[0].jumlah)
        assertEquals(produkB, faktur.pengeluaranBarang.items[1].produk)
        assertEquals(10, faktur.pengeluaranBarang.items[1].jumlah)

        // Periksa apakah jumlah barang sudah berkurang
        produkA = fakturJualRepository.findProdukByIdFetchStokProduk(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(32, produkA.jumlah)
        assertEquals(17, produkB.jumlah)
        assertEquals(5, produkA.stok(gudang).jumlah)
        assertEquals(4, produkB.stok(gudang).jumlah)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        pengaturanRepository.cache[KeyPengaturan.WORKFLOW_GUDANG] = true
    }

}

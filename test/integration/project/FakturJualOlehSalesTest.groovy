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
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.BilyetGiro
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.Pembayaran
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import project.penjualan.FakturJualRepository
import domain.penjualan.Konsumen
import project.penjualan.KonsumenRepository
import domain.penjualan.Sales
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.user.NomorService
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class FakturJualOlehSalesTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(FakturJualOlehSalesTest)

    GudangRepository gudangRepository
    FakturJualRepository fakturJualRepository
    KonsumenRepository konsumenRepository
    NomorService nomorService

    protected void setUp() {
        super.setUp()
        setUpDatabase("fakturJual", "/project/data_penjualan.xls")
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')
        fakturJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('FakturJual')
        konsumenRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Konsumen')
        nomorService = app.serviceManager.findService('Nomor')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuatFakturJualOlehSalesDalamKota() {
        nomorService.refreshAll()

        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-1l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        // Periksa harga terakhir sebelum faktur dibuat
        assertEquals(1000, konsumenRepository.hargaTerakhir(konsumen, produkA))
        assertEquals(1500, konsumenRepository.hargaTerakhir(konsumen, produkB))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)

        assertEquals(StatusFakturJual.DIBUAT, fakturJualOlehSales.status)
        assertEquals(LocalDate.now().plusDays(30), fakturJualOlehSales.jatuhTempo)

        // Periksa apakah faktur ada di konsumen
        konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(-1l)
        assertTrue(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))

        // Periksa apakah harga terakhir terubah
        assertEquals(8000, konsumenRepository.hargaTerakhir(konsumen, produkA))
        assertEquals(1000, konsumenRepository.hargaTerakhir(konsumen, produkB))
    }

    public void testLimitDalamKota() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-1l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 100000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 100000))
        shouldFail(MelebihiBatasKredit) {
            fakturJualRepository.buat(fakturJualOlehSales)
        }
    }

    public void testBuatFakturJualOlehSalesLuarKota() {
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-2l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-2l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 3, 1000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1500))

        // Periksa harga terakhir sebelum faktur dibuat
        assertEquals(1100, konsumenRepository.hargaTerakhir(konsumen, produkA))
        assertEquals(1600, konsumenRepository.hargaTerakhir(konsumen, produkB))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)


        // Periksa faktur
        assertEquals(StatusFakturJual.DITERIMA, fakturJualOlehSales.status)
        assertEquals(LocalDate.now().plusDays(30), fakturJualOlehSales.jatuhTempo)
        assertNotNull(fakturJualOlehSales.pengeluaranBarang)
        assertTrue(fakturJualOlehSales.pengeluaranBarang.isiSamaDengan(fakturJualOlehSales))
        assertTrue(fakturJualOlehSales.pengeluaranBarang.sudahDiterima())

        // Periksa apakah jumlah barang berkurang
        fakturJualRepository.withTransaction {
            produkA = fakturJualRepository.findProdukById(-1l)
            produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(1, produkA.stok(sales.gudang).jumlah)
            assertEquals(1, produkB.stok(sales.gudang).jumlah)
        }

        // Periksa piutang
        fakturJualRepository.withTransaction {
            fakturJualOlehSales = merge(fakturJualOlehSales)
            assertNotNull(fakturJualOlehSales.piutang)
            assertEquals(fakturJualOlehSales.total(), fakturJualOlehSales.sisaPiutang())
        }

        // Periksa apakah faktur diterima oleh konsumen
        fakturJualRepository.withTransaction {
            konsumen = findKonsumenByIdFetchFakturBelumLunas(-2l)
            assertTrue(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))
            assertEquals(fakturJualOlehSales.total(), konsumen.jumlahPiutang())
        }
        // Periksa apakah harga terakhir terubah
        assertEquals(1000, konsumenRepository.hargaTerakhir(konsumen, produkA))
        assertEquals(1500, konsumenRepository.hargaTerakhir(konsumen, produkB))

    }

    public void testBatalkanPengeluaranBarangUntukSalesLuarKota() {
        fakturJualRepository.withTransaction {
            FakturJualOlehSales fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-3l)
            shouldFail(DataTidakBolehDiubah) {
                fakturJualOlehSales.hapusPengeluaranBarang()
            }
        }
    }

    public void testPengantaran() {
        fakturJualRepository.withTransaction {
            FakturJualOlehSales fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-4l)
            fakturJualOlehSales.kirim('Final Destination', 'Jocker')
            assertEquals(StatusFakturJual.DIANTAR, fakturJualOlehSales.status)
            assertNotNull(fakturJualOlehSales.pengeluaranBarang)
            assertTrue(fakturJualOlehSales.pengeluaranBarang.isiSamaDengan(fakturJualOlehSales))
            assertEquals(LocalDate.now(), fakturJualOlehSales.pengeluaranBarang.tanggal)
            assertEquals('Final Destination', fakturJualOlehSales.pengeluaranBarang.alamatTujuan)
            assertEquals('Jocker', fakturJualOlehSales.pengeluaranBarang.namaSupir)

            // Cek jumlah produk berkurang
            Produk produkA = fakturJualRepository.findProdukById(-1l)
            Produk produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(6, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(11, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)

            // Pembatalan
            fakturJualOlehSales.hapusPengeluaranBarang()
            assertEquals(StatusFakturJual.DIBUAT, fakturJualOlehSales.status)
            produkA = fakturJualRepository.findProdukById(-1l)
            produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(10, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(14, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
        }
    }

    public void testPenerimaan() {
        fakturJualRepository.withTransaction {
            FakturJualOlehSales fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-5l)
            fakturJualOlehSales.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))
            assertEquals(StatusFakturJual.DITERIMA, fakturJualOlehSales.status)
            assertEquals(LocalDate.now(), fakturJualOlehSales.pengeluaranBarang.buktiTerima.tanggalTerima)
            assertEquals('Mr. Stranger', fakturJualOlehSales.pengeluaranBarang.buktiTerima.namaPenerima)
            assertNotNull(fakturJualOlehSales.piutang)
            assertEquals(fakturJualOlehSales.total(), fakturJualOlehSales.piutang.jumlah)

            // Memeriksa poin konsumen
            Konsumen mrNiceGuy = fakturJualRepository.findKonsumenById(-1l)
            assertEquals(52, mrNiceGuy.poinTerkumpul)

            // Menghapus penerimaan
            fakturJualOlehSales.hapusBuktiTerima()
            assertEquals(StatusFakturJual.DIANTAR, fakturJualOlehSales.status)
            assertNull(fakturJualOlehSales.piutang)

            // Memeriksa poin konsumen setelah penghapusan
            mrNiceGuy = fakturJualRepository.findKonsumenById(-1l)
            assertEquals(50, mrNiceGuy.poinTerkumpul)

            // Menambah penerimaan, melakukan pembayaran, sehingga penerimaan tidak boleh dihapus
            fakturJualOlehSales.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))
            fakturJualOlehSales.bayar(new Pembayaran(LocalDate.now(), 1))
            shouldFail(DataTidakBolehDiubah) {
                fakturJualOlehSales.hapusBuktiTerima()
            }
        }
    }

    public void testPembayaran() {
        FakturJualOlehSales fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-7l)
        BilyetGiro bg = new BilyetGiro(nomorSeri: 'NX-0001', nominal: 10000, jatuhTempo: LocalDate.now().minusDays(1))
        fakturJualOlehSales = fakturJualRepository.bayar(fakturJualOlehSales, new Pembayaran(LocalDate.now(), 10000), bg)
        assertEquals(0, fakturJualOlehSales.piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_GIRO_BELUM_CAIR))
        assertEquals(20000, fakturJualOlehSales.piutang.sisa(KRITERIA_PEMBAYARAN.TANPA_GIRO_BELUM_CAIR))
        assertEquals(bg, fakturJualRepository.findBilyetGiroByNomorSeri('NX-0001'))

        fakturJualOlehSales = fakturJualRepository.bayar(fakturJualOlehSales, new Pembayaran(LocalDate.now(), 10000))
        assertEquals(10000, fakturJualOlehSales.piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.TANPA_GIRO_BELUM_CAIR))
        assertEquals(10000, fakturJualOlehSales.piutang.sisa(KRITERIA_PEMBAYARAN.TANPA_GIRO_BELUM_CAIR))
        assertFalse(fakturJualOlehSales.piutang.lunas)

        shouldFail(IllegalArgumentException) {
            fakturJualRepository.bayar(fakturJualOlehSales, new Pembayaran(LocalDate.now(), 100000))
        }

        fakturJualRepository.withTransaction {

            bg = findBilyetGiroByNomorSeri('NX-0001')
            fakturJualOlehSales = findFakturJualOlehSalesById(-7l)

            bg.cairkan(LocalDate.now())
            assertTrue(fakturJualOlehSales.piutang.lunas)

            Konsumen konsumen = findKonsumenById(-1l)
            assertFalse(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))
        }
        assertEquals(StatusFakturJual.LUNAS, fakturJualOlehSales.status)
    }

    public void testBonus() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-1l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        // Tambah bonus
        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true, [new ItemBarang(produkA, 1), new ItemBarang(produkB, 3)])

        // Periksa bahwa total faktur tidak dipengaruhi oleh bonus
        assertEquals(69000, fakturJualOlehSales.total())

        // Periksa bahwa jumlah barang sudah berkurang
        fakturJualRepository.kirim(fakturJualOlehSales, 'Alamat', 'Supir')
        fakturJualRepository.withTransaction {
            produkA = fakturJualRepository.findProdukById(-1l)
            produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(1, produkA.stok(sales.gudang).jumlah)  // dari 10 berkurang sebanyak 8 item + 1 item bonus
            assertEquals(6, produkB.stok(sales.gudang).jumlah) // dari 14 berkurang sebanyak 5 item + 3 item bonus
        }
    }

    public void testBonusGagal() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-1l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        shouldFail(StokTidakCukup) {
            fakturJualRepository.buat(fakturJualOlehSales, true, [new ItemBarang(produkA, 5), new ItemBarang(produkB, 3)])
        }
    }

}

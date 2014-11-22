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
import domain.exception.HargaSelisih
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.BilyetGiro
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.faktur.Pembayaran
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.PeriodeItemStok
import domain.pembelian.PenerimaanBarang
import domain.pengaturan.Pengaturan
import domain.penjualan.FakturJualEceran
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import project.inventory.ProdukRepository
import project.pengaturan.PengaturanRepository
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
    ProdukRepository produkRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_penjualan.xlsx")
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')
        fakturJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('FakturJual')
        konsumenRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Konsumen')
        produkRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Produk')
        nomorService = app.serviceManager.findService('Nomor')
        (SimpleJpaUtil.instance.repositoryManager.findRepository('pengaturan') as PengaturanRepository).refreshAll()
    }

    public void testBuatPenjualanLuarKota() {
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-2l)
        Gudang gudang = fakturJualRepository.findGudangById(-2l)

        FakturJualOlehSales f = new FakturJualOlehSales(konsumen:  konsumen, tanggal: LocalDate.now())
        f.tambah(new ItemFaktur(produkA, 3, 10000))
        f.tambah(new ItemFaktur(produkB, 5, 12000))

        f = fakturJualRepository.buat(f, true)

        // Penjualan luar kota langsung terkirim
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertNotNull(f.pengeluaranBarang)
        assertEquals(2, f.pengeluaranBarang.items.size())
        assertEquals(produkA, f.pengeluaranBarang.items[0].produk)
        assertEquals(3, f.pengeluaranBarang.items[0].jumlah)
        assertEquals(produkB, f.pengeluaranBarang.items[1].produk)
        assertEquals(5, f.pengeluaranBarang.items[1].jumlah)
        assertNotNull(f.pengeluaranBarang.buktiTerima)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.buktiTerima.tanggalTerima)

        // Stok langsung berubah
        produkA = fakturJualRepository.findProdukByIdFetchStokProduk(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(1, produkA.stok(gudang).jumlah)
        assertEquals(1, produkB.stok(gudang).jumlah)
    }

    public void testBuatPenjualanLuarKotaKirimDariGudangUtama() {
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-2l)
        Gudang gudang = gudangRepository.cariGudangUtama()
        Gudang gudangLuarKota = gudangRepository.findGudangById(-2l)

        FakturJualOlehSales f = new FakturJualOlehSales(konsumen:  konsumen, tanggal: LocalDate.now(), kirimDariGudangUtama: true)
        f.tambah(new ItemFaktur(produkA, 3, 10000))
        f.tambah(new ItemFaktur(produkB, 5, 12000))

        f = fakturJualRepository.buat(f, true)

        // Penjualan luar kota yang dikirim dari gudang utama perlu dikirim secara manual
        assertEquals(StatusFakturJual.DIBUAT, f.status)
        assertNull(f.pengeluaranBarang)

        // Stok tidak langsung berubah
        produkA = fakturJualRepository.findProdukByIdFetchStokProduk(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(4, produkA.stok(gudangLuarKota).jumlah)
        assertEquals(10, produkA.stok(gudang).jumlah)
        assertEquals(3, produkA.jumlahAkanDikirim)
        assertEquals(6, produkB.stok(gudangLuarKota).jumlah)
        assertEquals(14, produkB.stok(gudang).jumlah)
        assertEquals(5, produkB.jumlahAkanDikirim)

        // Buat pengeluaran barang
        f = fakturJualRepository.kirim(f, 'Alamat 1')
        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertNotNull(f.pengeluaranBarang)
        produkA = fakturJualRepository.findProdukByIdFetchStokProduk(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(4, produkA.stok(gudangLuarKota).jumlah)
        assertEquals(7, produkA.stok(gudang).jumlah)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(6, produkB.stok(gudangLuarKota).jumlah)
        assertEquals(9, produkB.stok(gudang).jumlah)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testBuatFakturJualOlehSalesDalamKota() {
        nomorService.refreshAll()

        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-4l)
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
        konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(-4l)
        assertTrue(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))

        // Periksa apakah harga terakhir terubah
        assertEquals(8000, konsumenRepository.hargaTerakhir(konsumen, produkA))
        assertEquals(1000, konsumenRepository.hargaTerakhir(konsumen, produkB))

        // Periksa apakah jumlah pemesanan di produk bertambah
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        // Test menghapus faktur dan periksa efeknya pada jumlah pemesanan di produk
        fakturJualRepository.hapus(fakturJualOlehSales)
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
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
        FakturJualOlehSales fakturJualOlehSales

        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1l)
            Produk produkB = fakturJualRepository.findProdukById(-2l)
            produkA.jumlahAkanDikirim = 10
            produkB.jumlahAkanDikirim = 10
        }

        fakturJualRepository.withTransaction {
            fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-4l)
            fakturJualOlehSales.kirim('Final Destination')
            assertEquals(StatusFakturJual.DIANTAR, fakturJualOlehSales.status)
            assertNotNull(fakturJualOlehSales.pengeluaranBarang)
            assertTrue(fakturJualOlehSales.pengeluaranBarang.isiSamaDengan(fakturJualOlehSales))
            assertEquals(LocalDate.now(), fakturJualOlehSales.pengeluaranBarang.tanggal)
            assertEquals('Final Destination', fakturJualOlehSales.pengeluaranBarang.alamatTujuan)

            // Cek jumlah produk berkurang
            Produk produkA = fakturJualRepository.findProdukById(-1l)
            Produk produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(6, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(11, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)

            // Cek pemesanan produk berkurang
            assertEquals(6, produkA.jumlahAkanDikirim)
            assertEquals(7, produkB.jumlahAkanDikirim)
        }

        // Pembatalan
        fakturJualOlehSales = fakturJualRepository.batalKirim(fakturJualOlehSales)
        fakturJualRepository.withTransaction {
            assertEquals(StatusFakturJual.DIBUAT, fakturJualOlehSales.status)
            Produk produkA = fakturJualRepository.findProdukById(-1l)
            Produk produkB = fakturJualRepository.findProdukById(-2l)
            assertEquals(10, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(14, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
            assertEquals(10, produkA.jumlahAkanDikirim)
            assertEquals(10, produkB.jumlahAkanDikirim)
        }
    }

    public void testPengantaranDenganCetakSuratJalanDahulu() {
        fakturJualRepository.withTransaction {
            Produk produkA = fakturJualRepository.findProdukById(-1l)
            Produk produkB = fakturJualRepository.findProdukById(-2l)
            produkA.jumlahAkanDikirim = 10
            produkB.jumlahAkanDikirim = 10
        }
        FakturJualOlehSales f = fakturJualRepository.findFakturJualOlehSalesById(-4l)
        Gudang g = gudangRepository.cariGudangUtama()
        f = fakturJualRepository.buatSuratJalan(f, 'Final Destination')
        assertEquals(StatusFakturJual.DIBUAT, f.status)
        assertNotNull(f.pengeluaranBarang)
        assertTrue(f.pengeluaranBarang.isiSamaDengan(f))
        assertEquals(LocalDate.now(), f.pengeluaranBarang.tanggal)
        assertEquals('Final Destination', f.pengeluaranBarang.alamatTujuan)

        // Cek stok produk tidak berkurang
        Produk produkA = fakturJualRepository.findProdukByIdFetchComplete(-1l)
        Produk produkB = fakturJualRepository.findProdukByIdFetchComplete(-2l)
        assertEquals(10, produkA.stok(g).jumlah)
        assertEquals(14, produkB.stok(g).jumlah)
        assertEquals(10, produkA.jumlahAkanDikirim)
        assertEquals(10, produkB.jumlahAkanDikirim)

        // Kirim surat jalan
        f = fakturJualRepository.kirimSuratJalan(f)

        // Cek stok produk berkurang
        produkA = fakturJualRepository.findProdukByIdFetchComplete(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchComplete(-2l)
        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertEquals(6, produkA.stok(g).jumlah)
        assertEquals(11, produkB.stok(g).jumlah)
        assertEquals(6, produkA.jumlahAkanDikirim)
        assertEquals(7, produkB.jumlahAkanDikirim)

        // Pembatalan
        f = fakturJualRepository.batalKirim(f)
        assertEquals(StatusFakturJual.DIBUAT, f.status)
        produkA = fakturJualRepository.findProdukByIdFetchComplete(-1l)
        produkB = fakturJualRepository.findProdukByIdFetchComplete(-2l)
        assertEquals(10, produkA.stok(gudangRepository.cariGudangUtama()).jumlah)
        assertEquals(14, produkB.stok(gudangRepository.cariGudangUtama()).jumlah)
        assertEquals(10, produkA.jumlahAkanDikirim)
        assertEquals(10, produkB.jumlahAkanDikirim)
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
            assertEquals(1, mrNiceGuy.listRiwayatPoin.size())
            assertEquals(LocalDate.now(), mrNiceGuy.listRiwayatPoin[0].tanggal)
            assertEquals(2, mrNiceGuy.listRiwayatPoin[0].poin)
            assertEquals(fakturJualOlehSales.pengeluaranBarang.nomor, mrNiceGuy.listRiwayatPoin[0].referensi)

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

        shouldFail(HargaSelisih) {
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

    public void testHapusPembayaran() {
        FakturJualOlehSales fakturJualOlehSales = fakturJualRepository.findFakturJualOlehSalesById(-7l)
        Konsumen konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(-1l)
        assertTrue(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))
        assertEquals(90000, konsumen.creditTerpakai)
        Pembayaran pembayaran = new Pembayaran(LocalDate.now(), 20000)
        fakturJualOlehSales = fakturJualRepository.bayar(fakturJualOlehSales, pembayaran)
        assertTrue(fakturJualOlehSales.piutang.lunas)
        konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(-1l)
        assertFalse(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))
        fakturJualOlehSales = fakturJualRepository.hapusPembayaran(fakturJualOlehSales, pembayaran)
        assertFalse(fakturJualOlehSales.piutang.lunas)
        assertEquals(StatusFakturJual.DITERIMA, fakturJualOlehSales.status)
        assertEquals(0, fakturJualOlehSales.jumlahDibayar())
        konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(-1l)
        assertTrue(konsumen.listFakturBelumLunas.contains(fakturJualOlehSales))
        assertEquals(90000, konsumen.creditTerpakai)

        // Pembayaran dengan menggunakan giro harus menyebabkan giro tersebut dihapus
//        BilyetGiro bg = new BilyetGiro(nomorSeri: 'NX-0001', nominal: 20000, jatuhTempo: LocalDate.now().minusDays(1))
//        pembayaran = new Pembayaran(LocalDate.now(), 20000, null, bg)
//        fakturJualOlehSales = fakturJualRepository.bayar(fakturJualOlehSales, pembayaran)
//        fakturJualOlehSales = fakturJualRepository.hapusPembayaran(fakturJualOlehSales, pembayaran)
//        bg = fakturJualRepository.findBilyetGiroByNomorSeri('NX-0001', [excludeDeleted: false])
//        assertEquals('Y', bg.deleted)
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

        // Periksa bahwa pesanan produk sudah ada
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(9, produkA.jumlahAkanDikirim)
        assertEquals(8, produkB.jumlahAkanDikirim)

        // Periksa bahwa jumlah barang sudah berkurang
        fakturJualRepository.kirim(fakturJualOlehSales, 'Alamat')
        fakturJualRepository.withTransaction {
            produkA = findProdukById(-1l)
            produkB = findProdukById(-2l)
            assertEquals(1, produkA.stok(sales.gudang).jumlah)  // dari 10 berkurang sebanyak 8 item + 1 item bonus
            assertEquals(6, produkB.stok(sales.gudang).jumlah) // dari 14 berkurang sebanyak 5 item + 3 item bonus
            assertEquals(0, produkA.jumlahAkanDikirim)
            assertEquals(0, produkB.jumlahAkanDikirim)
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

    public void testPeriksaJumlahStok() {
        Konsumen k = produkRepository.findKonsumenById(-1)
        Gudang g = gudangRepository.cariGudangUtama()
        Produk p1 = produkRepository.findProdukById(-1l)
        Produk p2 = produkRepository.findProdukById(-2l)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: k, tanggal: LocalDate.now())
        f.tambah(new ItemFaktur(p1, 2, 10000))
        f.tambah(new ItemFaktur(p2, 5, 10000))
        f = fakturJualRepository.buat(f, true)

        // Pastikan bahwa stok masih belum berkurang dan jumlah akan dikirim bertambah
        produkRepository.withTransaction {
            p1 = findProdukById(-1l)
            assertEquals(37, p1.jumlah)
            assertEquals(10, p1.stok(g).jumlah)
            assertEquals(2, p1.jumlahAkanDikirim)
            p2 = findProdukById(-2l)
            assertEquals(27, p2.jumlah)
            assertEquals(14, p2.stok(g).jumlah)
            assertEquals(5, p2.jumlahAkanDikirim)
        }

        // Melakukan pengiriman barang dan memeriksa perubahan pada stok serta jumlah akan dikirim yang harus berkurang
        f = fakturJualRepository.kirim(f, 'test')
        produkRepository.withTransaction {
            p1 = findProdukById(-1l)
            assertEquals(35, p1.jumlah)
            assertEquals(8, p1.stok(g).jumlah)
            assertEquals(0, p1.jumlahAkanDikirim)
            PeriodeItemStok pis = p1.stok(g).periode(LocalDate.now())
            assertEquals(-2, pis.jumlah)
            assertEquals(-2, pis.listItem[0].jumlah)
            assertEquals(LocalDate.now(), pis.listItem[0].tanggal)
            assertNotNull(pis.listItem[0].referensiStok)
            assertEquals('FakturJualOlehSales', pis.listItem[0].referensiStok.classFinance)
            assertEquals(f.nomor, pis.listItem[0].referensiStok.nomorFinance)
            assertEquals(k.nama, pis.listItem[0].referensiStok.pihakTerkait)

            p2 = findProdukById(-2l)
            assertEquals(22, p2.jumlah)
            assertEquals(9, p2.stok(g).jumlah)
            assertEquals(0, p2.jumlahAkanDikirim)
            pis = p2.stok(g).periode(LocalDate.now())
            assertEquals(-5, pis.jumlah)
            assertEquals(-5, pis.listItem[0].jumlah)
            assertEquals(LocalDate.now(), pis.listItem[0].tanggal)
            assertNotNull(pis.listItem[0].referensiStok)
            assertEquals('FakturJualOlehSales', pis.listItem[0].referensiStok.classFinance)
            assertEquals(f.nomor, pis.listItem[0].referensiStok.nomorFinance)
            assertEquals(k.nama, pis.listItem[0].referensiStok.pihakTerkait)
        }
    }

    public void testReturSudahDiterima() {
        Konsumen k = produkRepository.findKonsumenById(-1)
        Gudang g = gudangRepository.cariGudangUtama()
        Produk p1 = produkRepository.findProdukById(-1l)
        Produk p2 = produkRepository.findProdukById(-2l)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: k, tanggal: LocalDate.now())
        f.tambah(new ItemFaktur(p1, 10, 10000))
        f.tambah(new ItemFaktur(p2, 10, 20000))
        f = fakturJualRepository.buat(f, true)
        f = fakturJualRepository.kirim(f, 'Destination')
        f = fakturJualRepository.terima(f, new BuktiTerima(LocalDate.now(), 'Receiver', 'Driver'))

        PenerimaanBarang retur = new PenerimaanBarang(nomor: 'NOMOR', tanggal: LocalDate.now())
        retur.tambah(new ItemBarang(p1, 10))
        retur.tambah(new ItemBarang(p2, 5))
        f = fakturJualRepository.retur(f, retur)

        fakturJualRepository.withTransaction {
            f = findFakturJualOlehSalesById(f.id)

            // Periksa retur
            assertEquals(1, f.retur.size())
            assertEquals(2, f.retur[0].items.size())
            assertEquals(p1, f.retur[0].items[0].produk)
            assertEquals(10, f.retur[0].items[0].jumlah)
            assertEquals(p2, f.retur[0].items[1].produk)
            assertEquals(5, f.retur[0].items[1].jumlah)

            // Periksa apakah piutang berkurang
            assertEquals(300000, f.piutang.jumlah)
            assertEquals(200000, f.piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN))

            // Periksa apakah bonus berkurang
            k = findKonsumenById(-1l)
            assertEquals(60, k.poinTerkumpul)
            assertEquals(30, k.listRiwayatPoin[0].poin)
            assertEquals(-20, k.listRiwayatPoin[1].poin)

            // Periksa apakah stok bertambah
            p1 = findProdukById(-1l)
            assertEquals(37, p1.jumlah)
            assertEquals(10, p1.stok(g).jumlah)
            assertNotNull(p1.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == -10})
            assertNotNull(p1.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == 10})
            p2 = findProdukById(-2l)
            assertEquals(22, p2.jumlah)
            assertEquals(9, p2.stok(g).jumlah)
            assertNotNull(p2.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == -10})
            assertNotNull(p2.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == 5})
        }

        f = fakturJualRepository.hapusRetur(f, 'NOMOR')
        fakturJualRepository.withTransaction {
            f = findFakturJualOlehSalesById(f.id)
            assertTrue(f.retur.empty)

            // Periksa apakah piutang bertambah
            assertEquals(300000, f.jumlahPiutang())
            assertEquals(0, f.potonganPiutang())

            // Periksa apakah bonus poin bertambah
            k = findKonsumenById(-1l)
            assertEquals(80, k.poinTerkumpul)
            assertEquals(30, k.listRiwayatPoin[0].poin)
            assertEquals(-20, k.listRiwayatPoin[1].poin)
            assertEquals(20, k.listRiwayatPoin[2].poin)

            // Periksa apakah stok berkurang
            p1 = findProdukById(-1l)
            assertEquals(27, p1.jumlah)
            assertEquals(0, p1.stok(g).jumlah)
            p2 = findProdukById(-2l)
            assertEquals(17, p2.jumlah)
            assertEquals(4, p2.stok(g).jumlah)
        }
    }

    public void testReturDiantar() {
        Konsumen k = produkRepository.findKonsumenById(-1)
        Gudang g = gudangRepository.cariGudangUtama()
        Produk p1 = produkRepository.findProdukById(-1l)
        Produk p2 = produkRepository.findProdukById(-2l)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: k, tanggal: LocalDate.now())
        f.tambah(new ItemFaktur(p1, 10, 10000))
        f.tambah(new ItemFaktur(p2, 10, 20000))
        f = fakturJualRepository.buat(f, true)
        f = fakturJualRepository.kirim(f, 'Destination')

        PenerimaanBarang retur = new PenerimaanBarang(nomor: 'NOMOR', tanggal: LocalDate.now())
        retur.tambah(new ItemBarang(p1, 10))
        retur.tambah(new ItemBarang(p2, 5))
        f = fakturJualRepository.retur(f, retur)

        fakturJualRepository.withTransaction {
            f = findFakturJualOlehSalesById(f.id)

            // Periksa retur
            assertEquals(1, f.retur.size())
            assertEquals(2, f.retur[0].items.size())
            assertEquals(p1, f.retur[0].items[0].produk)
            assertEquals(10, f.retur[0].items[0].jumlah)
            assertEquals(p2, f.retur[0].items[1].produk)
            assertEquals(5, f.retur[0].items[1].jumlah)

            // Periksa apakah piutang dan bonus masih kosong
            assertNull(f.piutang)
            k = findKonsumenById(-1l)
            assertEquals(50, k.poinTerkumpul)

            // Periksa apakah stok bertambah
            p1 = findProdukById(-1l)
            assertEquals(37, p1.jumlah)
            assertEquals(10, p1.stok(g).jumlah)
            assertNotNull(p1.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == -10})
            assertNotNull(p1.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == 10})
            p2 = findProdukById(-2l)
            assertEquals(22, p2.jumlah)
            assertEquals(9, p2.stok(g).jumlah)
            assertNotNull(p2.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == -10})
            assertNotNull(p2.stok(g).periode(LocalDate.now()).listItem.find { it.jumlah == 5})
        }

        f = fakturJualRepository.terima(f, new BuktiTerima(LocalDate.now(), 'Receiver', 'Driver'))

        fakturJualRepository.withTransaction {
            f = findFakturJualOlehSalesById(f.id)

            // Periksa piutang
            assertEquals(100000, f.piutang.jumlah)

            // Periksa apakah bonus berkurang
            k = findKonsumenById(-1l)
            assertEquals(60, k.poinTerkumpul)
        }

        // Hapus retur harus gagal karena menyebabkan piutang bertambah
        shouldFail(DataTidakBolehDiubah) {
            fakturJualRepository.hapusRetur(f, 'NOMOR')
        }
    }

    public void testHitungBarangYangHarusDikirim() {
        DaftarBarangSementara d = fakturJualRepository.hitungBarangYangHarusDikirim()
        Produk p1 = fakturJualRepository.findProdukById(-1l)
        Produk p2 = fakturJualRepository.findProdukById(-2l)
        def items = d.items
        assertEquals(p1, items[0].produk)
        assertEquals(4, items[0].jumlah)
        assertEquals(p2, items[1].produk)
        assertEquals(3, items[1].jumlah)
    }

    public void testPenjualanDalamKotaMelebihiQtyReady() {
        Produk produkA1 = fakturJualRepository.findProdukById(-12l)
        Konsumen konsumen1 = fakturJualRepository.findKonsumenById(-4l)
        Konsumen konsumen2 = fakturJualRepository.findKonsumenById(-2l)

        // Melebih qty ready
        shouldFail(StokTidakCukup) {
            FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen1)
            fakturJualOlehSales.tambah(new ItemFaktur(produkA1, 50, 8000))
            fakturJualRepository.buat(fakturJualOlehSales, true)
        }

        shouldFail(StokTidakCukup) {
            FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen2, kirimDariGudangUtama: true)
            fakturJualOlehSales.tambah(new ItemFaktur(produkA1, 50, 8000))
            fakturJualRepository.buat(fakturJualOlehSales, true)
        }

        // Penjualan luar kota tidak divalidasi berdasarkan qty ready
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen2, kirimDariGudangUtama: true)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA1, 10, 8000))
        fakturJualRepository.buat(fakturJualOlehSales, true)
    }

    public void testQtyReadyFakturJualOlehSales1() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-4l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)
        // Pastikan jumlah akan dikirim bertambah setelah faktur dibuat.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.buatSuratJalan(fakturJualOlehSales, 'Test')
        // Pastikan jumlah akan dikirim tidak berubah setelah surat jalan dibuat.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.kirimSuratJalan(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim berkurang setelah barang dikirim.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.batalKirim(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim bertambah setelah pengiriman dihapus.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.buatSuratJalan(fakturJualOlehSales, 'Test')
        fakturJualOlehSales = fakturJualRepository.kirimSuratJalan(fakturJualOlehSales)
        PenerimaanBarang p = new PenerimaanBarang(nomor: 'TEST', tanggal: LocalDate.now())
        p.tambah(new ItemBarang(produkA, 3))
        fakturJualOlehSales = fakturJualRepository.retur(fakturJualOlehSales, p)
        // Pastikan jumlah akan dikirim tidak berubah setelah retur dilakukan.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testQtyReadyFakturJualOlehSales2() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Sales sales = fakturJualRepository.findSalesById(-1l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-4l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)
        // Pastikan jumlah akan dikirim bertambah setelah faktur dibuat.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.hapus(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim berkurang setelah faktur dihapus.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testQtyReadyFakturLuarKota() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-2l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 3, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)
        // Pastikan jumlah akan dikirim tidak bertambah setelah faktur dibuat karena ini penjualan luar kota.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.hapusBuktiTerima(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim tidak berkurang setelah pengeluaran faktur ini dhapus karena ini penjualan luar kota.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.batalKirim(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim tidak berkurang setelah pengeluaran faktur ini dhapus karena ini penjualan luar kota.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.hapus(fakturJualOlehSales)
        // Pastikan jumlah akan dikirim tidak berkurang setelah faktur dihapus karena ini adalah penjualan luar kota.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testQtyReadyFakturLuarKotaKirimDariGudangUtama() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        Konsumen konsumen = fakturJualRepository.findKonsumenById(-2l)
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(tanggal: LocalDate.now(), konsumen: konsumen, kirimDariGudangUtama: true)
        fakturJualOlehSales.tambah(new ItemFaktur(produkA, 3, 8000))
        fakturJualOlehSales.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true)
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(3, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.kirim(fakturJualOlehSales, 'test')
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.batalKirim(fakturJualOlehSales)
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(3, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualOlehSales = fakturJualRepository.hapus(fakturJualOlehSales)
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

    public void testQtyReadyFakturJualEceran1() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        FakturJualEceran fakturJualEceran = new FakturJualEceran(tanggal: LocalDate.now(), namaPembeli: 'Test')
        fakturJualEceran.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualEceran.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualEceran = fakturJualRepository.buat(fakturJualEceran, true)
        // Pastikan jumlah akan dikirim bertambah setelah faktur dibuat.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualEceran = fakturJualRepository.antar(fakturJualEceran)
        // Pastikan jumlah akan dikirim berkurang setelah barang di atnar.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)

        fakturJualEceran = fakturJualRepository.batalAntar(fakturJualEceran)
        // Pastikan jumlah akan dikirim berkurang setelah barang dikirim.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)
    }

    public void testQtyReadyFakturJualEceran2() {
        nomorService.refreshAll()
        Produk produkA = fakturJualRepository.findProdukById(-1l)
        Produk produkB = fakturJualRepository.findProdukById(-2l)
        FakturJualEceran fakturJualEceran = new FakturJualEceran(tanggal: LocalDate.now(), namaPembeli: 'Test')
        fakturJualEceran.tambah(new ItemFaktur(produkA, 8, 8000))
        fakturJualEceran.tambah(new ItemFaktur(produkB, 5, 1000))

        fakturJualEceran = fakturJualRepository.buat(fakturJualEceran, true)
        // Pastikan jumlah akan dikirim bertambah setelah faktur dibuat.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(8, produkA.jumlahAkanDikirim)
        assertEquals(5, produkB.jumlahAkanDikirim)

        fakturJualEceran = fakturJualRepository.hapus(fakturJualEceran)
        // Pastikan jumlah akan dikirim berkurang setelah faktur dihapus.
        produkA = fakturJualRepository.findProdukById(-1l)
        produkB = fakturJualRepository.findProdukById(-2l)
        assertEquals(0, produkA.jumlahAkanDikirim)
        assertEquals(0, produkB.jumlahAkanDikirim)
    }

}

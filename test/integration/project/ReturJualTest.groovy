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

import domain.exception.StokTidakCukup
import domain.faktur.Pembayaran
import domain.faktur.Referensi
import domain.inventory.DaftarBarang
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.labarugi.JenisTransaksiKas
import domain.labarugi.Kas
import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.retur.ItemRetur
import domain.retur.Klaim
import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimServis
import domain.retur.KlaimTukar
import domain.retur.KlaimTambahBayaran
import domain.retur.KlaimTukarUang
import domain.retur.ReturJual
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.penjualan.FakturJualRepository
import project.retur.ReturJualRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class ReturJualTest extends DbUnitTestCase {

    ReturJualRepository returJualRepository
    FakturJualRepository fakturJualRepository
    GudangRepository gudangRepository

	protected void setUp() {
		super.setUp()
		setUpDatabase("/project/data_retur_jual.xlsx")
        returJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('ReturJual')
        fakturJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('FakturJual')
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')
	}

    public void testJumlahReturDiProduk() {
        Produk p1 = returJualRepository.findProdukById(-1l)
        Produk p2 = returJualRepository.findProdukById(-2l)
        Produk p3 = returJualRepository.findProdukById(-3l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        ReturJual returJual = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: gudangRepository.cariGudangUtama())
        returJual.tambah(new ItemRetur(p1, 10, [new KlaimTukar(p1, 1)] as Set))
        returJual.tambah(new ItemRetur(p2, 20, [new KlaimPotongPiutang(1)] as Set))
        returJual.tambah(new ItemRetur(p3, 30, [new KlaimPotongPiutang(1)] as Set))
        returJualRepository.buat(returJual)

        // Periksa nilai jumlah retur di produk
        p1 = returJualRepository.findProdukById(-1l)
        assertEquals(20, p1.jumlahRetur)
        p2 = returJualRepository.findProdukById(-2l)
        assertEquals(23, p2.jumlahRetur)
        p3 = returJualRepository.findProdukById(-3l)
        assertEquals(35, p3.jumlahRetur)
    }

    public void testJumlahTukarDiProduk() {
        returJualRepository.withTransaction {
            returJualRepository.findProdukById(-1l).jumlahTukar = 10
            returJualRepository.findProdukById(-2l).jumlahTukar = 20
            returJualRepository.findProdukById(-3l).jumlahTukar = 30
        }
        Produk p1 = returJualRepository.findProdukById(-1l)
        Produk p2 = returJualRepository.findProdukById(-2l)
        Produk p3 = returJualRepository.findProdukById(-3l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        ReturJual returJual = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: gudangRepository.cariGudangUtama())
        returJual.tambah(new ItemRetur(p1, 10, [new KlaimTukar(p1, 1), new KlaimServis(p1, 3)] as Set))
        returJual.tambah(new ItemRetur(p2, 20, [new KlaimPotongPiutang(1), new KlaimServis(p2, 5)] as Set))
        returJual.tambah(new ItemRetur(p3, 30, [new KlaimPotongPiutang(1)] as Set))
        returJualRepository.buat(returJual)

        // Pastikan klaim servis sudah diproses seusai dibuat
        assertTrue(returJual.getDaftarBarangServis(true).items.empty)

        // Periksa nilai jumlah retur di produk
        p1 = returJualRepository.findProdukById(-1l)
        assertEquals(7, p1.jumlahTukar)
        assertEquals(20, p1.jumlahRetur)
        p2 = returJualRepository.findProdukById(-2l)
        assertEquals(15, p2.jumlahTukar)
        assertEquals(23, p2.jumlahRetur)
        p3 = returJualRepository.findProdukById(-3l)
        assertEquals(30, p3.jumlahTukar)
        assertEquals(35, p3.jumlahRetur)

        // Retur dengan jumlah servis yang tidak cukup
        shouldFail(StokTidakCukup) {
            returJual = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-2', konsumen: k, gudang: gudangRepository.cariGudangUtama())
            returJual.tambah(new ItemRetur(p1, 10, [new KlaimTukar(p1, 1), new KlaimServis(p1, 200)] as Set))
            returJual.tambah(new ItemRetur(p2, 20, [new KlaimPotongPiutang(1), new KlaimServis(p2, 300)] as Set))
            returJual.tambah(new ItemRetur(p3, 30, [new KlaimPotongPiutang(1)] as Set))
            returJualRepository.buat(returJual)
        }
    }

    public void testJumlahReturDiProdukSetelahHapus() {
        returJualRepository.withTransaction {
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)
            p1.jumlahAkanDikirim = 100
            p2.jumlahAkanDikirim = 100
            p3.jumlahAkanDikirim = 100
        }
        ReturJual returJual = returJualRepository.findReturJualOlehSalesById(-1l)
        returJualRepository.hapus(returJual)

        // Periksa nilai jumlah retur di produk
        Produk p1 = returJualRepository.findProdukById(-1l)
        assertEquals(5, p1.jumlahRetur)
        Produk p2 = returJualRepository.findProdukById(-2l)
        assertEquals(0, p2.jumlahRetur)
        Produk p3 = returJualRepository.findProdukById(-3l)
        assertEquals(3, p3.jumlahRetur)
    }

    public void testJumlahTukarDiProdukSetelahHapus() {
        returJualRepository.withTransaction {
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)
            p1.jumlahAkanDikirim = 100
            p2.jumlahAkanDikirim = 100
            p3.jumlahAkanDikirim = 100
            p1.jumlahTukar = 100
            p2.jumlahTukar = 100
            p3.jumlahTukar = 100
        }
        ReturJual returJual = returJualRepository.findReturJualOlehSalesById(-2l)
        returJualRepository.hapus(returJual)

        // Periksa nilai jumlah retur di produk
        Produk p1 = returJualRepository.findProdukById(-1l)
        assertEquals(105, p1.jumlahTukar)
        Produk p2 = returJualRepository.findProdukById(-2l)
        assertEquals(103, p2.jumlahTukar)
        Produk p3 = returJualRepository.findProdukById(-3l)
        assertEquals(102, p3.jumlahTukar)
    }

    public void testTukarBaru() {
        returJualRepository.withTransaction {
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)
            p1.jumlahAkanDikirim = 100
            p2.jumlahAkanDikirim = 100
            p3.jumlahAkanDikirim = 100
            ReturJual returJual = returJualRepository.findReturJualOlehSalesById(-1l)
            returJual = returJualRepository.tukar(returJual)

            assertTrue(returJual.sudahDiproses)
            assertTrue(returJual.getKlaimsTukar(true).empty)
            assertNotNull(returJual.pengeluaranBarang)
            assertTrue(returJual.pengeluaranBarang.sudahDiterima())

            Gudang g = findGudangById(-1l)
            p1 = findProdukById(-1l)
            p2 = findProdukById(-2l)
            p3 = findProdukById(-3l)

            assertEquals('Mr. Nice Guy', returJual.pengeluaranBarang.buktiTerima.namaPenerima)
            assertEquals(LocalDate.now(), returJual.pengeluaranBarang.buktiTerima.tanggalTerima)
            assertEquals(2, returJual.pengeluaranBarang.items.size())
            assertEquals(p1, returJual.pengeluaranBarang.items[0].produk)
            assertEquals(5, returJual.pengeluaranBarang.items[0].jumlah)
            assertEquals(p2, returJual.pengeluaranBarang.items[1].produk)
            assertEquals(3, returJual.pengeluaranBarang.items[1].jumlah)

            assertEquals(5, p1.stok(g).jumlah)
            assertEquals(11, p2.stok(g).jumlah)
            assertEquals(15, p3.stok(g).jumlah)
        }
    }

    public void testHapus() {
        Produk p1 = returJualRepository.findProdukById(-1l)
        Produk p2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenByIdFetchComplete(-1l)
        BigDecimal sisaPiutangAwal = k.jumlahPiutang()
        ReturJualOlehSales r = new ReturJualOlehSales(nomor: 'TEST-1', tanggal: LocalDate.now(), konsumen: k, gudang: gudangRepository.cariGudangUtama())
        r.tambah(new ItemRetur(p1, 10, [new KlaimPotongPiutang(10000)] as Set))
        r.tambah(new ItemRetur(p2, 20, [new KlaimTukar(p2, 1)] as Set))
        r = returJualRepository.buat(r)
        Gudang g = gudangRepository.cariGudangUtama()
        k = returJualRepository.findKonsumenByIdFetchComplete(-1l)
        assertEquals(sisaPiutangAwal - 10000, k.jumlahPiutang())

        // Hapus
        returJualRepository.hapus(r)

        // Periksa apakah qty retur berkurang di produk
        p1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        p2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(10, p1.jumlahRetur)
        assertEquals(3, p2.jumlahRetur)

        // Periksa apakah qty stok untuk barang yang ditukar bertambah kembali
        assertEquals(14, p2.stok(g).jumlah)

        // Periksa apakah jumlah piutang berkurang
        k = returJualRepository.findKonsumenByIdFetchComplete(-1l)
        assertEquals(sisaPiutangAwal, k.jumlahPiutang())
    }

    public void testTukarPengeluaranBarang() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Produk produk3 = returJualRepository.findProdukById(-3l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))
        retur.tambah(new ItemRetur(produk3, 12, [new KlaimTukar(produk3, 12)] as Set))
        retur = returJualRepository.buat(retur)
        returJualRepository.tukar(retur)

        retur = returJualRepository.findReturJualOlehSalesByNomor(retur.nomor)
        assertNotNull(retur.pengeluaranBarang)
        assertTrue(retur.items[0].getKlaims(KlaimTukar).find { it.produk == produk1 }.sudahDiproses)
        assertTrue(retur.items[1].getKlaims(KlaimTukar).find { it.produk == produk2 }.sudahDiproses)
        assertTrue(retur.items[2].getKlaims(KlaimTukar).find { it.produk == produk3 }.sudahDiproses)
        DaftarBarang d = retur.yangHarusDitukar()
        assertEquals(0, d.items.size())
        assertTrue(retur.sudahDiproses)

        // Periksa stok tersedia
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        produk3 = returJualRepository.findProdukByIdFetchStokProduk(-3l)
        assertEquals(2, produk1.stok(g).jumlah)
        assertEquals(4, produk2.stok(g).jumlah)
        assertEquals(3, produk3.stok(g).jumlah)
    }

    public void testHapusPenukaran() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Produk produk3 = returJualRepository.findProdukById(-3l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))
        retur.tambah(new ItemRetur(produk3, 12, [new KlaimTukar(produk3, 12)] as Set))
        retur = returJualRepository.buat(retur)
        retur = returJualRepository.tukar(retur)
        assertTrue(retur.sudahDiproses)

        // Hapus
        returJualRepository.hapusPengeluaranBarang(retur)
        retur = returJualRepository.findReturJualOlehSalesByNomor(retur.nomor)
        assertFalse(retur.sudahDiproses)
        assertNull(retur.pengeluaranBarang)
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        produk3 = returJualRepository.findProdukByIdFetchStokProduk(-3l)
        assertEquals(10, produk1.stok(g).jumlah)
        assertEquals(14, produk2.stok(g).jumlah)
        assertEquals(15, produk3.stok(g).jumlah)
    }

    public void testQtyReadyReturJualOlehSales1() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))

        retur = returJualRepository.buat(retur)
        // Pastikan jumlah akan dikirim bertambah setelah faktur retur dibuat.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(8, produk1.jumlahAkanDikirim)
        assertEquals(10, produk2.jumlahAkanDikirim)

        retur = returJualRepository.tukar(retur)
        // Pastikan jumlah akan dikirim berkurang setelah penukaran dilakukan.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(0, produk1.jumlahAkanDikirim)
        assertEquals(0, produk2.jumlahAkanDikirim)

        returJualRepository.hapusPengeluaranBarang(retur)
        // Pastikan jumlah akan dikirim bertambah kembali setelah penukaran dihapus.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(8, produk1.jumlahAkanDikirim)
        assertEquals(10, produk2.jumlahAkanDikirim)
    }

    public void testQtyReadyReturJualOlehSales2() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))

        retur = returJualRepository.buat(retur)
        // Pastikan jumlah akan dikirim bertambah setelah faktur retur dibuat.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(8, produk1.jumlahAkanDikirim)
        assertEquals(10, produk2.jumlahAkanDikirim)

        returJualRepository.hapus(retur)
        // Pastikan jumlah akan dikirim berkurang setelah retur dihapus.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(0, produk1.jumlahAkanDikirim)
        assertEquals(0, produk2.jumlahAkanDikirim)
    }

    public void testQtyReadyReturJualEceran1() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        ReturJualEceran retur = new ReturJualEceran(nomor: 'R-01', tanggal: LocalDate.now(), namaKonsumen: 'Anonym')
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))

        retur = returJualRepository.buat(retur)
        // Pastikan jumlah akan dikirim bertambah setelah faktur retur dibuat.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(8, produk1.jumlahAkanDikirim)
        assertEquals(10, produk2.jumlahAkanDikirim)

        returJualRepository.tukar(retur)
        // Pastikan jumlah akan dikirim berkurang setelah penukaran dilakukan.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(0, produk1.jumlahAkanDikirim)
        assertEquals(0, produk2.jumlahAkanDikirim)
    }

    public void testQtyReadyReturJualEceran2() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        ReturJualEceran retur = new ReturJualEceran(tanggal: LocalDate.now(), nomor: 'TEST-1', namaKonsumen: 'Anonym')
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimTukar(produk1, 8)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))

        retur = returJualRepository.buat(retur)
        // Pastikan jumlah akan dikirim bertambah setelah faktur retur dibuat.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(8, produk1.jumlahAkanDikirim)
        assertEquals(10, produk2.jumlahAkanDikirim)

        returJualRepository.hapus(retur)
        // Pastikan jumlah akan dikirim berkurang setelah retur dihapus.
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(0, produk1.jumlahAkanDikirim)
        assertEquals(0, produk2.jumlahAkanDikirim)
    }

    void testBuatReturLuarKota() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-2l)
        Gudang g = returJualRepository.findGudangById(-2l)
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-1', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 4, [new KlaimTukar(produk1, 4)] as Set))
        retur.tambah(new ItemRetur(produk2, 6, [new KlaimTukar(produk2, 6)] as Set))
        retur = returJualRepository.buat(retur)

        // Pastikan bahwa penukaran telah dilakukan
        assertNotNull(retur.pengeluaranBarang)
        assertTrue(retur.yangHarusDitukar().items.empty)
        assertTrue(retur.items.every { ItemRetur i -> i.klaims.every { Klaim klaim -> klaim.sudahDiproses }})

        // Pastikan stok barang sudah berkurang
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        assertEquals(0, produk1.stok(g).jumlah)
        assertEquals(0, produk2.stok(g).jumlah)
        assertEquals(0, produk1.jumlahAkanDikirim)
        assertEquals(0, produk2.jumlahAkanDikirim)
    }

    void testPerubahanPembayaranPiutang() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: 'TEST-X', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 8, [new KlaimPotongPiutang(500)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimPotongPiutang(1000)] as Set))
        retur = returJualRepository.buat(retur)
        assertEquals(1, retur.fakturPotongPiutang.size())
        assertTrue(retur.fakturPotongPiutang.contains(new Referensi(FakturJualOlehSales, '000004/042014/SA')))

        // Hapus potongan piutang
        FakturJualOlehSales f = returJualRepository.findFakturJualOlehSalesByIdFetchComplete(-7l)
        f = fakturJualRepository.hapusPembayaran(f, f.piutang.listPembayaran[0])
        retur = returJualRepository.findReturJualOlehSalesById(retur.id)
        assertTrue(retur.fakturPotongPiutang.empty)

        // Tambah pembayaran piutang secara manual
        fakturJualRepository.bayar(f, new Pembayaran(LocalDate.now(), 1200, true, null, new Referensi(ReturJual, retur.nomor)))
        retur = returJualRepository.findReturJualOlehSalesById(retur.id)
        assertEquals(1, retur.fakturPotongPiutang.size())
        assertTrue(retur.fakturPotongPiutang.contains(new Referensi(FakturJualOlehSales, '000004/042014/SA')))
    }

    void testReturTukarTambah() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: '000001-RS-KB-112014', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk2, 6), new KlaimTambahBayaran(60000)] as Set))
        retur = returJualRepository.buat(retur)
        retur = returJualRepository.tukar(retur)
        assertTrue(retur.sudahDiproses)

        // Pastikan jumlah stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        assertEquals(20, produk1.jumlahRetur)
        assertEquals(37, produk1.jumlah)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(21, produk2.jumlah)

        // Pastikan pendapatan tukar dibuat
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            JenisTransaksiKas j = findJenisTransaksiKasById(-1l)
            KategoriKas kk = findKategoriKasById(1l)
            TransaksiKas t = kas.listNilaiPeriodik[0].listItemPeriodik[0]
            assertEquals(LocalDate.now(), t.tanggal)
            assertEquals(kk, t.kategoriKas)
            assertEquals(j, t.jenis)
            assertEquals(60000, t.jumlah)
            assertEquals(60000, kas.jumlah)
        }

        // Hapus retur
        retur = returJualRepository.hapusPengeluaranBarang(retur)
        returJualRepository.hapus(retur)

        // Pastikan jumlah stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        assertEquals(10, produk1.jumlahRetur)
        assertEquals(37, produk1.jumlah)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(27, produk2.jumlah)

        // Pastikan pedapatan tukar berkurang
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            assertEquals(0, kas.jumlah)
        }
    }

    void testReturTukarUang() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJualOlehSales retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: '000001-RS-KB-112014', konsumen: k, gudang: g)
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk2, 6), new KlaimTukarUang(60000)] as Set))
        retur = returJualRepository.buat(retur)
        retur = returJualRepository.tukar(retur)
        assertTrue(retur.sudahDiproses)

        // Pastikan jumlah stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        assertEquals(20, produk1.jumlahRetur)
        assertEquals(37, produk1.jumlah)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(21, produk2.jumlah)

        // Pastikan pendapatan tukar dibuat
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            JenisTransaksiKas j = findJenisTransaksiKasById(-1l)
            KategoriKas kk = findKategoriKasById(2l)
            TransaksiKas t = kas.listNilaiPeriodik[0].listItemPeriodik[0]
            assertEquals(LocalDate.now(), t.tanggal)
            assertEquals(j, t.jenis)
            assertEquals(kk, t.kategoriKas)
            assertEquals(60000, t.jumlah)
            assertEquals(-60000, kas.jumlah)
        }

        // Hapus retur
        retur = returJualRepository.hapusPengeluaranBarang(retur)
        returJualRepository.hapus(retur)

        // Pastikan jumlah stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        assertEquals(10, produk1.jumlahRetur)
        assertEquals(37, produk1.jumlah)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(27, produk2.jumlah)

        // Pastikan pedapatan tukar berkurang
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            assertEquals(0, kas.jumlah)
        }
    }

    void testReturTukarBisaDijualKembali() {
        Produk produk1 = returJualRepository.findProdukById(-1l)
        Produk produk2 = returJualRepository.findProdukById(-2l)
        Konsumen k = returJualRepository.findKonsumenById(-1l)
        Gudang g = gudangRepository.cariGudangUtama()
        ReturJual retur = new ReturJualOlehSales(tanggal: LocalDate.now(), nomor: '000001-RS-KB-112014', konsumen: k, gudang: g, bisaDijualKembali: true)
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk2, 6), new KlaimTukarUang(60000)] as Set))
        retur = returJualRepository.buat(retur)
        retur = returJualRepository.tukar(retur)
        assertTrue(retur.sudahDiproses)

        // Pastikan stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(10, produk1.jumlahRetur)
        assertEquals(47, produk1.jumlah)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(21, produk2.jumlah)

        // Pastikan pedapatan tukar bertambah
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            JenisTransaksiKas j = findJenisTransaksiKasById(-1l)
            KategoriKas kk = findKategoriKasById(2l)
            TransaksiKas t = kas.listNilaiPeriodik[0].listItemPeriodik[0]
            assertEquals(LocalDate.now(), t.tanggal)
            assertEquals(j, t.jenis)
            assertEquals(kk, t.kategoriKas)
            assertEquals(60000, t.jumlah)
            assertEquals(-60000, kas.jumlah)
        }

        // Lakukan percobaan pada retur jual eceran
        retur = new ReturJualEceran(tanggal: LocalDate.now(), namaKonsumen: 'Test', bisaDijualKembali: true)
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk2, 5), new KlaimTukarUang(60000)] as Set))
        retur = returJualRepository.buat(retur)
        retur = returJualRepository.tukar(retur)
        assertTrue(retur.sudahDiproses)

        // Pastikan stok benar
        produk1 = returJualRepository.findProdukById(-1l)
        produk2 = returJualRepository.findProdukById(-2l)
        assertEquals(10, produk1.jumlahRetur)
        assertEquals(57, produk1.jumlah)
        assertEquals(3, produk2.jumlahRetur)
        assertEquals(16, produk2.jumlah)

        // Pastikan pedapatan tukar berkurang
        returJualRepository.withTransaction {
            Kas kas = findKasBySistem(true)
            assertEquals(-120000, kas.jumlah)
        }
    }

    void testProsesSemuaReturJualSales() {
        returJualRepository.prosesSemuaReturJualSales()
        returJualRepository.withTransaction {
            ReturJualOlehSales r1 = findReturJualOlehSalesById(-1l)
            assertNotNull(r1.pengeluaranBarang)
            ReturJualOlehSales r2 = findReturJualOlehSalesById(-2l)
            assertNull(r2.pengeluaranBarang)
        }
    }

}
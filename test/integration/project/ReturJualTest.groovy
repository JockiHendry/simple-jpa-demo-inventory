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

import domain.exception.DataTidakKonsisten
import domain.inventory.DaftarBarang
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.Supplier
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import domain.retur.ItemRetur
import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimTukar
import domain.retur.ReturJual
import domain.retur.ReturJualOlehSales
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.inventory.GudangRepository
import project.retur.ReturJualRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class ReturJualTest extends DbUnitTestCase {

	private static final Logger log = LoggerFactory.getLogger(ReturJualTest)

    ReturJualRepository returJualRepository
    GudangRepository gudangRepository

	protected void setUp() {
		super.setUp()
		setUpDatabase("returJual", "/project/data_penjualan.xls")
        returJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('ReturJual')
        gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')
	}

	protected void tearDown() {
		super.tearDown()
		super.deleteAll()
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

    public void testJumlahReturDiProdukSetelahHapus() {
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

    public void testTukarBaru() {
        returJualRepository.withTransaction {
            ReturJual returJual = returJualRepository.findReturJualOlehSalesById(-1l)
            returJual = returJualRepository.tukar(returJual)

            assertTrue(returJual.sudahDiproses)
            assertTrue(returJual.getKlaimsTukar(true).empty)
            assertEquals(1, returJual.pengeluaranBarang.size())
            assertTrue(returJual.pengeluaranBarang[0].sudahDiterima())

            Gudang g = findGudangById(-1l)
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)

            assertEquals('Mr. Nice Guy', returJual.pengeluaranBarang[0].buktiTerima.namaPenerima)
            assertEquals(LocalDate.now(), returJual.pengeluaranBarang[0].buktiTerima.tanggalTerima)
            assertEquals(2, returJual.pengeluaranBarang[0].items.size())
            assertEquals(p1, returJual.pengeluaranBarang[0].items[0].produk)
            assertEquals(5, returJual.pengeluaranBarang[0].items[0].jumlah)
            assertEquals(p2, returJual.pengeluaranBarang[0].items[1].produk)
            assertEquals(3, returJual.pengeluaranBarang[0].items[1].jumlah)

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
        ReturJual r = new ReturJualOlehSales(nomor: 'TEST-1', tanggal: LocalDate.now(), konsumen: k, gudang: gudangRepository.cariGudangUtama())
        r.tambah(new ItemRetur(p1, 10, [new KlaimPotongPiutang(10000)] as Set))
        r.tambah(new ItemRetur(p2, 20, [new KlaimTukar(p2, 1)] as Set))
        r = returJualRepository.buat(r)
        k = returJualRepository.findKonsumenByIdFetchComplete(-1l)
        assertEquals(sisaPiutangAwal - 10000, k.jumlahPiutang())

        // Hapus
        returJualRepository.hapus(r)

        // Periksa apakah qty retur berkurang di produk
        p1 = returJualRepository.findProdukById(-1l)
        p2 = returJualRepository.findProdukById(-2l)
        assertEquals(10, p1.jumlahRetur)
        assertEquals(3, p2.jumlahRetur)

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

        PengeluaranBarang p1 = new PengeluaranBarang(tanggal: LocalDate.now(), alamatTujuan: 'Test')
        p1.tambah(new ItemBarang(produk1, 8))
        returJualRepository.tukar(retur, p1)

        retur = returJualRepository.findReturJualOlehSalesByNomorFetchPengeluaranBarang(retur.nomor)
        assertEquals(1, retur.pengeluaranBarang.size())
        assertTrue(retur.items[0].getKlaims(KlaimTukar).find { it.produk == produk1 }.sudahDiproses)
        assertFalse(retur.items[1].getKlaims(KlaimTukar).find { it.produk == produk2 }.sudahDiproses)
        assertFalse(retur.items[2].getKlaims(KlaimTukar).find { it.produk == produk3 }.sudahDiproses)
        DaftarBarang d = retur.yangHarusDitukar()
        assertEquals(2, d.items.size())
        assertTrue(d.items.containsAll([new ItemBarang(produk2, 10), new ItemBarang(produk3, 12)]))
        assertFalse(retur.sudahDiproses)

        // Periksa stok tersedia
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        produk3 = returJualRepository.findProdukByIdFetchStokProduk(-3l)
        assertEquals(2, produk1.stok(g).jumlah)
        assertEquals(14, produk2.stok(g).jumlah)
        assertEquals(15, produk3.stok(g).jumlah)

        PengeluaranBarang p2 = new PengeluaranBarang(tanggal: LocalDate.now(), alamatTujuan: 'Test')
        p2.tambah(new ItemBarang(produk2, 10))
        p2.tambah(new ItemBarang(produk3, 12))
        returJualRepository.tukar(retur, p2)

        retur = returJualRepository.findReturJualOlehSalesByNomorFetchPengeluaranBarang(retur.nomor)
        assertEquals(2, retur.pengeluaranBarang.size())
        assertTrue(retur.items[0].getKlaims(KlaimTukar).find { it.produk == produk1 }.sudahDiproses)
        assertTrue(retur.items[1].getKlaims(KlaimTukar).find { it.produk == produk2 }.sudahDiproses)
        assertTrue(retur.items[2].getKlaims(KlaimTukar).find { it.produk == produk3 }.sudahDiproses)
        d = retur.yangHarusDitukar()
        assertEquals(0, d.items.size())
        assertTrue(retur.pengeluaranBarang.containsAll(p1, p2))
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

        PengeluaranBarang p1 = new PengeluaranBarang(tanggal: LocalDate.now(), alamatTujuan: 'Test')
        p1.tambah(new ItemBarang(produk1, 8))
        retur = returJualRepository.tukar(retur, p1)

        PengeluaranBarang p2 = new PengeluaranBarang(tanggal: LocalDate.now(), alamatTujuan: 'Test')
        p2.tambah(new ItemBarang(produk2, 10))
        p2.tambah(new ItemBarang(produk3, 12))
        retur = returJualRepository.tukar(retur, p2)

        assertTrue(retur.sudahDiproses)

        // Hapus p2
        returJualRepository.hapusPengeluaranBarang(retur, p2)
        retur = returJualRepository.findReturJualOlehSalesByNomorFetchPengeluaranBarang(retur.nomor)
        assertFalse(retur.sudahDiproses)
        assertEquals(1, retur.pengeluaranBarang.size())
        assertFalse(retur.pengeluaranBarang.contains(p2))
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        produk3 = returJualRepository.findProdukByIdFetchStokProduk(-3l)
        assertEquals(2, produk1.stok(g).jumlah)
        assertEquals(14, produk2.stok(g).jumlah)
        assertEquals(15, produk3.stok(g).jumlah)

        // Hapus p1
        returJualRepository.hapusPengeluaranBarang(retur, p1)
        retur = returJualRepository.findReturJualOlehSalesByNomorFetchPengeluaranBarang(retur.nomor)
        assertFalse(retur.sudahDiproses)
        assertEquals(0, retur.pengeluaranBarang.size())
        produk1 = returJualRepository.findProdukByIdFetchStokProduk(-1l)
        produk2 = returJualRepository.findProdukByIdFetchStokProduk(-2l)
        produk3 = returJualRepository.findProdukByIdFetchStokProduk(-3l)
        assertEquals(10, produk1.stok(g).jumlah)
        assertEquals(14, produk2.stok(g).jumlah)
        assertEquals(15, produk3.stok(g).jumlah)
    }

}
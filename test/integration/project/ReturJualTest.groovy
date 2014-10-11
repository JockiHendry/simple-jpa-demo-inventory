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
import domain.penjualan.Konsumen
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
            assertNotNull(returJual.pengeluaranBarang)
            assertTrue(returJual.pengeluaranBarang.sudahDiterima())

            Gudang g = findGudangById(-1l)
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            Produk p3 = findProdukById(-3l)

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

}
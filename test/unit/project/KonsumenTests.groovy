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

import domain.faktur.Diskon
import domain.faktur.ItemFaktur
import domain.faktur.KewajibanPembayaran
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import domain.penjualan.Sales
import domain.penjualan.StatusFakturJual
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class KonsumenTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testSisaPiutang() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(sales: sales)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 20000, 20100, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(nomor: 'F1', konsumen: mrNiceGuy)
        f1.tambah(new ItemFaktur(produkA, 10, 10000))
        f1.tambah(new ItemFaktur(produkB, 5, 20000))
        FakturJualOlehSales f2 = new FakturJualOlehSales(nomor: 'F2', konsumen: mrNiceGuy)
        f2.tambah(new ItemFaktur(produkA, 3, 10000))
        f2.tambah(new ItemFaktur(produkB, 2, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        mrNiceGuy.tambahFakturBelumLunas(f2)
        assertEquals(270000, mrNiceGuy.jumlahPiutang())
        assertEquals(270000, mrNiceGuy.creditTerpakai)

        // Hapus
        mrNiceGuy.hapusFakturBelumLunas(f2)
        assertEquals(200000, mrNiceGuy.jumlahPiutang())
        assertEquals(200000, mrNiceGuy.creditTerpakai)
    }

    public void testAdaTagihanJatuhTempo() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(sales: sales)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 20000, 20100, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(nomor: 'FA1', konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().plusDays(5))
        f1.tambah(new ItemFaktur(produkA, 10, 10000))
        f1.tambah(new ItemFaktur(produkB, 5, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        assertFalse(mrNiceGuy.adaTagihanJatuhTempo())

        FakturJualOlehSales f2 = new FakturJualOlehSales(nomor: 'FA2', konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().minusDays(1))
        f2.tambah(new ItemFaktur(produkA, 3, 10000))
        f2.tambah(new ItemFaktur(produkB, 2, 20000))
        mrNiceGuy.tambahFakturBelumLunas(f2)
        assertTrue(mrNiceGuy.adaTagihanJatuhTempo())
    }

    public void testBolehKredit() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(creditLimit: 50000, sales: sales)
        Produk produkA = new Produk('Produk A', 1000, 1010, 50)
        Produk produkB = new Produk('Produk B', 2000, 2010, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(konsumen: mrNiceGuy, jatuhTempo: LocalDate.now().plusDays(5))
        f1.tambah(new ItemFaktur(produkA, 3, 1000))
        f1.tambah(new ItemFaktur(produkB, 5, 2000))
        mrNiceGuy.tambahFakturBelumLunas(f1)
        assertFalse(mrNiceGuy.adaTagihanJatuhTempo())
        assertTrue(mrNiceGuy.bolehKredit(20000))
        assertTrue(mrNiceGuy.bolehKredit(37000))
        assertFalse(mrNiceGuy.bolehKredit(40000))
    }

    public void testTambahPoin() {
        Konsumen mrNiceGuy = new Konsumen()

        mrNiceGuy.tambahPoin(50)
        assertEquals(50, mrNiceGuy.poinTerkumpul)

        mrNiceGuy.tambahPoin(20)
        assertEquals(70, mrNiceGuy.poinTerkumpul)

        Produk produkA = new Produk(nama: 'Produk A', poin: 10)
        Produk produkB = new Produk(nama: 'Produk B', poin: 0)
        Produk produkC = new Produk(nama: 'Produk C', poin: 5)
        PengeluaranBarang daftarBarang = new PengeluaranBarang(nomor: 'PB-123')
        daftarBarang.tambah(new ItemBarang(produkA, 10))
        daftarBarang.tambah(new ItemBarang(produkB, 20))
        daftarBarang.tambah(new ItemBarang(produkC, 30))

        mrNiceGuy.tambahPoin(daftarBarang)
        assertEquals(320, mrNiceGuy.poinTerkumpul)
        assertEquals(3, mrNiceGuy.listRiwayatPoin.size())
        assertEquals(LocalDate.now(), mrNiceGuy.listRiwayatPoin[2].tanggal)
        assertEquals(250, mrNiceGuy.listRiwayatPoin[2].poin)
        assertEquals('PB-123', mrNiceGuy.listRiwayatPoin[2].referensi)
    }

    public void testHapusPoin() {
        Konsumen mrNiceGuy = new Konsumen(poinTerkumpul: 100)

        mrNiceGuy.hapusPoin(20)
        assertEquals(80, mrNiceGuy.poinTerkumpul)

        Produk produkA = new Produk(nama: 'Produk A', poin: 10)
        Produk produkB = new Produk(nama: 'Produk B', poin: 0)
        Produk produkC = new Produk(nama: 'Produk C', poin: 5)
        PengeluaranBarang daftarBarang = new PengeluaranBarang(nomor: 'PB-123')
        daftarBarang.tambah(new ItemBarang(produkA, 10))
        daftarBarang.tambah(new ItemBarang(produkB, 20))
        daftarBarang.tambah(new ItemBarang(produkC, 30))

        mrNiceGuy.hapusPoin(daftarBarang)
        assertEquals(0, mrNiceGuy.poinTerkumpul)
        assertEquals(2, mrNiceGuy.listRiwayatPoin.size())
        assertEquals(LocalDate.now(), mrNiceGuy.listRiwayatPoin[0].tanggal)
        assertEquals(-20, mrNiceGuy.listRiwayatPoin[0].poin)
        assertNull(mrNiceGuy.listRiwayatPoin[0].referensi)
        assertEquals(LocalDate.now(), mrNiceGuy.listRiwayatPoin[1].tanggal)
        assertEquals(-250, mrNiceGuy.listRiwayatPoin[1].poin)
        assertEquals('PB-123', mrNiceGuy.listRiwayatPoin[1].referensi)
    }

    public void testPotongPiutang() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen mrNiceGuy = new Konsumen(sales: sales)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 20000, 20100, 50)
        FakturJualOlehSales f1 = new FakturJualOlehSales(nomor: 'F1', tanggal: LocalDate.now().minusDays(3), konsumen: mrNiceGuy, status: StatusFakturJual.DITERIMA)  // Piutang: 200.000
        f1.tambah(new ItemFaktur(produkA, 10, 10000))
        f1.tambah(new ItemFaktur(produkB, 5, 20000))
        f1.piutang = new KewajibanPembayaran(jumlah: f1.total())
        FakturJualOlehSales f2 = new FakturJualOlehSales(nomor: 'F2', tanggal: LocalDate.now().minusDays(2), konsumen: mrNiceGuy, status: StatusFakturJual.DITERIMA)  // Piutang:  70.000
        f2.tambah(new ItemFaktur(produkA, 3, 10000))
        f2.tambah(new ItemFaktur(produkB, 2, 20000))
        f2.piutang = new KewajibanPembayaran(jumlah: f2.total())
        FakturJualOlehSales f3 = new FakturJualOlehSales(nomor: 'F3', tanggal: LocalDate.now(), konsumen: mrNiceGuy, status: StatusFakturJual.DITERIMA)  // Piutang:  30.000
        f3.tambah(new ItemFaktur(produkA, 3, 10000))
        f3.piutang = new KewajibanPembayaran(jumlah: f3.total())
        mrNiceGuy.tambahFakturBelumLunas(f1)
        mrNiceGuy.tambahFakturBelumLunas(f2)
        mrNiceGuy.tambahFakturBelumLunas(f3)
        assertEquals(300000, mrNiceGuy.jumlahPiutang())
        assertEquals(300000, mrNiceGuy.creditTerpakai)

        mrNiceGuy.potongPiutang(150000)
        assertEquals(150000, mrNiceGuy.jumlahPiutang())
        assertEquals(300000, mrNiceGuy.creditTerpakai)
        assertEquals(3, mrNiceGuy.listFakturBelumLunas.size())
        assertEquals(50000, f1.sisaPiutang())
        assertEquals(70000, f2.sisaPiutang())
        assertEquals(30000, f3.sisaPiutang())

        mrNiceGuy.potongPiutang(50000)
        assertEquals(100000, mrNiceGuy.jumlahPiutang())
        assertEquals(100000, mrNiceGuy.creditTerpakai)
        assertEquals(2, mrNiceGuy.listFakturBelumLunas.size())
        assertEquals(70000, f2.sisaPiutang())
        assertEquals(30000, f3.sisaPiutang())

        mrNiceGuy.potongPiutang(80000)
        assertEquals(20000, mrNiceGuy.jumlahPiutang())
        assertEquals(30000, mrNiceGuy.creditTerpakai)
        assertEquals(1, mrNiceGuy.listFakturBelumLunas.size())
        assertEquals(20000, f3.sisaPiutang())

        shouldFail { mrNiceGuy.potongPiutang(500000)}
    }

}

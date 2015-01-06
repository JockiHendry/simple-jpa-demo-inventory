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

import domain.faktur.ItemFaktur
import domain.faktur.Pembayaran
import domain.faktur.Referensi
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.penjualan.ReturFaktur
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import domain.inventory.Periode
import domain.inventory.Produk
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.Sales
import domain.penjualan.StatusFakturJual
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil

class FakturJualOlehSalesTests extends GriffonUnitTestCase{

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        PengaturanRepository.metaClass.getValue = { x -> true }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
        SimpleJpaUtil.instance.repositoryManager.instances['PengaturanRepository'] = new PengaturanRepository()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testKirim() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(nama: 'Konsumen', sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen:  konsumen)
        f.tambah(new ItemFaktur(produkA, 10))
        f.tambah(new ItemFaktur(produkB, 20))
        f.proses()
        assertEquals(StatusFakturJual.DIBUAT, f.status)

        f.proses([alamatTujuan: 'Xtra Street'])
        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.tanggal)
        assertEquals('Xtra Street', f.pengeluaranBarang.alamatTujuan)
    }

    public void testTambahBuktiTerima() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        produkA.poin = 10
        produkB.poin = 0
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(nama: 'Konsumen', sales: showroom, poinTerkumpul: 50)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        f.proses([alamatTujuan: 'Xtra Street'])
        assertEquals(StatusFakturJual.DIANTAR, f.status)

        f.proses([buktiTerima: new BuktiTerima(LocalDate.now(), 'Mr. Stranger', 'Mr. Nice Guy')])
        assertNotNull(f.pengeluaranBarang.buktiTerima)
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertNotNull(f.piutang)
        assertEquals(0, f.total().compareTo(f.piutang.jumlah))
        assertEquals(150, konsumen.poinTerkumpul)
    }

    public void testBayar() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        k.tambahFakturBelumLunas(f)
        f.proses([alamatTujuan: 'Xtra Street'])
        f.proses([buktiTerima: new BuktiTerima(LocalDate.now(), 'Mr. Stranger', 'Mr. Nice Guy')])
        f.bayar(new Pembayaran(LocalDate.now(), 120000))
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        f.bayar(new Pembayaran(LocalDate.now(), 220000))
        assertEquals(StatusFakturJual.LUNAS, f.status)
        shouldFail {
            f.bayar(new Pembayaran(LocalDate.now(), 5000))
        }
    }

    public void testHapusPembayaran() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        k.tambahFakturBelumLunas(f)
        f.proses([alamatTujuan: 'Xtra Street'])
        f.proses([buktiTerima: new BuktiTerima(LocalDate.now(), 'Mr. Stranger', 'Mr. Nice Guy')])

        Pembayaran pembayaran1 = new Pembayaran(LocalDate.now(), 100000)
        Pembayaran pembayaran2 = new Pembayaran(LocalDate.now(), 240000)
        f.bayar(pembayaran1)
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertTrue(k.listFakturBelumLunas.contains(f))
        f.bayar(pembayaran2)
        assertEquals(StatusFakturJual.LUNAS, f.status)
        assertFalse(k.listFakturBelumLunas.contains(f))

        // Hapus
        f.hapusPembayaran(pembayaran2)
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertTrue(k.listFakturBelumLunas.contains(f))
        assertEquals(340000, f.jumlahPiutang())
        assertEquals(100000, f.jumlahDibayar())

        f.hapusPembayaran(pembayaran1)
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertTrue(k.listFakturBelumLunas.contains(f))
        assertEquals(340000, f.jumlahPiutang())
        assertEquals(0, f.jumlahDibayar())
    }

    public void testHapusPembayaranBerdasarkanReferensi() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        k.tambahFakturBelumLunas(f)
        f.proses([alamatTujuan: 'Xtra Street'])
        f.proses([buktiTerima: new BuktiTerima(LocalDate.now(), 'Mr. Stranger', 'Mr. Nice Guy')])
        Pembayaran pembayaran1 = new Pembayaran(LocalDate.now(), 1000, true, null, new Referensi(FakturJualOlehSales, 'R-001'))
        Pembayaran pembayaran2 = new Pembayaran(LocalDate.now(), 240000)
        f.bayar(pembayaran1)
        f.bayar(pembayaran2)

        // Hapus
        f.hapusPembayaran('R-001')

        // Periksa
        assertEquals(1, f.piutang.listPembayaran.size())
        assertEquals(pembayaran2, f.piutang.listPembayaran[0])
        assertEquals(340000, f.jumlahPiutang())
        assertEquals(240000, f.jumlahDibayar())
    }


    public void testSisaPiutang() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        k.tambahFakturBelumLunas(f)
        f.proses([alamatTujuan: 'Xtra Street'])
        f.proses([buktiTerima: new BuktiTerima(LocalDate.now(), 'Mr. Stranger', 'Mr. Nice Guy')])

        assertEquals(340000, f.sisaPiutang())
        f.bayar(new Pembayaran(LocalDate.now(), 120000))
        assertEquals(220000, f.sisaPiutang())
        f.bayar(new Pembayaran(LocalDate.now(), 220000))
        assertEquals(0, f.sisaPiutang())
    }

    public void testTambahBonus() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        assertEquals(340000, f.total())

        f.tambahBonus([new ItemBarang(produkA, 2), new ItemBarang(produkB, 1)])
        assertNotNull(f.bonusPenjualan)
        assertEquals(3, f.bonusPenjualan.jumlah())
        assertEquals(2, f.bonusPenjualan.jumlah(produkA))
        assertEquals(1, f.bonusPenjualan.jumlah(produkB))
        assertEquals(340000, f.total())
    }

    public void testHapusBonus() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.tambahBonus([new ItemBarang(produkA, 2), new ItemBarang(produkB, 1)])
        assertNotNull(f.bonusPenjualan)
        f.hapusBonus()
        assertNull(f.bonusPenjualan)
    }

    public void testSudahJatuhTempo() {
        FakturJualOlehSales f = new FakturJualOlehSales(tanggal: Periode.format.parseLocalDate('01-01-2013'))
        f.jatuhTempo = f.tanggal.plusDays(15)
        assertTrue(f.sudahJatuhTempo(Periode.format.parseLocalDate('16-01-2013')))
        assertTrue(f.sudahJatuhTempo(Periode.format.parseLocalDate('17-01-2013')))
        assertFalse(f.sudahJatuhTempo(Periode.format.parseLocalDate('15-01-2013')))
    }

    public void testBarangYangHarusDikirim() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.tambahBonus([new ItemBarang(produkA, 2), new ItemBarang(produkB, 1)])

        DaftarBarangSementara hasil = f.barangYangHarusDikirim()
        assertEquals(2, hasil.items.size())
        assertEquals(12, hasil.items.find {it.produk==produkA}.jumlah)
        assertEquals(21, hasil.items.find {it.produk==produkB}.jumlah)
    }

    public void testReturFakturDiterima() {
        Produk produkA = new Produk('Produk A', 50, 10000, 10100)
        produkA.setPoin(3)
        Produk produkB = new Produk('Produk B', 50, 12000, 12100)
        produkB.setPoin(4)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        f.proses([alamatTujuan: 'Tujuan'])
        f.proses([buktiTerima: new BuktiTerima()])
        assertEquals(340000, f.piutang.jumlah)
        assertEquals(110, konsumen.poinTerkumpul)

        ReturFaktur retur1 = new ReturFaktur()
        retur1.tambah(new ItemBarang(produkA, 5))
        f.tambahRetur(retur1)
        assertEquals(1, f.retur.size())
        assertEquals(retur1, f.retur[0])
        assertEquals(340000, f.piutang.jumlah)
        assertEquals(290000, f.piutang.sisa())
        assertEquals(1, f.piutang.listPembayaran.size())
        assertEquals(50000, f.piutang.listPembayaran[0].jumlah)
        assertTrue(f.piutang.listPembayaran[0].potongan)
        assertEquals(95, konsumen.poinTerkumpul)
        assertEquals(110, konsumen.listRiwayatPoin[0].poin)
        assertEquals(-15, konsumen.listRiwayatPoin[1].poin)

        ReturFaktur retur2 = new ReturFaktur()
        retur2.tambah(new ItemBarang(produkB, 20))
        f.tambahRetur(retur2)
        assertEquals(2, f.retur.size())
        assertEquals(retur2, f.retur[1])
        assertEquals(340000, f.piutang.jumlah)
        assertEquals(50000, f.piutang.sisa())
        assertEquals(2, f.piutang.listPembayaran.size())
        assertEquals(240000, f.piutang.listPembayaran[1].jumlah)
        assertTrue(f.piutang.listPembayaran[1].potongan)
        assertEquals(15, konsumen.poinTerkumpul)
        assertEquals(110, konsumen.listRiwayatPoin[0].poin)
        assertEquals(-15, konsumen.listRiwayatPoin[1].poin)
        assertEquals(-80, konsumen.listRiwayatPoin[2].poin)
    }

    public void testReturFakturDiantar() {
        Produk produkA = new Produk('Produk A', 50, 10000, 10100)
        produkA.setPoin(3)
        Produk produkB = new Produk('Produk B', 50, 12000, 12100)
        produkB.setPoin(4)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses()
        f.proses([alamatTujuan: 'Tujuan'])

        ReturFaktur retur1 = new ReturFaktur()
        retur1.tambah(new ItemBarang(produkA, 5))
        f.tambahRetur(retur1)
        assertEquals(1, f.retur.size())
        assertEquals(retur1, f.retur[0])
        assertNull(f.piutang)
        assertEquals(0, konsumen.poinTerkumpul)

        ReturFaktur retur2 = new ReturFaktur()
        retur2.tambah(new ItemBarang(produkB, 20))
        f.tambahRetur(retur2)
        assertEquals(2, f.retur.size())
        assertEquals(retur2, f.retur[1])
        assertNull(f.piutang)
        assertEquals(0, konsumen.poinTerkumpul)

        // Pastikan barang yang diantar sudah dikurangi barang retur
        f.proses([buktiTerima: new BuktiTerima()])
        assertEquals(50000, f.piutang.jumlah)
        assertEquals(15, konsumen.poinTerkumpul)
    }

    public void testTotalRetur() {
        Produk produkA = new Produk('Produk A', 50, 10000, 10100)
        produkA.setPoin(3)
        Produk produkB = new Produk('Produk B', 50, 12000, 12100)
        produkB.setPoin(4)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen konsumen = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen: konsumen)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        f.proses([alamatTujuan: 'Tujuan'])

        ReturFaktur retur1 = new ReturFaktur()
        retur1.tambah(new ItemBarang(produkA, 5))
        f.tambahRetur(retur1)

        ReturFaktur retur2 = new ReturFaktur()
        retur2.tambah(new ItemBarang(produkB, 20))
        f.tambahRetur(retur2)

        assertEquals(290000, f.totalRetur())
    }
}

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
import domain.faktur.ItemFaktur
import domain.faktur.Pembayaran
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
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
import simplejpa.SimpleJpaUtil

class FakturJualOlehSalesTests extends GriffonUnitTestCase{

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
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
        assertEquals(StatusFakturJual.DIBUAT, f.status)

        f.kirim('Xtra Street', 'Mr. Nice Guy')
        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.tanggal)
        assertEquals('Xtra Street', f.pengeluaranBarang.alamatTujuan)
        assertEquals('Mr. Nice Guy', f.pengeluaranBarang.namaSupir)
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
        f.kirim('Xtra Street', 'Mr. Nice Guy')
        assertEquals(StatusFakturJual.DIANTAR, f.status)

        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))
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
        k.tambahFakturBelumLunas(f)
        f.kirim('Xtra Street', 'Mr. Nice Guy')
        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))

        f.bayar(new Pembayaran(LocalDate.now(), 120000))
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        f.bayar(new Pembayaran(LocalDate.now(), 220000))
        assertEquals(StatusFakturJual.LUNAS, f.status)
        shouldFail(DataTidakBolehDiubah) {
            f.bayar(new Pembayaran(LocalDate.now(), 5000))
        }
    }

    public void testHapusPembayaranGagal() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        k.tambahFakturBelumLunas(f)

        f.kirim('Xtra Street', 'Mr. Nice Guy')
        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))

        f.bayar(new Pembayaran(LocalDate.now(), 100000))
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        f.bayar(new Pembayaran(LocalDate.now(), 240000))
        assertEquals(StatusFakturJual.LUNAS, f.status)
        shouldFail(DataTidakBolehDiubah) {
            f.hapus(new Pembayaran(LocalDate.now(), 10000))
        }
    }

    public void testSisaPiutang() {
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        Sales showroom = new Sales('Showroom', null, gudangUtama)
        Konsumen k = new Konsumen(sales: showroom)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F1', konsumen: k)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 12000))
        k.tambahFakturBelumLunas(f)
        f.kirim('Xtra Street', 'Mr. Nice Guy')
        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Stranger'))

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

    void testSudahJatuhTempo() {
        FakturJualOlehSales f = new FakturJualOlehSales(tanggal: Periode.format.parseLocalDate('01-01-2013'))
        f.jatuhTempo = f.tanggal.plusDays(15)
        assertTrue(f.sudahJatuhTempo(Periode.format.parseLocalDate('16-01-2013')))
        assertTrue(f.sudahJatuhTempo(Periode.format.parseLocalDate('17-01-2013')))
        assertFalse(f.sudahJatuhTempo(Periode.format.parseLocalDate('15-01-2013')))
    }

    void testBarangYangHarusDikirim() {
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

}

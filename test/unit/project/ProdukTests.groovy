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
import domain.inventory.ItemStok
import domain.inventory.Periode
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.pengaturan.KeyPengaturan
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil

class ProdukTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testTersediaUntuk() {
        Produk p = new Produk(nama: 'Produk A', hargaDalamKota: 10000, jumlah: 100, jumlahAkanDikirim: 30)
        assertTrue(p.tersediaUntuk(50))
        assertTrue(p.tersediaUntuk(65))
        assertTrue(p.tersediaUntuk(70))
        assertFalse(p.tersediaUntuk(80))
        assertFalse(p.tersediaUntuk(90))
        assertFalse(p.tersediaUntuk(110))
    }

    public void testLevelMinimum() {
        PengaturanRepository pengaturanRepository = new PengaturanRepository()
        pengaturanRepository.cache[KeyPengaturan.LEVEL_MINIMUM_STOK] = 5
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['PengaturanRepository'] = pengaturanRepository

        Produk p1 = new Produk(nama: 'Produk A', levelMinimum: 10)
        assertEquals(10, p1.levelMinimum)

        Produk p2 = new Produk(nama: 'Produk B')
        assertEquals(5, p2.levelMinimum)
    }

    public void testSaldoKumulatifSebelumPeriode() {
        StokProduk s = new StokProduk()
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 5))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-03'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-05'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-05'), jumlah: 10))

        // Periksa saldo kumulatif
        PeriodeItemStok p1 = s.listPeriodeRiwayat[0]
        PeriodeItemStok p2 = s.listPeriodeRiwayat[1]
        PeriodeItemStok p3 = s.listPeriodeRiwayat[2]
        assertEquals(0, s.saldoKumulatifSebelum(p1))
        assertEquals(60, s.saldoKumulatifSebelum(p2))
        assertEquals(95, s.saldoKumulatifSebelum(p3))
    }

    public void testSaldoKumulatifSebelumTanggal() {
        StokProduk s = new StokProduk()
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 5))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-03'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-05'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-05'), jumlah: 10))

        // Periksa saldo kumulatif
        assertEquals(0, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-01')))
        assertEquals(60, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-10')))
        assertEquals(30, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-05')))
        assertEquals(60, s.saldoKumulatifSebelum(LocalDate.parse('2014-02-01')))
        assertEquals(105, s.saldoKumulatifSebelum(LocalDate.parse('2014-03-03')))
    }

    public void testCariItemStokBerdasarkanPeriodeItemStok() {
        StokProduk s = new StokProduk()
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 5))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-03'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-05'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-01'), jumlah: 10))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-03'), jumlah: 20))
        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-03-05'), jumlah: 10))

        PeriodeItemStok p1 = s.listPeriodeRiwayat[0]
        PeriodeItemStok p2 = s.listPeriodeRiwayat[1]
        PeriodeItemStok p3 = s.listPeriodeRiwayat[2]

        // Cari item stok untuk periode 1
        List hasil = s.cariItemStok(p1)
        assertEquals(10, hasil[0].saldo)
        assertEquals(30, hasil[1].saldo)
        assertEquals(60, hasil[2].saldo)

        // Cari item stok untuk periode 2
        hasil = s.cariItemStok(p2)
        assertEquals(65, hasil[0].saldo)
        assertEquals(75, hasil[1].saldo)
        assertEquals(95, hasil[2].saldo)

        // Cari item stok untuk periode 3
        hasil = s.cariItemStok(p3)
        assertEquals(105, hasil[0].saldo)
        assertEquals(125, hasil[1].saldo)
        assertEquals(135, hasil[2].saldo)
    }

    public void testCariItemStokBerdasarkanPeriode() {
        StokProduk s = new StokProduk()

        // Periode 1
        PeriodeItemStok p1 = new PeriodeItemStok(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai:  LocalDate.parse('2014-01-31'), jumlah: 60)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30)
        s.listPeriodeRiwayat << p1

        // Periode 2
        PeriodeItemStok p2 = new PeriodeItemStok(tanggalMulai: LocalDate.parse('2014-02-01'), tanggalSelesai:  LocalDate.parse('2014-02-28'), jumlah: 35)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 5)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-03'), jumlah: 10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-05'), jumlah: 20)
        s.listPeriodeRiwayat << p2

        // Periode 3
        PeriodeItemStok p3 = new PeriodeItemStok(tanggalMulai: LocalDate.parse('2014-03-01'), tanggalSelesai:  LocalDate.parse('2014-03-31'), jumlah: 40)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-01'), jumlah: 10)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-03'), jumlah: 20)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-05'), jumlah: 10)
        s.listPeriodeRiwayat << p3

        // Cari item stok untuk periode

        List hasil = s.cariItemStok(Periode.dari('01-01-2014', '31-01-2014'))
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)
        assertEquals(30, hasil[2].jumlah)

        hasil = s.cariItemStok(Periode.dari('01-01-2014', '15-01-2014'))
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)

        hasil = s.cariItemStok(Periode.dari('05-01-2014', '31-01-2014'))
        assertEquals(30, hasil[0].jumlah)

        hasil = s.cariItemStok(Periode.dari('15-01-2014', '31-01-2014'))
        assertTrue(hasil.empty)

        hasil = s.cariItemStok(Periode.dari('01-02-2014', '28-02-2014'))
        assertEquals(5,  hasil[0].jumlah)
        assertEquals(10, hasil[1].jumlah)
        assertEquals(20, hasil[2].jumlah)

        hasil = s.cariItemStok(Periode.dari('01-03-2014', '31-03-2014'))
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)
        assertEquals(10, hasil[2].jumlah)

        hasil = s.cariItemStok(Periode.dari('01-01-2014', '31-03-2014'))
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)
        assertEquals(30, hasil[2].jumlah)
        assertEquals( 5, hasil[3].jumlah)
        assertEquals(10, hasil[4].jumlah)
        assertEquals(20, hasil[5].jumlah)
        assertEquals(10, hasil[6].jumlah)
        assertEquals(20, hasil[7].jumlah)
        assertEquals(10, hasil[8].jumlah)
    }

    public void testSemuaItemStok() {
        Produk produk = new Produk()
        Gudang g1 = new Gudang('Gudang1', true)
        Gudang g2 = new Gudang('Gudang2')

        StokProduk s1 = new StokProduk(gudang: g1, produk: produk)
        PeriodeItemStok p1 = new PeriodeItemStok(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai: LocalDate.parse('2014-01-31'), jumlah: 60)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-15'), jumlah: 20)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-15'), jumlah: -15)
        s1.listPeriodeRiwayat << p1
        produk.daftarStok[g1] = s1

        StokProduk s2 = new StokProduk(gudang: g2, produk: produk)
        PeriodeItemStok p2 = new PeriodeItemStok(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai: LocalDate.parse('2014-01-31'), jumlah: 35)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: -10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-15'), jumlah: 30)
        s2.listPeriodeRiwayat << p2
        produk.daftarStok[g2] = s2

        // Periksa hasil
        List hasil = produk.semuaItemStok(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'))
        assertEquals( 10, hasil[0].jumlah)
        assertEquals( 10, hasil[1].jumlah)
        assertEquals(-10, hasil[2].jumlah)
        assertEquals( 30, hasil[3].jumlah)
        assertEquals( 20, hasil[4].jumlah)
        assertEquals(-15, hasil[5].jumlah)

        hasil = produk.semuaItemStok(LocalDate.parse('2014-01-15'), LocalDate.parse('2014-01-31'))
        assertEquals( 30, hasil[0].jumlah)
        assertEquals( 20, hasil[1].jumlah)
        assertEquals(-15, hasil[2].jumlah)
    }

    public void testSaldo() {
        Produk p = new Produk()
        Gudang g = new Gudang('Gudang1', true)
        StokProduk s = new StokProduk(gudang: g, produk: p)
        assertEquals(0, s.jumlah)

        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10))
        assertEquals(10, s.jumlah)
        assertEquals(10, s.listNilaiPeriodik[0].saldo)
        assertEquals(10, s.listNilaiPeriodik[0].listItemPeriodik[0].saldo)

        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-01-31'), jumlah: 20))
        assertEquals(30, s.jumlah)
        assertEquals(30, s.listNilaiPeriodik[0].saldo)
        assertEquals(10, s.listNilaiPeriodik[0].listItemPeriodik[0].saldo)
        assertEquals(30, s.listNilaiPeriodik[0].listItemPeriodik[1].saldo)

        s.tambah(new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 30))
        assertEquals(60, s.jumlah)
        assertEquals(30, s.listNilaiPeriodik[0].saldo)
        assertEquals(60, s.listNilaiPeriodik[1].saldo)
        assertEquals(60, s.listNilaiPeriodik[1].listItemPeriodik[0].saldo)
    }

}

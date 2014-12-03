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

import domain.inventory.ItemStok
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

        // Periode 1
        PeriodeItemStok p1 = new PeriodeItemStok(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), 60)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30)
        s.listPeriodeRiwayat << p1

        // Periode 2
        PeriodeItemStok p2 = new PeriodeItemStok(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-28'), 35)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 5)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 20)
        s.listPeriodeRiwayat << p2

        // Periode 3
        PeriodeItemStok p3 = new PeriodeItemStok(LocalDate.parse('2014-03-01'), LocalDate.parse('2014-03-31'), 40)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 10)
        s.listPeriodeRiwayat << p3

        // Periksa saldo kumulatif
        assertEquals(0, s.saldoKumulatifSebelum(p1))
        assertEquals(60, s.saldoKumulatifSebelum(p2))
        assertEquals(95, s.saldoKumulatifSebelum(p3))
    }

    public void testSaldoKumulatifSebelumTanggal() {
        StokProduk s = new StokProduk()

        // Periode 1
        PeriodeItemStok p1 = new PeriodeItemStok(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), 60)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30)
        s.listPeriodeRiwayat << p1

        // Periode 2
        PeriodeItemStok p2 = new PeriodeItemStok(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-28'), 35)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-01'), jumlah: 5)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-03'), jumlah: 10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-02-05'), jumlah: 20)
        s.listPeriodeRiwayat << p2

        // Periode 3
        PeriodeItemStok p3 = new PeriodeItemStok(LocalDate.parse('2014-03-01'), LocalDate.parse('2014-03-31'), 40)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-01'), jumlah: 10)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-03'), jumlah: 20)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-03-05'), jumlah: 10)
        s.listPeriodeRiwayat << p3

        // Periksa saldo kumulatif
        assertEquals(0, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-01')))
        assertEquals(60, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-10')))
        assertEquals(30, s.saldoKumulatifSebelum(LocalDate.parse('2014-01-05')))
        assertEquals(60, s.saldoKumulatifSebelum(LocalDate.parse('2014-02-01')))
        assertEquals(105, s.saldoKumulatifSebelum(LocalDate.parse('2014-03-03')))
    }

    public void testCariItemStok() {
        StokProduk s = new StokProduk()

        // Periode 1
        PeriodeItemStok p1 = new PeriodeItemStok(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), 60)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p1.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 30)
        s.listPeriodeRiwayat << p1

        // Periode 2
        PeriodeItemStok p2 = new PeriodeItemStok(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-28'), 35)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 5)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 10)
        p2.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 20)
        s.listPeriodeRiwayat << p2

        // Periode 3
        PeriodeItemStok p3 = new PeriodeItemStok(LocalDate.parse('2014-03-01'), LocalDate.parse('2014-03-31'), 40)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-01'), jumlah: 10)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20)
        p3.listItem << new ItemStok(tanggal: LocalDate.parse('2014-01-05'), jumlah: 10)
        s.listPeriodeRiwayat << p3

        // Cari item stok untuk periode 1
        List hasil = s.cariItemStok(p1)
        assertEquals(0, hasil[0].saldo)
        assertEquals(10, hasil[1].saldo)
        assertEquals(30, hasil[2].saldo)
        assertEquals(60, hasil[3].saldo)

        // Cari item stok untuk periode 2
        hasil = s.cariItemStok(p2)
        assertEquals(60, hasil[0].saldo)
        assertEquals(65, hasil[1].saldo)
        assertEquals(75, hasil[2].saldo)
        assertEquals(95, hasil[3].saldo)

        // Cari item stok untuk periode 3
        hasil = s.cariItemStok(p3)
        assertEquals(95, hasil[0].saldo)
        assertEquals(105, hasil[1].saldo)
        assertEquals(125, hasil[2].saldo)
        assertEquals(135, hasil[3].saldo)
    }

}

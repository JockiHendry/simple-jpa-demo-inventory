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

import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.JenisTransaksiKas
import domain.labarugi.KategoriKas
import domain.labarugi.PeriodeKas
import domain.labarugi.TransaksiKas
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class PeriodeKasTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testJumlahPerKategoriDanJenis() {
        PeriodeKas p = new PeriodeKas(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai: LocalDate.parse('2014-01-31'), saldo: 0, jumlah: 0)
        KategoriKas k1 = new KategoriKas('Kategori1')
        KategoriKas k2 = new KategoriKas('Kategori2')
        KategoriKas k3 = new KategoriKas('Kategori3')
        JenisTransaksiKas j1 = new JenisTransaksiKas('Jenis1')
        JenisTransaksiKas j2 = new JenisTransaksiKas('Jenis2')
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 10000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20000, kategoriKas: k1, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-04'), jumlah: 1000, kategoriKas: k2, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-05'), jumlah: 5000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-06'), jumlah: 30000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-07'), jumlah: 4000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 5000, kategoriKas: k3, jenis: j1))
        assertEquals(60000, p.jumlah(k1))
        assertEquals(40000, p.jumlah(k1, j1))
        assertEquals(20000, p.jumlah(k1, j2))
        assertEquals(10000, p.jumlah(k2))
        assertEquals(1000, p.jumlah(k2, j1))
        assertEquals(9000, p.jumlah(k2, j2))
        assertEquals(5000, p.jumlah(k3))
        assertEquals(5000, p.jumlah(k3, j1))
    }

    void testJumlahPerJenisKategoriKas() {
        PeriodeKas p = new PeriodeKas(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai: LocalDate.parse('2014-01-31'), saldo: 0, jumlah: 0)
        KategoriKas k1 = new KategoriKas('Kategori1', JENIS_KATEGORI_KAS.PENDAPATAN)
        KategoriKas k2 = new KategoriKas('Kategori2', JENIS_KATEGORI_KAS.PENGELUARAN)
        KategoriKas k3 = new KategoriKas('Kategori3', JENIS_KATEGORI_KAS.PENGELUARAN)
        JenisTransaksiKas j1 = new JenisTransaksiKas('Jenis1')
        JenisTransaksiKas j2 = new JenisTransaksiKas('Jenis2')
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 10000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20000, kategoriKas: k1, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-04'), jumlah: 1000, kategoriKas: k2, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-05'), jumlah: 5000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-06'), jumlah: 30000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-07'), jumlah: 4000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 5000, kategoriKas: k3, jenis: j1))
        assertEquals(60000, p.jumlah(JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(15000, p.jumlah(JENIS_KATEGORI_KAS.PENGELUARAN))
    }

    void testJumlahPerJenisKategoriKas2() {
        PeriodeKas p = new PeriodeKas(tanggalMulai: LocalDate.parse('2014-01-01'), tanggalSelesai: LocalDate.parse('2014-01-31'), saldo: 0, jumlah: 0)
        KategoriKas k1 = new KategoriKas('Kategori1', JENIS_KATEGORI_KAS.PENDAPATAN, false, true)
        KategoriKas k2 = new KategoriKas('Kategori2', JENIS_KATEGORI_KAS.PENGELUARAN, false, false)
        KategoriKas k3 = new KategoriKas('Kategori3', JENIS_KATEGORI_KAS.PENGELUARAN, false, true)
        JenisTransaksiKas j1 = new JenisTransaksiKas('Jenis1')
        JenisTransaksiKas j2 = new JenisTransaksiKas('Jenis2')
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 10000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-03'), jumlah: 20000, kategoriKas: k1, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-04'), jumlah: 1000, kategoriKas: k2, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-05'), jumlah: 5000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-06'), jumlah: 30000, kategoriKas: k1, jenis: j1))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-07'), jumlah: 4000, kategoriKas: k2, jenis: j2))
        p.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-02'), jumlah: 5000, kategoriKas: k3, jenis: j1))
        assertEquals(60000, p.jumlah(JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals( 5000, p.jumlah(JENIS_KATEGORI_KAS.PENGELUARAN, true))
    }

}

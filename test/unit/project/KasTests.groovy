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

import domain.general.NilaiPeriodik
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.Kas
import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class KasTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testTambahTransaksiKas() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('Pendapatan', JENIS_KATEGORI_KAS.PENDAPATAN)
        KategoriKas k2 = new KategoriKas('Pengeluaran', JENIS_KATEGORI_KAS.PENGELUARAN)

        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, kategoriKas: k1, keterangan: 'Transfer'))
        assertEquals(1000000, kas.jumlah)
        assertEquals(1, kas.listNilaiPeriodik.size())
        assertEquals(LocalDate.parse('2014-01-01'), kas.listNilaiPeriodik[0].tanggalMulai)
        assertEquals(LocalDate.parse('2014-01-31'), kas.listNilaiPeriodik[0].tanggalSelesai)
        assertEquals(1000000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(1, kas.listNilaiPeriodik[0].listItemPeriodik.size())
        assertEquals(1000000, kas.listNilaiPeriodik[0].listItemPeriodik[0].saldo)

        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah:  150000, kategoriKas: k2, keterangan: 'Biaya'))
        assertEquals(850000, kas.jumlah)
        assertEquals(1, kas.listNilaiPeriodik.size())
        assertEquals(LocalDate.parse('2014-01-01'), kas.listNilaiPeriodik[0].tanggalMulai)
        assertEquals(LocalDate.parse('2014-01-31'), kas.listNilaiPeriodik[0].tanggalSelesai)
        assertEquals(850000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(2, kas.listNilaiPeriodik[0].listItemPeriodik.size())
        assertEquals(1000000, kas.listNilaiPeriodik[0].listItemPeriodik[0].saldo)
        assertEquals(850000, kas.listNilaiPeriodik[0].listItemPeriodik[1].saldo)

        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah: 200000, kategoriKas: k2, keterangan: 'Biaya'))
        assertEquals(650000, kas.jumlah)
        assertEquals(2, kas.listNilaiPeriodik.size())
        assertEquals(LocalDate.parse('2014-02-01'), kas.listNilaiPeriodik[1].tanggalMulai)
        assertEquals(LocalDate.parse('2014-02-28'), kas.listNilaiPeriodik[1].tanggalSelesai)
        assertEquals(650000, kas.listNilaiPeriodik[1].saldo)
        assertEquals(1, kas.listNilaiPeriodik[1].listItemPeriodik.size())
        assertEquals(650000, kas.listNilaiPeriodik[1].listItemPeriodik[0].saldo)
    }

    void testTambahTransaksiKasPadaPeriodeBerbeda() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('Pendapatan', JENIS_KATEGORI_KAS.PENDAPATAN)
        KategoriKas k2 = new KategoriKas('Pengeluaran', JENIS_KATEGORI_KAS.PENGELUARAN)
        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, kategoriKas: k1, keterangan: 'Transfer'))
        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah:  150000, kategoriKas: k2, keterangan: 'Biaya'))
        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah:  200000, kategoriKas: k2, keterangan: 'Biaya'))
        assertEquals(650000, kas.jumlah)
        assertEquals(2, kas.listNilaiPeriodik.size())
        assertEquals(850000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(650000, kas.listNilaiPeriodik[1].saldo)

        // Tambah transaksi kas baru pada bulan januari (periode pertama)
        kas.tambah(new TransaksiKas(tanggal: LocalDate.parse('2014-01-20'), jumlah: 500000, kategoriKas: k2, keterangan: 'Biaya'))
        assertEquals( 150000, kas.jumlah)
        assertEquals(2, kas.listNilaiPeriodik.size())
        assertEquals( 350000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(1000000, kas.listNilaiPeriodik[0].listTransaksiKas[0].saldo)
        assertEquals( 850000, kas.listNilaiPeriodik[0].listTransaksiKas[1].saldo)
        assertEquals( 350000, kas.listNilaiPeriodik[0].listTransaksiKas[2].saldo)
        assertEquals( 150000, kas.listNilaiPeriodik[1].saldo)
        assertEquals( 150000, kas.listNilaiPeriodik[1].listTransaksiKas[0].saldo)
    }

    void testHapusTransaksiKas() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('Pendapatan', JENIS_KATEGORI_KAS.PENDAPATAN)
        KategoriKas k2 = new KategoriKas('Pengeluaran', JENIS_KATEGORI_KAS.PENGELUARAN)
        def tr1 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, kategoriKas: k1, keterangan: 'Transfer')
        def tr2 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah:  150000, kategoriKas: k2, keterangan: 'Biaya')
        def tr3 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah:  200000, kategoriKas: k2, keterangan: 'Biaya')
        def tr4 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah:  500000, kategoriKas: k2, keterangan: 'Biaya')
        kas.tambah(tr1)
        kas.tambah(tr2)
        kas.tambah(tr3)
        kas.tambah(tr4)
        assertEquals(150000, kas.jumlah)
        assertEquals(850000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(150000, kas.listNilaiPeriodik[1].saldo)

        kas.hapus(tr4)
        assertEquals(650000, kas.jumlah)
        assertEquals(850000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(1000000, kas.listNilaiPeriodik[0].listItemPeriodik[0].saldo)
        assertEquals( 850000, kas.listNilaiPeriodik[0].listItemPeriodik[1].saldo)
        assertEquals( 650000, kas.listNilaiPeriodik[1].saldo)
        assertEquals( 650000, kas.listNilaiPeriodik[1].listItemPeriodik[0].saldo)

        kas.hapus(tr2)
        assertEquals(800000, kas.jumlah)
        assertEquals(1000000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(1, kas.listNilaiPeriodik[0].listItemPeriodik.size())
        assertEquals(1000000, kas.listNilaiPeriodik[0].listItemPeriodik[0].saldo)
        assertEquals(800000, kas.listNilaiPeriodik[1].saldo)
        assertEquals(1, kas.listNilaiPeriodik[1].listItemPeriodik.size())
        assertEquals(800000, kas.listNilaiPeriodik[1].listItemPeriodik[0].saldo)

        kas.hapus(tr3)
        assertEquals(1000000, kas.jumlah)
        assertEquals(1000000, kas.listNilaiPeriodik[0].saldo)
        assertEquals(1, kas.listNilaiPeriodik[0].listItemPeriodik.size())
        assertEquals(1000000, kas.listNilaiPeriodik[0].listItemPeriodik[0].saldo)
        assertEquals(1000000, kas.listNilaiPeriodik[1].saldo)
        assertEquals(0, kas.listNilaiPeriodik[1].listItemPeriodik.size())
    }

    void testKategoriKas() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('kategori1')
        KategoriKas k2 = new KategoriKas('kategori2')
        def tr1 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, keterangan: 'Transfer', kategoriKas: k1)
        def tr2 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah: -150000, keterangan: 'Biaya', kategoriKas: k2)
        def tr3 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah: -200000, keterangan: 'Biaya', kategoriKas: k2)
        def tr4 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah: -500000, keterangan: 'Biaya', kategoriKas: k2)
        kas.tambah(tr1)
        kas.tambah(tr2)
        kas.tambah(tr3)
        kas.tambah(tr4)
        assertEquals( 1000000, kas.listNilaiPeriodik[0].jumlah(k1))
        assertEquals( -150000, kas.listNilaiPeriodik[0].jumlah(k2))
        assertEquals(       0, kas.listNilaiPeriodik[1].jumlah(k1))
        assertEquals( -700000, kas.listNilaiPeriodik[1].jumlah(k2))

        kas.hapus(tr2)
        assertEquals( 1000000, kas.listNilaiPeriodik[0].jumlah(k1))
        assertEquals(       0, kas.listNilaiPeriodik[0].jumlah(k2))
        assertEquals(       0, kas.listNilaiPeriodik[1].jumlah(k1))
        assertEquals( -700000, kas.listNilaiPeriodik[1].jumlah(k2))

        kas.hapus(tr3)
        assertEquals( 1000000, kas.listNilaiPeriodik[0].jumlah(k1))
        assertEquals(       0, kas.listNilaiPeriodik[0].jumlah(k2))
        assertEquals(       0, kas.listNilaiPeriodik[1].jumlah(k1))
        assertEquals( -500000, kas.listNilaiPeriodik[1].jumlah(k2))
    }

    void testJumlahPerJenisKategoriKas() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('kategori1', JENIS_KATEGORI_KAS.PENDAPATAN)
        KategoriKas k2 = new KategoriKas('kategori2', JENIS_KATEGORI_KAS.PENGELUARAN)
        KategoriKas k3 = new KategoriKas('kategori3', JENIS_KATEGORI_KAS.PENGELUARAN)
        def tr1 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, keterangan: 'Transfer', kategoriKas: k1)
        def tr2 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah:  150000, keterangan: 'Biaya', kategoriKas: k2)
        def tr3 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah:  200000, keterangan: 'Biaya', kategoriKas: k2)
        def tr4 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah:  500000, keterangan: 'Biaya', kategoriKas: k2)
        def tr5 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah:   10000, keterangan: 'Biaya', kategoriKas: k3)
        kas.tambah(tr1)
        kas.tambah(tr2)
        kas.tambah(tr3)
        kas.tambah(tr4)
        kas.tambah(tr5)

        assertEquals(1000000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(150000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), JENIS_KATEGORI_KAS.PENGELUARAN))

        assertEquals(1000000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-02-28'), JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(860000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-02-28'), JENIS_KATEGORI_KAS.PENGELUARAN))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-01-15'), LocalDate.parse('2014-02-15'), JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(350000, kas.jumlah(LocalDate.parse('2014-01-15'), LocalDate.parse('2014-02-15'), JENIS_KATEGORI_KAS.PENGELUARAN))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-10'), JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(200000, kas.jumlah(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-10'), JENIS_KATEGORI_KAS.PENGELUARAN))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-02-10'), LocalDate.parse('2014-02-20'), JENIS_KATEGORI_KAS.PENDAPATAN))
        assertEquals(510000, kas.jumlah(LocalDate.parse('2014-02-10'), LocalDate.parse('2014-02-20'), JENIS_KATEGORI_KAS.PENGELUARAN))
    }

    void testJumlahPerJenisKategoriKas2() {
        Kas kas = new Kas('Kas Kecil')
        KategoriKas k1 = new KategoriKas('kategori1', JENIS_KATEGORI_KAS.PENDAPATAN, false, true)
        KategoriKas k2 = new KategoriKas('kategori2', JENIS_KATEGORI_KAS.PENGELUARAN, false, false)
        KategoriKas k3 = new KategoriKas('kategori3', JENIS_KATEGORI_KAS.PENGELUARAN, false, true)
        def tr1 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-01'), jumlah: 1000000, keterangan: 'Transfer', kategoriKas: k1)
        def tr2 = new TransaksiKas(tanggal: LocalDate.parse('2014-01-15'), jumlah:  150000, keterangan: 'Biaya', kategoriKas: k2)
        def tr3 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-01'), jumlah:  200000, keterangan: 'Biaya', kategoriKas: k2)
        def tr4 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah:  500000, keterangan: 'Biaya', kategoriKas: k2)
        def tr5 = new TransaksiKas(tanggal: LocalDate.parse('2014-02-20'), jumlah:   10000, keterangan: 'Biaya', kategoriKas: k3)
        kas.tambah(tr1)
        kas.tambah(tr2)
        kas.tambah(tr3)
        kas.tambah(tr4)
        kas.tambah(tr5)

        assertEquals(1000000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals(0, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-01-31'), JENIS_KATEGORI_KAS.PENGELUARAN, true))

        assertEquals(1000000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-02-28'), JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals(10000, kas.jumlah(LocalDate.parse('2014-01-01'), LocalDate.parse('2014-02-28'), JENIS_KATEGORI_KAS.PENGELUARAN, true))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-01-15'), LocalDate.parse('2014-02-15'), JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals(0, kas.jumlah(LocalDate.parse('2014-01-15'), LocalDate.parse('2014-02-15'), JENIS_KATEGORI_KAS.PENGELUARAN, true))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-10'), JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals(0, kas.jumlah(LocalDate.parse('2014-02-01'), LocalDate.parse('2014-02-10'), JENIS_KATEGORI_KAS.PENGELUARAN, true))

        assertEquals(0, kas.jumlah(LocalDate.parse('2014-02-10'), LocalDate.parse('2014-02-20'), JENIS_KATEGORI_KAS.PENDAPATAN, true))
        assertEquals(10000, kas.jumlah(LocalDate.parse('2014-02-10'), LocalDate.parse('2014-02-20'), JENIS_KATEGORI_KAS.PENGELUARAN, true))
    }

}

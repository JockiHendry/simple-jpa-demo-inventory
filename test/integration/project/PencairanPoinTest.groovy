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

import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.Kas
import domain.labarugi.PeriodeKas
import domain.labarugi.TransaksiKas
import domain.pengaturan.KeyPengaturan
import project.labarugi.KasRepository
import project.pengaturan.PengaturanRepository
import domain.penjualan.*
import org.joda.time.LocalDate
import project.penjualan.FakturJualRepository
import project.penjualan.PencairanPoinRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class PencairanPoinTest extends DbUnitTestCase {

    FakturJualRepository fakturJualRepository
    PencairanPoinRepository pencairanPoinRepository
    PengaturanRepository pengaturanRepository
    KasRepository kasRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_poin.xlsx")
        fakturJualRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('FakturJual')
        pencairanPoinRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('PencairanPoin')
        pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan')
        kasRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Kas')
    }

    public void testCairkanPoinTukarUang() {
        PencairanPoin p
        pencairanPoinRepository.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(2000, pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            p = new PencairanPoinTukarUang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen)
            p = pencairanPoinRepository.buat(p)

            assertNotNull(p.nomor)
            assertEquals(2000, p.rate)
            assertEquals(60000, p.nominal)
            assertEquals(konsumen, p.konsumen)
            assertEquals(30, p.jumlahPoin)

            konsumen = findKonsumenById(-1l)
            assertEquals(20, konsumen.poinTerkumpul)
        }

        pencairanPoinRepository.withTransaction {
            // Pastikan transaksi sistem dengan kategori pengeluaran lain telah dibuat.
            Kas kas = kasRepository.cariUntukSistem()
            assertEquals(-60000, kas.jumlah)
            PeriodeKas periodeKas = kas.getListPeriodeRiwayat()[0]
            assertEquals(-60000, periodeKas.jumlah)
            assertEquals(-60000, periodeKas.saldo)
            TransaksiKas tr = periodeKas.getListTransaksiKas()[0]
            assertEquals(p.nomor, tr.pihakTerkait)
            assertEquals(JENIS_KATEGORI_KAS.PENGELUARAN, tr.kategoriKas.jenis)
            assertEquals(60000, tr.jumlah)
            assertEquals(-60000, tr.saldo)
        }

        // Test hapus pencairan tunai
        pengaturanRepository.withTransaction {
            pencairanPoinRepository.hapus(p)
        }

        pencairanPoinRepository.withTransaction {
            // Pastiakan transasi sistem dengan kategori pengeluaran lain telah di-invers.
            Kas kas = kasRepository.cariUntukSistem()
            assertEquals(0, kas.jumlah)
            PeriodeKas periodeKas = kas.getListPeriodeRiwayat()[0]
            assertEquals(0, periodeKas.jumlah)
            assertEquals(0, periodeKas.saldo)
            TransaksiKas tr1 = periodeKas.getListTransaksiKas()[0]
            assertEquals(p.nomor, tr1.pihakTerkait)
            assertEquals(JENIS_KATEGORI_KAS.PENGELUARAN, tr1.kategoriKas.jenis)
            assertEquals(60000, tr1.jumlah)
            assertEquals(-60000, tr1.saldo)
            TransaksiKas tr2 = periodeKas.getListTransaksiKas()[1]
            assertEquals(p.nomor, tr2.pihakTerkait)
            assertEquals(JENIS_KATEGORI_KAS.PENDAPATAN, tr2.kategoriKas.jenis)
            assertEquals(60000, tr2.jumlah)
            assertEquals(0, tr2.saldo)
        }

    }

    public void testCairkanPoinTukarBarang() {
        pencairanPoinRepository.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            Produk produkA = findProdukById(-1l)
            Produk produkB = findProdukById(-2l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(2000, pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            PencairanPoinTukarBarang p = new PencairanPoinTukarBarang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen,
                listItemBarang: [new ItemBarang(produkA, 10), new ItemBarang(produkB, 5)])
            p = pencairanPoinRepository.buat(p)

            assertNotNull(p.nomor)
            assertEquals(2000, p.rate)
            assertEquals(konsumen, p.konsumen)
            assertEquals(20, p.jumlahPoin)

            konsumen = findKonsumenById(-1l)
            assertEquals(30, konsumen.poinTerkumpul)

            // Periksa jumlah barang yang berkurang
            produkA = findProdukById(-1l)
            produkB = findProdukById(-2l)
            Gudang gudang = konsumen.sales.gudang
            assertEquals(27, produkA.jumlah)
            assertEquals(0, produkA.stok(gudang).jumlah)
            assertEquals(22, produkB.jumlah)
            assertEquals(9, produkB.stok(gudang).jumlah)
        }
    }

    public void testCairkanPoinPotongPiutang() {
        pencairanPoinRepository.withTransaction {
            Produk p1 = findProdukById(-1l)
            Produk p2 = findProdukById(-2l)
            p1.jumlahAkanDikirim = 100
            p2.jumlahAkanDikirim = 100
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(110000, konsumen.jumlahPiutang())
            assertEquals(2000, pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            // Melakukan penerimaan untuk salah satu faktur sehingga piutangnya bisa dibayar
            FakturJualOlehSales f = konsumen.listFakturBelumLunas[0]
            f = fakturJualRepository.proses(f, [alamatTujuan: 'test'])
            fakturJualRepository.proses(f, [buktiTerima: new BuktiTerima(LocalDate.now(), 'test')])
            // Poin bertambah akibat penerimaan
            assertEquals(60, konsumen.poinTerkumpul)

            PencairanPoinPotongPiutang p = new PencairanPoinPotongPiutang(tanggal: LocalDate.now(), konsumen: konsumen, jumlahPoin: 10)
            p = pencairanPoinRepository.buat(p)

            assertNotNull(p.nomor)
            assertEquals(2000, p.rate)
            assertEquals(konsumen, p.konsumen)
            assertEquals(10, p.jumlahPoin)

            konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(90000, konsumen.jumlahPiutang())
        }
    }

    public void testHapus() {
        pencairanPoinRepository.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            PencairanPoinTukarUang p = new PencairanPoinTukarUang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen)
            p = pencairanPoinRepository.buat(p)

            // Sebelum dihapus
            konsumen = findKonsumenById(-1l)
            assertEquals(20, konsumen.poinTerkumpul)

            // Hapus
            p = pencairanPoinRepository.hapus(p)
            assertEquals('Y', p.deleted)

            // Setelah dihapus
            konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
        }
    }

    public void testHapusTukarBarang() {
        pencairanPoinRepository.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            Produk produkA = findProdukById(-1l)
            Produk produkB = findProdukById(-2l)
            assertEquals(50, konsumen.poinTerkumpul)

            PencairanPoinTukarBarang p = new PencairanPoinTukarBarang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen,
                    listItemBarang: [new ItemBarang(produkA, 10), new ItemBarang(produkB, 5)])
            p = pencairanPoinRepository.buat(p)

            konsumen = findKonsumenById(-1l)
            assertEquals(30, konsumen.poinTerkumpul)

            pencairanPoinRepository.hapus(p)
            assertEquals(50, konsumen.poinTerkumpul)

            // Periksa jumlah barang apakah bertambah kembali
            produkA = findProdukById(-1l)
            produkB = findProdukById(-2l)
            Gudang gudang = konsumen.sales.gudang
            assertEquals(37, produkA.jumlah)
            assertEquals(10, produkA.stok(gudang).jumlah)
            assertEquals(27, produkB.jumlah)
            assertEquals(14, produkB.stok(gudang).jumlah)
        }
    }


}

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

import domain.Container
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pengaturan.KeyPengaturan
import domain.penjualan.*
import org.joda.time.LocalDate
import simplejpa.testing.DbUnitTestCase

class PencairanPoinTest extends DbUnitTestCase {

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("konsumen", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testCairkanPoinTukarUang() {
        PencairanPoinRepository repo = Container.app.pencairanPoinRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(2000, Container.app.pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            PencairanPoinTukarUang p = new PencairanPoinTukarUang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen)
            p = repo.buat(p)

            assertNotNull(p.nomor)
            assertEquals(2000, p.rate)
            assertEquals(60000, p.nominal)
            assertEquals(konsumen, p.konsumen)
            assertEquals(30, p.jumlahPoin)

            konsumen = findKonsumenById(-1l)
            assertEquals(20, konsumen.poinTerkumpul)
        }
    }

    public void testCairkanPoinTukarBarang() {
        PencairanPoinRepository repo = Container.app.pencairanPoinRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            Produk produkA = findProdukById(-1l)
            Produk produkB = findProdukById(-2l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(2000, Container.app.pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            PencairanPoinTukarBarang p = new PencairanPoinTukarBarang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen,
                listItemBarang: [new ItemBarang(produkA, 10), new ItemBarang(produkB, 5)])
            p = repo.buat(p)

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
        PencairanPoinRepository repo = Container.app.pencairanPoinRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(90000, konsumen.jumlahPiutang())
            assertEquals(2000, Container.app.pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE))

            // Melakukan penerimaan untuk salah satu faktur sehingga piutangnya bisa dibayar
            FakturJualOlehSales f = konsumen.listFakturBelumLunas[0]
            Container.app.fakturJualRepository.kirim(f, 'test', 'test')
            Container.app.fakturJualRepository.terima(f, new BuktiTerima(LocalDate.now(), 'test'))
            // Poin bertambah akibat penerimaan
            assertEquals(60, konsumen.poinTerkumpul)

            PencairanPoinPotongPiutang p = new PencairanPoinPotongPiutang(tanggal: LocalDate.now(), konsumen: konsumen, jumlahPoin: 10)
            p = repo.buat(p)

            assertNotNull(p.nomor)
            assertEquals(2000, p.rate)
            assertEquals(konsumen, p.konsumen)
            assertEquals(10, p.jumlahPoin)

            konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            assertEquals(70000, konsumen.jumlahPiutang())
            assertEquals(50000, konsumen.listFakturBelumLunas[0].sisaPiutang(true))
        }
    }

    public void testHapus() {
        PencairanPoinRepository repo = Container.app.pencairanPoinRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
            PencairanPoinTukarUang p = new PencairanPoinTukarUang(tanggal: LocalDate.now(), jumlahPoin: 30, konsumen: konsumen)
            p = repo.buat(p)

            // Sebelum dihapus
            konsumen = findKonsumenById(-1l)
            assertEquals(20, konsumen.poinTerkumpul)

            // Hapus
            p = repo.hapus(p)
            assertEquals('Y', p.deleted)

            // Setelah dihapus
            konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)
        }
    }


}

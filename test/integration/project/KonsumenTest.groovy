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
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.KonsumenRepository
import domain.penjualan.PencairanPoin
import domain.penjualan.PencairanPoinPotongPiutang
import domain.penjualan.PencairanPoinTukarBarang
import domain.penjualan.PencairanPoinTukarUang
import org.joda.time.LocalDate
import simplejpa.testing.DbUnitTestCase

class KonsumenTest extends DbUnitTestCase {

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("konsumen", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testPotongPiutang() {
        KonsumenRepository repo = Container.app.konsumenRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-3l)
            assertEquals(50000, konsumen.jumlahPiutang())
            konsumen.potongPiutang(30000)

            FakturJualOlehSales f = findFakturJualOlehSalesById(-3l)
            assertEquals(30000, f.piutang.jumlahDibayar())
            assertEquals(20000, f.sisaPiutang())
            assertEquals(20000, konsumen.jumlahPiutang())

            konsumen.potongPiutang(20000)
            assertEquals(0, konsumen.listFakturBelumLunas.size())
            assertEquals(0, konsumen.jumlahPiutang())
        }
    }

    public void testCairkanPoinTukarUang() {
        KonsumenRepository repo = Container.app.konsumenRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            assertEquals(50, konsumen.poinTerkumpul)

            BigDecimal rate = Container.app.pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE)
            repo.cairkanPoinTukarUang(konsumen, LocalDate.now(), 30)

            konsumen = findKonsumenById(-1l)
            assertEquals(1, konsumen.listPencairanPoin.size())
            PencairanPoin p = konsumen.listPencairanPoin[0]
            assertEquals(PencairanPoinTukarUang, p.class)
            assertEquals(LocalDate.now(), p.tanggal)
            assertEquals(30, p.jumlahPoin)
            assertEquals(rate, p.rate)
            assertEquals(rate * 30, p.nominal)
            assertEquals(20, konsumen.poinTerkumpul)
        }
    }

    public void testCairkanPoinTukarBarang() {
        KonsumenRepository repo = Container.app.konsumenRepository
        repo.withTransaction {
            Konsumen konsumen = findKonsumenById(-1l)
            Produk produkA = findProdukById(-1l)
            Produk produkB = findProdukById(-2l)
            assertEquals(50, konsumen.poinTerkumpul)

            BigDecimal rate = Container.app.pengaturanRepository.getValue(KeyPengaturan.BONUS_POINT_RATE)
            repo.cairkanPoinTukarBarang(konsumen, LocalDate.now(), 30, [new ItemBarang(produkA, 10), new ItemBarang(produkB, 5)])

            konsumen = findKonsumenById(-1l)
            assertEquals(1, konsumen.listPencairanPoin.size())
            PencairanPoin p = konsumen.listPencairanPoin[0]
            assertEquals(PencairanPoinTukarBarang, p.class)
            assertEquals(LocalDate.now(), p.tanggal)
            assertEquals(20, p.jumlahPoin)
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
        KonsumenRepository repo = Container.app.konsumenRepository
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

            repo.cairkanPoinPotongPiutang(konsumen, LocalDate.now(), 10)

            konsumen = findKonsumenById(-1l)
            assertEquals(1, konsumen.listPencairanPoin.size())
            PencairanPoin p = konsumen.listPencairanPoin[0]
            assertEquals(PencairanPoinPotongPiutang, p.class)
            assertEquals(LocalDate.now(), p.tanggal)
            assertEquals(10, p.jumlahPoin)
            assertEquals(20000, p.nominal)
            assertEquals(50, konsumen.poinTerkumpul)

            // Periksa piutang
            assertEquals(70000, konsumen.jumlahPiutang())
            assertEquals(50000, konsumen.listFakturBelumLunas[0].sisaPiutang(true))
        }
    }


}

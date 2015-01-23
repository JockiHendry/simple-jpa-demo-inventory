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
import domain.inventory.ItemPenyesuaian
import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.ReferensiStok
import project.inventory.PenyesuaianStokRepository
import domain.inventory.Produk
import domain.inventory.StokProduk
import project.inventory.ProdukRepository
import domain.inventory.PeriodeItemStok
import domain.general.Pesan
import domain.general.PesanLevelMinimum
import project.user.PesanRepository
import org.dbunit.dataset.ITable
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase
import domain.inventory.Periode

class ProdukTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ProdukTest)

    ProdukRepository produkRepository
    PesanRepository pesanRepository
    PenyesuaianStokRepository penyesuaianStokRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_inventory.xlsx")
        produkRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Produk')
        pesanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pesan')
        penyesuaianStokRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('PenyesuaianStok')
    }

    public void testStokProduk() {
        Gudang gudang = produkRepository.findGudangByNama("Gudang")
        Gudang warehouseA = produkRepository.findGudangByNama("Warehouse A")
        Gudang warehouseB = produkRepository.findGudangByNama("Warehouse B")
        Gudang warehouseC = produkRepository.findGudangByNama("Warehouse C")
        Gudang warehouseD = produkRepository.findGudangByNama("Warehouse D")
        Gudang warehouseE = produkRepository.findGudangByNama("Warehouse E")
        Gudang warehouseF = produkRepository.findGudangByNama("Warehouse F")

        Produk produk = produkRepository.findProdukByNamaFetchComplete("Produk A")
        assertEquals(7, produk.daftarStok.size())
        assertEquals(10, produk.stok(gudang).jumlah)
        assertEquals(4, produk.stok(warehouseA).jumlah)
        assertEquals(10, produk.stok(warehouseB).jumlah)
        assertEquals(5, produk.stok(warehouseC).jumlah)
        assertEquals(3, produk.stok(warehouseD).jumlah)
        assertEquals(2, produk.stok(warehouseE).jumlah)
        assertEquals(3, produk.stok(warehouseF).jumlah)

        produk = produkRepository.findProdukByNamaFetchComplete("Produk B")
        assertEquals(3, produk.daftarStok.size())
        assertEquals(14, produk.stok(gudang).jumlah)
        assertEquals(6, produk.stok(warehouseA).jumlah)
        assertEquals(7, produk.stok(warehouseB).jumlah)
    }

    public void testPeriodeItemStok() {
        Gudang gudang = produkRepository.findGudangByNama("Gudang")
        Gudang warehouseC = produkRepository.findGudangByNama("Warehouse C")

        Produk produk = produkRepository.findProdukByNamaFetchComplete("Produk A")
        StokProduk stok = produk.stok(gudang)
        assertEquals(3, stok.listPeriodeRiwayat.size())
        assertEquals(3, stok.periode(Periode.format.parseLocalDate('15-12-2013')).jumlah)
        assertEquals(Periode.format.parseLocalDate('01-12-2013'), stok.periode(Periode.format.parseLocalDate('15-12-2013')).tanggalMulai)
        assertEquals(Periode.format.parseLocalDate('31-12-2013'), stok.periode(Periode.format.parseLocalDate('15-12-2013')).tanggalSelesai)
        assertEquals(2, stok.periode(Periode.dari('01-01-2014', '31-01-2014'))[0].jumlah)

        stok = produk.stok(warehouseC)
        assertEquals(5, stok.periode(Periode.dari('01-01-2014', '31-01-2014'))[0].jumlah)

        shouldFail {
            stok.periodeUntukArsip(2)
        }

        assertTrue(stok.periodeUntukArsip(3).isEmpty())

        LocalDate lampau = Periode.format.parseLocalDate('15-01-2010')
        PeriodeItemStok periodeLampau = stok.buatPeriode(lampau)
        assertEquals(Periode.format.parseLocalDate('01-01-2010'), periodeLampau.tanggalMulai)
        assertEquals(Periode.format.parseLocalDate('31-01-2010'), periodeLampau.tanggalSelesai)

        assertNotNull(periodeLampau)
        assertTrue(!stok.periodeUntukArsip(3).isEmpty())
    }

    public void testArsipItemStok() {
        log.debug "Mulai dari testArsipItemStok..."
        log.debug "Mencari produk Z..."
        Produk produk = produkRepository.findProdukByNama("Produk Z", [fetchGraph: 'Produk.Complete'])
        log.debug "Produk ditemukan."

        log.debug "Mencari gudang Gudang"
        Gudang gudang = produkRepository.findGudangByNama("Gudang")
        log.debug "Gudang ditemukan."

        log.debug "Mencari StokProduk untuk produk Z dan Gudang..."
        StokProduk stok = produk.stok(gudang)
        log.debug "Stok ditemukan."

        log.debug "Mencari periode item stok untuk tahun 2010..."
        PeriodeItemStok periodeLampau = stok.periode(Periode.format.parseLocalDate('01-01-2010'))
        log.debug "Periode item stok ditemukan."

        assertNotNull(periodeLampau)
        assertEquals(10, periodeLampau.jumlah)

        log.debug "Memulai proses pengarsipan..."
        produkRepository.arsipItemStok(3)
        log.debug "Proses pengarsipan selesai."

        ITable aktualItemStok = CONNECTION.createQueryTable("AktualItemStok",
                "SELECT * FROM PeriodeItemStok_itemStok WHERE periodeItemStok_Id <> -18")
        assertEquals(22, aktualItemStok.rowCount)

        produkRepository.withTransaction {
            log.debug "Mencari produk Z..."
            produk = produkRepository.findProdukByNama("Produk Z", [fetchGraph: 'Produk.Complete'])
            log.debug "Produk ditemukan."

            log.debug "Mencari gudang Gudang..."
            gudang = produkRepository.findGudangByNama("Gudang")
            log.debug "Gudang ditemukan."

            log.debug "Mencari StokProduk untuk produk Z dan Gudang..."
            stok = produk.stok(gudang)
            log.debug "Stok ditemukan."

            log.debug "Mencari periode item stok untuk tahun 2010..."
            periodeLampau = stok.periode(Periode.format.parseLocalDate('01-01-2010'))
            log.debug "Periode item stok ditemukan."

            assertTrue(periodeLampau.arsip)
            assertEquals(10, periodeLampau.jumlah)

            assertTrue(periodeLampau.listItem.isEmpty())
        }
    }

    public void testLevelMinimum() {
        Produk produk = produkRepository.findProdukById(-1l)
        Gudang gudang = produkRepository.findGudangById(-1l)

        assertTrue(pesanRepository.refresh().isEmpty())

        PenyesuaianStok p = new PenyesuaianStok(tanggal: LocalDate.now(), gudang: gudang, bertambah: false)
        p.tambah(new ItemPenyesuaian(produk, 1))
        penyesuaianStokRepository.buat(p)

        assertTrue(pesanRepository.refresh().isEmpty())

        p = new PenyesuaianStok(tanggal: LocalDate.now(), gudang: gudang, bertambah: false)
        p.tambah(new ItemPenyesuaian(produk, 2))
        penyesuaianStokRepository.buat(p)

        List<Pesan> listPesan = pesanRepository.refresh()
        assertEquals(1, listPesan.size())
        assertTrue(listPesan[0] instanceof PesanLevelMinimum)
        assertEquals(produk, listPesan[0].produk)

        // Menaikkan jumlah stok kembali
        p = new PenyesuaianStok(tanggal: LocalDate.now(), gudang: gudang, bertambah: true)
        p.tambah(new ItemPenyesuaian(produk, 3))
        penyesuaianStokRepository.buat(p)

        // Pesan harus hilang
        assertTrue(pesanRepository.refresh().isEmpty())
    }

    public void testCariSeluruhPenerimaanBarang() {
        Gudang gudang = produkRepository.findGudangByUtama(true)
        Gudang gudang2 = produkRepository.findGudangById(-2l)
        Produk produkAAA = new Produk(nama: 'AAA', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        Produk produkBBB = new Produk(nama: 'BBB', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        produkRepository.withTransaction {
            persist(produkAAA)
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-03-01'), new ReferensiStok(classGudang: 'Transfer'), 40))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkAAA.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
            persist(produkBBB)
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkBBB.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
        }

        List<ItemStok> hasil = produkRepository.cariSeluruhPenerimaan(produkAAA, gudang , LocalDate.now())
        assertEquals(4, hasil.size())
        assertEquals(40, hasil[0].jumlah)
        assertEquals(30, hasil[1].jumlah)
        assertEquals(20, hasil[2].jumlah)
        assertEquals(10, hasil[3].jumlah)

        hasil = produkRepository.cariSeluruhPenerimaan(produkAAA, gudang2, LocalDate.now())
        assertEquals(1, hasil.size())
        assertEquals(40, hasil[0].jumlah)

        hasil = produkRepository.cariSeluruhPenerimaan(gudang, LocalDate.parse('2014-01-01'))
        assertEquals(11, hasil.size())
        assertEquals(4, hasil.findAll { (it[0] == produkAAA) || (it[0] == produkBBB) }.size())
        List hasilAAA = hasil.findAll { it[0] == produkAAA }
        assertEquals(produkAAA, hasilAAA[0][0])
        assertEquals(20,        hasilAAA[0][1].jumlah)
        assertEquals(produkAAA, hasilAAA[1][0])
        assertEquals(10,        hasilAAA[1][1].jumlah)
        List hasilBBB = hasil.findAll { it[0] == produkBBB }
        assertEquals(produkBBB, hasilBBB[0][0])
        assertEquals(20,        hasilBBB[0][1].jumlah)
        assertEquals(produkBBB, hasilBBB[1][0])
        assertEquals(10,        hasilBBB[1][1].jumlah)

        hasil = produkRepository.cariSeluruhPenerimaan(gudang2, LocalDate.parse('2014-01-01'))
        assertEquals(0, hasil.size())
    }

    public void testCariQtyTerakhir() {
        Gudang gudang = produkRepository.findGudangByUtama(true)
        Gudang gudang2 = produkRepository.findGudangById(-2l)
        Produk produkAAA = new Produk(nama: 'AAA', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        Produk produkBBB = new Produk(nama: 'BBB', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        produkRepository.withTransaction {
            persist(produkAAA)
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-03-01'), new ReferensiStok(classGudang: 'Transfer'), 40))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkAAA.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
            persist(produkBBB)
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkBBB.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
        }

        List hasil = produkRepository.cariQtyTerakhir(LocalDate.parse('2014-01-01'))
        assertEquals(70, hasil.find { it[0] == produkAAA}[1])
        assertEquals(30, hasil.find { it[0] == produkBBB}[1])

        hasil = produkRepository.cariQtyTerakhir(LocalDate.now())
        assertEquals(210, hasil.find { it[0] == produkAAA}[1])
        assertEquals(170, hasil.find { it[0] == produkBBB}[1])
    }

    public void testCariSeluruhPerubahan() {
        Gudang gudang = produkRepository.findGudangByUtama(true)
        Gudang gudang2 = produkRepository.findGudangById(-2l)
        Produk produkAAA = new Produk(nama: 'AAA', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        Produk produkBBB = new Produk(nama: 'BBB', satuan: produkRepository.findSatuanById(-1l), hargaDalamKota: 1111, hargaLuarKota: 1111)
        produkRepository.withTransaction {
            persist(produkAAA)
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-03-01'), new ReferensiStok(classGudang: 'Transfer'), 40))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkAAA.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkAAA.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
            persist(produkBBB)
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-01-01'), null, 10))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2013-02-01'), null, 20))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), null, 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-01-01'), new ReferensiStok(classGudang: 'Transfer'), 30))
            produkBBB.perubahanStok(gudang, new ItemStok(LocalDate.parse('2014-02-01'), null, 40))
            produkBBB.perubahanStok(gudang2, new ItemStok(LocalDate.parse('2014-03-01'), null, 40))
        }

        List<ItemStok> hasil = produkRepository.cariSeluruhPerubahan(produkAAA, LocalDate.parse('2014-01-01'), LocalDate.now())
        assertEquals(4, hasil.size())
        assertEquals(30, hasil[0].jumlah)
        assertEquals(30, hasil[1].jumlah)
        assertEquals(40, hasil[2].jumlah)
        assertEquals(40, hasil[3].jumlah)

        hasil = produkRepository.cariSeluruhPerubahan(produkAAA, LocalDate.parse('2013-01-01'), LocalDate.parse('2013-12-01'))
        assertEquals(3, hasil.size())
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)
        assertEquals(40, hasil[2].jumlah)

        hasil = produkRepository.cariSeluruhPerubahan(produkBBB, LocalDate.parse('2013-01-01'), LocalDate.parse('2013-12-01'))
        assertEquals(2, hasil.size())
        assertEquals(10, hasil[0].jumlah)
        assertEquals(20, hasil[1].jumlah)

        hasil = produkRepository.cariSeluruhPerubahan(LocalDate.parse('2013-01-01'), LocalDate.parse('2013-12-01'))
        List hasilAAA = hasil.findAll { it[0] == produkAAA }
        assertEquals(3,  hasilAAA.size())
        assertEquals(10, hasilAAA[0][1].jumlah)
        assertEquals(20, hasilAAA[1][1].jumlah)
        assertEquals(40, hasilAAA[2][1].jumlah)
        List hasilBBB = hasil.findAll { it[0] == produkBBB }
        assertEquals(2,  hasilBBB.size())
        assertEquals(10, hasilBBB[0][1].jumlah)
        assertEquals(20, hasilBBB[1][1].jumlah)
    }
}

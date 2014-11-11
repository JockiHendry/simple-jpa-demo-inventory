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

import domain.exception.DataTidakKonsisten
import domain.exception.FakturTidakDitemukan
import domain.faktur.ItemFaktur
import domain.faktur.Referensi
import domain.inventory.DaftarBarang
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.Supplier
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import domain.penjualan.Sales
import domain.retur.ItemRetur
import domain.retur.KlaimPotongPiutang
import domain.retur.Klaim
import domain.retur.KlaimServis
import domain.retur.KlaimTukar
import domain.retur.ReturJual
import domain.retur.ReturJualOlehSales
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.user.NomorService
import simplejpa.SimpleJpaUtil
import griffon.core.*

class ReturJualTests extends GriffonUnitTestCase {

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
        MockGriffonApplication app = new MockGriffonApplication()
        app.serviceManager = new ServiceManager() {

            NomorService nomorService = new NomorService()

            @Override
            Map<String, GriffonService> getServices() {
                [:]
            }

            @Override
            GriffonService findService(String name) {
                if (name == 'Nomor') {
                    return nomorService
                }
                null
            }

            @Override
            GriffonApplication getApp() {
                return null
            }
        }
        ApplicationHolder.application = app
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testTukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(nama: 'Produk A', supplier: supplier)
        Produk produk2 = new Produk(nama: 'Produk B', supplier: supplier)
        Produk produk3 = new Produk(nama: 'Produk C', supplier: supplier)
        Konsumen konsumen = new Konsumen()
        ReturJual retur = new ReturJualOlehSales(konsumen: konsumen, gudang: gudangUtama)
        retur.tambah(new ItemRetur(produk1, 1, [new KlaimTukar(produk1, 1)] as Set))
        retur.tambah(new ItemRetur(produk2, 3, [new KlaimTukar(produk2, 3)] as Set))
        retur.tambah(new ItemRetur(produk3, 5, [new KlaimPotongPiutang(1000)] as Set))

        PengeluaranBarang pengeluaranBarang = retur.tukar()
        assertTrue(retur.getKlaimsTukar(true).empty)
        assertTrue(pengeluaranBarang.sudahDiterima())
        List<ItemBarang> items = pengeluaranBarang.items
        assertEquals(2, items.size())
        assertEquals(produk1, items[0].produk)
        assertEquals(1, items[0].jumlah)
        assertEquals(produk2, items[1].produk)
        assertEquals(3, items[1].jumlah)
    }

    public void testGetKlaimPotongan() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJualOlehSales()
        retur.tambah(new ItemRetur(produk1, 1, [new KlaimPotongPiutang(10000)] as Set))
        retur.tambah(new ItemRetur(produk2, 3, [new KlaimPotongPiutang(20000)] as Set))
        retur.tambah(new ItemRetur(produk3, 5, [new KlaimTukar(produk1, 1)] as Set))

        Set<Klaim> hasil = retur.getKlaimsPotongPiutang()
        assertEquals(2, hasil.size())
        assertEquals(10000, hasil[0].jumlah)
        assertEquals(20000, hasil[1].jumlah)
    }

    public void testGetKlaimTukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJualOlehSales()
        retur.tambah(new ItemRetur(produk1, 1, [new KlaimTukar(produk1, 1)] as Set))
        retur.tambah(new ItemRetur(produk2, 3, [new KlaimPotongPiutang(1000)] as Set))
        retur.tambah(new ItemRetur(produk3, 5, [new KlaimPotongPiutang(1000)] as Set))

        Set<Klaim> hasil = retur.getKlaimsTukar()
        assertEquals(1, hasil.size())
        assertEquals(produk1, hasil[0].produk)
        assertEquals(1, hasil[0].jumlah)
        assertFalse(hasil[0].sudahDiproses)
    }

    public void testPotongPiutang() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen konsumen = new Konsumen(sales: sales)
        Produk produk1 = new Produk(jumlah: 10)
        Produk produk2 = new Produk(jumlah: 20)
        FakturJualOlehSales f = new FakturJualOlehSales(nomor: 'F-01', konsumen: konsumen)
        f.tambah(new ItemFaktur(produk: produk1, jumlah: 10, harga: 10000))
        f.tambah(new ItemFaktur(produk: produk2, jumlah: 20, harga: 20000))
        f.kirim("test")
        f.tambah(new BuktiTerima())
        konsumen.tambahFakturBelumLunas(f)
        assertEquals(500000, f.total())
        assertEquals(500000, konsumen.jumlahPiutang())

        ReturJual r = new ReturJualOlehSales(konsumen: konsumen)
        r.tambah(new ItemRetur(produk1, 10, [new KlaimPotongPiutang(250000)] as Set))
        assertEquals(250000, r.sisaPotongPiutang())
        r.potongPiutang()
        assertEquals(0, r.sisaPotongPiutang())
        assertEquals(250000, konsumen.jumlahPiutang())
        assertEquals(1, r.fakturPotongPiutang.size())
        assertTrue(r.fakturPotongPiutang.contains(new Referensi(FakturJualOlehSales, 'F-01')))
    }

    public void testToDaftarBarang() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')
        ReturJual r = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now().minusDays(1), keterangan: 'TEST')
        r.tambah(new ItemRetur(produkA, 10, [new KlaimPotongPiutang(1000)] as Set))
        r.tambah(new ItemRetur(produkA, 20, [new KlaimPotongPiutang(2000)] as Set))
        r.tambah(new ItemRetur(produkB, 30, [new KlaimPotongPiutang(3000)] as Set))
        r.tambah(new ItemRetur(produkB, 10, [new KlaimPotongPiutang(4000)] as Set))
        r.tambah(new ItemRetur(produkC, 10, [new KlaimPotongPiutang(5000)] as Set))
        assertEquals(15000, r.jumlahPotongPiutang())
        assertEquals(15000, r.sisaPotongPiutang())

        DaftarBarang d = r.toDaftarBarang()
        assertEquals('R-01', d.nomor)
        assertEquals(LocalDate.now().minusDays(1), d.tanggal)
        assertEquals('TEST', d.keterangan)
        assertEquals(3, d.items.size())
        assertEquals(produkA, d.items[0].produk)
        assertEquals(30, d.items[0].jumlah)
        assertEquals(produkB, d.items[1].produk)
        assertEquals(40, d.items[1].jumlah)
        assertEquals(produkC, d.items[2].produk)
        assertEquals(10, d.items[2].jumlah)
    }

    public void testItemReturGetKlaims() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        ReturJual r = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now().minusDays(1), keterangan: 'TEST')

        def klaimPotongPiutang = new KlaimPotongPiutang(jumlah: 1000)
        def klaimTukar1 = new KlaimTukar(id: 2, produk: produkA, jumlah: 5)
        def klaimTukar2 = new KlaimTukar(id: 3, produk: produkB, jumlah: 1, sudahDiproses: true)

        r.tambah(new ItemRetur(produkA, 10, [klaimPotongPiutang, klaimTukar1, klaimTukar2] as Set))

        def hasil = r.items[0].getKlaims(KlaimPotongPiutang)
        assertEquals(1, hasil.size())
        assertTrue(hasil.contains(klaimPotongPiutang))

        hasil = r.items[0].getKlaims(KlaimTukar)
        assertEquals(2, hasil.size())
        assertTrue(hasil.contains(klaimTukar2))
        assertTrue(hasil.contains(klaimTukar1))

        hasil = r.items[0].getKlaims(KlaimTukar, true)
        assertEquals(1, hasil.size())
        assertTrue(hasil.contains(klaimTukar1))
    }

    public void testItemReturJumlahBarangDitukar() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')
        def klaimTukar1 = new KlaimTukar(produk: produkA, jumlah: 5)
        def klaimTukar2 = new KlaimTukar(produk: produkB, jumlah: 1, sudahDiproses: true)
        def klaimTukar3 = new KlaimTukar(produk: produkB, jumlah: 19)

        ItemRetur itemRetur1 = new ItemRetur(produkA, 10, [klaimTukar1] as Set)
        assertEquals(5, itemRetur1.jumlahBarangDitukar())

        ItemRetur itemRetur2 = new ItemRetur(produkB, 10, [klaimTukar2, klaimTukar3] as Set)
        assertEquals(20, itemRetur2.jumlahBarangDitukar())

        ItemRetur itemRetur3 = new ItemRetur(produkC, 10)
        assertEquals(0, itemRetur3.jumlahBarangDitukar())
    }

    public void testGetKlaim() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')
        ReturJual r = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now().minusDays(1), keterangan: 'TEST')

        def potongPiutang1 = new KlaimPotongPiutang(1000)
        def potongPiutang2 = new KlaimPotongPiutang(2000)
        def potongPiutang3 = new KlaimPotongPiutang(3000)
        def klaimTukar1 = new KlaimTukar(id: 4, produk: produkA, jumlah: 5)
        def klaimTukar2 = new KlaimTukar(id: 5, produk: produkB, jumlah: 1, sudahDiproses: true)
        def klaimTukar3 = new KlaimTukar(id: 6, produk: produkB, jumlah: 19)
        def klaimTukar4 = new KlaimTukar(id: 7, produk: produkA, jumlah: 10)
        def klaimTukar5 = new KlaimTukar(id: 8, produk: produkB, jumlah: 10)
        def klaimTukar6 = new KlaimTukar(id: 9, produk: produkC, jumlah: 10)

        r.tambah(new ItemRetur(produkA, 10, [potongPiutang1, klaimTukar1, klaimTukar2] as Set))
        r.tambah(new ItemRetur(produkB, 20, [potongPiutang2, klaimTukar3] as Set))
        r.tambah(new ItemRetur(produkC, 30, [potongPiutang3, klaimTukar4, klaimTukar5, klaimTukar6] as Set))

        def hasil = r.getKlaimsPotongPiutang()
        assertEquals(3, hasil.size())
        assertTrue(hasil.contains(potongPiutang1))
        assertTrue(hasil.contains(potongPiutang2))
        assertTrue(hasil.contains(potongPiutang3))

        hasil = r.getKlaimsTukar()
        assertEquals(6, hasil.size())
        assertTrue(hasil.contains(klaimTukar1))
        assertTrue(hasil.contains(klaimTukar2))
        assertTrue(hasil.contains(klaimTukar3))
        assertTrue(hasil.contains(klaimTukar4))
        assertTrue(hasil.contains(klaimTukar5))
        assertTrue(hasil.contains(klaimTukar6))

        hasil = r.getKlaimsTukar(true)
        assertEquals(5, hasil.size())
        assertTrue(hasil.contains(klaimTukar1))
        assertTrue(hasil.contains(klaimTukar3))
        assertTrue(hasil.contains(klaimTukar4))
        assertTrue(hasil.contains(klaimTukar5))
        assertTrue(hasil.contains(klaimTukar6))
    }

    void testItemReturMerge() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        def potongPiutang1 = new KlaimPotongPiutang(1000)
        def potongPiutang2 = new KlaimPotongPiutang(2000)
        def potongPiutang3 = new KlaimPotongPiutang(3000)
        def klaimTukar1 = new KlaimTukar(id: 4, produk: produkA, jumlah: 5)
        def klaimTukar2 = new KlaimTukar(id: 5, produk: produkA, jumlah: 19)

        ItemRetur i1 = new ItemRetur(produkA, 10, [potongPiutang1, klaimTukar1] as Set)
        ItemRetur i2 = new ItemRetur(produkA, 20, [potongPiutang2, klaimTukar2] as Set)
        i1.merge(i2)
        assertEquals(produkA, i1.produk)
        assertEquals(30, i1.jumlah)
        assertEquals(2, i1.klaims.size())
        assertNotNull(i1.klaims.find { (it instanceof KlaimPotongPiutang) && it.jumlah == 3000})
        assertNotNull(i1.klaims.find { (it instanceof KlaimTukar) && it.jumlah == 24})

        ItemRetur i3 = new ItemRetur(produkA, 30, [potongPiutang3] as Set)
        i1 << i3
        assertEquals(produkA, i1.produk)
        assertEquals(60, i1.jumlah)
        assertEquals(2, i1.klaims.size())
        assertNotNull(i1.klaims.find { (it instanceof KlaimPotongPiutang) && it.jumlah == 6000})
        assertNotNull(i1.klaims.find { (it instanceof KlaimTukar) && it.jumlah == 24})

        ItemRetur i4 = new ItemRetur(produkB, 40)
        shouldFail(IllegalArgumentException) {
            i1 << i4
        }
    }

    void testProdukYangSama() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')
        ReturJual r = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now().minusDays(1), keterangan: 'TEST')
        def potongPiutang1 = new KlaimPotongPiutang(1000)
        def potongPiutang2 = new KlaimPotongPiutang(2000)
        def potongPiutang3 = new KlaimPotongPiutang(3000)
        def klaimTukar1 = new KlaimTukar(id: 4, produk: produkA, jumlah: 5)
        def klaimTukar3 = new KlaimTukar(id: 6, produk: produkB, jumlah: 10)
        def klaimTukar4 = new KlaimTukar(id: 7, produk: produkA, jumlah: 10)
        def klaimTukar5 = new KlaimTukar(id: 8, produk: produkB, jumlah: 3)
        def klaimTukar6 = new KlaimTukar(id: 9, produk: produkC, jumlah: 10)

        r.tambah(new ItemRetur(produkA, 10, [potongPiutang1, klaimTukar1] as Set))
        r.tambah(new ItemRetur(produkB, 20, [potongPiutang2, klaimTukar3] as Set))
        r.tambah(new ItemRetur(produkC, 30, [klaimTukar4] as Set))
        r.tambah(new ItemRetur(produkA, 5, [potongPiutang3, klaimTukar5] as Set))
        r.tambah(new ItemRetur(produkB, 10, [klaimTukar6] as Set))

        List<ItemRetur> hasil = r.normalisasi()
        assertEquals(3, hasil.size())
        assertEquals(produkA, hasil[0].produk)
        assertEquals(15, hasil[0].jumlah)
        assertEquals(3, hasil[0].klaims.size())
        assertNotNull(hasil[0].klaims.find { (it instanceof KlaimPotongPiutang) && (it.jumlah == 4000 )})
        assertTrue(hasil[0].klaims.containsAll([klaimTukar1, klaimTukar5]))
        assertEquals(produkB, hasil[1].produk)
        assertEquals(30, hasil[1].jumlah)
        assertEquals(3, hasil[1].klaims.size())
        assertNotNull(hasil[1].klaims.containsAll([potongPiutang2, klaimTukar3, klaimTukar6]))
        assertEquals(produkC, hasil[2].produk)
        assertEquals(30, hasil[2].jumlah)
        assertTrue(hasil[2].klaims.containsAll([klaimTukar4]))
    }

    void testHapusSemuaKlaimPotongPiutang() {
        ItemRetur i = new ItemRetur()
        i.tambahKlaim(new KlaimPotongPiutang(1000))
        i.tambahKlaim(new KlaimPotongPiutang(2000))
        i.tambahKlaim(new KlaimPotongPiutang(3000))
        i.hapusSemuaKlaimPotongPiutang()

        assertTrue(i.getKlaims(KlaimPotongPiutang).empty)
        assertEquals(0, i.jumlahPotongPiutang())
    }

    void testHapusPengeluaranBarang() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(nama: 'Produk A', supplier: supplier)
        Produk produk2 = new Produk(nama: 'Produk B', supplier: supplier)
        Produk produk3 = new Produk(nama: 'Produk C', supplier: supplier)
        Konsumen konsumen = new Konsumen()
        ReturJualOlehSales retur = new ReturJualOlehSales(konsumen: konsumen, gudang: gudangUtama)
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 3), new KlaimTukar(produk1, 7)] as Set))
        retur.tambah(new ItemRetur(produk2, 20, [new KlaimTukar(produk2, 20)] as Set))
        retur.tambah(new ItemRetur(produk3, 30, [new KlaimTukar(produk3, 25)] as Set))
        retur.tukar()

        // Hapus
        retur.hapusPenukaran()
        assertFalse(retur.sudahDiproses)
        assertNull(retur.pengeluaranBarang)
        DaftarBarang d = retur.yangHarusDitukar()
        assertEquals(3, d.items.size())
        assertEquals(new ItemBarang(produk1, 10), d.items[0])
        assertEquals(new ItemBarang(produk2, 20), d.items[1])
        assertEquals(new ItemBarang(produk3, 25), d.items[2])
        assertTrue(retur.items[0].klaims.every { !it.sudahDiproses })
        assertTrue(retur.items[1].klaims.every { !it.sudahDiproses })
        assertTrue(retur.items[2].klaims.every { !it.sudahDiproses })
    }

    void testHapusReferensiFaktur() {
        ReturJualOlehSales r = new ReturJualOlehSales()
        r.fakturPotongPiutang = [new Referensi(FakturJualOlehSales, 'F-001'), new Referensi(FakturJualOlehSales, 'F-002')] as Set
        r.hapusReferensiFaktur('F-001')
        assertEquals(1, r.fakturPotongPiutang.size())
        assertTrue(r.fakturPotongPiutang.contains(new Referensi(FakturJualOlehSales, 'F-002')))
        r.hapusReferensiFaktur('F-002')
        assertTrue(r.fakturPotongPiutang.empty)
        shouldFail(FakturTidakDitemukan) {
            r.hapusReferensiFaktur('F-003')
        }
    }

    void testNormalisasi() {
        Produk produk1 = new Produk(nama: 'Produk A')
        Produk produk2 = new Produk(nama: 'Produk B')
        ReturJualOlehSales retur = new ReturJualOlehSales()
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 3), new KlaimTukar(produk1, 7)] as Set))
        retur.tambah(new ItemRetur(produk1, 20, [new KlaimTukar(produk1, 20)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))
        List<ItemRetur> hasil = retur.normalisasi()
        assertEquals(2, hasil.size())
        assertEquals(produk1, hasil[0].produk)
        assertEquals(30, hasil[0].jumlah)
        assertEquals(2, hasil[0].klaims.size())
        assertEquals(30, hasil[0].jumlahBarangDitukar())
        assertEquals(produk2, hasil[1].produk)
        assertEquals(10, hasil[1].jumlah)
        assertEquals(1, hasil[1].klaims.size())
        assertTrue(hasil[1].klaims.contains(new KlaimTukar(produk2, 10)))

        retur = new ReturJualOlehSales()
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 10)] as Set))
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 10)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimTukar(produk2, 10)] as Set))
        hasil = retur.normalisasi()
        assertEquals(2, hasil.size())
        assertEquals(produk1, hasil[0].produk)
        assertEquals(20, hasil[0].jumlah)
        assertEquals(1, hasil[0].klaims.size())
        assertEquals(20, hasil[0].jumlahBarangDitukar())
        assertEquals(produk2, hasil[1].produk)
        assertEquals(10, hasil[1].jumlah)
        assertEquals(1, hasil[1].klaims.size())
        assertTrue(hasil[1].klaims.contains(new KlaimTukar(produk2, 10)))
    }

    void testGetDaftarBarangServis() {
        Produk produk1 = new Produk(nama: 'Produk A')
        Produk produk2 = new Produk(nama: 'Produk B')
        ReturJualOlehSales retur = new ReturJualOlehSales()
        retur.tambah(new ItemRetur(produk1, 10, [new KlaimServis(produk1, 10)] as Set))
        retur.tambah(new ItemRetur(produk1, 20, [new KlaimServis(produk1, 20)] as Set))
        retur.tambah(new ItemRetur(produk2, 10, [new KlaimServis(produk2, 10)] as Set))

        DaftarBarang d = retur.getDaftarBarangServis()
        assertFalse(d.items.empty)
        assertEquals(-1, d.faktor())
        assertEquals(2, d.items.size())
        assertEquals(produk1, d.items[0].produk)
        assertEquals(30, d.items[0].jumlah)
        assertEquals(produk2, d.items[1].produk)
        assertEquals(10, d.items[1].jumlah)
    }
}

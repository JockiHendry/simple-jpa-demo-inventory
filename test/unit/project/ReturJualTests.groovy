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

import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.Supplier
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import domain.penjualan.Sales
import domain.retur.BarangRetur
import domain.retur.ReturBeli
import domain.retur.ReturJual
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import griffon.util.ApplicationHolder
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

    public void testKlaimSatuan() {
        Produk produk1 = new Produk()
        Produk produk2 = new Produk()
        Produk produk3 = new Produk()
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk1, 1))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))

        assertFalse(retur.getBelumDiklaim().empty)
        assertEquals(0, retur.items[0].jumlahDiKlaim)
        assertNull(retur.items[0].nomorKlaim)
        assertEquals(0, retur.items[1].jumlahDiKlaim)
        assertNull(retur.items[1].nomorKlaim)
        assertEquals(0, retur.items[2].jumlahDiKlaim)
        assertNull(retur.items[2].nomorKlaim)

        retur.prosesKlaim(produk1, 1, "KLAIM-1")
        assertEquals(2, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertFalse(retur.sudahDiklaim)

        retur.prosesKlaim(produk2, 3, "KLAIM-2")
        assertEquals(1, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-2", retur.items[1].nomorKlaim)
        assertFalse(retur.sudahDiklaim)

        retur.prosesKlaim(produk3, 4, "KLAIM-3")
        assertEquals(1, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-2", retur.items[1].nomorKlaim)
        assertEquals(produk3, retur.items[2].produk)
        assertEquals(5, retur.items[2].jumlah)
        assertEquals(4, retur.items[2].jumlahDiKlaim)
        assertEquals("KLAIM-3", retur.items[2].nomorKlaim)
        assertFalse(retur.sudahDiklaim)

        retur.prosesKlaim(produk3, 1, "KLAIM-4")
        assertEquals(0, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-2", retur.items[1].nomorKlaim)
        assertEquals(produk3, retur.items[2].produk)
        assertEquals(5, retur.items[2].jumlah)
        assertEquals(5, retur.items[2].jumlahDiKlaim)
        assertEquals("KLAIM-3,KLAIM-4", retur.items[2].nomorKlaim)
        assertTrue(retur.sudahDiklaim)
    }

    public void testKlaim() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual(nomor: 'RETUR-1')
        retur.tambah(new BarangRetur(produk1, 1))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))

        assertFalse(retur.getBelumDiklaim().empty)
        assertEquals(0, retur.items[0].jumlahDiKlaim)
        assertNull(retur.items[0].nomorKlaim)
        assertEquals(0, retur.items[1].jumlahDiKlaim)
        assertNull(retur.items[1].nomorKlaim)
        assertEquals(0, retur.items[2].jumlahDiKlaim)
        assertNull(retur.items[2].nomorKlaim)

        ReturBeli returBeli = new ReturBeli(nomor: 'KLAIM-1', supplier: supplier)
        returBeli.tambah(new BarangRetur(produk1, 1))
        returBeli.tambah(new BarangRetur(produk2, 3))
        returBeli.tambah(new BarangRetur(produk3, 4))

        retur.klaim(returBeli)
        assertEquals(1, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[1].nomorKlaim)
        assertEquals(produk3, retur.items[2].produk)
        assertEquals(5, retur.items[2].jumlah)
        assertEquals(4, retur.items[2].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[2].nomorKlaim)
        assertFalse(retur.sudahDiklaim)

        ReturBeli returBeli1 = new ReturBeli(nomor: 'KLAIM-2', supplier: supplier)
        returBeli1.tambah(new BarangRetur(produk3, 1))

        retur.klaim(returBeli1)
        assertEquals(0, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[1].nomorKlaim)
        assertEquals(produk3, retur.items[2].produk)
        assertEquals(5, retur.items[2].jumlah)
        assertEquals(5, retur.items[2].jumlahDiKlaim)
        assertEquals("KLAIM-1,KLAIM-2", retur.items[2].nomorKlaim)
        assertTrue(retur.sudahDiklaim)
    }

    public void testKlaimSupplierBerbeda() {
        Supplier supplier1 = new Supplier()
        Produk produk1 = new Produk(supplier: supplier1)
        Produk produk2 = new Produk(supplier: supplier1)
        Produk produk3 = new Produk(supplier: supplier1)
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk1, 1))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))

        Supplier supplier2 = new Supplier()
        ReturBeli klaim = new ReturBeli(supplier: supplier2)
        shouldFail(UnsupportedOperationException) {
            retur.klaim(klaim)
        }
    }

    public void testKlaimAdaTukarBarang() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        Produk produk4 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk1, 1))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))
        retur.tambah(new BarangRetur(produk: produk4, jumlah: 2, tukar: true))

        assertFalse(retur.getBelumDiklaim().empty)
        assertEquals(0, retur.items[0].jumlahDiKlaim)
        assertNull(retur.items[0].nomorKlaim)
        assertEquals(0, retur.items[1].jumlahDiKlaim)
        assertNull(retur.items[1].nomorKlaim)
        assertEquals(0, retur.items[2].jumlahDiKlaim)
        assertNull(retur.items[2].nomorKlaim)

        ReturBeli klaim1 = new ReturBeli(nomor: 'KLAIM-1', supplier: supplier)
        klaim1.tambah(new BarangRetur(produk1, 1))
        klaim1.tambah(new BarangRetur(produk2, 3))
        klaim1.tambah(new BarangRetur(produk3, 5))

        retur.klaim(klaim1)
        assertEquals(0, retur.getBelumDiklaim().size())
        assertEquals(produk1, retur.items[0].produk)
        assertEquals(1, retur.items[0].jumlah)
        assertEquals(1, retur.items[0].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[0].nomorKlaim)
        assertEquals(produk2, retur.items[1].produk)
        assertEquals(3, retur.items[1].jumlah)
        assertEquals(3, retur.items[1].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[1].nomorKlaim)
        assertEquals(produk3, retur.items[2].produk)
        assertEquals(5, retur.items[2].jumlah)
        assertEquals(5, retur.items[2].jumlahDiKlaim)
        assertEquals("KLAIM-1", retur.items[2].nomorKlaim)
        assertTrue(retur.sudahDiklaim)
    }

    public void testTukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk: produk1, jumlah: 1, tukar: true))
        retur.tambah(new BarangRetur(produk: produk2, jumlah: 3, tukar: true))
        retur.tambah(new BarangRetur(produk3, 5))

        PengeluaranBarang pengeluaranBarang = retur.tukarBaru()
        assertFalse(pengeluaranBarang.sudahDiterima())
        List<ItemBarang> items = pengeluaranBarang.items
        assertEquals(2, items.size())
        assertEquals(produk1, items[0].produk)
        assertEquals(1, items[0].jumlah)
        assertEquals(produk2, items[1].produk)
        assertEquals(3, items[1].jumlah)
    }

    public void testYangBelumDiklaim() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk: produk1, jumlah: 1, tukar: true))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))

        List<BarangRetur> hasil = retur.getBelumDiklaim()
        assertEquals(2, hasil.size())
        assertEquals(produk2, hasil[0].produk)
        assertEquals(3, hasil[0].jumlah)
        assertEquals(produk3, hasil[1].produk)
        assertEquals(5, hasil[1].jumlah)
    }

    public void testGetBarangDitukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual()
        retur.tambah(new BarangRetur(produk: produk1, jumlah: 1, tukar: true))
        retur.tambah(new BarangRetur(produk2, 3))
        retur.tambah(new BarangRetur(produk3, 5))

        List<BarangRetur> hasil = retur.getBarangDitukar()
        assertEquals(1, hasil.size())
        assertEquals(produk1, hasil[0].produk)
        assertEquals(1, hasil[0].jumlah)
    }

    public void testPotongPiutang() {
        Gudang gudang = new Gudang()
        Sales sales = new Sales(gudang: gudang)
        Konsumen konsumen = new Konsumen(sales: sales)
        Produk produk1 = new Produk(jumlah: 10)
        Produk produk2 = new Produk(jumlah: 20)
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen)
        f.tambah(new ItemFaktur(produk: produk1, jumlah: 10, harga: 10000))
        f.tambah(new ItemFaktur(produk: produk2, jumlah: 20, harga: 20000))
        f.kirim("test", "test")
        f.tambah(new BuktiTerima())
        konsumen.tambahFakturBelumLunas(f)
        assertEquals(500000, f.total())
        assertEquals(500000, konsumen.jumlahPiutang())

        ReturJual r = new ReturJual(konsumen: konsumen, potongan: 250000)
        assertEquals(250000, r.sisaPotongan())
        r.potongPiutang(100000)
        assertEquals(100000, r.potonganCair)
        assertEquals(150000, r.sisaPotongan())
        assertEquals(400000, konsumen.jumlahPiutang())
        r.potongPiutang(50000)
        assertEquals(150000, r.potonganCair)
        assertEquals(100000, r.sisaPotongan())
        assertEquals(350000, konsumen.jumlahPiutang())
    }

}

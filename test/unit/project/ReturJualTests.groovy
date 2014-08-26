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
import domain.retur.KlaimRetur
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

    public void testTukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        Konsumen konsumen = new Konsumen()
        ReturJual retur = new ReturJual(konsumen: konsumen)
        retur.tambah(new ItemBarang(produk1, 1))
        retur.tambah(new ItemBarang(produk2, 3))
        retur.tambah(new ItemBarang(produk3, 5))
        retur.tambahKlaimTukar(produk1, 1)
        retur.tambahKlaimTukar(produk2, 3)

        PengeluaranBarang pengeluaranBarang = retur.tukar()
        assertTrue(retur.getKlaimTukar(true).empty)
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
        ReturJual retur = new ReturJual()
        retur.tambah(new ItemBarang(produk1, 1))
        retur.tambah(new ItemBarang(produk2, 3))
        retur.tambah(new ItemBarang(produk3, 5))
        retur.tambahKlaimPotongan(10000)
        retur.tambahKlaimPotongan(20000)

        List<KlaimRetur> hasil = retur.getKlaimPotongan()
        assertEquals(2, hasil.size())
        assertEquals(10000, hasil[0].potongan)
        assertEquals(20000, hasil[1].potongan)
    }

    public void testGetKlaimTukar() {
        Supplier supplier = new Supplier()
        Produk produk1 = new Produk(supplier: supplier)
        Produk produk2 = new Produk(supplier: supplier)
        Produk produk3 = new Produk(supplier: supplier)
        ReturJual retur = new ReturJual()
        retur.tambah(new ItemBarang(produk1, 1))
        retur.tambah(new ItemBarang(produk2, 3))
        retur.tambah(new ItemBarang(produk3, 5))
        retur.tambahKlaimTukar(produk1, 1)

        List<KlaimRetur> hasil = retur.getKlaimTukar()
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
        FakturJualOlehSales f = new FakturJualOlehSales(konsumen)
        f.tambah(new ItemFaktur(produk: produk1, jumlah: 10, harga: 10000))
        f.tambah(new ItemFaktur(produk: produk2, jumlah: 20, harga: 20000))
        f.kirim("test", "test")
        f.tambah(new BuktiTerima())
        konsumen.tambahFakturBelumLunas(f)
        assertEquals(500000, f.total())
        assertEquals(500000, konsumen.jumlahPiutang())

        ReturJual r = new ReturJual(konsumen: konsumen)
        r.tambahKlaimPotongan(250000)
        assertEquals(250000, r.sisaPotongan())
        r.potongPiutang()
        assertEquals(0, r.sisaPotongan())
        assertEquals(250000, konsumen.jumlahPiutang())
    }
    
}

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

import domain.inventory.DaftarBarang
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import domain.retur.Kemasan
import domain.retur.ReturBeli
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.user.NomorService
import simplejpa.SimpleJpaUtil

class ReturBeliTests extends GriffonUnitTestCase {

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

    public void testTukarBaru() {
        Produk produk1 = new Produk(nama: 'Produk A')
        Produk produk2 = new Produk(nama: 'Produk B')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)

        Kemasan klaimKemasan1 = new Kemasan()
        klaimKemasan1.tambah(new ItemBarang(produk1, 5))
        returBeli.tambah(klaimKemasan1)
        Kemasan klaimKemasan2 = new Kemasan()
        klaimKemasan2.tambah(new ItemBarang(produk1, 5))
        klaimKemasan2.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(klaimKemasan2)

        PenerimaanBarang penerimaanBarang = returBeli.terima()
        assertEquals(penerimaanBarang, returBeli.penerimaanBarang)
        assertNotNull(penerimaanBarang.nomor)
        assertEquals(LocalDate.now(), penerimaanBarang.tanggal)
        assertEquals(gudangUtama, penerimaanBarang.gudang)
        assertEquals(2, penerimaanBarang.items.size())
        assertEquals(produk1, penerimaanBarang.items[0].produk)
        assertEquals(10, penerimaanBarang.items[0].jumlah)
        assertEquals(produk2, penerimaanBarang.items[1].produk)
        assertEquals(20, penerimaanBarang.items[1].jumlah)
    }

    public void testTambahKemasanRetur() {
        Produk produk1 = new Produk('Produk A')
        Produk produk2 = new Produk('Produk B')
        Produk produk3 = new Produk('Produk C')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)

        Kemasan kemasan1 = new Kemasan()
        kemasan1.tambah(new ItemBarang(produk1, 10))
        kemasan1.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(kemasan1)
        assertEquals(1, returBeli.items.size())
        assertEquals(1, returBeli.items[0].nomor)

        Kemasan kemasan2 = new Kemasan()
        kemasan2.tambah(new ItemBarang(produk1, 5))
        kemasan2.tambah(new ItemBarang(produk2, 6))
        returBeli.tambah(kemasan2)
        assertEquals(2, returBeli.items.size())
        assertEquals(2, returBeli.items[1].nomor)
        assertEquals(2, returBeli.items.size())

        Kemasan kemasan3 = new Kemasan()
        kemasan3.tambah(new ItemBarang(produk2, 5))
        kemasan3.tambah(new ItemBarang(produk3, 10))
        returBeli.tambah(kemasan3)
        assertEquals(3, returBeli.items.size())
        assertEquals(3, returBeli.items[2].nomor)
        assertEquals(3, returBeli.items.size())
    }

    public void testHapusKemasanRetur() {
        Produk produk1 = new Produk('Produk A')
        Produk produk2 = new Produk('Produk B')
        Produk produk3 = new Produk('Produk C')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)

        Kemasan kemasan1 = new Kemasan()
        kemasan1.tambah(new ItemBarang(produk1, 10))
        kemasan1.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(kemasan1)
        Kemasan kemasan2 = new Kemasan()
        kemasan2.tambah(new ItemBarang(produk1, 5))
        kemasan2.tambah(new ItemBarang(produk2, 6))
        kemasan2.tambah(new ItemBarang(produk3, 10))
        returBeli.tambah(kemasan2)
        Kemasan kemasan3 = new Kemasan()
        kemasan3.tambah(new ItemBarang(produk2, 5))
        returBeli.tambah(kemasan3)

        returBeli.hapus(kemasan2)
        assertEquals(2, returBeli.items.size())
        assertEquals(1, returBeli.items[0].nomor)
        assertEquals(3, returBeli.items[1].nomor)
        assertEquals(2, returBeli.items.size())
    }

    public void testToDaftarBarang() {
        Produk produkA = new Produk('Produk A')
        Produk produkB = new Produk('Produk B')
        Produk produkC = new Produk('Produk C')
        ReturBeli r = new ReturBeli(nomor: 'R-01', tanggal: LocalDate.now().minusDays(1), keterangan: 'TEST')

        Kemasan k1 = new Kemasan(1, LocalDate.now())
        k1.tambah(new ItemBarang(produkA, 10))
        k1.tambah(new ItemBarang(produkA, 20))
        k1.tambah(new ItemBarang(produkB, 10))

        Kemasan k2 = new Kemasan(2, LocalDate.now())
        k2.tambah(new ItemBarang(produkB, 10))
        k2.tambah(new ItemBarang(produkC, 10))

        r.tambah(k1)
        r.tambah(k2)

        DaftarBarang d = r.toDaftarBarang()
        assertEquals('R-01', d.nomor)
        assertEquals(LocalDate.now().minusDays(1), d.tanggal)
        assertEquals('TEST', d.keterangan)
        assertEquals(3, d.items.size())
        assertEquals(produkA, d.items[0].produk)
        assertEquals(30, d.items[0].jumlah)
        assertEquals(produkB, d.items[1].produk)
        assertEquals(20, d.items[1].jumlah)
        assertEquals(produkC, d.items[2].produk)
        assertEquals(10, d.items[2].jumlah)
    }

}

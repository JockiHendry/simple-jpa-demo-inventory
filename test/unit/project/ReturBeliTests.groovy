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

import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import domain.retur.KlaimKemasan
import domain.retur.KlaimPotongan
import domain.retur.KlaimRetur
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
        Produk produk3 = new Produk(nama: 'Produk C')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier, gudang: gudangUtama)
        returBeli.tambah(new ItemBarang(produk1, 10))
        returBeli.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(new ItemBarang(produk3, 30))

        KlaimKemasan klaimKemasan1 = new KlaimKemasan()
        klaimKemasan1.tambah(new ItemBarang(produk1, 5))
        returBeli.tambah(klaimKemasan1)
        KlaimKemasan klaimKemasan2 = new KlaimKemasan()
        klaimKemasan2.tambah(new ItemBarang(produk1, 5))
        klaimKemasan2.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(klaimKemasan2)

        PenerimaanBarang penerimaanBarang = returBeli.tukar()
        assertTrue(returBeli.getKlaim(KlaimKemasan, true).empty)
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

    public void testSisaPotongan() {
        Produk produk1 = new Produk()
        Produk produk2 = new Produk()
        Produk produk3 = new Produk()
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)
        returBeli.tambah(new ItemBarang(produk1, 10))
        returBeli.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(new ItemBarang(produk3, 30))
        returBeli.tambah(new KlaimPotongan(10000))
        assertEquals(10000, returBeli.sisaPotongan())

        KlaimPotongan klaim1 = new KlaimPotongan(sudahDiproses: true, potongan: 20000)
        returBeli.tambah(klaim1)
        assertEquals(10000, returBeli.sisaPotongan())
    }

    public void testProsesSisaPotongan() {
        Produk produk1 = new Produk()
        Produk produk2 = new Produk()
        Produk produk3 = new Produk()
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)
        returBeli.tambah(new ItemBarang(produk1, 10))
        returBeli.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(new ItemBarang(produk3, 30))
        returBeli.tambah(new KlaimPotongan(10000))
        assertEquals(10000, returBeli.sisaPotongan())
        returBeli.prosesSisaPotongan()
        assertEquals(0, returBeli.sisaPotongan())
    }

    public void testTambahKemasanRetur() {
        Produk produk1 = new Produk('Produk A')
        Produk produk2 = new Produk('Produk B')
        Produk produk3 = new Produk('Produk C')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)

        KlaimKemasan kemasan1 = new KlaimKemasan()
        kemasan1.tambah(new ItemBarang(produk1, 10))
        kemasan1.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(kemasan1)
        assertEquals(1, returBeli.listKlaimRetur.size())
        assertEquals(1, returBeli.listKlaimRetur[0].nomor)
        assertEquals(2, returBeli.items.size())
        assertEquals(new ItemBarang(produk1, 10), returBeli.items[0])
        assertEquals(new ItemBarang(produk2, 20), returBeli.items[1])

        KlaimKemasan kemasan2 = new KlaimKemasan()
        kemasan2.tambah(new ItemBarang(produk1, 5))
        kemasan2.tambah(new ItemBarang(produk2, 6))
        returBeli.tambah(kemasan2)
        assertEquals(2, returBeli.listKlaimRetur.size())
        assertEquals(2, returBeli.listKlaimRetur[1].nomor)
        assertEquals(2, returBeli.items.size())
        assertEquals(new ItemBarang(produk1, 15), returBeli.items[0])
        assertEquals(new ItemBarang(produk2, 26), returBeli.items[1])

        KlaimKemasan kemasan3 = new KlaimKemasan()
        kemasan3.tambah(new ItemBarang(produk2, 5))
        kemasan3.tambah(new ItemBarang(produk3, 10))
        returBeli.tambah(kemasan3)
        assertEquals(3, returBeli.listKlaimRetur.size())
        assertEquals(3, returBeli.listKlaimRetur[2].nomor)
        assertEquals(3, returBeli.items.size())
        assertEquals(new ItemBarang(produk1, 15), returBeli.items[0])
        assertEquals(new ItemBarang(produk2, 31), returBeli.items[1])
        assertEquals(new ItemBarang(produk3, 10), returBeli.items[2])
    }

    public void testHapusKemasanRetur() {
        Produk produk1 = new Produk('Produk A')
        Produk produk2 = new Produk('Produk B')
        Produk produk3 = new Produk('Produk C')
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)

        KlaimKemasan kemasan1 = new KlaimKemasan()
        kemasan1.tambah(new ItemBarang(produk1, 10))
        kemasan1.tambah(new ItemBarang(produk2, 20))
        returBeli.tambah(kemasan1)
        KlaimKemasan kemasan2 = new KlaimKemasan()
        kemasan2.tambah(new ItemBarang(produk1, 5))
        kemasan2.tambah(new ItemBarang(produk2, 6))
        kemasan2.tambah(new ItemBarang(produk3, 10))
        returBeli.tambah(kemasan2)
        KlaimKemasan kemasan3 = new KlaimKemasan()
        kemasan3.tambah(new ItemBarang(produk2, 5))
        returBeli.tambah(kemasan3)

        returBeli.hapus(kemasan2)
        assertEquals(2, returBeli.listKlaimRetur.size())
        assertEquals(1, returBeli.listKlaimRetur[0].nomor)
        assertEquals(3, returBeli.listKlaimRetur[1].nomor)
        assertEquals(2, returBeli.items.size())
        assertEquals(new ItemBarang(produk1, 10), returBeli.items[0])
        assertEquals(new ItemBarang(produk2, 25), returBeli.items[1])
    }


}

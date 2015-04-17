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

import domain.exception.StokTidakCukup
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.Sales
import domain.penjualan.StatusFakturJual
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.pengaturan.PengaturanRepository
import project.user.NomorService
import simplejpa.SimpleJpaUtil
import simplejpa.testing.StubRepositoryManager

class FakturJualTests extends GriffonUnitTestCase {

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        PengaturanRepository.metaClass.getValue = { x -> true }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
        SimpleJpaUtil.instance.repositoryManager.instances['PengaturanRepository'] = new PengaturanRepository()
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

    public void testStokTidakCukup() {
        FakturJual f = new FakturJualOlehSales()
        Produk produkA = new Produk(nama: 'Produk A', hargaDalamKota: 10000, jumlah: 50)
        Produk produkB = new Produk(nama: 'Produk B', hargaDalamKota: 12000, jumlah: 30)
        f.tambah(new ItemFaktur(produkB, 20))
        shouldFail(StokTidakCukup) {
            f.tambah(new ItemFaktur(produkA, 70))
        }
    }

    public void testProsesSampai() {
        Gudang g = new Gudang(nama: 'Gudang', utama: true)
        Sales s = new Sales(nama: 'Sales', gudang: g)
        Konsumen k = new Konsumen(nama: 'Konsumen', sales: s)
        FakturJual f = new FakturJualOlehSales(konsumen: k)
        Produk produkA = new Produk(nama: 'Produk A', hargaDalamKota: 10000, jumlah: 50)
        Produk produkB = new Produk(nama: 'Produk B', hargaDalamKota: 12000, jumlah: 30)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 20, 20000))
        f.prosesSampai(StatusFakturJual.DITERIMA, [
            Mulai: [:],
            Dibuat: [alamatTujuan: 'Alamat Tujuan'],
            Diantar: [buktiTerima: new BuktiTerima(LocalDate.now(), 'penerima', 'supir')]
        ])

        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertNotNull(f.pengeluaranBarang)
        assertEquals('Alamat Tujuan', f.pengeluaranBarang.alamatTujuan)
        assertNotNull(f.pengeluaranBarang.buktiTerima)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.buktiTerima.tanggalTerima)
        assertEquals('penerima', f.pengeluaranBarang.buktiTerima.namaPenerima)
        assertEquals('supir', f.pengeluaranBarang.buktiTerima.namaSupir)
    }

}

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

import domain.exception.BarangSelisih
import domain.faktur.Diskon
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.pembelian.Supplier
import domain.retur.ReturBeli
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import domain.inventory.Periode
import domain.inventory.Produk
import domain.pembelian.FakturBeli
import domain.faktur.Pembayaran
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PurchaseOrder
import domain.pembelian.StatusPurchaseOrder
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate
import simplejpa.SimpleJpaUtil

class PurchaseOrderTests extends GriffonUnitTestCase {

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testSisaBelumDiterima() {
        Produk produkA = new Produk('Produk A', 1000)
        Produk produkB = new Produk('Produk B', 2000)
        Produk produkC = new Produk('Produk C', 3000)
        PurchaseOrder p = new PurchaseOrder()
        p.tambah(new ItemFaktur(produk: produkA, jumlah: 10))
        p.tambah(new ItemFaktur(produk: produkB, jumlah: 7))
        p.tambah(new ItemFaktur(produk: produkC, jumlah: 15))

        assertEquals(3, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkA, 10), p.sisaBelumDiterima()[0])
        assertEquals(new ItemBarang(produkB, 7), p.sisaBelumDiterima()[1])
        assertEquals(new ItemBarang(produkC, 15), p.sisaBelumDiterima()[2])

        PenerimaanBarang penerimaan1 = new PenerimaanBarang()
        penerimaan1.tambah(new ItemBarang(produk: produkA, jumlah: 5))
        penerimaan1.tambah(new ItemBarang(produk: produkB, jumlah: 5))
        p.tambah(penerimaan1)
        assertEquals(3, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkA, 5), p.sisaBelumDiterima()[0])
        assertEquals(new ItemBarang(produkB, 2), p.sisaBelumDiterima()[1])
        assertEquals(new ItemBarang(produkC, 15), p.sisaBelumDiterima()[2])

        PenerimaanBarang penerimaan2 = new PenerimaanBarang()
        penerimaan2.tambah(new ItemBarang(produk: produkA, jumlah: 5))
        penerimaan2.tambah(new ItemBarang(produk: produkB, jumlah: 2))
        p.tambah(penerimaan2)
        assertEquals(1, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkC, 15), p.sisaBelumDiterima()[0])

        PenerimaanBarang penerimaan3 = new PenerimaanBarang()
        penerimaan3.tambah(new ItemBarang(produk: produkC, jumlah: 15))
        p.tambah(penerimaan3)
        assertTrue(p.sisaBelumDiterima().empty)
    }

    public void testSisaBelumDiterimaSetelahAdaFakturBeli() {
        Produk produkA = new Produk('Produk A', 1000)
        Produk produkB = new Produk('Produk B', 2000)
        Produk produkC = new Produk('Produk C', 3000)
        PurchaseOrder p = new PurchaseOrder()
        p.tambah(new ItemFaktur(produk: produkA, jumlah: 10))
        p.tambah(new ItemFaktur(produk: produkB, jumlah: 7))
        p.tambah(new ItemFaktur(produk: produkC, jumlah: 15))

        // Tambah faktur beli
        FakturBeli f = new FakturBeli(nomor: 'FB-001', tanggal: LocalDate.now())
        f.tambah(new ItemFaktur(produk: produkA, jumlah: 12, harga: 12000))
        f.tambah(new ItemFaktur(produk: produkB, jumlah: 5 , harga:  5000))
        f.tambah(new ItemFaktur(produk: produkC, jumlah: 10, harga: 10000))
        p.tambah(f, false)

        assertEquals(3, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkA, 12), p.sisaBelumDiterima()[0])
        assertEquals(new ItemBarang(produkB, 5), p.sisaBelumDiterima()[1])
        assertEquals(new ItemBarang(produkC, 10), p.sisaBelumDiterima()[2])

        PenerimaanBarang penerimaan1 = new PenerimaanBarang()
        penerimaan1.tambah(new ItemBarang(produk: produkA, jumlah: 5))
        penerimaan1.tambah(new ItemBarang(produk: produkB, jumlah: 4))
        p.tambah(penerimaan1)
        assertEquals(3, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkA, 7), p.sisaBelumDiterima()[0])
        assertEquals(new ItemBarang(produkB, 1), p.sisaBelumDiterima()[1])
        assertEquals(new ItemBarang(produkC, 10), p.sisaBelumDiterima()[2])

        PenerimaanBarang penerimaan2 = new PenerimaanBarang()
        penerimaan2.tambah(new ItemBarang(produk: produkA, jumlah: 7))
        penerimaan2.tambah(new ItemBarang(produk: produkB, jumlah: 1))
        p.tambah(penerimaan2)
        assertEquals(1, p.sisaBelumDiterima().size())
        assertEquals(new ItemBarang(produkC, 10), p.sisaBelumDiterima()[0])

        shouldFail(BarangSelisih) {
            PenerimaanBarang penerimaan3 = new PenerimaanBarang()
            penerimaan3.tambah(new ItemBarang(produk: produkC, jumlah: 15))
            p.tambah(penerimaan3)
        }

        PenerimaanBarang penerimaan3 = new PenerimaanBarang()
        penerimaan3.tambah(new ItemBarang(produk: produkC, jumlah: 10))
        p.tambah(penerimaan3)
        assertTrue(p.sisaBelumDiterima().empty)
    }

    public void testIsDiterimaPenuh() {
        Produk produkA = new Produk('Produk A', 1000)
        Produk produkB = new Produk('Produk B', 2000)
        Produk produkC = new Produk('Produk C', 3000)
        PurchaseOrder p = new PurchaseOrder()
        p.tambah(new ItemFaktur(produk: produkA, jumlah: 10))
        p.tambah(new ItemFaktur(produk: produkB, jumlah: 7))
        p.tambah(new ItemFaktur(produk: produkC, jumlah: 15))
        assertFalse(p.diterimaPenuh())

        PenerimaanBarang penerimaanBarang1 = new PenerimaanBarang()
        penerimaanBarang1.tambah(new ItemBarang(produk: produkA, jumlah: 10))
        penerimaanBarang1.tambah(new ItemBarang(produk: produkB, jumlah:  7))
        p.tambah(penerimaanBarang1)
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)
        assertFalse(p.diterimaPenuh())

        PenerimaanBarang penerimaanBarang2 = new PenerimaanBarang()
        penerimaanBarang2.tambah(new ItemBarang(produk: produkC, jumlah: 15))
        p.tambah(penerimaanBarang2)
        assertEquals(StatusPurchaseOrder.BARANG_DITERIMA, p.status)
        assertTrue(p.diterimaPenuh())
    }

    void testBayarHutang() {
        PurchaseOrder p = new PurchaseOrder()
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        Produk produkC = new Produk('Produk C',  8000)
        p.tambah(new ItemFaktur(produkA, 10, 9000))
        p.tambah(new ItemFaktur(produkB, 5,  11000))
        p.tambah(new ItemFaktur(produkC, 8,  5000))
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang.tambah(new ItemBarang(produkB, 5))
        penerimaanBarang.tambah(new ItemBarang(produkC, 8))
        p.tambah(penerimaanBarang)
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now())
        fakturBeli.tambah(new ItemFaktur(produkA, 10,  9000))
        fakturBeli.tambah(new ItemFaktur(produkB,  5, 11000))
        fakturBeli.tambah(new ItemFaktur(produkC,  8,  5000))
        p.tambah(fakturBeli)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-02-2013'), 50000))
        assertEquals(135000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-03-2013'), 70000))
        assertEquals(65000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-04-2013'), 50000))
        assertEquals(15000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-05-2013'), 15000))
        assertEquals(0, p.sisaHutang())
        assertTrue(p.fakturBeli.hutang.lunas)
        assertTrue(p.status == StatusPurchaseOrder.LUNAS)
    }

    void testBayarHutangDenganReturBeli() {
        Supplier supplier = new Supplier()
        PurchaseOrder p = new PurchaseOrder(supplier: supplier)
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        Produk produkC = new Produk('Produk C',  8000)
        p.tambah(new ItemFaktur(produkA, 10, 9000))
        p.tambah(new ItemFaktur(produkB, 5,  11000))
        p.tambah(new ItemFaktur(produkC, 8,  5000))
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 10))
        penerimaanBarang.tambah(new ItemBarang(produkB, 5))
        penerimaanBarang.tambah(new ItemBarang(produkC, 8))
        p.tambah(penerimaanBarang)
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now())
        fakturBeli.tambah(new ItemFaktur(produkA, 10,  9000))
        fakturBeli.tambah(new ItemFaktur(produkB,  5, 11000))
        fakturBeli.tambah(new ItemFaktur(produkC,  8,  5000))
        p.tambah(fakturBeli)

        ReturBeli r1 = new ReturBeli(supplier: supplier, nilaiPotonganHutang: 50000)
        assertFalse(r1.sudahDiterima)
        p.bayar(r1)
        assertTrue(r1.sudahDiterima)
        assertEquals(135000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        ReturBeli r2 = new ReturBeli(supplier: supplier, nilaiPotonganHutang: 70000)
        assertFalse(r2.sudahDiterima)
        p.bayar(r2)
        assertTrue(r2.sudahDiterima)
        assertEquals(65000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        ReturBeli r3 = new ReturBeli(supplier: supplier, nilaiPotonganHutang: 50000)
        assertFalse(r3.sudahDiterima)
        p.bayar(r3)
        assertTrue(r3.sudahDiterima)
        assertEquals(15000, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        ReturBeli r4 = new ReturBeli(supplier: new Supplier(), nilaiPotonganHutang: 10000)
        shouldFail(IllegalArgumentException) {
            p.bayar(r4)
        }
        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-05-2013'), 15000))
        assertEquals(0, p.sisaHutang())
        assertTrue(p.fakturBeli.hutang.lunas)
        assertTrue(p.status == StatusPurchaseOrder.LUNAS)
    }

    void testBayarHutangDesimal() {
        PurchaseOrder p = new PurchaseOrder()
        Produk produkA = new Produk('Produk A', 1000)
        Produk produkB = new Produk('Produk B', 2000)
        Produk produkC = new Produk('Produk C', 3000)
        p.tambah(new ItemFaktur(produkA, 5, 1000, null, new Diskon(1, 100)))
        p.tambah(new ItemFaktur(produkB, 3, 2000, null, new Diskon(2)))
        p.tambah(new ItemFaktur(produkC, 4, 3000, null, new Diskon(3)))
        p.diskon = new Diskon(1)
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 5))
        penerimaanBarang.tambah(new ItemBarang(produkB, 3))
        penerimaanBarang.tambah(new ItemBarang(produkC, 4))
        p.tambah(penerimaanBarang)
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now(), diskon: new Diskon(1))
        fakturBeli.tambah(new ItemFaktur(produkA, 5, 1000, null, new Diskon(1, 100)))
        fakturBeli.tambah(new ItemFaktur(produkB, 3, 2000, null, new Diskon(2)))
        fakturBeli.tambah(new ItemFaktur(produkC, 4, 3000, null, new Diskon(3)))
        p.tambah(fakturBeli)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-02-2013'), 10000))
        assertEquals(11750.3, p.sisaHutang())
        assertFalse(p.fakturBeli.hutang.lunas)
        assertFalse(p.status == StatusPurchaseOrder.LUNAS)

        p.bayar(new Pembayaran(Periode.format.parseLocalDate('01-03-2013'), 11750.3))
        assertEquals(0, fakturBeli.hutang.sisa())
        assertTrue(p.fakturBeli.hutang.lunas)
        assertTrue(p.status == StatusPurchaseOrder.LUNAS)
    }

    void testMenambahBarangYangTidakDipesan() {
        PurchaseOrder p = new PurchaseOrder()
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        Produk produkC = new Produk('Produk C',  8000)
        p.tambah(new ItemFaktur(produkA, 5))
        p.tambah(new ItemFaktur(produkB, 3))
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 5))
        penerimaanBarang.tambah(new ItemBarang(produkB, 3))
        penerimaanBarang.tambah(new ItemBarang(produkC, 4))
        shouldFail(BarangSelisih) {
            p.tambah(penerimaanBarang)
        }
    }

    void testMenambahFakturYangTidakSama() {
        PurchaseOrder p = new PurchaseOrder()
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        p.tambah(new ItemFaktur(produkA, 5))
        p.tambah(new ItemFaktur(produkB, 3))
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 5))
        penerimaanBarang.tambah(new ItemBarang(produkB, 3))
        p.tambah(penerimaanBarang)
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now(), diskon: new Diskon(1))
        fakturBeli.tambah(new ItemFaktur(produkA, 3, 1000, null, new Diskon(1, 100)))
        fakturBeli.tambah(new ItemFaktur(produkB, 5, 2000, null, new Diskon(2)))
        shouldFail(BarangSelisih) {
            p.tambah(fakturBeli)
        }
    }

    void testStatus() {
        PurchaseOrder p = new PurchaseOrder()
        Produk produkA = new Produk('Produk A', 10000)
        Produk produkB = new Produk('Produk B', 12000)
        p.tambah(new ItemFaktur(produkA, 5, 1000, null, new Diskon(1, 100)))
        p.tambah(new ItemFaktur(produkB, 3, 2000, null, new Diskon(2)))
        p.diskon = new Diskon(1)
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)

        // Tambah Faktur Beli
        FakturBeli fakturBeli = new FakturBeli(tanggal: LocalDate.now(), diskon: new Diskon(1))
        fakturBeli.tambah(new ItemFaktur(produkA, 5, 1000, null, new Diskon(1, 100)))
        fakturBeli.tambah(new ItemFaktur(produkB, 3, 2000, null, new Diskon(2)))
        p.tambah(fakturBeli)
        assertEquals(StatusPurchaseOrder.FAKTUR_DITERIMA, p.status)

        // Hapus Faktur
        p.hapusFaktur()
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)

        // Tambah Penerimaan
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 5))
        p.tambah(penerimaanBarang)
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)

        // Tambah Faktur
        p.tambah(fakturBeli)
        assertEquals(StatusPurchaseOrder.FAKTUR_DITERIMA, p.status)

        // Hapus Faktur
        p.hapusFaktur()
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)

        // Hapus Penerimaan
        p.hapus(penerimaanBarang)
        assertEquals(StatusPurchaseOrder.DIBUAT, p.status)

        // Tambah Penerimaan
        penerimaanBarang = new PenerimaanBarang()
        penerimaanBarang.tambah(new ItemBarang(produkA, 5))
        penerimaanBarang.tambah(new ItemBarang(produkB, 3))
        p.tambah(penerimaanBarang)
        assertEquals(StatusPurchaseOrder.BARANG_DITERIMA, p.status)

        // Tambah Faktur
        p.tambah(fakturBeli)
        assertEquals(StatusPurchaseOrder.OK, p.status)

        // Bayar hutang
        p.bayar(new Pembayaran(LocalDate.now(), p.fakturBeli.total()))
        assertEquals(StatusPurchaseOrder.LUNAS, p.status)
    }

}

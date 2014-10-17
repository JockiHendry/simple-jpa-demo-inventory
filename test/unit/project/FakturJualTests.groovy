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

import domain.exception.StokTidakCukup
import domain.faktur.ItemFaktur
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import domain.penjualan.StatusFakturJual
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class FakturJualTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testTambahPengeluaranBarang() {
        FakturJual f = new FakturJualOlehSales()
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        f.tambah(new ItemFaktur(produkA, 10))
        f.tambah(new ItemFaktur(produkB, 7))

        PengeluaranBarang p = new PengeluaranBarang()
        p.tambah(new ItemBarang(produkA, 10))
        p.tambah(new ItemBarang(produkB, 7))
        f.tambah(p)

        assertEquals(StatusFakturJual.DIANTAR, f.status)
    }

//
//  TODO: Tidak dapat melakukan pemeriksaan pengeluaran barang berdasarkan isi barang
//  TODO: karena terdapat bonus (dan bonus hanya berlaku untuk faktur jual oleh sales).
//  TODO: Cari cara untuk mengatasi ini jika perlu!
//
//    public void testTambahPengeluaranBarangBarangTidakSama() {
//        FakturJual f = new FakturJual() {
//            public void tambah(PengeluaranBarang p) {
//                super.tambah(p)
//            }
//        }
//        Produk produkA = new Produk('Produk A', 10000, 50)
//        Produk produkB = new Produk('Produk B', 12000, 50)
//        f.tambah(new ItemFaktur(produkA, 10))
//        f.tambah(new ItemFaktur(produkB, 7))
//
//        PengeluaranBarang p = new PengeluaranBarang()
//        p.tambah(new ItemBarang(produkA, 10))
//        p.tambah(new ItemBarang(produkB, 10))
//        shouldFail(DataTidakKonsisten) {
//            f.tambah(p)
//        }
//    }

    public void testHapusPengeluaranBarang() {
        FakturJual f = new FakturJualOlehSales()
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        f.tambah(new ItemFaktur(produkA, 10))
        f.tambah(new ItemFaktur(produkB, 7))

        PengeluaranBarang p = new PengeluaranBarang()
        p.tambah(new ItemBarang(produkA, 10))
        p.tambah(new ItemBarang(produkB, 7))
        f.tambah(p)

        assertEquals(StatusFakturJual.DIANTAR, f.status)

        f.hapusPengeluaranBarang()
        assertNull(f.pengeluaranBarang)
        assertEquals(StatusFakturJual.DIBUAT, f.status)
    }

    public void testTambahBuktiTerima() {
        Konsumen k = new Konsumen()
        FakturJual f = new FakturJualOlehSales(konsumen: k)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 7, 12000))

        PengeluaranBarang p = new PengeluaranBarang()
        p.tambah(new ItemBarang(produkA, 10))
        p.tambah(new ItemBarang(produkB, 7))
        f.tambah(p)

        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Xu'))
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.buktiTerima.tanggalTerima)
        assertEquals('Mr. Xu', f.pengeluaranBarang.buktiTerima.namaPenerima)
    }

    public void testHapusBuktiTerima() {
        Konsumen k = new Konsumen()
        FakturJual f = new FakturJualOlehSales(konsumen: k)
        Produk produkA = new Produk('Produk A', 10000, 10100, 50)
        Produk produkB = new Produk('Produk B', 12000, 12100, 50)
        f.tambah(new ItemFaktur(produkA, 10, 10000))
        f.tambah(new ItemFaktur(produkB, 7, 12000))

        PengeluaranBarang p = new PengeluaranBarang()
        p.tambah(new ItemBarang(produkA, 10))
        p.tambah(new ItemBarang(produkB, 7))
        f.tambah(p)

        f.tambah(new BuktiTerima(LocalDate.now(), 'Mr. Xu'))
        assertEquals(StatusFakturJual.DITERIMA, f.status)
        f.hapusBuktiTerima()
        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertNull(f.pengeluaranBarang.buktiTerima)
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

}

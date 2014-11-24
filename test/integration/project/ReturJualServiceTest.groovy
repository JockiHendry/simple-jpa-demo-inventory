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

import domain.exception.HargaSelisih
import domain.exception.StokTidakCukup
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.penjualan.Konsumen
import domain.retur.ItemRetur
import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimServis
import domain.retur.KlaimTukar
import domain.retur.ReturJual
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import org.joda.time.LocalDate
import project.retur.ReturJualService
import simplejpa.testing.DbUnitTestCase

class ReturJualServiceTest extends DbUnitTestCase {

    ReturJualService returJualService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_retur_jual.xlsx")
        returJualService = app.serviceManager.findService('ReturJual')
    }

    public void testHitungPotonganPiutang() {
        Produk produk1 = returJualService.findProdukById(-1l)
        Produk produk2 = returJualService.findProdukById(-2l)
        Produk produk3 = returJualService.findProdukById(-3l)
        Konsumen konsumen = returJualService.findKonsumenById(-1l)
        Gudang gudang = returJualService.findGudangById(-1l)

        ReturJual r1 = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r1.tambah(new ItemRetur(produk1, 10))
        r1.tambah(new ItemRetur(produk2, 20))
        r1.tambah(new ItemRetur(produk3, 30))
        returJualService.potongPiutang(r1)
        assertEquals(1000, r1.items[0].jumlahPotongPiutang())
        assertEquals(4000, r1.items[1].jumlahPotongPiutang())
        assertEquals(9000, r1.items[2].jumlahPotongPiutang())

        ReturJual r2 = new ReturJualOlehSales(nomor: 'R-02', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r2.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 5)] as Set))
        r2.tambah(new ItemRetur(produk2, 20))
        r2.tambah(new ItemRetur(produk3, 30))
        returJualService.potongPiutang(r2)
        assertEquals( 500, r2.items[0].jumlahPotongPiutang())
        assertEquals(4000, r2.items[1].jumlahPotongPiutang())
        assertEquals(9000, r2.items[2].jumlahPotongPiutang())

        ReturJual r3 = new ReturJualOlehSales(nomor: 'R-03', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r3.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(id: 1, produk: produk1, jumlah: 3), new KlaimTukar(id: 2, produk: produk1, jumlah: 7)] as Set))
        r3.tambah(new ItemRetur(produk2, 20))
        r3.tambah(new ItemRetur(produk3, 30))
        returJualService.potongPiutang(r3)
        assertEquals(0, r3.items[0].jumlahPotongPiutang())
        assertEquals(4000, r3.items[1].jumlahPotongPiutang())
        assertEquals(9000, r3.items[2].jumlahPotongPiutang())

        ReturJual r4 = new ReturJualOlehSales(nomor: 'R-04', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r4.tambah(new ItemRetur(produk1, 10, [new KlaimTukar(produk1, 10)] as Set))
        r4.tambah(new ItemRetur(produk2, 20))
        r4.tambah(new ItemRetur(produk3, 30))
        returJualService.potongPiutang(r4)
        assertEquals(0, r4.items[0].jumlahPotongPiutang())
        assertEquals(4000, r4.items[1].jumlahPotongPiutang())
        assertEquals(9000, r4.items[2].jumlahPotongPiutang())

        ReturJual r5 = new ReturJualOlehSales(nomor: 'R-05', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r5.tambah(new ItemRetur(produk1, 10))
        r5.tambah(new ItemRetur(produk2, 20, [new KlaimTukar(produk2, 5)] as Set))
        r5.tambah(new ItemRetur(produk3, 30, [new KlaimTukar(produk3, 5)] as Set))
        returJualService.potongPiutang(r5)
        assertEquals(1000, r5.items[0].jumlahPotongPiutang())
        assertEquals(3000, r5.items[1].jumlahPotongPiutang())
        assertEquals(7500, r5.items[2].jumlahPotongPiutang())
    }

    public void testHitungPotonganPiutangDenganGiro() {
        Produk produk1 = returJualService.findProdukById(-1l)
        Konsumen konsumen = returJualService.findKonsumenById(-5l)
        Gudang gudang = returJualService.findGudangById(-1l)

        ReturJual r1 = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r1.tambah(new ItemRetur(produk1, 20))
        shouldFail(HargaSelisih) {
            returJualService.potongPiutang(r1)
        }

        r1 = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r1.tambah(new ItemRetur(produk1, 17))
        shouldFail(HargaSelisih) {
            returJualService.potongPiutang(r1)
        }

        r1 = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r1.tambah(new ItemRetur(produk1, 10))
        returJualService.potongPiutang(r1)
        assertEquals(100000, r1.jumlahPotongPiutang())
    }

    public void testAutoKlaimReturJualOlehSales() {
        returJualService.withTransaction {
            returJualService.findProdukById(-1l).jumlahTukar = 10
            returJualService.findProdukById(-2l).jumlahTukar = 2
            returJualService.findProdukById(-4l).jumlahTukar = 2
        }

        Produk produk1 = returJualService.findProdukById(-1l)
        Produk produk2 = returJualService.findProdukById(-2l)
        Produk produk3 = returJualService.findProdukById(-3l)
        Produk produk4 = returJualService.findProdukById(-4l)
        Produk produk5 = returJualService.findProdukById(-5l)
        Konsumen konsumen = returJualService.findKonsumenById(-1l)
        Gudang gudang = returJualService.findGudangById(-1l)

        ReturJual r1 = new ReturJualOlehSales(nomor: 'R-01', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r1.tambah(new ItemRetur(produk1, 10))
        r1.tambah(new ItemRetur(produk2, 20))
        r1.tambah(new ItemRetur(produk3, 30))
        returJualService.autoKlaim(r1, true)
        assertEquals(produk1, r1.items[0].getKlaims(KlaimTukar).toList()[0].produk)
        assertEquals(10, r1.items[0].getKlaims(KlaimTukar).toList()[0].jumlah)
        assertEquals(produk2, r1.items[1].getKlaims(KlaimTukar).toList()[0].produk)
        assertEquals(14, r1.items[1].getKlaims(KlaimTukar).toList()[0].jumlah)
        assertEquals(2, r1.items[1].getKlaims(KlaimServis).toList()[0].jumlah)
        assertEquals(800, r1.items[1].getKlaims(KlaimPotongPiutang).toList()[0].jumlah)
        assertEquals(produk3, r1.items[2].getKlaims(KlaimTukar).toList()[0].produk)
        assertEquals(15, r1.items[2].getKlaims(KlaimTukar).toList()[0].jumlah)
        assertEquals(4500, r1.items[2].getKlaims(KlaimPotongPiutang).toList()[0].jumlah)

        ReturJual r2 = new ReturJualOlehSales(nomor: 'R-02', tanggal: LocalDate.now(), konsumen: konsumen, gudang: gudang)
        r2.tambah(new ItemRetur(produk3, 16))
        r2.tambah(new ItemRetur(produk4, 2))
        r2.tambah(new ItemRetur(produk5, 2))
        returJualService.autoKlaim(r2, true)
        assertEquals(produk3, r2.items[0].getKlaims(KlaimTukar).toList()[0].produk)
        assertEquals(15, r2.items[0].getKlaims(KlaimTukar).toList()[0].jumlah)
        assertEquals(300, r2.items[0].getKlaims(KlaimPotongPiutang).toList()[0].jumlah)
        assertEquals(produk4, r2.items[1].getKlaims(KlaimServis).toList()[0].produk)
        assertEquals(2, r2.items[1].getKlaims(KlaimServis).toList()[0].jumlah)
        assertEquals(1000, r2.items[2].getKlaims(KlaimPotongPiutang).toList()[0].jumlah)
    }

    public void testAutoKlaimReturJualEceran() {
        Produk produk1 = returJualService.findProdukById(-1l)
        ReturJual r1 = new ReturJualEceran(nomor: 'R-01', tanggal: LocalDate.now(), namaKonsumen: 'Aname')
        r1.tambah(new ItemRetur(produk1, 13))
        returJualService.autoKlaim(r1, true)
        assertEquals(produk1, r1.items[0].getKlaims(KlaimTukar).toList()[0].produk)
        assertEquals(10, r1.items[0].getKlaims(KlaimTukar).toList()[0].jumlah)
        assertEquals(produk1, r1.items[0].getKlaims(KlaimServis).toList()[0].produk)
        assertEquals(3, r1.items[0].getKlaims(KlaimServis).toList()[0].jumlah)

        ReturJual r2 = new ReturJualEceran(nomor: 'R-02', tanggal: LocalDate.now(), namaKonsumen: 'Aname')
        r2.tambah(new ItemRetur(produk1, 30))
        shouldFail(StokTidakCukup) {
            returJualService.autoKlaim(r2)
        }
    }

}

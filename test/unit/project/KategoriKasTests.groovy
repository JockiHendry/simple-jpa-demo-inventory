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

import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.JenisTransaksiKas
import domain.labarugi.SaldoKas
import domain.labarugi.KategoriKas
import griffon.test.*

class KategoriKasTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testJumlah() {
        KategoriKas k = new KategoriKas('Test', JENIS_KATEGORI_KAS.PENDAPATAN, false)
        def dalamKota = new JenisTransaksiKas('Dalam Kota')
        def luarKota = new JenisTransaksiKas('Luar Kota')

        k.listSaldoKas << new SaldoKas(1, 2014, 10000, dalamKota)
        k.listSaldoKas << new SaldoKas(1, 2014, 15000, luarKota)
        k.listSaldoKas << new SaldoKas(2, 2014, 20000, dalamKota)
        k.listSaldoKas << new SaldoKas(2, 2014, 25000, luarKota)
        k.listSaldoKas << new SaldoKas(3, 2014, 30000, dalamKota)
        k.listSaldoKas << new SaldoKas(3, 2014, 35000, luarKota)

        // Jumlah untuk bulan Januari
        assertEquals(25000, k.saldo(1, 2014))
        assertEquals(10000, k.saldo(1, 2014, dalamKota))
        assertEquals(15000, k.saldo(1, 2014, luarKota))

        // Jumlah untuk bulan Februari
        assertEquals(45000, k.saldo(2, 2014))
        assertEquals(20000, k.saldo(2, 2014, dalamKota))
        assertEquals(25000, k.saldo(2, 2014, luarKota))

        // Jumlah untuk bulan Maret
        assertEquals(65000, k.saldo(3, 2014))
        assertEquals(30000, k.saldo(3, 2014, dalamKota))
        assertEquals(35000, k.saldo(3, 2014, luarKota))
    }

    void testPerubahanSaldo() {
        KategoriKas k = new KategoriKas('Test', JENIS_KATEGORI_KAS.PENDAPATAN, false)
        def dalamKota = new JenisTransaksiKas('Dalam Kota')
        def luarKota = new JenisTransaksiKas('Luar Kota')

        k.perubahanSaldo(1, 2014, 10000.0, dalamKota)
        k.perubahanSaldo(1, 2014, 15000.0, luarKota)
        assertEquals(10000.0, k.saldo(1, 2014, dalamKota))
        assertEquals(15000.0, k.saldo(1, 2014, luarKota))
        assertEquals(2, k.listSaldoKas.size())

        k.perubahanSaldo(1, 2014, 5000.0, dalamKota)
        k.perubahanSaldo(1, 2014, 6000.0, luarKota)
        assertEquals(15000.0, k.saldo(1, 2014, dalamKota))
        assertEquals(21000.0, k.saldo(1, 2014, luarKota))
        assertEquals(2, k.listSaldoKas.size())

        k.perubahanSaldo(1, 2014, -1000.0, dalamKota)
        k.perubahanSaldo(1, 2014, -2000.0, luarKota)
        assertEquals(14000.0, k.saldo(1, 2014, dalamKota))
        assertEquals(19000.0, k.saldo(1, 2014, luarKota))
        assertEquals(2, k.listSaldoKas.size())

        k.perubahanSaldo(2, 2014, 1000.0, dalamKota)
        k.perubahanSaldo(2, 2014, 2000.0, luarKota)
        assertEquals(1000.0, k.saldo(2, 2014, dalamKota))
        assertEquals(2000.0, k.saldo(2, 2014, luarKota))
        assertEquals(4, k.listSaldoKas.size())
    }

}

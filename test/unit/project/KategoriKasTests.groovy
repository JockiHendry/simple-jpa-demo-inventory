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

import domain.inventory.Periode
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.JENIS_TRANSAKSI_KAS
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

        final periodeJanuari = Periode.dari('01-01-2014', '31-01-2014')
        final periodeFebruari = Periode.dari('01-02-2014', '28-02-2014')
        final periodeMaret = Periode.dari('01-03-2014', '31-03-2014')

        k.listJumlahKas << new SaldoKas(periodeJanuari, 10000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(periodeJanuari, 15000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        k.listJumlahKas << new SaldoKas(periodeFebruari, 20000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(periodeFebruari, 25000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        k.listJumlahKas << new SaldoKas(periodeMaret, 30000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(periodeMaret, 35000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)

        // Jumlah untuk bulan Januari
        assertEquals(25000, k.saldo(periodeJanuari))
        assertEquals(10000, k.saldo(periodeJanuari, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(15000, k.saldo(periodeJanuari, JENIS_TRANSAKSI_KAS.LUAR_KOTA))

        // Jumlah untuk bulan Februari
        assertEquals(45000, k.saldo(periodeFebruari))
        assertEquals(20000, k.saldo(periodeFebruari, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(25000, k.saldo(periodeFebruari, JENIS_TRANSAKSI_KAS.LUAR_KOTA))

        // Jumlah untuk bulan Maret
        assertEquals(65000, k.saldo(periodeMaret))
        assertEquals(30000, k.saldo(periodeMaret, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(35000, k.saldo(periodeMaret, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
    }

}

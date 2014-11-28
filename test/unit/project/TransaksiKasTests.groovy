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

import domain.labarugi.JenisTransaksiKas
import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class TransaksiKasTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testTambahKas() {
        KategoriKas k = new KategoriKas()
        JenisTransaksiKas dalamKota = new JenisTransaksiKas('Dalam Kota')
        TransaksiKas t = new TransaksiKas(tanggal: LocalDate.now(), jumlah: 10000, kategoriKas: k, jenis: dalamKota)
        t.tambahKas()
        assertEquals(10000, k.saldo(LocalDate.now().monthOfYear, LocalDate.now().year, dalamKota))
        t.tambahKas(true)
        assertEquals(0, k.saldo(LocalDate.now().monthOfYear, LocalDate.now().year, dalamKota))
    }

}

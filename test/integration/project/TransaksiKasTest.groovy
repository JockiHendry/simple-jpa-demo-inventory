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

import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import org.joda.time.LocalDate
import project.labarugi.TransaksiKasRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class TransaksiKasTest extends DbUnitTestCase {

	TransaksiKasRepository transaksiKasRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_laba_rugi.xlsx")
		transaksiKasRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('TransaksiKas')
    }


	void testBuat() {
		KategoriKas k = transaksiKasRepository.findKategoriKasById(-1l)
		def dalamKota = transaksiKasRepository.findJenisTransaksiKasById(-1l)

		TransaksiKas t = new TransaksiKas('TR-003', LocalDate.parse('2014-01-02'), 'Unknown', k, 10000, dalamKota)
		transaksiKasRepository.buat(t)
		k = transaksiKasRepository.findKategoriKasById(-1l)
		assertEquals(20000, k.saldo(1, 2014, dalamKota))
		assertEquals(0, k.saldo(2, 2014, dalamKota))

		t = new TransaksiKas('TR-004', LocalDate.parse('2014-02-01'), 'Unknown', k, 20000, dalamKota)
		transaksiKasRepository.buat(t)
		k = transaksiKasRepository.findKategoriKasById(-1l)
		assertEquals(20000, k.saldo(1, 2014, dalamKota))
		assertEquals(20000, k.saldo(2, 2014, dalamKota))

		k = transaksiKasRepository.findKategoriKasById(-2l)
		t = new TransaksiKas('TR-005', LocalDate.parse('2014-02-01'), 'Unknown', k, 20000, dalamKota)
		transaksiKasRepository.buat(t)
		k = transaksiKasRepository.findKategoriKasById(-2l)
		assertEquals(20000, k.saldo(2, 2014, dalamKota))
	}

	void testHapus() {
		TransaksiKas t = transaksiKasRepository.findTransaksiKasById(-1l)
		transaksiKasRepository.hapus(t)
		KategoriKas k = transaksiKasRepository.findKategoriKasById(-1l)
		def dalamKota = transaksiKasRepository.findJenisTransaksiKasById(-1l)
		def luarKota = transaksiKasRepository.findJenisTransaksiKasById(-2l)

		assertEquals(0, k.saldo(1, 2014, dalamKota))
		assertEquals(12000, k.saldo(1, 2014, luarKota))

		t = transaksiKasRepository.findTransaksiKasById(-2l)
		transaksiKasRepository.hapus(t)
		k = transaksiKasRepository.findKategoriKasById(-1l)
		assertEquals(0, k.saldo(1, 2014, dalamKota))
		assertEquals(0, k.saldo(1, 2014, luarKota))

	}

}
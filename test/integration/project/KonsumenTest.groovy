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

import domain.faktur.Referensi
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import project.penjualan.KonsumenRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class KonsumenTest extends DbUnitTestCase {

    KonsumenRepository konsumenRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("konsumen", "/project/data_penjualan.xls")
        konsumenRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Konsumen')
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testPotongPiutang() {
        konsumenRepository.withTransaction {
            Konsumen konsumen = findKonsumenById(-3l)
            assertEquals(50000, konsumen.jumlahPiutang())
            konsumen.potongPiutang(30000)

            FakturJualOlehSales f = findFakturJualOlehSalesById(-3l)
            assertEquals(30000, f.piutang.jumlahDibayar())
            assertEquals(20000, f.sisaPiutang())
            assertEquals(20000, konsumen.jumlahPiutang())

            konsumen.potongPiutang(20000)
            assertEquals(0, konsumen.listFakturBelumLunas.size())
            assertEquals(0, konsumen.jumlahPiutang())
        }
    }

    public void testPotongPiutangPerFaktur() {
        konsumenRepository.withTransaction {
            Konsumen konsumen = findKonsumenByIdFetchFakturBelumLunas(-3l)
            FakturJualOlehSales faktur = findFakturJualOlehSalesById(-3l)
            Referensi referensi = konsumen.potongPiutang(30000, faktur)
            assertEquals('FakturJualOlehSales', referensi.namaClass)
            assertEquals(faktur.nomor, referensi.nomor)
            assertEquals(20000, konsumen.jumlahPiutang())
            faktur = findFakturJualOlehSalesByIdFetchComplete(-3l)
            assertEquals(20000, faktur.sisaPiutang())
        }
    }

}

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

import domain.faktur.BilyetGiro
import domain.general.Pesan
import domain.general.PesanGiroJatuhTempo
import project.faktur.BilyetGiroRepository
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import project.penjualan.BilyetGiroService
import project.user.PesanRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class BilyetGiroTest extends DbUnitTestCase {

    BilyetGiroRepository bilyetGiroRepository
    BilyetGiroService bilyetGiroService
    PesanRepository pesanRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_bilyet_giro.xlsx")
        bilyetGiroRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('BilyetGiro')
        pesanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pesan')
        bilyetGiroService = app.serviceManager.findService('BilyetGiro')
    }

    public void testPencairan() {
        BilyetGiro bilyetGiro = bilyetGiroRepository.findBilyetGiroById(-1l)
        bilyetGiro = bilyetGiroRepository.cairkan(bilyetGiro)
        assertTrue(bilyetGiro.sudahDicairkan())
        FakturJualOlehSales f = bilyetGiroRepository.findFakturJualOlehSalesById(-4l)
        assertEquals(StatusFakturJual.LUNAS, f.status)
    }

    public void testPeriksaJatuhTempo() {
        BilyetGiro bg1 = new BilyetGiro(nomorSeri: 'BS-0001', nominal: 10000, jatuhTempo: LocalDate.now().minusDays(1))
        BilyetGiro bg2 = new BilyetGiro(nomorSeri: 'BS-0002', nominal: 10000, jatuhTempo: LocalDate.now().plusMonths(1))
        bilyetGiroRepository.buat(bg1)
        bilyetGiroRepository.buat(bg2)

        bilyetGiroService.periksaJatuhTempo()

        List<Pesan> result = pesanRepository.refresh()
        assertTrue(result.find {(it instanceof PesanGiroJatuhTempo) && it.bilyetGiro == bg1} != null)
    }

    public void testFakturJualYangDibayarDengan() {
        BilyetGiro bilyetGiro = bilyetGiroRepository.findBilyetGiroById(-2l)
        List<FakturJualOlehSales> result = bilyetGiroService.cariFakturJualYangDibayarDengan(bilyetGiro)
        assertEquals(3, result.size())
        assertEquals('000002/042014/SA', result[0].nomor)
        assertEquals('000003/042014/SA', result[1].nomor)
        assertEquals('000004/042014/SA', result[2].nomor)

        bilyetGiro = bilyetGiroRepository.findBilyetGiroById(-3l)
        result = bilyetGiroService.cariFakturJualYangDibayarDengan(bilyetGiro)
        assertEquals(1, result.size())
        assertEquals('000004/042014/SA', result[0].nomor)
    }

}

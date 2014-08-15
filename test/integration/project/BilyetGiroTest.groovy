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

import domain.Container
import domain.faktur.BilyetGiro
import domain.faktur.BilyetGiroRepository
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class BilyetGiroTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(BilyetGiroTest)

    BilyetGiroRepository bilyetGiroRepository = SimpleJpaUtil.container.bilyetGiroRepository

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("fakturJual", "/project/data_penjualan.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testPencairan() {
        bilyetGiroRepository.withTransaction {
            BilyetGiro bilyetGiro = bilyetGiroRepository.cari('AB-111')[0]
            bilyetGiro.cairkan(LocalDate.now())
            assertTrue(bilyetGiro.sudahDicairkan())
        }
        FakturJualOlehSales f = bilyetGiroRepository.findFakturJualOlehSalesById(-6l)
        assertEquals(StatusFakturJual.LUNAS, f.status)
    }

    public void testClearingService() {
        BilyetGiro bg1 = new BilyetGiro(nomorSeri: 'BS-0001', nominal: 10000, jatuhTempo: LocalDate.now().minusDays(1))
        BilyetGiro bg2 = new BilyetGiro(nomorSeri: 'BS-0002', nominal: 10000, jatuhTempo: LocalDate.now().plusMonths(1))
        bilyetGiroRepository.buat(bg1)
        bilyetGiroRepository.buat(bg2)

        Container.app.bilyetGiroClearingService.periksaJatuhTempo()

        bg1 = bilyetGiroRepository.findBilyetGiroByNomorSeri('BS-0001')
        assertEquals(LocalDate.now(), bg1.tanggalPencairan)
        assertTrue(bg1.sudahDicairkan())

        bg2 = bilyetGiroRepository.findBilyetGiroByNomorSeri('BS-0002')
        assertNull(bg2.tanggalPencairan)
        assertFalse(bg2.sudahDicairkan())
    }

}

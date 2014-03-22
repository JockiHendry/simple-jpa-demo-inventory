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

import domain.*
import domain.exception.DataTidakBolehDiubah
import domain.pembelian.FakturBeliRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class FakturBeliTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(FakturBeliTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("fakturBeli", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
    }


    void testTidakBolehDelete() {
        FakturBeliRepository repo = Container.app.fakturBeliRepository

        repo.hapus(repo.findFakturBeliById(-1))
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.hapus(repo.findFakturBeliById(-2))
        }
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.hapus(repo.findFakturBeliById(-5))
        }
    }

    void testTidakBolehUpdate() {
        FakturBeliRepository repo = Container.app.fakturBeliRepository

        repo.update(repo.findFakturBeliById(-1))
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.update(repo.findFakturBeliById(-2))
        }
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.update(repo.findFakturBeliById(-5))
        }
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            repo.update(repo.findFakturBeliById(-7))
        }
    }

}
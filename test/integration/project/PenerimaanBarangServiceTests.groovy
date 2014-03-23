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
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.inventory.DaftarBarang
import domain.inventory.Periode
import domain.pembelian.FakturBeli
import domain.pembelian.FakturBeliRepository
import domain.pembelian.PembayaranHutang
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PenerimaanBarangService
import domain.pembelian.StatusFakturBeli
import griffon.core.GriffonApplication
import griffon.test.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

class PenerimaanBarangServiceTests extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ProdukTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("penerimaanBarang", "/project/data_pembelian.xls")
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAssign() {
        PenerimaanBarangService service = Container.app.penerimaanBarangService

        FakturBeli fakturBeli = service.findFakturBeliByIdFetchComplete(-1l)
        PenerimaanBarang penerimaan1 = service.findPenerimaanBarangByIdFetchComplete(-1l)
        PenerimaanBarang penerimaan2 = service.findPenerimaanBarangByIdFetchComplete(-2l)
        PenerimaanBarang penerimaan3 = service.findPenerimaanBarangByIdFetchComplete(-3l)

        penerimaan1 = service.assign(penerimaan1, fakturBeli)
        assertEquals(fakturBeli.nomor, penerimaan1.faktur.nomor)
        assertEquals(StatusFakturBeli.DIBUAT, penerimaan1.faktur.status)
        assertNull(penerimaan1.faktur.hutang)

        penerimaan2 = service.assign(penerimaan2, fakturBeli)
        assertEquals(fakturBeli.nomor, penerimaan2.faktur.nomor)
        assertEquals(StatusFakturBeli.DIBUAT, penerimaan2.faktur.status)
        assertNull(penerimaan2.faktur.hutang)

        penerimaan3 = service.assign(penerimaan3, fakturBeli)
        assertEquals(fakturBeli.nomor, penerimaan3.faktur.nomor)
        assertEquals(StatusFakturBeli.BARANG_DITERIMA, penerimaan3.faktur.status)
        assertNotNull(penerimaan3.faktur.hutang)
        assertEquals(fakturBeli.total(), penerimaan3.faktur.hutang.sisa())
    }

    void testAssignGagal() {
        PenerimaanBarangService service = Container.app.penerimaanBarangService

        FakturBeli fakturBeli = service.findFakturBeliByIdFetchComplete(-2l)
        PenerimaanBarang penerimaan1 = service.findPenerimaanBarangByIdFetchComplete(-1l)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            service.assign(penerimaan1, fakturBeli)
        }

        fakturBeli = service.findFakturBeliByIdFetchComplete(-3l)
        penerimaan1 = service.findPenerimaanBarangByIdFetchComplete(-4l)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            service.assign(penerimaan1, fakturBeli)
        }

        fakturBeli = service.findFakturBeliByIdFetchComplete(-3l)
        penerimaan1 = service.findPenerimaanBarangByIdFetchComplete(-1l)
        GroovyAssert.shouldFail(DataTidakKonsisten) {
            service.assign(penerimaan1, fakturBeli)
        }
    }

    void testHapusAssignment() {
        PenerimaanBarangService service = Container.app.penerimaanBarangService

        PenerimaanBarang penerimaanBarang = service.findPenerimaanBarangByIdFetchComplete(-6l)
        penerimaanBarang = service.hapusAssignment(penerimaanBarang)
        assertNull(penerimaanBarang.faktur)

        FakturBeli fakturBeli = service.findFakturBeliById(-4l)
        assertEquals(StatusFakturBeli.DIBUAT, fakturBeli.status)
        assertNull(fakturBeli.hutang)
    }

    void testHapusAssignmentGagal() {
        PenerimaanBarangService service = Container.app.penerimaanBarangService

        PenerimaanBarang penerimaanBarang = service.findPenerimaanBarangByIdFetchComplete(-7l)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            service.hapusAssignment(penerimaanBarang)
        }

        FakturBeli fakturBeli = service.findFakturBeliByIdFetchComplete(-4l)
        penerimaanBarang = service.findPenerimaanBarangByIdFetchComplete(-6l)
        fakturBeli.bayarHutang(new PembayaranHutang(Periode.format.parseLocalDate('15-03-2014'), 5000))
        fakturBeli = service.merge(fakturBeli)
        GroovyAssert.shouldFail(DataTidakBolehDiubah) {
            service.hapusAssignment(penerimaanBarang)
        }
    }

}

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
package project.laporan

import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import project.penjualan.FakturJualRepository

import javax.swing.SwingUtilities

class LaporanPembayaranPiutangController {

    LaporanPembayaranPiutangModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        model.result = fakturJualRepository.findAllFakturJualOlehSalesByDslFetchComplete([orderBy: 'konsumen__sales__nama,tanggal,nomor']) {
            tanggal between(model.tanggalMulaiCari, model.tanggalSelesaiCari)
            and()
            status isIn([StatusFakturJual.DITERIMA, StatusFakturJual.LUNAS])
            if (model.salesSearch) {
                and()
                konsumen__sales__nama like("%${model.salesSearch}%")
            }
            if (model.konsumenSearch) {
                and()
                konsumen__nama like("%${model.konsumenSearch}%")
            }
        }
        model.params.'tanggalMulaiCari' = model.tanggalMulaiCari
        model.params.'tanggalSelesaiCari' = model.tanggalSelesaiCari
        close()
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

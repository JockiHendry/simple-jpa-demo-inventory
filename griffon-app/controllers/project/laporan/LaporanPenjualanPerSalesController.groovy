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

import project.penjualan.FakturJualRepository
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.SwingUtilities
import java.awt.Dimension

class LaporanPenjualanPerSalesController {

    LaporanPenjualanPerSalesModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        execInsideUISync {
            model.salesList.clear()
        }
        List sales = fakturJualRepository.findAllSales([orderBy: 'nama'])
        execInsideUISync {
            model.salesList.addAll(sales)
        }
    }

    def tampilkanLaporan = {
        model.result = fakturJualRepository.findAllFakturJualOlehSalesByDslFetchItems([orderBy: 'konsumen__sales__nama,tanggal,nomor']) {
            tanggal between(model.tanggalMulaiCari, model.tanggalSelesaiCari)
            if (model.sales.selectedItem) {
                and()
                konsumen__sales eq(model.sales.selectedItem)
            }
            if (model.konsumenSearch) {
                and()
                konsumen eq(model.konsumenSearch)
            }
        }
        model.params.'tanggalMulaiCari' = model.tanggalMulaiCari
        model.params.'tanggalSelesaiCari' = model.tanggalSelesaiCari
        close()
    }

    def reset = {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        model.sales.selectedItem = null
        model.konsumenSearch = null
    }

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, view, dialogProps) { m, v, c ->
                if (!v.table.selectionModel.isSelectionEmpty()) {
                    model.konsumenSearch = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

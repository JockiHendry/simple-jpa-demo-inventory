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
package project.penjualan

import domain.penjualan.Konsumen
import project.pembelian.PurchaseOrderRepository
import domain.penjualan.FakturJualOlehSales
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.event.ListSelectionEvent
import java.awt.Dimension

class PiutangController {

    PiutangModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = PurchaseOrderRepository.StatusHutangSearch.BELUM_LUNAS
        }
    }

    def search = {
        List result = fakturJualRepository.cariPiutang(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.konsumenSearch, model.chkJatuhTempo, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturJualList.clear()
            model.fakturJualList.addAll(result)
        }
    }

    def showPembayaran = {
        execInsideUISync {
            def args = [faktur: view.table.selectionModel.selected[0], listPembayaran: model.listPembayaranPiutang]
            def dialogProps = [title: 'Pembayaran Piutang', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('pembayaranPiutangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listPembayaranPiutang.clear()
                model.listPembayaranPiutang.addAll(m.pembayaranList)
                view.table.selectionModel.selected[0] = m.faktur
            }
        }
    }

    def showInfoPiutangKonsumen = {
        if (!view.table.selectionModel.selectionEmpty) {
            Konsumen konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(view.table.selectionModel.selected[0].konsumen.id)
            execInsideUISync {
                def args = [konsumen: konsumen]
                def dialogProps = [title: 'Faktur Belum Lunas', preferredSize: new Dimension(900, 420)]
                DialogUtils.showMVCGroup('fakturJualOlehSalesAsChild', args, app, view, dialogProps)
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.listPembayaranPiutang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                FakturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.listPembayaranPiutang.clear()
                if (selected.piutang) {
                    model.listPembayaranPiutang.addAll(selected.piutang.listPembayaran)
                }
            }
        }
    }

}

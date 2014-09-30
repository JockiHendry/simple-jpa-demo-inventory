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
package project.pembelian

import domain.pembelian.PurchaseOrder
import domain.retur.ReturBeli
import org.joda.time.LocalDate
import project.retur.ReturBeliViewMode
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import java.awt.Dimension

class HutangController {

    HutangModel model
    def view

    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = PurchaseOrderRepository.StatusHutangSearch.BELUM_LUNAS
        }
    }

    def search = {
        List result = purchaseOrderRepository.cariHutang(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.supplierSearch, model.chkJatuhTempo? model.jatuhTempoSearch: null, model.statusSearch.selectedItem)
        execInsideUISync {
            model.purchaseOrderList.clear()
            model.purchaseOrderList.addAll(result)
        }
    }

    def showPembayaran = {
        execInsideUISync {
            def args = [purchaseOrder: view.table.selectionModel.selected[0], listPembayaran: model.listPembayaranHutang]
            def dialogProps = [title: 'Pembayaran Hutang', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('pembayaranHutangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listPembayaranHutang.clear()
                model.listPembayaranHutang.addAll(m.pembayaranList)
                view.table.selectionModel.selected[0] = m.purchaseOrder
            }
        }
    }

    def showPembayaranRetur = {
        ReturBeli returBeli = null
        PurchaseOrder purchaseOrder = view.table.selectionModel.selected[0]
        execInsideUISync {
            def args = [mode: ReturBeliViewMode.BAYAR, forSupplier: purchaseOrder.supplier]
            def dialogProps = [title: 'Pembayaran Hutang', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('returBeli', args, app, view, dialogProps) { m, v, c ->
                returBeli = v.table.selectionModel.selected[0]
            }
        }
        if (!returBeli) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada retur beli yang dipilih!', 'Data Tidak Lengkap', JOptionPane.ERROR_MESSAGE)
            return
        } else if (returBeli.deleted == 'Y') {
            JOptionPane.showMessageDialog(view.mainPanel, 'Retur beli yang sudah dihapus tidak dapat dipakai lagi!', 'Data Tidak Valid', JOptionPane.ERROR_MESSAGE)
            return
        }
        if (JOptionPane.showConfirmDialog(view.mainPanel, "Anda yakin ingin melunasi hutang ini dengan retur beli [${returBeli.nomor}]?", 'Konfirmasi', JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return
        }
        purchaseOrderRepository.withTransaction {
            purchaseOrder = merge(purchaseOrder)
            returBeli = merge(returBeli)
            purchaseOrder.bayar(returBeli)
        }
        execInsideUISync {
            view.table.selectionModel.selected[0] = purchaseOrder
        }
        JOptionPane.showMessageDialog(view.mainPanel, "Potongan hutang berdasarkan retur beli [${returBeli.nomor}] telah ditambahkan.", 'Informasi', JOptionPane.INFORMATION_MESSAGE)
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.listPembayaranHutang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PurchaseOrder selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.listPembayaranHutang.clear()
                if (selected?.fakturBeli?.hutang) {
                    model.listPembayaranHutang.addAll(selected.fakturBeli.hutang.listPembayaran)
                }
            }
        }
    }

}

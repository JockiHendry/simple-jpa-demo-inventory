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

import ast.NeedSupervisorPassword
import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.pembelian.PembayaranHutang
import domain.pembelian.PurchaseOrderRepository
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class PembayaranHutangAsChildController {

    PembayaranHutangAsChildModel model
    def view

    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        purchaseOrderRepository = Container.app.purchaseOrderRepository
        model.purchaseOrder = args.'purchaseOrder'
        model.editable = true
        execInsideUISync {
            model.pembayaranHutangList.clear()
            model.pembayaranHutangList.addAll(args.'listPembayaranHutang')
        }
    }

    def save = {
        PembayaranHutang pembayaranHutang = new PembayaranHutang(tanggal: model.tanggal, jumlah: model.jumlah)
        if (!purchaseOrderRepository.validate(pembayaranHutang, Default, model)) return

        try {
            purchaseOrderRepository.withTransaction {
                model.purchaseOrder = merge(model.purchaseOrder)
                model.purchaseOrder.bayar(pembayaranHutang)
            }
            execInsideUISync {
                model.pembayaranHutangList << pembayaranHutang
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Pembayaran tidak dapat dilakukan lagi!', 'Pembayaran gagal disimpan', JOptionPane.ERROR_MESSAGE)
        } catch (IllegalArgumentException ex) {
            model.errors['jumlah'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PembayaranHutang pembayaranHutang = view.table.selectionModel.selected[0]
            purchaseOrderRepository.withTransaction {
                model.purchaseOrder = purchaseOrderRepository.merge(model.purchaseOrder)
                model.purchaseOrder.hapus(pembayaranHutang)
            }
            execInsideUISync {
                model.pembayaranHutangList.remove(pembayaranHutang)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Pembayaran tidak dapat dihapus lagi!', 'Pembayaran gagal dihapus', JOptionPane.ERROR_MESSAGE)
        } catch (IllegalArgumentException ex) {
            model.errors['jumlah'] = ex.message
        }
    }

    def clear = {
        execInsideUISync {
            model.tanggal = null
            model.jumlah = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PembayaranHutang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.tanggal = selected.tanggal
                model.jumlah = selected.jumlah
            }
        }
    }

}

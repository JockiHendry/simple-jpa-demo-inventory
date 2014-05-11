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

import ast.NeedSupervisorPassword
import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.faktur.Pembayaran
import domain.pembelian.PurchaseOrderRepository
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PembayaranAsChildController {

    PembayaranAsChildModel model
    def view

    PurchaseOrderRepository repo

    void mvcGroupInit(Map args) {
        repo = Container.app.purchaseOrderRepository
        model.faktur = args.'faktur'
        model.editable = true
        execInsideUISync {
            model.pembayaranList.clear()
            model.pembayaranList.addAll(args.'listPembayaran')
        }
    }

    def save = {
        Pembayaran pembayaran = new Pembayaran(tanggal: model.tanggal, jumlah: model.jumlah, potongan: model.potongan, bilyetGiro: model.bilyetGiro)
        if (!repo.validate(pembayaran, Default, model)) return

        try {
            repo.withTransaction {
                model.faktur = merge(model.faktur)
                model.faktur.bayar(pembayaran)
            }
            execInsideUISync {
                model.pembayaranList << pembayaran
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
            Pembayaran pembayaranHutang = view.table.selectionModel.selected[0]
            repo.withTransaction {
                model.faktur = repo.merge(model.faktur)
                model.faktur.hapus(pembayaranHutang)
            }
            execInsideUISync {
                model.pembayaranList.remove(pembayaranHutang)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Pembayaran tidak dapat dihapus lagi!', 'Pembayaran gagal dihapus', JOptionPane.ERROR_MESSAGE)
        } catch (IllegalArgumentException ex) {
            model.errors['jumlah'] = ex.message
        }
    }

    def showBilyetGiro = {
        def args = [popupMode: true]
        def dialogProps = [title: 'Cari Bilyet Giro', size: new Dimension(900, 420)]
        DialogUtils.showMVCGroup('giro', args, app, view, dialogProps) { m, v, c ->
            if (v.table.selectionModel.isSelectionEmpty()) {
                JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada bilyet giro yang dipilih!', 'Cari Bilyet Giro', JOptionPane.ERROR_MESSAGE)
            } else {
                model.bilyetGiro = v.table.selectionModel.selected[0]
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.tanggal = null
            model.jumlah = null
            model.bilyetGiro = null
            model.potongan = false
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Pembayaran selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.tanggal = selected.tanggal
                model.jumlah = selected.jumlah
                model.potongan = selected.potongan
                model.bilyetGiro = selected.bilyetGiro
            }
        }
    }

}

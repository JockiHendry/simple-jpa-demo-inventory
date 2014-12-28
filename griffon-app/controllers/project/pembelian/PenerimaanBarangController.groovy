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
import domain.exception.DataTidakBolehDiubah
import domain.pembelian.*
import domain.validation.TanpaGudang
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import domain.exception.DataDuplikat
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class PenerimaanBarangController {

    PenerimaanBarangModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        model.purchaseOrder = args.'purchaseOrder'
        model.editable = args.containsKey('editable')? args.'editable': false
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        init()
    }

    def init = {
        execInsideUISync {
            model.penerimaanBarangList.clear()
            model.penerimaanBarangList.addAll(model.purchaseOrder.listPenerimaanBarang)
        }
    }

    def save = {
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan)
        if (!purchaseOrderRepository.validate(penerimaanBarang, TanpaGudang, model)) return
        try {
            model.purchaseOrder = purchaseOrderRepository.tambah(model.purchaseOrder, penerimaanBarang, model.listItemBarang)
            execInsideUISync {
                model.penerimaanBarangList << penerimaanBarang
                view.table.changeSelection(model.penerimaanBarangList.size() - 1, 0, false, false)
                clear()
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Penerimaan barang tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
            def (resultPurchaseOrder,resultPenerimaanBarang) = purchaseOrderRepository.hapus(model.purchaseOrder, penerimaanBarang)
            model.purchaseOrder = resultPurchaseOrder
            penerimaanBarang = resultPenerimaanBarang
            execInsideUISync {
                view.table.selectionModel.selected[0] = penerimaanBarang
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Penerimaan barang tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.listItemBarang, allowTambahProduk: model.allowTambahProduk]
            def dialogProps = [title: 'Daftar Barang', size: new Dimension(400, 320)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemBarang.clear()
                model.listItemBarang.addAll(m.itemBarangList)
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.notDeleted = true
            model.nomor = null
            model.tanggal = null
            model.keterangan = null
            model.listItemBarang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PenerimaanBarang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.notDeleted = (selected.deleted == 'N')
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.listItemBarang.clear()
                model.listItemBarang.addAll(selected.items)
            }
        }
    }

}
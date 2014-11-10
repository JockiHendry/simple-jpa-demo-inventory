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

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.pembelian.PenerimaanBarang
import domain.validation.TanpaGudang
import project.user.NomorService
import simplejpa.SimpleJpaUtil
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import java.awt.Dimension

class ReturController {

    ReturModel model
    def view
    FakturJualRepository fakturJualRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        model.fakturJualOlehSales = args.'fakturJualOlehSales'
        init()
    }

    def init = {
        model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENERIMAAN_BARANG)
        execInsideUISync {
            model.penerimaanBarangList.clear()
            model.penerimaanBarangList.addAll(model.fakturJualOlehSales.retur)
        }
    }

    @NeedSupervisorPassword
    def save = {
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, createdBy: SimpleJpaUtil.instance.user)
        penerimaanBarang.items.addAll(model.listItemBarang)
        if (!fakturJualRepository.validate(penerimaanBarang, TanpaGudang, model)) return
        model.fakturJualOlehSales = fakturJualRepository.retur(model.fakturJualOlehSales, penerimaanBarang)
        execInsideUISync {
            model.penerimaanBarangList << penerimaanBarang
            view.table.changeSelection(model.penerimaanBarangList.size() - 1, 0, false, false)
            clear()
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.listItemBarang, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang', size: new Dimension(400, 320)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemBarang.clear()
                model.listItemBarang.addAll(m.itemBarangList)
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'retur_faktur.json']
            def dialogProps = [title: 'Preview Retur Jual', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
        model.fakturJualOlehSales = fakturJualRepository.hapusRetur(model.fakturJualOlehSales, penerimaanBarang.nomor)
        execInsideUISync {
            model.penerimaanBarangList.remove(penerimaanBarang)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENERIMAAN_BARANG)
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
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.listItemBarang.clear()
                model.listItemBarang.addAll(selected.items)
            }
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}

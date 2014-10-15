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
package project.retur

import domain.exception.DataTidakKonsisten
import domain.penjualan.PengeluaranBarang
import domain.validation.PenjualanOlehSales
import project.user.NomorService
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PengirimanReturController {

    PengirimanReturModel model
    def view
    ReturJualRepository returJualRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.allowKirim = true
        model.allowBatalKirim = true
        init()
    }

    def init = {
        execInsideUISync {
            model.pengeluaranBarangList.clear()
            model.pengeluaranBarangList.addAll(model.parent.pengeluaranBarang)
        }
    }

    def save = {
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, alamatTujuan: model.alamatTujuan)
        model.listItemBarang.each { pengeluaranBarang.tambah(it) }
        if (!returJualRepository.validate(pengeluaranBarang, PenjualanOlehSales, model)) return
        try {
            model.parent = returJualRepository.tukar(model.parent, pengeluaranBarang)
            execInsideUISync {
                model.pengeluaranBarangList << pengeluaranBarang
                view.table.changeSelection(model.pengeluaranBarangList.size() - 1, 0, false, false)
                clear()
            }
        } catch (DataTidakKonsisten ex) {
            model.errors['items'] = ex.message
        }
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.listItemBarang, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang', size: new Dimension(900, 320)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listItemBarang.clear()
                model.listItemBarang.addAll(m.itemBarangList)
            }
        }
    }

    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        PengeluaranBarang pengeluaranBarang = view.table.selectionModel.selected[0]
        if (model.parent) {
            model.parent = returJualRepository.hapusPengeluaranBarang(model.parent, pengeluaranBarang)
        }
        execInsideUISync {
            model.pengeluaranBarangList.remove(pengeluaranBarang)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.keterangan = null
            model.listItemBarang.clear()
            model.alamatTujuan = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PengeluaranBarang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.alamatTujuan = selected.alamatTujuan
                model.keterangan = selected.keterangan
                model.listItemBarang.clear()
                model.listItemBarang.addAll(selected.items)
            }
        }
    }

}

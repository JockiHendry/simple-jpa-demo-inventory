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

import ast.NeedSupervisorPassword
import domain.retur.*
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils

import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class ReturJualController {

    ReturJualModel model
    def view
    ReturJualRepository returJualRepository
    GudangRepository gudangRepository

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.mode: ReturJualViewMode.INPUT
        if (model.mode == ReturJualViewMode.INPUT) {
            model.showSave = true
            model.showPenukaran = false
            model.statusSearch.selectedItem = StatusReturJual.SEMUA
        } else if (model.mode == ReturJualViewMode.PENGELUARAN) {
            model.showSave = false
            model.showPenukaran = true
            model.statusSearch.selectedItem = StatusReturJual.BELUM_DIPROSES
        }
        listAll()
        search()
    }

    void mvcGroupDestroy() {
    }

    def listAll = {
        execInsideUISync {
            model.nomorSearch = null
            model.konsumenSearch = null
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.konsumenList.clear()
        }
        List konsumenResult = returJualRepository.findAllKonsumen()
        execInsideUISync {
            model.konsumenList.addAll(konsumenResult)
        }
    }

    def search = {
        Boolean sudahDiproses = null
        if (model.statusSearch.selectedItem == StatusReturJual.SUDAH_DIPROSES) {
            sudahDiproses = true
        } else if (model.statusSearch.selectedItem == StatusReturJual.BELUM_DIPROSES) {
            sudahDiproses = false
        }
        List result = returJualRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.konsumenSearch, sudahDiproses)
        execInsideUISync {
            model.returJualList.clear()
            model.returJualList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        ReturJual returJual = new ReturJual(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, items: new ArrayList(model.items), konsumen: model.konsumen.selectedItem)
        returJual.gudang = gudangRepository.cariGudangUtama()
        returJual.listKlaimRetur.addAll(model.listKlaimRetur)
        if (model.potongan > 0) {
            returJual.tambahKlaimPotongan(model.potongan)
        }

        if (!returJualRepository.validate(returJual, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                returJualRepository.buat(returJual)
                execInsideUISync {
                    model.returJualList << returJual
                    view.table.changeSelection(model.returJualList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                returJual = returJualRepository.update(returJual)
                execInsideUISync { view.table.selectionModel.selected[0] = returJual }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    def prosesTukar = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin barang retur yang ditukar telah diterima oleh konsumen?', 'Konfirmasi Penerimaan', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        ReturJual returJual = view.table.selectionModel.selected[0]
        returJual = returJualRepository.tukar(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        ReturJual returJual = view.table.selectionModel.selected[0]
        returJual = returJualRepository.hapus(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    def showBarangRetur = {
        execInsideUISync {
            def args = [listItemBarang: model.items, parent: view.table.selectionModel.selected[0], allowTambahProduk: false]
            def props = [title: 'Items']
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def showKlaimRetur = {
        execInsideUISync {
            def args = [parentList: model.listKlaimRetur, parent: view.table.selectionModel.selected[0]]
            def props = [title: 'Items']
            DialogUtils.showMVCGroup('klaimReturAsChild', args, app, view, props) { m, v, c ->
                model.listKlaimRetur.clear()
                model.listKlaimRetur.addAll(m.klaimReturList)
            }
        }
    }
    
    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.keterangan = null
            model.items.clear()
            model.listKlaimRetur.clear()
            model.potongan = null
            model.konsumen.selectedItem = null
            model.created = null
            model.createdBy = null
            model.modified = null
            model.modifiedBy = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                ReturJual selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.items.clear()
                model.items.addAll(selected.items)
                model.listKlaimRetur.clear()
                model.listKlaimRetur.addAll(selected.getKlaimTukar())
                model.potongan = selected.getKlaimPotongan().sum { it.potongan }
                model.konsumen.selectedItem = selected.konsumen
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
            }
        }
    }

}
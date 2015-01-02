/*
 * Copyright 2015 Jocki Hendry.
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
import domain.exception.StokTidakCukup
import domain.retur.ReturJualEceran
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ReturJualEceranController {

    ReturJualEceranModel model
    def view
    ReturJualRepository returJualRepository
    NomorService nomorService
    ReturJualService returJualService

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.mode: ReturJualViewMode.INPUT
        if (model.mode == ReturJualViewMode.INPUT) {
            model.showSave = true
            model.showTanggal = true
            model.showPenukaran = false
            model.statusSearch.selectedItem = StatusReturJual.SEMUA
            model.excludeDeleted = false
        } else if (model.mode == ReturJualViewMode.PENGELUARAN) {
            model.showSave = false
            model.showTanggal = false
            model.showPenukaran = true
            model.statusSearch.selectedItem = StatusReturJual.BELUM_DIPROSES
            model.excludeDeleted = true
        }
        init()
        search()
    }

    void mvcGroupDestroy() {
    }

    def init = {
        execInsideUISync {
            model.nomorSearch = null
            model.konsumenSearch = null
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_JUAL_SALES)
        }
    }

    def search = {
        Boolean sudahDiproses = null
        execInsideUISync {
            if (model.statusSearch.selectedItem == StatusReturJual.SUDAH_DIPROSES) {
                sudahDiproses = true
            } else if (model.statusSearch.selectedItem == StatusReturJual.BELUM_DIPROSES) {
                sudahDiproses = false
            }
        }
        List result
        if (model.mode == ReturJualViewMode.INPUT) {
            result = returJualRepository.cariReturEceran(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.konsumenSearch, sudahDiproses, model.excludeDeleted)
        } else if (model.mode == ReturJualViewMode.PENGELUARAN) {
            result = returJualRepository.cariReturEceranUntukDiantar(model.nomorSearch, model.konsumenSearch, sudahDiproses)
        }
        execInsideUISync {
            model.returJualList.clear()
            model.returJualList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                return
            }
        }

        ReturJualEceran returJual = new ReturJualEceran(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            keterangan: model.keterangan, namaKonsumen: model.namaKonsumen, bisaDijualKembali: model.bisaDijualKembali)
        model.items.each { returJual.tambah(it) }
        if (!returJualRepository.validate(returJual, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                returJual = returJualRepository.buat(returJual)
                execInsideUISync {
                    model.returJualList << returJual
                    view.table.changeSelection(model.returJualList.size() - 1, 0, false, false)
                    clear()
                    cetak(returJual)
                }
            } else {
                // Update operation
                returJual = returJualRepository.update(returJual)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = returJual
                    clear()
                }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        } catch (StokTidakCukup ex) {
            model.errors['items'] = ex.message
        }
    }

    def prosesTukar = {
        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin barang retur yang ditukar telah diterima oleh konsumen?', 'Konfirmasi Tukar', JOptionPane.QUESTION_MESSAGE)) {
            return
        }
        ReturJualEceran returJual = view.table.selectionModel.selected[0]
        returJualRepository.tukar(returJual)
        execInsideUISync {
            model.returJualList.remove(returJual)
            clear()
        }
    }

    def prosesSemuaFaktur = {
        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin semua barang retur yang ditukar telah dikirim ke konsumen?', 'Konfirmasi Tukar', JOptionPane.QUESTION_MESSAGE)) {
            return
        }
        returJualRepository.prosesSemuaReturJualEceran()
        execInsideUISync {
            model.returJualList.clear()
            clear()
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        ReturJualEceran returJual = view.table.selectionModel.selected[0]
        returJual = returJualRepository.hapus(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    def showBarangRetur = {
        execInsideUISync {
            def args = [parentList: model.items, parent: view.table.selectionModel.selected[0], showPiutang: false, modusEceran: true]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemReturAsChild', args, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemReturList)
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'retur_jual_eceran.json']
            if (e instanceof ReturJualEceran) args.dataSource = e
            def dialogProps = [title: 'Preview Retur Jual', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_JUAL_SALES)
            model.tanggal = null
            model.keterangan = null
            model.items.clear()
            model.namaKonsumen = null
            model.bisaDijualKembali = false
            model.created = null
            model.createdBy = null
            model.modified = null
            model.modifiedBy = null
            model.deleted = false
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
                model.allowPenukaran = false
            } else {
                ReturJualEceran selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.items.clear()
                model.items.addAll(selected.items)
                model.namaKonsumen = selected.namaKonsumen
                model.bisaDijualKembali = (selected.bisaDijualKembali != null)? model.bisaDijualKembali: false
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                if (model.mode == ReturJualViewMode.PENGELUARAN && selected.deleted == 'N') {
                    model.allowPenukaran = true
                }
                model.deleted = (selected.deleted != 'N')
            }
        }
    }

}

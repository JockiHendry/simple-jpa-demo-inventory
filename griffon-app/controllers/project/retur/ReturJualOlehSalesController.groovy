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
import domain.exception.StokTidakCukup
import domain.retur.*
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ReturJualOlehSalesController {

    ReturJualOlehSalesModel model
    def view
    ReturJualRepository returJualRepository
    NomorService nomorService
    ReturJualService returJualService

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.mode: ReturJualViewMode.INPUT
        if (model.mode == ReturJualViewMode.INPUT) {
            model.showSave = true
            model.statusSearch.selectedItem = StatusReturJual.SEMUA
            model.showPiutang = true
            model.excludeDeleted = false
        } else if (model.mode == ReturJualViewMode.PENGELUARAN) {
            model.showSave = false
            model.statusSearch.selectedItem = StatusReturJual.BELUM_DIPROSES
            model.showPiutang = false
            model.excludeDeleted = true
        }
        List gudangResult = returJualRepository.findAllGudang([orderBy: 'nama'])
        execInsideUISync {
            model.nomorSearch = null
            model.konsumenSearch = null
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.gudangList.clear()
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_JUAL_SALES)
            model.gudangList.addAll(gudangResult)
        }
        search()
    }

    void mvcGroupDestroy() {
    }

    def init = {
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
        List result = returJualRepository.cariReturOlehSales(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.konsumenSearch, sudahDiproses, model.excludeDeleted)
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

        ReturJual returJual = new ReturJualOlehSales(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            keterangan: model.keterangan, konsumen: model.konsumen, gudang: model.gudang.selectedItem,
            bisaDijualKembali: model.bisaDijualKembali)
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
        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin barang retur yang ditukar telah diterima oleh konsumen?', 'Konfirmasi Penerimaan', JOptionPane.QUESTION_MESSAGE)) {
            return
        }
        ReturJual returJual = view.table.selectionModel.selected[0]
        if (returJual.pengeluaranBarang != null) {
            DialogUtils.message(view.mainPanel, 'Pengantaran sebelumnya telah dilakukan untuk retur ini!', 'Pesan kesalahan', JOptionPane.ERROR_MESSAGE)
            return
        }
        returJual = returJualRepository.tukar(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    @NeedSupervisorPassword
    def hapusPengeluaran = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        ReturJual returJual = view.table.selectionModel.selected[0]
        if (!returJual.pengeluaranBarang) {
            throw new IllegalStateException('Tidak ada pengantaran barang yang dapat dihapus!')
        }
        returJual = returJualRepository.hapusPengeluaranBarang(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        ReturJual returJual = view.table.selectionModel.selected[0]
        returJual = returJualRepository.hapus(returJual)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returJual
            clear()
        }
    }

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    DialogUtils.message(view.mainPanel, 'Tidak ada konsumen yang dipilih!', 'Cari Konsumen', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.konsumen = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def showBarangRetur = {
        execInsideUISync {
            def args = [parentList: model.items, parent: view.table.selectionModel.selected[0], parentGudang: model.gudang.selectedItem, parentKonsumen: model.konsumen, showPiutang: model.showPiutang]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemReturAsChild', args, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemReturList)
            }
        }
    }

    def showBarangYangHarusDikirim = {
        ReturJualOlehSales retur = returJualRepository.findReturJualOlehSalesById(view.table.selectionModel.selected[0].id)
        execInsideUISync {
            def args = [listItemBarang: retur.yangHarusDitukar().items, editable: false]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, props)
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'retur_jual_sales.json']
            if (e instanceof ReturJual) args.dataSource = e
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
            model.konsumen = null
            model.gudang.selectedItem = null
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
                ReturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.items.clear()
                model.items.addAll(selected.items)
                model.konsumen = selected.konsumen
                model.gudang.selectedItem = selected.gudang
                model.bisaDijualKembali = (selected.bisaDijualKembali != null)? selected.bisaDijualKembali: false
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
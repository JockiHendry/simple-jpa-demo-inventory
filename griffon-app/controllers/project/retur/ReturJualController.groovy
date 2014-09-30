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
import project.user.NomorService
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class ReturJualController {

    ReturJualModel model
    def view
    ReturJualRepository returJualRepository
    NomorService nomorService
    ReturJualService returJualService

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.mode: ReturJualViewMode.INPUT
        if (model.mode == ReturJualViewMode.INPUT) {
            model.showSave = true
            model.statusSearch.selectedItem = StatusReturJual.SEMUA
        } else if (model.mode == ReturJualViewMode.PENGELUARAN) {
            model.showSave = false
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
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.gudangList.clear()
        }
        model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_JUAL)
        List gudangResult = returJualRepository.findAllGudang([orderBy: 'nama'])
        execInsideUISync {
            model.gudangList.addAll(gudangResult)
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

        ReturJual returJual = new ReturJual(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, items: new ArrayList(model.items), konsumen: model.konsumen, gudang: model.gudang.selectedItem)
        returJual.listKlaimRetur.addAll(model.listKlaimRetur)
        if (model.potongan > 0) {
            returJual.tambah(new KlaimPotongan(model.potongan))
        }

        if (!model.potongan && model.listKlaimRetur.empty) {
            model.errors['listKlaimRetur'] = 'Barang yang di-klaim tidak boleh kosong bila tidak ada potongan piutang!'
            return
        }
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
        } catch (IllegalStateException ex) {
            model.errors['listKlaimRetur'] = ex.message
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

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, app, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada konsumen yang dipilih!', 'Cari Konsumen', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.konsumen = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def showBarangRetur = {
        execInsideUISync {
            def args = [listItemBarang: model.items, parent: view.table.selectionModel.selected[0], allowTambahProduk: false]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def autoCalculate = {
        if (!model.gudang.selectedItem) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus memilih gudang terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        if (model.items.empty) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus mengisi item retur terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        model.listKlaimRetur.clear()
        model.listKlaimRetur.addAll(returJualService.cariBarangYangBisaDitukar(model.items, model.gudang.selectedItem))
        model.potongan = returJualService.hitungPotonganPiutang(model.items, model.listKlaimRetur, model.konsumen)
    }

    def showKlaimRetur = {
        if (!model.konsumen) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus memilih konsumen terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [parentList: model.listKlaimRetur, parent: view.table.selectionModel.selected[0]]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('klaimReturAsChild', args, app, view, props) { m, v, c ->
                model.listKlaimRetur.clear()
                model.listKlaimRetur.addAll(m.klaimReturList)
            }
        }
        if (view.table.selectionModel.isSelectionEmpty()) {
            model.potongan = returJualService.hitungPotonganPiutang(model.items, model.listKlaimRetur, model.konsumen)
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'retur_jual.json']
            if (e instanceof ReturJual) args.dataSource = e
            def dialogProps = [title: 'Preview Retur Jual', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, app, view, dialogProps)
        }
    }
    
    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_JUAL)
            model.tanggal = null
            model.keterangan = null
            model.items.clear()
            model.listKlaimRetur.clear()
            model.potongan = null
            model.konsumen = null
            model.gudang.selectedItem = null
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
                model.allowPenukaran = false
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
                model.listKlaimRetur.addAll(selected.getKlaim(KlaimTukar))
                model.potongan = selected.getKlaim(KlaimPotongan).sum { it.potongan }
                model.konsumen = selected.konsumen
                model.gudang.selectedItem = selected.gudang
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                if (model.mode == ReturJualViewMode.PENGELUARAN && selected.deleted != 'Y') {
                    model.allowPenukaran = true
                }
            }
        }
    }

}
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
import domain.exception.DataTidakBolehDiubah
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

class ReturBeliController {

    ReturBeliModel model
    def view
    ReturBeliRepository returBeliRepository
    GudangRepository gudangRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.mode: ReturBeliViewMode.INPUT
        if (model.mode == ReturBeliViewMode.INPUT) {
            model.showSave = true
            model.showPenukaran = false
            model.statusSearch.selectedItem = StatusReturBeli.SEMUA
        } else if (model.mode == ReturBeliViewMode.PENERIMAAN) {
            model.showSave = false
            model.showPenukaran = true
            model.statusSearch.selectedItem = StatusReturBeli.BELUM_DIPROSES
        } else if (model.mode == ReturBeliViewMode.BAYAR) {
            model.showSave = false
            model.showPenukaran = false
            model.statusSearch.selectedItem = StatusReturBeli.BELUM_DIPROSES
        }
        model.forSupplier = args.forSupplier?: null
        listAll()
        search()
    }

    void mvcGroupDestroy() {
    }

    def listAll = {
        execInsideUISync {
            model.supplierList.clear()
        }
        model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_BELI)
        List supplierResult = returBeliRepository.findAllSupplier([orderBy: 'nama'])
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.nomorSearch = null
            model.supplierSearch = null
            model.supplierList.addAll(supplierResult)
        }
    }

    def search = {
        Boolean sudahDiproses = null
        if (model.statusSearch.selectedItem == StatusReturBeli.SUDAH_DIPROSES) {
            sudahDiproses = true
        } else if (model.statusSearch.selectedItem == StatusReturBeli.BELUM_DIPROSES) {
            sudahDiproses = false
        }
        List result
        if (model.forSupplier) {
            result = returBeliRepository.cariForSupplier(model.forSupplier, model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, sudahDiproses, !model.showSave)
        } else {
            result = returBeliRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.supplierSearch, sudahDiproses, !model.showSave)
        }
        execInsideUISync {
            model.returBeliList.clear()
            model.returBeliList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }
        ReturBeli returBeli = new ReturBeli(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, supplier: model.supplier.selectedItem)
        model.items.each { returBeli.tambah(it) }
        returBeli.nilaiPotonganHutang = model.potongan?: 0

        if (!returBeliRepository.validate(returBeli, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                returBeliRepository.buat(returBeli)
                execInsideUISync {
                    model.returBeliList << returBeli
                    view.table.changeSelection(model.returBeliList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                returBeli = returBeliRepository.update(returBeli)
                execInsideUISync { view.table.selectionModel.selected[0] = returBeli }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, ex.message, 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    def prosesTukar = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin barang retur yang ditukar telah diterima dari supplier?', 'Konfirmasi Penerimaan', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        ReturBeli returBeli = view.table.selectionModel.selected[0]
        returBeli = returBeliRepository.terima(returBeli)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returBeli
            clear()
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        ReturBeli returBeli = view.table.selectionModel.selected[0]
        returBeli = returBeliRepository.hapus(returBeli)
        execInsideUISync {
            view.table.selectionModel.selected[0] = returBeli
            clear()
        }
    }

    def showSeluruhItem = {
        ReturBeli returBeli = view.table.selectionModel.selected[0]
        if (returBeli) {
            execInsideUISync {
                def args = [listItemBarang: returBeli.toDaftarBarang().items, parent: returBeli, editable: false]
                def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
                DialogUtils.showMVCGroup('itemBarangAsChild', args, view, props)
            }
        }
    }

    def showKlaimRetur = {
        if (model.supplier.selectedItem == null) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus memilih supplier terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [parentList: model.items, parent: view.table.selectionModel.selected[0], supplierSearch: model.supplier.selectedItem]
            def props = [title: 'List Klaim Retur', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('kemasanRetur', args, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.kemasanReturList)
                if (m.parent) {
                    view.table.selectionModel.selected[0] = m.parent
                }
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'faktur_retur_beli.jasper']
            def dialogProps = [title: 'Preview Faktur Retur Beli', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewFaktur', args, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.RETUR_BELI)
            model.tanggal = null
            model.keterangan = null
            model.items.clear()
            model.potongan = null
            model.supplier.selectedItem = null
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
            } else {
                ReturBeli selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.items.clear()
                model.items.addAll(selected.items)
                model.potongan = selected.nilaiPotonganHutang
                model.supplier.selectedItem = selected.supplier
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                model.deleted = (selected.deleted != 'N')
            }
        }
    }

}
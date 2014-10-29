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
package project.servis

import ast.NeedSupervisorPassword
import domain.servis.*
import domain.inventory.*
import domain.inventory.*
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.user.NomorService
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PenerimaanServisController {

    PenerimaanServisModel model
    def view
    PenerimaanServisRepository penerimaanServisRepository
    NomorService nomorService
    GudangRepository gudangRepository

    void mvcGroupInit(Map args) {
        execInsideUISync {
            model.nomorSearch = null
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENERIMAAN_SERVIS)
            model.deleted = false
        }
        search()
    }

    void mvcGroupDestroy() {
    }

    def search = {
        List result = penerimaanServisRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch)
        execInsideUISync {
            model.penerimaanServisList.clear()
            model.penerimaanServisList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }
        PenerimaanServis penerimaanServis = new PenerimaanServis(id: model.id, keterangan: model.keterangan, tanggal: model.tanggal, nomor: model.nomor)
        penerimaanServis.gudang = gudangRepository.cariGudangUtama()
        penerimaanServis.items.addAll(model.items)

        if (!penerimaanServisRepository.validate(penerimaanServis, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                penerimaanServis = penerimaanServisRepository.buat(penerimaanServis)
                execInsideUISync {
                    model.penerimaanServisList << penerimaanServis
                    view.table.changeSelection(model.penerimaanServisList.size() - 1, 0, false, false)
                    clear()
                    cetak(penerimaanServis)
                }
            } else {
                // Update operation
                penerimaanServis = penerimaanServisRepository.update(penerimaanServis)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = penerimaanServis
                    clear()
                }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        } catch (IllegalArgumentException ex) {
            model.errors['items'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        PenerimaanServis penerimaanServis = view.table.selectionModel.selected[0]
        penerimaanServis = penerimaanServisRepository.hapus(penerimaanServis)
        execInsideUISync {
            view.table.selectionModel.selected[0] = penerimaanServis
            clear()
        }
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.items, allowTambahProduk: false, showReturOnly: true]
            def dialogProps = [title: 'Daftar Barang', size: new Dimension(400, 320)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'penerimaan_servis.json']
            if (e instanceof PenerimaanServis) args.dataSource = e
            def dialogProps = [title: 'Preview Penerimaan Servis', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, app, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENERIMAAN_SERVIS)
            model.items.clear()
            model.keterangan = null
            model.tanggal = null
            model.nomor = null
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
                PenerimaanServis selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.items.clear()
                model.items.addAll(selected.items)
                model.keterangan = selected.keterangan
                model.tanggal = selected.tanggal
                model.nomor = selected.nomor
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                model.deleted = (selected.deleted != 'N')
            }
        }
    }

}
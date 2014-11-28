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
package project.labarugi

import ast.NeedSupervisorPassword
import domain.exception.DataDuplikat
import domain.labarugi.*
import org.joda.time.LocalDate
import project.user.NomorService
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

@SuppressWarnings("GroovyUnusedDeclaration")
class TransaksiKasController {

    TransaksiKasModel model
    def view
    TransaksiKasRepository transaksiKasRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        List kategoriKasResult = transaksiKasRepository.findAllKategoriKas([orderBy: 'jenis,nama'])
        List jenisTransaksiKasResult = transaksiKasRepository.findAllJenisTransaksiKas([orderBy: 'nama'])
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.nomorSearch = null
            model.kategoriKasList.clear()
            model.kategoriKasList.addAll(kategoriKasResult)
            model.jenisTransaksiKasList.clear()
            model.jenisTransaksiKasList.addAll(jenisTransaksiKasResult)
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.TRANSAKSI_KAS)
        }
        search()
    }

    def search = {
        List result = transaksiKasRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch,
            model.pihakTerkaitSearch, model.kategoriKasSearch)
        execInsideUISync {
            model.transaksiKasList.clear()
            model.transaksiKasList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        TransaksiKas transaksiKas = new TransaksiKas(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            pihakTerkait: model.pihakTerkait, kategoriKas: model.kategoriKas.selectedItem, jumlah: model.jumlah,
            jenis: model.jenisTransaksiKas.selectedItem, keterangan: model.keterangan)

        if (!transaksiKasRepository.validate(transaksiKas, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                transaksiKasRepository.buat(transaksiKas)
                execInsideUISync {
                    model.transaksiKasList << transaksiKas
                    view.table.changeSelection(model.transaksiKasList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                transaksiKas = transaksiKasRepository.update(transaksiKas)
                execInsideUISync { view.table.selectionModel.selected[0] = transaksiKas }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        TransaksiKas transaksiKas = view.table.selectionModel.selected[0]
        transaksiKas = transaksiKasRepository.hapus(transaksiKas)
        execInsideUISync {
            view.table.selectionModel.selected[0] = transaksiKas
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.TRANSAKSI_KAS)
            model.tanggal = null
            model.pihakTerkait = null
            model.kategoriKas.selectedItem = null
            model.jumlah = null
            model.jenisTransaksiKas.selectedItem = null
            model.keterangan = null
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
                TransaksiKas selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.pihakTerkait = selected.pihakTerkait
                model.kategoriKas.selectedItem = selected.kategoriKas
                model.jumlah = selected.jumlah
                model.jenisTransaksiKas.selectedItem = selected.jenis
                model.keterangan = selected.keterangan
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                model.deleted = (selected.deleted == 'Y')
            }
        }
    }

}
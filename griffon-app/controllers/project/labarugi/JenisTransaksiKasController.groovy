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

import domain.exception.DataDuplikat
import domain.labarugi.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

@SuppressWarnings("GroovyUnusedDeclaration")
class JenisTransaksiKasController {

    JenisTransaksiKasModel model
    def view
    JenisTransaksiKasRepository jenisTransaksiKasRepository

    void mvcGroupInit(Map args) {
        search()
    }

    def search = {
        List result = jenisTransaksiKasRepository.cari(model.namaSearch)
        execInsideUISync {
            model.jenisTransaksiKasList.clear()
            model.jenisTransaksiKasList.addAll(result)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        JenisTransaksiKas jenisTransaksiKas = new JenisTransaksiKas(id: model.id, nama: model.nama, sistem: model.sistem)

        if (!jenisTransaksiKasRepository.validate(jenisTransaksiKas, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                jenisTransaksiKasRepository.buat(jenisTransaksiKas)
                execInsideUISync {
                    model.jenisTransaksiKasList << jenisTransaksiKas
                    view.table.changeSelection(model.jenisTransaksiKasList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                jenisTransaksiKas = jenisTransaksiKasRepository.update(jenisTransaksiKas)
                execInsideUISync { view.table.selectionModel.selected[0] = jenisTransaksiKas }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        JenisTransaksiKas jenisTransaksiKas = view.table.selectionModel.selected[0]
        jenisTransaksiKasRepository.remove(jenisTransaksiKas)
        execInsideUISync {
            model.jenisTransaksiKasList.remove(jenisTransaksiKas)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.sistem = false
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
                JenisTransaksiKas selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.sistem = selected.sistem
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
            }
        }
    }

}
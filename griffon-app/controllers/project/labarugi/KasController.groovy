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
import domain.labarugi.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class KasController {

    KasModel model
    def view
    KasRepository kasRepository

    void mvcGroupInit(Map args) {
        listAll()
    }

    void mvcGroupDestroy() {
    }

    def listAll = {
        List kasResult = kasRepository.findAllKas()
        execInsideUISync {
            model.kasList.clear()
            model.kasList.addAll(kasResult)
        }
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }
        Kas kas = new Kas(id: model.id, nama: model.nama, sistem: model.sistem)
        if (!kasRepository.validate(kas, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                kasRepository.buat(kas)
                execInsideUISync {
                    model.kasList << kas
                    view.table.changeSelection(model.kasList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                kas = kasRepository.update(kas)
                execInsideUISync { view.table.selectionModel.selected[0] = kas }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['jumlah'] = app.getMessage('simplejpa.error.alreadyExist.message')
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
        Kas kas = view.table.selectionModel.selected[0]
        kas = kasRepository.hapus(kas)
        execInsideUISync {
            view.table.selectionModel.selected[0] = kas
            clear()
        }
    }

    def showPeriodeKas = {
        execInsideUISync {
            def args = [kas: view.table.selectionModel.selected[0]]
            def props = [title: 'Transaksi Kas', preferredSize: new Dimension(900, 620)]
            DialogUtils.showMVCGroup('transaksiKas', args, view, props) { m, v, c ->
                view.table.selectionModel.selected[0] = m.kas
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.jumlah = null
            model.nama = null
            model.listPeriodeRiwayat.clear()
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
                Kas selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.jumlah = selected.jumlah
                model.nama = selected.nama
                model.listPeriodeRiwayat.clear()
                model.listPeriodeRiwayat.addAll(selected.listPeriodeRiwayat)
                model.sistem = selected.sistem
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
            }
        }
    }

}
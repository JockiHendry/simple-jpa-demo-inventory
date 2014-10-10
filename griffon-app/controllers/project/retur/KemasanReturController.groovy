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

import domain.retur.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class KemasanReturController {

    KemasanReturModel model
    def view
    ReturBeliRepository returBeliRepository

    void mvcGroupInit(Map args) {
        model.parent = args.parent
        model.supplierSearch = args.supplierSearch
        model.kemasanReturList.clear()
        model.kemasanReturList.addAll(args.parentList)
        model.nomor = model.kemasanReturList.size() + 1
    }

    def save = {
        if (model.id != null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }
        Kemasan kemasanRetur = new Kemasan(id: model.id, nomor: model.nomor, tanggal: model.tanggal, keterangan: model.keterangan, items: new ArrayList(model.items))

        if (!returBeliRepository.validate(kemasanRetur, Default, model)) return

        try {
            if (model.id == null) {
                // Insert operation
                execInsideUISync {
                    model.kemasanReturList << kemasanRetur
                    view.table.changeSelection(model.kemasanReturList.size() - 1, 0, false, false)
                }
            } else {
                // Update operation
                execInsideUISync { view.table.selectionModel.selected[0] = kemasanRetur }
            }
        } catch (DuplicateEntityException ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
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
        Kemasan kemasanRetur = view.table.selectionModel.selected[0]
        execInsideUISync {
            model.kemasanReturList.remove(kemasanRetur)
            clear()
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    def showItemBarang = {
        execInsideUISync {
            def args = [listItemBarang: model.items, parent: view.table.selectionModel.selected[0], allowTambahProduk: false, showReturOnly: true, supplierSearch: model.supplierSearch]
            def props = [title: 'Items', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, props) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def cetak = { e ->
        if (!model.parent) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Tidak dapat mencetak kemasan retur jual yang belum disimpan!', 'Kesalahan Cetak', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'kemasan_retur.json', options: ['nomorRetur': model.parent.nomor]]
            def dialogProps = [title: 'Preview Kemasan Retur Beli', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, app, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = model.kemasanReturList.size() + 1
            model.tanggal = null
            model.keterangan = null
            model.items.clear()
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
                Kemasan selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.items.clear()
                model.items.addAll(selected.items)
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
            }
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
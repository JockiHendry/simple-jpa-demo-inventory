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
import domain.inventory.*
import project.inventory.ProdukController
import project.retur.*
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class KlaimReturAsChildController {

    KlaimReturAsChildModel model
    def view
    ReturJualRepository returJualRepository

    void mvcGroupInit(Map args) {
        model.parent = args.parent
        model.editable = args.containsKey('editable')? args.'editable': (model.parent?.id == null)
        model.showReturOnly = args.containsKey('showReturOnly')? args.'showReturOnly': false
        model.supplierSearch = args.containsKey('supplierSearch')? args.'supplierSearch': null
        model.klaimReturList.addAll(args.'parentList' ?: [])
    }

    void mvcGroupDestroy() {
    }

    def save = {
        if (!view.table.selectionModel.selectionEmpty) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        KlaimRetur klaimRetur = new KlaimRetur(produk: model.produk, jumlah: model.jumlah)

        if (!returJualRepository.validate(klaimRetur, Default, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            // Insert operation
            execInsideUISync {
                model.klaimReturList << klaimRetur
                view.table.changeSelection(model.klaimReturList.size() - 1, 0, false, false)
            }
        } else {
            // Update operation
            KlaimRetur selectedKlaimRetur = view.table.selectionModel.selected[0]
            selectedKlaimRetur.produk = model.produk
            selectedKlaimRetur.jumlah = model.jumlah
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
        KlaimRetur klaimRetur = view.table.selectionModel.selected[0]
        execInsideUISync {
            model.klaimReturList.remove(klaimRetur)
            clear()
        }
    }

    def showProduk = {
        Produk produk = ProdukController.displayProdukPopup(view, false, model.showReturOnly, model.supplierSearch)
        if (produk) {
            model.produk = produk
            view.jumlah.requestFocusInWindow()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.produk = null
            model.jumlah = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                KlaimRetur selected = view.table.selectionModel.selected[0]
                model.errors.clear()

                model.produk = selected.produk
                model.jumlah = selected.jumlah
            }
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
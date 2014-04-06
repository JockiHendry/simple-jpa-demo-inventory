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
package project

import domain.Container
import domain.inventory.ItemBarang
import domain.inventory.Produk
import simplejpa.swing.DialogUtils

import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Window

class ItemBarangAsChildController {

    ItemBarangAsChildModel model
    def view

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.editable = args.containsKey('editable')? args.'editable': (model.parent?.id == null)
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        execInsideUISync {
            model.itemBarangList.clear()
            model.itemBarangList.addAll(args.'listItemBarang')
        }
    }

    def save = {
        ItemBarang itemBarang = new ItemBarang(produk: model.produk, jumlah: model.jumlah)

        if (!Container.app.purchaseOrderRepository.validate(itemBarang, Default, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            execInsideUISync {
                model.itemBarangList << itemBarang
                view.table.changeSelection(model.itemBarangList.size()-1, 0, false, false)
                clear()
            }
        } else {
            execInsideUISync {
                view.table.selectionModel.selected[0] = itemBarang
                clear()
            }
        }
    }

    def showProduk = {
        Produk produk = ProdukController.displayProdukPopup(view, model.allowTambahProduk)
        if (produk) {
            model.produk = produk
            view.jumlah.requestFocusInWindow()
        }
    }

    def delete = {
        execInsideUISync {
            model.itemBarangList.remove(view.table.selectionModel.selected[0])
            clear()
        }
    }

    def clear = {
        execInsideUISync {
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
                ItemBarang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.produk = selected.produk
                model.jumlah = selected.jumlah
            }
        }
    }

}

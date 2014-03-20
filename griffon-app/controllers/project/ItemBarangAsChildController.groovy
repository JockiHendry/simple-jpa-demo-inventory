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

    static MVCGroup produkPopupMVC

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.editable = !model.parent?.id
        model.itemBarangList.clear()
        model.itemBarangList.addAll(args.'listItemBarang')
    }

    def save = {
        ItemBarang itemBarang = new ItemBarang(produk: model.produk, jumlah: model.jumlah)

        if (!Container.app.penerimaanBarangRepository.validate(itemBarang, Default, model)) return

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
        if (!produkPopupMVC) {
            produkPopupMVC = app.mvcGroupManager.buildMVCGroup('produk', 'produkPopupDiItemBarang', [popup: true])
        }

        def m = produkPopupMVC.model
        def v = produkPopupMVC.view
        def c = produkPopupMVC.controller

        Window thisWindow = SwingUtilities.getWindowAncestor(view.mainPanel)
        JDialog dialog = new JDialog(thisWindow, Dialog.ModalityType.APPLICATION_MODAL)
        if (DialogUtils.defaultContentDecorator) {
            dialog.contentPane = DialogUtils.defaultContentDecorator(v.mainPanel)
        } else {
            dialog.contentPane = v.mainPanel
        }
        dialog.pack()
        dialog.title = "Cari Produk"
        dialog.size = new Dimension(900, 420)
        dialog.setLocationRelativeTo(thisWindow)
        dialog.setVisible(true)

        // Setelah dialog selesai ditampilkan
        if (v.table.selectionModel.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
        } else {
            model.produk = v.view.table.selectionModel.selected[0]
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
/*
 * Copyright 2015 Jocki Hendry.
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
package project.inventory

import domain.inventory.ItemPenyesuaian
import domain.inventory.Produk
import project.pembelian.PurchaseOrderRepository
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ItemPenyesuaianAsChildController {

    ItemPenyesuaianAsChildModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.editable = true
        execInsideUISync {
            model.itemPenyesuaianList.clear()
            model.itemPenyesuaianList.addAll(args.'listItemPenyesuaian')
        }
    }

    def save = {
        ItemPenyesuaian itemPenyesuaian = new ItemPenyesuaian(produk: model.produk, jumlah: model.jumlah, harga: model.harga)

        if (!purchaseOrderRepository.validate(itemPenyesuaian, Default, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            execInsideUISync {
                model.itemPenyesuaianList << itemPenyesuaian
                view.table.changeSelection(model.itemPenyesuaianList.size()-1, 0, false, false)
                clear()
            }
        } else {
            execInsideUISync {
                view.table.selectionModel.selected[0] = itemPenyesuaian
                clear()
            }
        }
    }

    def showProduk = {
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: false]
            def dialogProps = [title: 'Cari Produk', preferredSize: new Dimension(900, 600)]
            Produk produk = null
            DialogUtils.showMVCGroup('produk', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    DialogUtils.message(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
                } else {
                    produk = v.view.table.selectionModel.selected[0]
                }
                model.produk = produk
                view.jumlah.requestFocusInWindow()
            }
        }
    }

    def delete = {
        execInsideUISync {
            model.itemPenyesuaianList.remove(view.table.selectionModel.selected[0])
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.produk = null
            model.jumlah = null
            model.harga = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                ItemPenyesuaian selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.produk = selected.produk
                model.jumlah = selected.jumlah
                model.harga = selected.harga
            }
        }
    }

}

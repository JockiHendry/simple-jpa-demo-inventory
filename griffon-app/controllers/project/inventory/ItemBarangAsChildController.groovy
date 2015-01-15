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

import domain.inventory.ItemBarang
import domain.inventory.Produk
import project.main.MainGroupModel
import project.main.MainGroupView
import project.pembelian.PurchaseOrderRepository
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ItemBarangAsChildController {

    ItemBarangAsChildModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.editable = args.containsKey('editable')? args.'editable': (model.parent?.id == null)
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        model.showReturOnly = args.containsKey('showReturOnly')? args.'showReturOnly': false
        model.supplierSearch = args.containsKey('supplierSearch')? args.'supplierSearch': null
        execInsideUISync {
            model.itemBarangList.clear()
            model.itemBarangList.addAll(args.'listItemBarang')
        }
    }

    def save = {
        ItemBarang itemBarang = new ItemBarang(produk: model.produk, jumlah: model.jumlah)

        if (!purchaseOrderRepository.validate(itemBarang, Default, model)) return

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
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: model.allowTambahProduk, showReturOnly: model.showReturOnly, supplierSearch: model.supplierSearch]
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

    def tampilkanProduk = {
        ItemBarang itemBarang = view.table.selectionModel.selected[0]
        if (itemBarang) {
            MainGroupView mainView = app.getMvcGroupManager()['mainGroup'].view
            MainGroupModel mainModel = app.getMvcGroupManager()['mainGroup'].model
            if (mainModel.produkVisible) {
                mainView.mainTab.addMVCTab('produk', [namaSearch: itemBarang.produk.nama], "${itemBarang.produk.nama}")
            }
        }
    }

}

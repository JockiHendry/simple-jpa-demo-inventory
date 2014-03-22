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
import domain.faktur.Diskon
import domain.faktur.ItemFaktur
import domain.inventory.Produk

import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class ItemFakturAsChildController {

    ItemFakturAsChildModel model
    def view

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.editable = !model.parent?.id
        model.itemFakturList.clear()
        model.itemFakturList.addAll(args.'listItemFaktur')
    }

    def save = {
        ItemFaktur itemFaktur = new ItemFaktur(produk: model.produk, jumlah: model.jumlah, harga: model.harga, keterangan: model.keterangan)
        itemFaktur.diskon = new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung)

        if (!Container.app.fakturBeliRepository.validate(itemFaktur, Default, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            execInsideUISync {
                model.itemFakturList << itemFaktur
                view.table.changeSelection(model.itemFakturList.size()-1, 0, false, false)
                clear()
            }
        } else {
            execInsideUISync {
                view.table.selectionModel.selected[0] = itemFaktur
                clear()
            }
        }
    }

    def showProduk = {
        Produk produk = ProdukController.displayProdukPopup(view)
        if (produk) {
            model.produk = produk
            view.jumlah.requestFocusInWindow()
        }
    }

    def delete = {
        execInsideUISync {
            model.itemFakturList.remove(view.table.selectionModel.selected[0])
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.produk = null
            model.jumlah = null
            model.harga = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                ItemFaktur selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.produk = selected.produk
                model.jumlah = selected.jumlah
                model.harga = selected.harga
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
            }
        }
    }

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

    /*
        Remember that actions will be called outside of the UI thread
        by default. You can change this setting of course.
        Please read chapter 9 of the Griffon Guide to know more.
       
    def action = { evt = null ->
    }
    */
}

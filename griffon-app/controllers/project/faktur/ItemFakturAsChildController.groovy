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
package project.faktur

import domain.faktur.Diskon
import domain.faktur.ItemFaktur
import domain.inventory.Produk
import domain.pembelian.FakturBeli
import domain.pembelian.PurchaseOrder
import project.main.MainGroupModel
import project.main.MainGroupView
import project.pembelian.PurchaseOrderRepository
import project.penjualan.KonsumenRepository
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ItemFakturAsChildController {

    ItemFakturAsChildModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository
    KonsumenRepository konsumenRepository

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.konsumen = args.'konsumen'
        model.editable = args.containsKey('editable')? args.'editable': !model.parent?.id
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        model.showHarga = args.containsKey('showHarga')? args.'showHarga': true
        model.itemFakturList.clear()
        model.itemFakturList.addAll(args.'listItemFaktur')
    }

    def save = {
        ItemFaktur itemFaktur = new ItemFaktur(produk: model.produk, jumlah: model.jumlah, harga: model.harga, keterangan: model.keterangan)
        itemFaktur.diskon = new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung)

        if (!purchaseOrderRepository.validate(itemFaktur, Default, model)) return

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
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: model.allowTambahProduk, showReturOnly: false, supplierSearch: null]
            def dialogProps = [title: 'Cari Produk', preferredSize: new Dimension(1024, 600)]
            Produk produk = null
            DialogUtils.showMVCGroup('produk', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    DialogUtils.message(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
                    return
                } else {
                    produk = v.view.table.selectionModel.selected[0]
                }
                model.produk = produk
                if ((model.parent == null) || (model.parent instanceof PurchaseOrder) || (model.parent instanceof FakturBeli)) {
                    if (model.konsumen) {
                        model.harga = konsumenRepository.hargaTerakhir(model.konsumen, model.produk)
                    } else {
                        model.harga = produk.hargaDalamKota
                    }
                }
                view.jumlah.requestFocusInWindow()
            }
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

    def tampilkanProduk = {
        ItemFaktur itemFaktur = view.table.selectionModel.selected[0]
        if (itemFaktur) {
            MainGroupView mainView = app.getMvcGroupManager()['mainGroup'].view
            MainGroupModel mainModel = app.getMvcGroupManager()['mainGroup'].model
            if (mainModel.produkVisible) {
                mainView.mainTab.addMVCTab('produk', [namaSearch: itemFaktur.produk.nama], "${itemFaktur.produk.nama}")
            }
        }
    }

}

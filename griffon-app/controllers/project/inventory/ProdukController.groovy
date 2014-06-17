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

package project.inventory

import domain.Container
import domain.exception.DataDuplikat
import domain.inventory.Produk
import domain.inventory.ProdukRepository
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction

import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Window

class ProdukController {

    ProdukModel model
    def view

    ProdukRepository produkRepository

    public static Produk displayProdukPopup(view, boolean allowTambahProduk = true) {
        def args = [popup: true, allowTambahProduk: allowTambahProduk]
        def dialogProps = [title: 'Cari Produk', size: new Dimension(900,420)]
        Produk result = null
        DialogUtils.showMVCGroup('produk', args, ApplicationHolder.application, view, dialogProps) { m, v, c ->
            if (v.table.selectionModel.isSelectionEmpty()) {
                JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
            } else {
                result = v.view.table.selectionModel.selected[0]
            }
        }
        result
    }

    void mvcGroupInit(Map args) {
        model.popupMode = args.'popup'?: false
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        produkRepository = Container.app.produkRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.satuanList.clear()
        }
        List satuan = produkRepository.findAllSatuan()
        execInsideUISync {
            model.satuanList.addAll(satuan)
        }
    }

    def search = {
        List produkResult = produkRepository.cari(model.namaSearch)
        execInsideUISync {
            model.produkList.clear()
            model.produkList.addAll(produkResult)
            model.namaSearch = null
        }
    }

    def save = {
        Produk produk = new Produk(id: model.id, nama: model.nama, hargaDalamKota: model.hargaDalamKota,
            hargaLuarKota: model.hargaLuarKota, satuan: model.satuan.selectedItem, poin: model.poin, levelMinimum: model.levelMinimum)
        if (!produkRepository.validate(produk, Default, model)) return

        try {
            if (produk.id==null) {
                produkRepository.buat(produk)
                execInsideUISync {
                    model.produkList << produk
                    view.table.changeSelection(model.produkList.size()-1, 0, false, false)
                    clear()
                }
            } else {
                produk = produkRepository.update(produk)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = produk
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Produk produk = view.table.selectionModel.selected[0]
		produkRepository.remove(produk)
        execInsideUISync {
            model.produkList.remove(produk)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
			model.nama = null
			model.hargaDalamKota = null
            model.hargaLuarKota = null
            model.satuan.selectedItem = null
            model.poin = null
            model.levelMinimum = null
			model.daftarStok.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def showStokProduk = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0]]
            def dialogProps = [title: 'Stok Produk', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('stokProduk', args, app, view, dialogProps)
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Produk selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
				model.nama = selected.nama
				model.hargaDalamKota = selected.hargaDalamKota
                model.hargaLuarKota = selected.hargaLuarKota
                model.satuan.selectedItem = selected.satuan
                model.poin = selected.poin
                model.levelMinimum = selected.levelMinimum
				model.daftarStok.clear()
				model.daftarStok.addAll(selected.daftarStok)
            }
        }
    }

}
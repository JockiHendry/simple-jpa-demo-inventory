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

import ast.NeedSupervisorPassword
import domain.exception.DataDuplikat
import domain.inventory.Produk
import domain.pembelian.Supplier
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class ProdukController {

    ProdukModel model
    def view

    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.popupMode = args.'popup'?: false
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        model.showReturOnly = args.containsKey('showReturOnly')? args.'showReturOnly': false
        model.supplierSearch = args.containsKey('supplierSearch')? args.'supplierSearch': null
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.satuanList.clear()
            model.supplierList.clear()
        }
        List satuan = produkRepository.findAllSatuan([orderBy: 'nama'])
        List supplier = produkRepository.findAllSupplier([orderBy: 'nama'])
        execInsideUISync {
            model.satuanList.addAll(satuan)
            model.supplierList.addAll(supplier)
        }
    }

    def search = {
        List produkResult = produkRepository.cari(model.namaSearch, model.showReturOnly, model.supplierSearch)
        execInsideUISync {
            model.produkList.clear()
            model.produkList.addAll(produkResult)
            model.namaSearch = null
        }
    }

    def save = {
        Produk produk = new Produk(id: model.id, nama: model.nama, hargaDalamKota: model.hargaDalamKota, supplier: model.supplier.selectedItem,
            hargaLuarKota: model.hargaLuarKota, satuan: model.satuan.selectedItem, poin: model.poin, levelMinimum: model.levelMinimum, jumlahAkanDikirim: 0)
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

    @NeedSupervisorPassword
    def delete = {
        Produk produk = view.table.selectionModel.selected[0]
		produkRepository.remove(produk)
        execInsideUISync {
            model.produkList.remove(produk)
            clear()
        }
    }

    def refreshJumlahAkanDikirim = {
        produkRepository.refreshJumlahAkanDikirim()
        search()
        JOptionPane.showMessageDialog(view.mainPanel, 'Jumlah akan dikirim untuk seluruh produk sudah diperbaharui!', 'Sukses', JOptionPane.INFORMATION_MESSAGE)
    }
    
    @NeedSupervisorPassword
    def showUbahJumlahRetur = {
        String input = JOptionPane.showInputDialog(view.mainPanel, 'Masukkan nilai qty retur baru:', 'Input Qty Retur', JOptionPane.QUESTION_MESSAGE)
        try {
            Integer qtyRetur = new Integer(input)
            Produk produk = view.table.selectionModel.selected[0]
            produk = produkRepository.aturJumlahRetur(produk, qtyRetur)
            execInsideUISync {
                view.table.selectionModel.selected[0] = produk
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Nilai qty retur bukan nilai yang valid!', 'Kesalahan Input', JOptionPane.ERROR_MESSAGE)
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
            model.jumlahAkanDikirim = null
            model.supplier.selectedItem = null
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
                model.supplier.selectedItem = selected.supplier
                model.poin = selected.poin
                model.levelMinimum = selected.levelMinimum
                model.jumlahAkanDikirim = selected.jumlahAkanDikirim
				model.daftarStok.clear()
				model.daftarStok.addAll(selected.daftarStok)
            }
        }
    }

}
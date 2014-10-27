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
import domain.validation.InputReturJual
import project.retur.*
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class ItemReturAsChildController {

    ItemReturAsChildModel model
    def view
    ReturJualRepository returJualRepository
    ReturJualService returJualService

    void mvcGroupInit(Map args) {
        model.itemReturList.addAll(args.'parentList' ?: [])
        model.parent = args.parent
        if (model.parent) model.editable = false
        model.parentGudang = args.parentGudang
        model.parentKonsumen = args.parentKonsumen
        model.showPiutang = args.containsKey('showPiutang')? args.showPiutang: true
        model.modusEceran = args.containsKey('modusEceran')? args.modusEceran: false
    }

    void mvcGroupDestroy() {
    }

    def save = {
        if (!view.table.selectionModel.selectionEmpty) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        ItemRetur itemRetur = new ItemRetur(id: model.id, produk: model.produk, jumlah: model.jumlah, klaims: new HashSet(model.klaims))

        if (!returJualRepository.validate(itemRetur, InputReturJual, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            // Insert operation
            execInsideUISync {
                model.itemReturList << itemRetur
                view.table.changeSelection(model.itemReturList.size() - 1, 0, false, false)
            }
        } else {
            // Update operation
            ItemRetur selectedItemRetur = view.table.selectionModel.selected[0]
            selectedItemRetur.produk = model.produk
            selectedItemRetur.jumlah = model.jumlah
            selectedItemRetur.klaims.clear()
            selectedItemRetur.klaims.addAll(model.klaims)
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
        ItemRetur itemRetur = view.table.selectionModel.selected[0]
        execInsideUISync {
            model.itemReturList.remove(itemRetur)
            clear()
        }
    }

    def showKlaim = {
        execInsideUISync {
            def args = [parentList: model.klaims, parent: view.table.selectionModel.selected[0]]
            def props = [title: 'Klaims', preferredSize: new Dimension(900, 300)]
            DialogUtils.showMVCGroup('klaimAsChild', args, app, view, props) { m, v, c ->
                model.klaims.clear()
                model.klaims.addAll(m.klaimList)
                ItemRetur itemRetur = view.table.selectionModel.selected[0]
                if (itemRetur) {
                    itemRetur.klaims.clear()
                    m.klaimList.each { itemRetur.tambahKlaim(it) }
                }
            }
        }
    }

    def showProduk = {
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: false]
            def dialogProps = [title: 'Cari Produk', preferredSize: new Dimension(900, 600)]
            Produk produk = null
            DialogUtils.showMVCGroup('produk', args, ApplicationHolder.application, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
                } else {
                    produk = v.view.table.selectionModel.selected[0]
                }
                model.produk = produk
                view.jumlah.requestFocusInWindow()
            }
        }
    }

    def autoKlaim = {
        prosesAutoKlaim()
    }

    def autoKlaimPiutang = {
        prosesAutoKlaim(true)
    }

    def prosesAutoKlaim = { boolean hanyaPiutang = false ->
        if (!model.modusEceran && !model.parent && (!model.parentGudang || !model.parentKonsumen)) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Untuk memakai fasilitas auto klaim, Anda harus memilih gudang dan konsumen terlebih dahulu!', 'Data Tidak Lengkap', JOptionPane.ERROR_MESSAGE)
            return
        }
        ReturJual returJual
        if (model.modusEceran) {
            returJual = new ReturJualEceran()
        } else {
            returJual = new ReturJualOlehSales(gudang: model.parent ? model.parent.gudang : model.parentGudang, konsumen: model.parent ? model.parent.konsumen : model.parentKonsumen)
        }
        model.itemReturList.each { returJual.tambah(it) }
        if (hanyaPiutang) {
            returJualService.potongPiutang(returJual)
        } else {
            returJualService.autoKlaim(returJual)
        }
        execInsideUISync {
            view.table.repaint()
            clear()
            JOptionPane.showMessageDialog(view.mainPanel, 'Rencana klaim sudah ditentukan secara otomatis!', 'Informasi', JOptionPane.INFORMATION_MESSAGE)
        }
    }

    def resetKlaim = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin ingin menghapus rencana klaim retur yang sudah dibuat?', 'Konfirmasi Reset', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        model.itemReturList.each { it.klaims.clear() }
        execInsideUISync {
            view.table.repaint()
            clear()
            JOptionPane.showMessageDialog(view.mainPanel, 'Rencana klaim yang ada sudah dihapus semua!', 'Informasi', JOptionPane.INFORMATION_MESSAGE)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.produk = null
            model.jumlah = null
            model.klaims.clear()
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                ItemRetur selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.produk = selected.produk
                model.jumlah = selected.jumlah
                model.klaims.clear()
                model.klaims.addAll(selected.klaims)
            }
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
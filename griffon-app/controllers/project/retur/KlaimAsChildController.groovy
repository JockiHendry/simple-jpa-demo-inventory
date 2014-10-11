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

import domain.inventory.Produk
import domain.retur.*
import project.penjualan.JenisPencairanPoin
import project.retur.*
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class KlaimAsChildController {

    KlaimAsChildModel model
    def view
    ReturJualRepository returJualRepository

    void mvcGroupInit(Map args) {
        model.parent = args.parent
        if (model.parent?.id) model.editable = false
        model.klaimList.addAll(args.'parentList' ?: [])
        model.jenisKlaim.selectedItem = JenisKlaim.POTONG_PIUTANG
        model.produkVisible = false
    }

    void mvcGroupDestroy() {
    }

    def save = {
        if (!view.table.selectionModel.selectionEmpty) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

        Klaim klaim
        if (model.jenisKlaim.selectedItem == JenisKlaim.POTONG_PIUTANG) {
            klaim = new KlaimPotongPiutang(model.jumlah)
        } else if (model.jenisKlaim.selectedItem == JenisKlaim.TUKAR_BARANG) {
            klaim = new KlaimTukar(model.produk, model.jumlah as Integer)
        }

        if (!returJualRepository.validate(klaim, Default, model)) return

        if (view.table.selectionModel.selectionEmpty) {
            // Insert operation
            execInsideUISync {
                model.klaimList << klaim
                view.table.changeSelection(model.klaimList.size() - 1, 0, false, false)
            }
        } else {
            // Update operation
            Klaim selectedKlaim = view.table.selectionModel.selected[0]
            if (selectedKlaim instanceof KlaimPotongPiutang) {
                selectedKlaim.jumlah = model.jumlah
            } else if (selectedKlaim instanceof KlaimTukar) {
                selectedKlaim.produk = model.produk
                selectedKlaim.jumlah = model.jumlah
            }
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
        Klaim klaim = view.table.selectionModel.selected[0]
        execInsideUISync {
            model.klaimList.remove(klaim)
            clear()
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

    def onPerubahanJenisKlaim = {
        if (model.jenisKlaim.selectedItem == JenisKlaim.POTONG_PIUTANG) {
            model.produkVisible = false
        } else if (model.jenisKlaim.selectedItem == JenisKlaim.TUKAR_BARANG) {
            model.produkVisible = true
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
                Klaim selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                if (selected instanceof KlaimTukar) {
                    model.jenisKlaim.selectedItem = JenisKlaim.TUKAR_BARANG
                    model.produk = selected.produk
                    model.jumlah = selected.jumlah
                } else if (selected instanceof KlaimPotongPiutang) {
                    model.jenisKlaim.selectedItem = JenisKlaim.POTONG_PIUTANG
                    model.jumlah = selected.jumlah
                }
            }
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
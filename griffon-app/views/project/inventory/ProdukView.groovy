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

import domain.inventory.Gudang
import org.jdesktop.swingx.prompt.PromptSupport
import simplejpa.SimpleJpaUtil
import simplejpa.swing.DialogUtils
import javax.swing.JTable
import javax.swing.SwingUtilities
import java.awt.event.KeyEvent
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import java.awt.*

actions {
    action(id: 'pilih', name: 'Pilih', mnemonic: KeyEvent.VK_P, closure: {
        if (model.popupMode) {
            SwingUtilities.getWindowAncestor(mainPanel).visible = false
        } else {
            showStokProduk.actionPerformed(null)
        }
    })
    action(id: 'showStokProduk', name: 'Stok Produk...', closure: controller.showStokProduk)
    action(id: 'showNilaiInventory', name: 'Nilai Inventory...', closure: controller.showNilaiInventory)
    action(id: 'showUbahQty', name: 'Ubah Qty...', closure: controller.showUbahQty)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        borderLayout()
        label('<html><b>Petunjuk:</b> <i>Cari dan pilih jenis pekerjaan di tabel dan tekan Enter untuk selesai!</i></html>',
            visible: bind{model.popupMode}, horizontalAlignment: CENTER, constraints: PAGE_START)
        panel(constraints: CENTER) {
            flowLayout(alignment: FlowLayout.LEADING)
            textField(id: 'namaSearch', columns: 20, text: bind('namaSearch', target: model, mutual: true), actionPerformed: controller.search,
                keyPressed: { KeyEvent k ->
                    if (k.keyCode==KeyEvent.VK_DOWN) table.requestFocusInWindow()
                }
            )
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }
    }

    panel(constraints: CENTER) {
        borderLayout()
        panel(constraints: PAGE_START, layout: new FlowLayout(FlowLayout.LEADING)) {
            label(text: bind('searchMessage', source: model))
        }
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.produkList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: pilih, enterKeyAction: pilih, autoResizeMode: JTable.AUTO_RESIZE_OFF) {
                glazedColumn(name: 'Nama', property: 'nama', preferredWidth: 300)
                glazedColumn(name: 'Supplier', expression: { it.supplier?.nama?: ''}, preferredWidth: 200)
                glazedColumn(name: 'HET Dalam Kota', property: 'hargaDalamKota', preferredWidth: 110, columnClass: Integer) {
                    templateRenderer('this:currencyFormat', horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'HET Luar Kota', property: 'hargaLuarKota', preferredWidth: 110, columnClass: Integer) {
                    templateRenderer('this:currencyFormat', horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Ongkos Kirim Beli', property: 'ongkosKirimBeli', preferredWidth: 110, columnClass: Integer) {
                    templateRenderer('this:currencyFormat', horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Satuan', expression: { it.satuan.singkatan }, preferredWidth: 50)
                glazedColumn(name: 'Poin', property: 'poin', columnClass: Integer, preferredWidth: 50, visible: bind {!model.popupMode}) {
                    templateRenderer('this:numberFormat', horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Stok Level Minimum', property: 'levelMinimum', preferredWidth: 80, columnClass: Integer, visible: bind {!model.popupMode}) {
                    templateRenderer('this:numberFormat', horizontalAlignment: RIGHT)
                }
                SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang').findAllGudang().each { Gudang g ->
                    glazedColumn(name: "Qty ${g.nama}", expression: { it.stok(g).jumlah }, columnClass: Integer, preferredWidth: 80) {
                        templateRenderer('this:numberFormat', horizontalAlignment: RIGHT)
                    }
                }
                glazedColumn(name: 'Total', property: 'jumlah', columnClass: Integer, preferredWidth: 80) {
                    templateRenderer('this:numberFormat', horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Qty Retur', property: 'jumlahRetur', columnClass: Integer, preferredWidth: 80, visible: bind {!model.popupMode || model.showReturOnly}) {
                    templateRenderer(exp: {it? numberFormat(it): 0}, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Qty Tukar', property: 'jumlahTukar', columnClass: Integer, preferredWidth: 80, visible: bind {!model.popupMode }) {
                    templateRenderer(exp: {it? numberFormat(it): 0}, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Qty Kirim', property: 'jumlahAkanDikirim', columnClass: Integer, preferredWidth: 80) {
                    templateRenderer(exp: {it? numberFormat(it): 0}, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Qty Ready', expression: {it.jumlahReadyGudangUtama()}, columnClass: Integer, preferredWidth: 80) {
                    templateRenderer(exp: {it? numberFormat(it): 0}, horizontalAlignment: RIGHT)
                }
            }
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left][right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true,
          visible: bind { model.allowTambahProduk }) {
        label('Nama:')
        textField(id: 'nama', columns: 50, text: bind('nama', target: model, mutual: true), errorPath: 'nama')
        errorLabel(path: 'nama')
        label('Satuan:')
        comboBox(id: 'satuan', model: model.satuan, errorPath: 'satuan')
        errorLabel(path: 'satuan', constraints: 'wrap')
        label('HET Dalam Kota:')
        decimalTextField(id: 'hargaDalamKota', columns: 20, bindTo: 'hargaDalamKota', errorPath: 'hargaDalamKota')
        errorLabel(path: 'hargaDalamKota')
        label('HET Luar Kota:')
        decimalTextField(id: 'hargaLuarKota', columns: 20, bindTo: 'hargaLuarKota', errorPath: 'hargaLuarKota')
        errorLabel(path: 'hargaLuarKota', constraints: 'wrap')
        label('Supplier:')
        comboBox(id: 'supplier', model: model.supplier, errorPath: 'supplier')
        errorLabel(path: 'supplier')
        label('Ongkos Kirim Pembelian:')
        decimalTextField(id: 'ongkosKirimBeli', columns: 20, bindTo: 'ongkosKirimBeli', errorPath: 'ongkosKirimBeli')
        errorLabel(path: 'ongkosKirimBeli', constraints: 'wrap')
        label('Poin:')
        numberTextField(id: 'poin', columns: 20, bindTo: 'poin', errorPath: 'poin')
        errorLabel(path: 'poin')
        label('Stok Level Minimum:')
        numberTextField(id: 'levelMinimum', columns: 20, bindTo: 'levelMinimum', errorPath: 'levelMinimum')
        errorLabel(path: 'levelMinimum', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 50, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan', constraints: 'span 3')
        errorLabel(path: 'keterangan', constraints: 'wrap')
        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                if (model.id!=null) {
                    if (!DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                        return
                    }
                }
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            })
            button('Pilih', visible: bind('isRowSelected', source: table, converter: {it && model.popupMode}), action: pilih)
            button(id: 'stokProduk', action: showStokProduk, visible: bind{table.isRowSelected})
            button(id: 'nilaiInventory', action: showNilaiInventory, visible: bind{table.isRowSelected})
            button(id: 'ubahQty', action: showUbahQty, visible: bind{table.isRowSelected}, constraints: 'wrap')
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind{table.isRowSelected}, actionPerformed: controller.clear)
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind('isRowSelected', source: table, converter: {it && !model.popupMode}), actionPerformed: {
                if (DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
                    controller.delete()
                }
            })
        }
    }
}

PromptSupport.setPrompt("Cari Nama...", namaSearch)
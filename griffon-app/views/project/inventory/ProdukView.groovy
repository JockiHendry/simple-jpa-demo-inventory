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

import org.jdesktop.swingx.prompt.PromptSupport

import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
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
}

application {

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
                        doubleClickAction: pilih, enterKeyAction: pilih) {
					glazedColumn(name: 'Nama', property: 'nama')
                    glazedColumn(name: 'Supplier', expression: { it.supplier?.nama?: ''})
					glazedColumn(name: 'HET Dalam Kota', property: 'hargaDalamKota', columnClass: Integer) {
                        templateRenderer('${currencyFormat(it)}', horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'HET Luar Kota', property: 'hargaLuarKota', columnClass: Integer) {
                        templateRenderer('${currencyFormat(it)}', horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Satuan', expression: { it.satuan.singkatan })
                    glazedColumn(name: 'Poin', property: 'poin', columnClass: Integer) {
                        templateRenderer('${numberFormat(it)}', horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Stok Level Minimum', property: 'levelMinimum', columnClass: Integer) {
                        templateRenderer('${numberFormat(it)}', horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Qty', property: 'jumlah', columnClass: Integer) {
                        templateRenderer('${numberFormat(it)}', horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Qty Retur', property: 'jumlahRetur', columnClass: Integer) {
                        templateRenderer(exp: {it? numberFormat(it): 0}, horizontalAlignment: RIGHT)
                    }
                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true,
              visible: bind { model.allowTambahProduk }) {
			label('Nama:')
			textField(id: 'nama', columns: 50, text: bind('nama', target: model, mutual: true), errorPath: 'nama')
			errorLabel(path: 'nama', constraints: 'wrap')
			label('HET Dalam Kota:')
			decimalTextField(id: 'hargaDalamKota', columns: 20, bindTo: 'hargaDalamKota', errorPath: 'hargaDalamKota')
			errorLabel(path: 'hargaDalamKota', constraints: 'wrap')
            label('HET Luar Kota:')
            decimalTextField(id: 'hargaLuarKota', columns: 20, bindTo: 'hargaLuarKota', errorPath: 'hargaLuarKota')
            errorLabel(path: 'hargaLuarKota', constraints: 'wrap')
            label('Supplier:')
            comboBox(id: 'supplier', model: model.supplier, errorPath: 'supplier')
            errorLabel(path: 'supplier', constraints: 'wrap')
            label('Satuan:')
            comboBox(id: 'satuan', model: model.satuan, templateRenderer: '${value}', errorPath: 'satuan')
            errorLabel(path: 'satuan', constraints: 'wrap')
            label('Poin:')
            numberTextField(id: 'poin', columns: 20, bindTo: 'poin', errorPath: 'poin')
            errorLabel(path: 'poin', constraints: 'wrap')
            label('Stok Level Minimum:')
            numberTextField(id: 'levelMinimum', columns: 20, bindTo: 'levelMinimum', errorPath: 'levelMinimum')
            errorLabel(path: 'levelMinimum', constraints: 'wrap')
            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                    if (model.id!=null) {
                        if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.update.message"),
                            app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                                return
                        }
                    }
                    controller.save()
                    form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
                })
                button('Pilih', visible: bind('isRowSelected', source: table, converter: {it && model.popupMode}), action: pilih)
                button(id: 'stokProduk', action: showStokProduk, visible: bind{table.isRowSelected})
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind{table.isRowSelected}, actionPerformed: controller.clear)
                button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind('isRowSelected', source: table, converter: {it && !model.popupMode}), actionPerformed: {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                        app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                            controller.delete()
                    }
                })
            }
        }
    }
}

PromptSupport.setPrompt("Cari Nama...", namaSearch)
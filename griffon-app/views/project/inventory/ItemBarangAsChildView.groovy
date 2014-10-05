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

import net.miginfocom.swing.MigLayout

import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.text.NumberFormat

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.RIGHT

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.itemBarangList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                glazedColumn(name: 'Produk', property: 'produk') {
                    templateRenderer('${it.nama}')
                }
                glazedColumn(name: 'Qty', property: 'jumlah')
                glazedColumn(name: 'Satuan', expression: { it.produk.satuan.singkatan })
            }
        }
    }

    taskPane(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind { model.editable }, constraints: PAGE_END) {
        label('Produk:')
        panel {
            label(text: bind {model.produk?: '- kosong -'})
            button('Cari Produk...', id: 'cariProduk', errorPath: 'produk', mnemonic: KeyEvent.VK_P, actionPerformed: controller.showProduk)
        }
        errorLabel(path: 'produk', constraints: 'wrap')
        label('Qty:')
        numberTextField(id: 'jumlah', columns: 10, bindTo: 'jumlah', errorPath: 'jumlah')
        errorLabel(path: 'jumlah', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button('Simpan', actionPerformed: {
                if (!view.table.selectionModel.selectionEmpty) {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.update.message"),
                            app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        return
                    }
                }
                controller.save()
                cariProduk.requestFocusInWindow()
            }, visible: bind{model.editable}, mnemonic: KeyEvent.VK_S)
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind('isRowSelected', source: table, converter: {it && model.editable}), actionPerformed: controller.clear, mnemonic: KeyEvent.VK_B)
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind('isRowSelected', source: table, converter: {it && model.editable}), actionPerformed: {
                if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                        app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    controller.delete()
                }
            }, mnemonic: KeyEvent.VK_H)
            button(app.getMessage("simplejpa.dialog.close.button"), actionPerformed: {
                SwingUtilities.getWindowAncestor(mainPanel)?.dispose()
            }, mnemonic: KeyEvent.VK_T)
        }
    }
}
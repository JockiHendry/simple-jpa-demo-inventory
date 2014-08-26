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

import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*

actions {
    action(id: 'save', name: app.getMessage('simplejpa.dialog.update.button'), closure: controller.save)
    action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
}

application(title: 'Klaim Retur',
        preferredSize: [520, 340],
        pack: true,
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: CENTER) {
            borderLayout()
            panel(constraints: PAGE_START, layout: new FlowLayout(FlowLayout.LEADING)) {
                label(text: bind('searchMessage', source: model))
            }
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.klaimReturList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                    glazedColumn(name: 'Produk', property: 'produk')
                    glazedColumn(name: 'Jumlah', property: 'jumlah')
                    glazedColumn(name: 'Sudah Diproses', property: 'sudahDiproses') {
                        templateRenderer(exp: {it? 'Y': ''})
                    }
                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind { model.editable }, constraints: PAGE_END, focusCycleRoot: true) {
            label('Produk:')
            panel {
                label(text: bind {model.produk?: '- kosong -'})
                button('Cari Produk...', id: 'cariProduk', errorPath: 'produk', mnemonic: KeyEvent.VK_P, actionPerformed: controller.showProduk)
            }
            errorLabel(path: 'produk', constraints: 'wrap')
            label('Jumlah:')
            numberTextField(id: 'jumlah', columns: 20, bindTo: 'jumlah', errorPath: 'jumlah')
            errorLabel(path: 'jumlah', constraints: 'wrap')

            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button(action: save)
                button(visible: bind { table.isRowSelected }, action: cancel)
                button(visible: bind { table.isRowSelected }, action: delete)
                button(action: close)
            }
        }
    }
}

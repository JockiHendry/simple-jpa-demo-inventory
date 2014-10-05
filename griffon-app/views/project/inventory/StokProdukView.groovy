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

import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN

actions {
    action(id: 'showItemStok', name: 'Item Stok...', closure: controller.showItemStok)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.stokProdukList, sortingStrategy: SINGLE_COLUMN,
                    doubleClickAction: showItemStok, enterKeyAction: showItemStok) {
                glazedColumn(name: 'Gudang', property: 'gudang') {
                    templateRenderer('${it.nama}')
                }
                glazedColumn(name: 'Jumlah', property: 'jumlah') {
                    templateRenderer('${numberFormat(it)}')
                }
            }
        }
    }

    taskPane(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END) {
        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(id: 'itemStok', action: showItemStok, visible: bind{table.isRowSelected})
            button(app.getMessage("simplejpa.dialog.close.button"), actionPerformed: {
                SwingUtilities.getWindowAncestor(mainPanel)?.dispose()
            }, mnemonic: KeyEvent.VK_T)
        }
    }
}

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
package project.labarugi

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import java.awt.*

actions {
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
    action(id: 'hitungNilaiInventory', name: 'Hitung Nilai Inventory', closure: controller.hitungNilaiInventory)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: CENTER) {
        borderLayout()
        panel(constraints: PAGE_START, layout: new FlowLayout(FlowLayout.LEADING)) {
            label('Sampai Tanggal:')
            dateTimePicker(id: 'tanggalSearch', localDate: bind('tanggalSearch', target: model, mutual: true), timeVisible: false)
            button(action: hitungNilaiInventory)
            label(text: bind {model.informasi})
        }
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.itemNilaiInventory, sortingStrategy: SINGLE_COLUMN) {
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Nama', property: 'nama')
                glazedColumn(name: 'Faktur', property: 'faktur')
                glazedColumn(name: 'Qty', property: 'qty', columnClass: Integer)
                glazedColumn(name: 'Harga Beli Satuan', property: 'harga', columnClass: Integer) {
                    templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Total Harga Beli', expression: { it.total() }, columnClass: Integer) {
                    templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
            }
        }
        panel(constraints: PAGE_END) {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: close)
        }
    }

}

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
package project.pembelian

import java.awt.event.KeyEvent
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import java.awt.*

actions {
    action(id: 'showItemBarang', name: 'Item Barang...', closure: controller.showItemBarang, mnemonic: KeyEvent.VK_I)
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: CENTER) {
        borderLayout()
        panel(constraints: PAGE_START, layout: new FlowLayout(FlowLayout.LEADING)) {
            label(text: bind('searchMessage', source: model))
        }
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.penerimaanBarangList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemBarang, enterKeyAction: showItemBarang) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it=='Y'?'D':''})
                }
                glazedColumn(name: 'Nomor PO', property: 'nomor', width: 120)
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
                }
                glazedColumn(name: 'Keterangan', property: 'keterangan')
                glazedColumn(name: 'Qty', expression: { it.jumlah() }, columnClass: Integer)
            }
        }
    }

    panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nomor Surat Jalan:')
        textField(id: 'nomor', columns: 20, text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
        errorLabel(path: 'nomor', constraints: 'wrap')
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Isi:')
        button(id: 'items', action: showItemBarang, errorPath: 'items')
        errorLabel(path: 'items', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap', visible: bind {model.notDeleted}) {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save, visible: bind { !table.isRowSelected })
            button(action: cancel, visible: bind { table.isRowSelected })
            button(action: delete, visible: bind { table.isRowSelected })
        }
    }
}

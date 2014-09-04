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
package project.pembelian

import net.miginfocom.swing.MigLayout

import javax.swing.JOptionPane
import java.awt.FlowLayout
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
    action(id: 'close', name: 'Tutup', closure: controller.close, mnemonic: KeyEvent.VK_T)
}

application() {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.pembayaranList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                    glazedColumn(name: 'Tanggal', property: 'tanggal') {
                        templateRenderer(exp: {it.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Jumlah', property: 'jumlah', columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Potongan', property: 'potongan') {
                        templateRenderer(exp: {it? 'Y': ''})
                    }
                    glazedColumn(name: 'Bilyet Giro', property: 'bilyetGiro') {
                        templateRenderer(exp: {!it?'':it.nomorSeri})
                    }
                }
            }
        }

        taskPane(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END) {
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Jumlah:')
            decimalTextField(id: 'jumlah', columns: 10, bindTo: 'jumlah', errorPath: 'jumlah')
            errorLabel(path: 'jumlah', constraints: 'wrap')
            label('Potongan:')
            checkBox(id: 'potongan', selected: bind('potongan', target: model, mutual: true), errorPath: 'potongan')
            errorLabel(path: 'potongan', constraints: 'wrap')
            label('Bilyet Giro:')
            panel {
                label(text: bind { model.bilyetGiro?.nomorSeri?: '- kosong -' })
                button('Cari Bilyet Giro...', id: 'cariBilyetGiro', errorPath: 'bilyetGiro', mnemonic: KeyEvent.VK_G, actionPerformed: controller.showBilyetGiro)
            }
            errorLabel(path: 'bilyetGiro', constraints: 'wrap')
            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button(action: save, visible: bind{model.editable})
                button(action: cancel, visible: bind('isRowSelected', source: table, converter: {it && model.editable}))
                button(action: delete, visible: bind('isRowSelected', source: table, converter: {it && model.editable}))
                button(action: close)
            }
        }
    }
}

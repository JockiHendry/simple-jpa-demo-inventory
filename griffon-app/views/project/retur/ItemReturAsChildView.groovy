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
package project.retur

import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import java.awt.*

actions {
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
    action(id: 'close', name: 'Tutup', closure: controller.close, mnemonic: KeyEvent.VK_T)
    action(id: 'showKlaim', name: 'Klaims', closure: controller.showKlaim, mnemonic: KeyEvent.VK_K)
    action(id: 'autoKlaim', name: 'Tukar Baru + Piutang', closure: controller.autoKlaim, mnemonic: KeyEvent.VK_U)
    action(id: 'autoKlaimServis', name: 'Tukar Baru + Tukar Servis + Piutang', closure: controller.autoKlaimServis, mnemonic: KeyEvent.VK_V)
    action(id: 'autoKlaimPiutang', name: 'Piutang', closure: controller.autoKlaimPiutang, mnemonic: KeyEvent.VK_P)
    action(id: 'resetKlaim', name: 'Reset Klaim', closure: controller.resetKlaim, mnemonic: KeyEvent.VK_R)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel() {
        borderLayout()
        panel(visible: bind {model.editable}, constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEFT)
            button(action: autoKlaim)
            button(action: autoKlaimServis)
            button(action: autoKlaimPiutang, visible: bind {!model.modusEceran})
            button(action: resetKlaim)
        }
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.itemReturList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged, doubleClickAction: showKlaim, enterKeyAction: showKlaim) {
                glazedColumn(name: 'Produk', expression: { it.getDescription() })
                glazedColumn(name: 'Qty', property: 'jumlah', columnClass: Integer, width: 50)
                glazedColumn(name: 'Qty Tukar Baru', expression: { it.jumlahBarangDitukar() }, columnClass: Integer)
                glazedColumn(name: 'Qty Tukar Servis', expression: { it.jumlahBarangDiservis() }, columnClass: Integer)
                glazedColumn(name: 'Potong Piutang', expression: { it.jumlahPotongPiutang() }, columnClass: Integer, visible: bind{model.showPiutang}) {
                    templateRenderer('this:currencyFormat', horizontalAlignment: RIGHT)
                }
            }
        }
    }

    panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind {model.editable}, constraints: PAGE_END, focusCycleRoot: true) {
        label('Produk:')
        panel {
            label(text: bind {model.produk?: '- kosong -'})
            button('Cari Produk...', id: 'cariProduk', errorPath: 'produk', mnemonic: KeyEvent.VK_P, actionPerformed: controller.showProduk)
        }
        errorLabel(path: 'produk', constraints: 'wrap')
        label('Qty:')
        numberTextField(id: 'jumlah', columns: 20, bindTo: 'jumlah', errorPath: 'jumlah')
        errorLabel(path: 'jumlah', constraints: 'wrap')
        label('Klaims:')
        button(action: showKlaim, errorPath: 'klaims')
        errorLabel(path: 'klaims', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save)
            button(visible: bind { table.isRowSelected }, action: cancel)
            button(visible: bind { table.isRowSelected }, action: delete)
            button(action: close)
        }
    }
}

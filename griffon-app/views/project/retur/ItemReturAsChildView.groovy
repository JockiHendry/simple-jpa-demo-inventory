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
    action(id: 'save', name: 'Simpan', closure: controller.save)
    action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
    action(id: 'showKlaim', name: 'Klaims', closure: controller.showKlaim)
    action(id: 'autoKlaim', name: 'Auto Klaim (Tukar + Piutang)', closure: controller.autoKlaim)
    action(id: 'autoKlaimPiutang', name: 'Auto Klaim (Piutang)', closure: controller.autoKlaimPiutang)
    action(id: 'resetKlaim', name: 'Reset Klaim', closure: controller.resetKlaim)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel() {
        borderLayout()
        panel(visible: bind {model.editable}, constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEFT)
            button(action: autoKlaim)
            button(action: autoKlaimPiutang, visible: bind {!model.modusEceran})
            button(action: resetKlaim)
        }
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.itemReturList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged, doubleClickAction: showKlaim, enterKeyAction: showKlaim) {
                glazedColumn(name: 'Produk', property: 'produk')
                glazedColumn(name: 'Qty', property: 'jumlah', columnClass: Integer)
                glazedColumn(name: 'Qty Ditukar', expression: { it.jumlahBarangDitukar() }, columnClass: Integer)
                glazedColumn(name: 'Potong Piutang', expression: { it.jumlahPotongPiutang() }, columnClass: Integer, visible: bind{model.showPiutang}) {
                    templateRenderer("\${it? currencyFormat(it): ''}", horizontalAlignment: RIGHT)
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

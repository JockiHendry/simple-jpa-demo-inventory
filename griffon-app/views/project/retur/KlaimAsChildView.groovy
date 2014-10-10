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

import domain.retur.KlaimPotongPiutang
import domain.retur.KlaimTukar

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
}

panel(id: 'mainPanel') {
    borderLayout()

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.klaimList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: 'Jenis', expression: { griffon.util.GriffonNameUtils.getNaturalName(it?.class?.simpleName)})
            glazedColumn(name: 'Tukar Dengan', expression: { (it instanceof KlaimTukar)? it.produk.nama: '' })
            glazedColumn(name: 'Qty Ditukar', expression: { (it instanceof KlaimTukar)? it.jumlah: '' })
            glazedColumn(name: 'Potongan Piutang', expression: { (it instanceof KlaimPotongPiutang)? it.jumlah: null}, columnClass: Integer) {
                templateRenderer("\${it? currencyFormat(it): ''}", horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Sudah Diproses', property: 'sudahDiproses') {
                templateRenderer("\${it? 'Y': ''}", horizontalAlignment: RIGHT)
            }
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), visible: bind {model.editable}, constraints: PAGE_END, focusCycleRoot: true) {
        label('Jenis Klaim:')
        comboBox(id: 'jenisKlaim', model: model.jenisKlaim, actionPerformed: controller.onPerubahanJenisKlaim)
        errorLabel(path: 'jenisKlaim', constraints: 'wrap')

        label('Produk:', visible: bind {model.produkVisible})
        panel(visible: bind {model.produkVisible}) {
            label(text: bind {model.produk?: '- kosong -'})
            button('Cari Produk...', id: 'cariProduk', errorPath: 'produk', mnemonic: KeyEvent.VK_P, actionPerformed: controller.showProduk)
        }
        errorLabel(path: 'produk', visible: bind {model.produkVisible}, constraints: 'wrap')
        label('Jumlah:')
        decimalTextField(id: 'jumlah', columns: 20, bindTo: 'jumlah', nfParseBigDecimal: true, errorPath: 'jumlah')
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

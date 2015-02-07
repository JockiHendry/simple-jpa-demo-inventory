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
import griffon.util.*

actions {
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
    action(id: 'close', name: 'Tutup', closure: controller.close, mnemonic: KeyEvent.VK_T)
}

panel(id: 'mainPanel') {
    borderLayout()

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.klaimList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: 'Jenis', expression: { GriffonNameUtils.getNaturalName(it?.class?.simpleName)})
            glazedColumn(name: 'Tukar Dengan', expression: { it.informasiProduk()?.nama?: '' })
            glazedColumn(name: 'Qty Ditukar', expression: { it.informasiQty() }, columnClass: Integer)
            glazedColumn(name: 'Potongan Piutang / Tukar', expression: { it.informasiHarga() }, columnClass: Integer) {
                templateRenderer('this:currencyFormat', horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Sudah Diproses', property: 'sudahDiproses') {
                templateRenderer(exp: {it? 'Y': ''}, horizontalAlignment: RIGHT)
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

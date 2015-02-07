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

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport
import java.awt.FlowLayout
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.*

actions {
    action(id: 'showItemFaktur', name: 'Item Purchase Order...', closure: controller.showItemFaktur, mnemonic: KeyEvent.VK_I)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak, mnemonic: KeyEvent.VK_C)
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'showPenerimaanBarang', name: 'Penerimaan Barang', closure: controller.showPenerimaanBarang, mnemonic: KeyEvent.VK_P)
    action(id: 'showSisaBelumDiterima', name: 'Sisa Belum Diterima', closure: controller.showSisaBelumDiterima, mnemonic: KeyEvent.VK_D)
    action(id: 'showFakturBeli', name: 'Faktur Pembelian', controller.showFakturBeli, mnemonic: KeyEvent.VK_F)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        comboBox(id: 'statusSearch', model: model.statusSearch)
        textField(id: 'nomorPOSearch', columns: 10, text: bind('nomorPOSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'nomorFakturSearch', columns: 10, text: bind('nomorFakturSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'supplierSearch', columns: 10, text: bind('supplierSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }


    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.purchaseOrderList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemFaktur, enterKeyAction: showItemFaktur) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y' ? 'D' : '' })
                }
                glazedColumn(name: 'Nomor PO', property: 'nomor', width: 140)
                glazedColumn(name: 'Nomor Faktur', expression: { it.fakturBeli?.nomor })
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Supplier', property: 'supplier') {
                    templateRenderer(exp: { it?.nama })
                }
                glazedColumn(name: 'Keterangan', property: 'keterangan')
                glazedColumn(name: 'Status', property: 'status')
                glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer, visible: bind { model.showFakturBeli })
                glazedColumn(name: 'Jumlah Diskon', visible: bind { model.showFakturBeli },
                        expression: { it.jumlahDiskon() }, columnClass: Integer) {
                    templateRenderer(exp: { !it ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Total', expression: { it.total() }, columnClass: Integer, visible: bind { model.showFakturBeli }) {
                    templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
            }
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nomor:')
        label(id: 'nomor', text: bind('nomor', source: model), errorPath: 'nomor')
        errorLabel(path: 'nomor', constraints: 'wrap')
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Supplier:')
        comboBox(id: 'supplier', model: model.supplier, errorPath: 'supplier')
        errorLabel(path: 'supplier', constraints: 'wrap')
        label('Diskon:', visible: bind { model.showFakturBeli })
        panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0), visible: bind { model.showFakturBeli }) {
            decimalTextField(id: 'diskonPotonganPersen', columns: 5, bindTo: 'diskonPotonganPersen', errorPath: 'diskonPotonganPersen')
            label('% dan Potongan Langsung Rp')
            decimalTextField(id: 'diskonPotonganLangsung', columns: 20, bindTo: 'diskonPotonganLangsung', errorPath: 'diskonPotonganLangsung')
        }
        errorLabel(path: 'diskon', constraints: 'wrap', visible: bind { model.showFakturBeli })
        label('Isi:')
        panel {
            label(text: bind { model.informasi })
            button(id: 'listItemFaktur', action: showItemFaktur, errorPath: 'listItemFaktur')
        }
        errorLabel(path: 'listItemFaktur', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')

        panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Dibuat:')
            label(text: bind { model.created })
            label(text: bind { model.createdBy })
            label('   Dimodifikasi:')
            label(text: bind { model.modified })
            label(text: bind { model.modifiedBy })
        }

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save, visible: bind { model.allowAddPO })
            button(id: 'cetak', action: cetak, visible: bind('isRowSelected', source: table, converter: {it && model.allowAddPO}))
            button(action: showPenerimaanBarang, visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan }))
            button(action: showSisaBelumDiterima, visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan }))
            button(action: showFakturBeli, visible: bind('isRowSelected', source: table, converter: { it && model.showFakturBeli }))
            button(action: cancel, visible: bind { table.isRowSelected })
            button(action: delete, visible: bind { table.isRowSelected })
        }

    }
}

PromptSupport.setPrompt("Nomor PO...", nomorPOSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorFakturSearch)
PromptSupport.setPrompt("Supplier...", supplierSearch)
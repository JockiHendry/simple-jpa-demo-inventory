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
package project.penjualan

import java.awt.event.KeyEvent
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'showItemFaktur', name: 'Item Faktur Jual...', closure: controller.showItemFaktur, mnemonic: KeyEvent.VK_I)
    action(id: 'showBonus', name: 'Bonus...', closure: controller.showBonus, mnemonic: KeyEvent.VK_B)
    action(id: 'retur', name: 'Retur', closure: controller.showRetur, mnemonic: KeyEvent.VK_R)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak, mnemonic:  KeyEvent.VK_C)
    action(id: 'cetakSuratJalan', name: 'Cetak Surat Jalan', closure: controller.cetakSuratJalan, mnemonic:KeyEvent.VK_J)
    action(id: 'cariKonsumen', name: 'Cari Konsumen...', closure: controller.cariKonsumen, mnemonic: KeyEvent.VK_K)
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
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
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'salesSearch', columns: 10, text: bind('salesSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.fakturJualOlehSalesList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                doubleClickAction: showItemFaktur, enterKeyAction: showItemFaktur) {
            glazedColumn(name: '', expression: { it.deleted == 'Y'? 'D': (!it.retur.empty? 'R': '')}, width: 20)
            glazedColumn(name: 'Nomor', property: 'nomor', width: 140)
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Konsumen', expression: { it.konsumen.nama })
            glazedColumn(name: 'Sales', expression: { it.konsumen.sales.nama })
            glazedColumn(name: 'Status', property: 'status')
            glazedColumn(name: 'Keterangan', property: 'keterangan')
            glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer, visible: bind { model.showNilaiUang })
            glazedColumn(name: 'Jumlah Diskon', visible: bind { model.showNilaiUang }, expression: { it.jumlahDiskon() }, columnClass: Integer) {
                templateRenderer(exp: { !it ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Total', expression: { it.total() }, columnClass: Integer, visible: bind { model.showNilaiUang }) {
                templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
        }
    }

    panel(constraints: PAGE_END, focusCycleRoot: true) {
        borderLayout()
        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: CENTER, visible: bind { model.allowAddFakturJual }) {
            label('Nomor:')
            label(id: 'nomor', text: bind('nomor', source: model), errorPath: 'nomor')
            errorLabel(path: 'nomor', constraints: 'wrap')
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Konsumen:')
            panel {
                label(text: bind {model.konsumen?: '- kosong -'})
                button(action: cariKonsumen, id: 'cariKonsumen', errorPath: 'konsumen')
                checkBox('Kirim Dari Gudang Utama', selected: bind('kirimDariGudangUtama', target: model, mutual: true), errorPath: 'kirimDariGudangUtama')
                errorLabel(path: 'kirimDariGudangUtama')
            }
            errorLabel(path: 'konsumen', constraints: 'wrap')
            label('Diskon:')
            panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0)) {
                decimalTextField(id: 'diskonPotonganPersen', columns: 5, bindTo: 'diskonPotonganPersen', errorPath: 'diskonPotonganPersen')
                label('% dan Potongan Langsung Rp')
                decimalTextField(id: 'diskonPotonganLangsung', columns: 20, bindTo: 'diskonPotonganLangsung', errorPath: 'diskonPotonganLangsung')
            }
            errorLabel(path: 'diskon', constraints: 'wrap')
            label('Isi:')
            panel {
                label(text: bind { model.informasi })
                button(id: 'listItemFaktur', action: showItemFaktur, errorPath: 'listItemFaktur')
                checkBox('Mendapatkan Poin?', selected: bind('poinBerlaku', target: model, mutual: true), errorPath: 'poinBerlaku')
                errorLabel(path: 'poinBerlaku')
            }
            errorLabel(path: 'listItemFaktur', constraints: 'wrap')
            label('Bonus:')
            panel {
                label(text: bind { model.informasiBonus })
                button(id: 'listBonus', action: showBonus, errorPath: 'listBonus')
            }
            errorLabel(path: 'listBonus', constraints: 'wrap')
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
        }

        panel(constraints: PAGE_END) {
            flowLayout(alignment: FlowLayout.LEADING)
            button(id: 'save', action: save)
            button(id: 'cetak', action: cetak, visible: bind('isRowSelected', source: table, converter: {it && model.showFakturJual && model.allowPrint}))
            button(id: 'cetakSuratJalan', action: cetakSuratJalan, visible: bind('isRowSelected', source: table, converter: {it && model.showFakturJual && model.allowPrint}))
            button(id: 'showRetur', action: retur, visible: bind { table.isRowSelected })
            button(id: 'cancel', action: cancel, visible: bind { table.isRowSelected })
            button(id: 'delete', action: delete, visible: bind { table.isRowSelected })
        }
    }
}

PromptSupport.setPrompt("Sales...", salesSearch)
PromptSupport.setPrompt("Konsumen...", konsumenSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorSearch)
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
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'search', name: app.getMessage('simplejpa.search.label'), closure: controller.search)
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'cancel', name: 'Batal', closure: controller.clear, mnemonic: KeyEvent.VK_B)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
    action(id: 'showBarangRetur', name: 'Item Retur...', closure: controller.showBarangRetur, mnemonic: KeyEvent.VK_I)
    action(id: 'penukaran', name: 'Barang Retur Yang Ditukar Telah Diterima...', closure: controller.prosesTukar, mnemonic: KeyEvent.VK_D)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak, mnemonic: KeyEvent.VK_C)
    action(id: 'prosesSemuaFaktur', name: 'Proses Semua Faktur', closure: controller.prosesSemuaFaktur, mnemonic: KeyEvent.VK_P)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false, visible: bind { model.showTanggal })
        label(' s/d ', visible: bind { model.showTanggal })
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false, visible: bind { model.showTanggal })
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), action: search)
        textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), action: search)
        comboBox(id: 'statusSearch', model: model.statusSearch)
        button(action: search)
        button(action: prosesSemuaFaktur, visible: bind { model.showPenukaran })
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.returJualList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged, doubleClickAction: showBarangRetur, enterKeyAction: showBarangRetur) {
            glazedColumn(name: '', property: 'deleted', width: 20) {
                templateRenderer(exp: { it == 'Y'? 'D': ''})
            }
            glazedColumn(name: 'Nomor', property: 'nomor')
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Konsumen', expression: { it.namaKonsumen })
            glazedColumn(name: 'Qty Ditukar', expression: { it.jumlahDitukar() }, columnClass: Integer)
            glazedColumn(name: 'Sudah Diproses', property: 'sudahDiproses') {
                templateRenderer(exp: { it? 'Y': ''})
            }
            glazedColumn(name: 'Keterangan', property: 'keterangan')
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nomor:')
        label(id: 'nomor', text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
        errorLabel(path: 'nomor', constraints: 'wrap')
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', dateVisible: true, timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Nama Konsumen:')
        textField(id: 'namaKonsumen', columns: 30, text: bind('namaKonsumen', target: model, mutual: true), errorPath: 'namaKonsumen')
        errorLabel(path: 'namaKonsumen', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 50, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')
        label('Items:')
        button(action: showBarangRetur, errorPath: 'items')
        errorLabel(path: 'items', constraints: 'wrap')
        label('Kondisi:')
        checkBox('Barang masih bisa dijual kembali', selected: bind('bisaDijualKembali', target: model, mutual: true), errorPath: 'bisaDijualKembali')
        errorLabel(path: 'bisaDijualKembali', constraints: 'wrap')
        panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Dibuat:')
            label(text: bind { model.created })
            label(text: bind { model.createdBy })
            label('   Dimodifikasi:')
            label(text: bind { model.modified })
            label(text: bind { model.modifiedBy })
        }
        panel(visible: bind { !model.deleted }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save, visible: bind{ model.showSave })
            button(action: cetak, visible: bind { table.isRowSelected && model.showSave })
            button(visible: bind('isRowSelected', source: table, converter: { it && model.showSave }), action: cancel)
            button(visible: bind('isRowSelected', source: table, converter: { it && model.showSave }), action: delete)
            button(visible: bind{ model.allowPenukaran }, action: penukaran)
        }
    }
}

PromptSupport.setPrompt("Nomor Search", nomorSearch)
PromptSupport.setPrompt("Konsumen Search", konsumenSearch)
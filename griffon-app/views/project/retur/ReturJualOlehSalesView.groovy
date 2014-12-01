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
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'search', name: app.getMessage('simplejpa.search.label'), closure: controller.search)
    action(id: 'save', name: app.getMessage('simplejpa.dialog.save.button'), closure: controller.save)
    action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'showBarangRetur', name: 'Klik Disini Untuk Melihat Atau Mengisi Item Retur...', closure: controller.showBarangRetur)
    action(id: 'cariKonsumen', name: 'Cari Konsumen', closure: controller.cariKonsumen, mnemonic: KeyEvent.VK_K)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak)
    action(id: 'penukaran', name: 'Barang Retur Yang Ditukar Telah Diantar', closure: controller.prosesTukar)
    action(id: 'showBarangYangHarusDikirim', name: 'Barang Yang Belum Dikirim...', closure: controller.showBarangYangHarusDikirim)
    action(id: 'hapusPengeluaran', name: 'Hapus Pengantaran', closure: controller.hapusPengeluaran)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), action: search)
        textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), action: search)
        comboBox(id: 'statusSearch', model: model.statusSearch)
        button(action: search)
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
            glazedColumn(name: 'Konsumen', expression: { it.konsumen.nama })
            glazedColumn(name: 'Qty Ditukar', expression: { it.jumlahDitukar() }, columnClass: Integer)
            glazedColumn(name: 'Potong Piutang', expression: { it.jumlahPotongPiutang() }, columnClass: Integer, visible: bind {model.showPiutang}) {
                templateRenderer("\${it? currencyFormat(it): ''}", horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Ref Faktur', expression: {it.fakturPotongPiutang.collect{it.nomor}.join(',')})
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
        label('Gudang:')
        comboBox(id: 'gudang', model: model.gudang, templateRenderer: '${value}', errorPath: 'gudang')
        errorLabel(path: 'gudang', constraints: 'wrap')
        label('Konsumen:')
        panel {
            label(text: bind {model.konsumen?: '- kosong -'})
            button(action: cariKonsumen, id: 'cariKonsumen', errorPath: 'konsumen')
        }
        errorLabel(path: 'konsumen', constraints: 'wrap')
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
            button(action: showBarangYangHarusDikirim, visible: bind{model.allowPenukaran})
            button(visible: bind('isRowSelected', source: table, converter: { it && model.showSave }), action: cancel)
            button(visible: bind('isRowSelected', source: table, converter: { it && model.showSave }), action: delete)
            button(visible: bind{ model.allowPenukaran }, action: penukaran)
            button(visible: bind{ table.isRowSelected && model.allowPenukaran }, action: hapusPengeluaran)
        }
    }
}

PromptSupport.setPrompt("Nomor Search", nomorSearch)
PromptSupport.setPrompt("Konsumen Search", konsumenSearch)
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
package project.labarugi

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
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), action: search)
        textField(id: 'pihakTerkaitSearch', columns: 10, text: bind('pihakTerkaitSearch', target: model, mutual: true), action: search)
        textField(id: 'kategoriKasSearch', columns: 10, text: bind('kategoriKasSearch', target: model, mutual: true), action: search)
        comboBox(id: 'statusSearch', model: model.jenisTransaksiSearch)
        button(action: search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.transaksiKasList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: '', expression: { it.deleted == 'Y'? 'D': '' }, width: 20)
            glazedColumn(name: 'Nomor', property: 'nomor')
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Pihak Terkait', property: 'pihakTerkait')
            glazedColumn(name: 'Kategori Kas', property: 'kategoriKas')
            glazedColumn(name: 'Jumlah', property: 'jumlah', columnClass: Integer) {
                templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Jenis', property: 'jenis')
            glazedColumn(name: 'Keterangan', property: 'keterangan')
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true, visible: bind { !model.deleted }) {
        label('Nomor:')
        label(id: 'nomor', text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
        errorLabel(path: 'nomor', constraints: 'wrap')
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', dateVisible: true, timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Pihak Terkait:')
        textField(id: 'pihakTerkait', columns: 30, text: bind('pihakTerkait', target: model, mutual: true), errorPath: 'pihakTerkait')
        errorLabel(path: 'pihakTerkait', constraints: 'wrap')
        label('Kategori Kas:')
        comboBox(id: 'kategoriKas', model: model.kategoriKas, errorPath: 'kategoriKas')
        errorLabel(path: 'kategoriKas', constraints: 'wrap')
        label('Jumlah:')
        decimalTextField(id: 'jumlah', columns: 20, bindTo: 'jumlah', errorPath: 'jumlah')
        errorLabel(path: 'jumlah', constraints: 'wrap')
        label('Jenis Transaksi Kas:')
        comboBox(id: 'jenisTransaksiKas', model: model.jenisTransaksiKas, errorPath: 'jenisTransaksiKas')
        errorLabel(path: 'jenisTransaksiKas', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 50, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')
        panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Created:')
            label(text: bind { model.created })
            label(text: bind { model.createdBy })
            label('   Modified:')
            label(text: bind { model.modified })
            label(text: bind { model.modifiedBy })
        }
        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save)
            button(visible: bind { table.isRowSelected }, action: cancel)
            button(visible: bind { table.isRowSelected }, action: delete)
        }
    }
}

PromptSupport.setPrompt("Nomor", nomorSearch)
PromptSupport.setPrompt("Pihak Terkait", pihakTerkaitSearch)
PromptSupport.setPrompt("Kategori", kategoriKasSearch)
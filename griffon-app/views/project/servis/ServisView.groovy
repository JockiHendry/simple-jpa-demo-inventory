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
package project.servis

import javax.swing.JOptionPane
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
	action(id: 'search', name: app.getMessage('simplejpa.search.label'), closure: controller.search)
	action(id: 'save', name: app.getMessage('simplejpa.dialog.save.button'), closure: controller.save)
	action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
	action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        textField(id: 'namaKonsumenSearch', columns: 20, text: bind('namaKonsumenSearch', target: model, mutual: true), action: search)
        button(action: search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.servisList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: '', expression: { it.deleted == 'Y'? 'D': ''}, width: 20)
            glazedColumn(name: 'Nama Konsumen', property: 'namaKonsumen')
            glazedColumn(name: 'Tipe', property: 'tipe')
            glazedColumn(name: 'Tanggal Masuk', property: 'tanggalMasuk') {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Tanggal Selesai', property: 'tanggalSelesai') {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Tanggal Diambil', property: 'tanggalDiambil') {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nama Konsumen:')
        textField(id: 'namaKonsumen', columns: 50, text: bind('namaKonsumen', target: model, mutual: true), errorPath: 'namaKonsumen')
        errorLabel(path: 'namaKonsumen', constraints: 'wrap')
        label('Alamat:')
        textField(id: 'alamat', columns: 100, text: bind('alamat', target: model, mutual: true), errorPath: 'alamat')
        errorLabel(path: 'alamat', constraints: 'wrap')
        label('Tipe:')
        textField(id: 'tipe', columns: 50, text: bind('tipe', target: model, mutual: true), errorPath: 'tipe')
        errorLabel(path: 'tipe', constraints: 'wrap')
        label('Keluhan:')
        textField(id: 'keluhan', columns: 100, text: bind('keluhan', target: model, mutual: true), errorPath: 'keluhan')
        errorLabel(path: 'keluhan', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 100, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')
        label('Tanggal Masuk:')
        dateTimePicker(id: 'tanggalMasuk', localDate: bind('tanggalMasuk', target: model, mutual: true), errorPath: 'tanggalMasuk', dateVisible: true, timeVisible: false)
        errorLabel(path: 'tanggalMasuk', constraints: 'wrap')
        label('Tanggal Selesai:')
        dateTimePicker(id: 'tanggalSelesai', localDate: bind('tanggalSelesai', target: model, mutual: true), errorPath: 'tanggalSelesai', dateVisible: true, timeVisible: false)
        errorLabel(path: 'tanggalSelesai', constraints: 'wrap')
        label('Tanggal Diambil:')
        dateTimePicker(id: 'tanggalDiambil', localDate: bind('tanggalDiambil', target: model, mutual: true), errorPath: 'tanggalDiambil', dateVisible: true, timeVisible: false)
        errorLabel(path: 'tanggalDiambil', constraints: 'wrap')
        panel(visible: bind{table.isRowSelected}, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Created:')
            label(text: bind{model.created})
            label(text: bind{model.createdBy})
            label('   Modified:')
            label(text: bind{model.modified})
            label(text: bind{model.modifiedBy})
        }
        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(action: save)
            button(action: cetak)
            button(visible: bind{table.isRowSelected}, action: cancel)
            button(visible: bind{table.isRowSelected}, action: delete)
        }
    }
}

PromptSupport.setPrompt("Nama Konsumen Search", namaKonsumenSearch)
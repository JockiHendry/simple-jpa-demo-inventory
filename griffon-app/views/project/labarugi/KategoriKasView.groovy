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
	action(id: 'showJumlahKas', name: 'Lihat Saldo Kas', closure: controller.showJumlahKas)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        textField(id: 'namaSearch', columns: 20, text: bind('namaSearch', target: model, mutual: true), action: search)
		comboBox(id: 'statusSearch', model: model.jenisSearch)
        button(action: search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.kategoriKasList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged, doubleClickAction: showJumlahKas, enterKeyAction: showJumlahKas) {
			glazedColumn(name: '', expression: { it.deleted == 'Y'? 'D': ''}, width: 20)
			glazedColumn(name: 'Nama', property: 'nama')
			glazedColumn(name: 'Jenis', property: 'jenis')
			glazedColumn(name: 'Sistem', property: 'sistem') {
				templateRenderer(exp: { it? 'Y': '' })
			}
			glazedColumn(name: 'Dipakai Di Laporan', property: 'dipakaiDiLaporan') {
				templateRenderer(exp: { it? 'Y': 'N' })
			}
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true) {
		label('Nama:')
		textField(id: 'nama', columns: 50, text: bind('nama', target: model, mutual: true), errorPath: 'nama')
		errorLabel(path: 'nama', constraints: 'wrap')
		label('Jenis:')
		comboBox(id: 'jenis', model: model.jenis, errorPath: 'jenis')
		errorLabel(path: 'jenis', constraints: 'wrap')
		label('Dipakai Di Laporan:')
		checkBox(selected: bind('dipakaiDiLaporan', target: model, mutual: true), errorPath: 'dipakaiDiLaporan')
		errorLabel(path: 'dipakaiDiLaporan', constraints: 'wrap')
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
			button(action: showJumlahKas, visible: bind{table.isRowSelected})
            button(visible: bind{table.isRowSelected}, action: cancel)
            button(visible: bind{table.isRowSelected}, action: delete)
        }
    }
}

PromptSupport.setPrompt("Nama", namaSearch)
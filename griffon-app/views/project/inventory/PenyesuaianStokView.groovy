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
package project.inventory

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport
import simplejpa.swing.DialogUtils
import java.awt.FlowLayout
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER

actions {
    action(id: 'showItemBarang', name: 'Klik Disini Untuk Melihat Atau Mengisi Item Barang...', closure: controller.showItemBarang)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'gudangSearch', columns: 10, text: bind('gudangSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }


    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.penyesuaianStokList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemBarang, enterKeyAction: showItemBarang) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y' ? 'D' : '' })
                }
                glazedColumn(name: 'Nomor', property: 'nomor')
                glazedColumn(name: 'Tanggal', property: 'tanggal') {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Gudang', property: 'gudang') {
                    templateRenderer(exp: { it?.nama })
                }
                glazedColumn(name: 'Keterangan', property: 'keterangan')
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
        label('Gudang:')
        comboBox(id: 'gudang', model: model.gudang, errorPath: 'gudang')
        errorLabel(path: 'gudang', constraints: 'wrap')
        label('Perubahan:')
        panel {
            buttonGroup(id: 'bertambahGroup')
            radioButton('Bertambah', id: 'bertambah', buttonGroup: bertambahGroup, errorPath: 'bertambah')
            bind(source: bertambah, sourceEvent: 'stateChanged', sourceValue: {bertambah.selected}, target: model, targetProperty: 'bertambah')
            radioButton('Berkurang', id: 'berkurang', buttonGroup: bertambahGroup, errorPath: 'bertambah')
            bind(source: berkurang, sourceEvent: 'stateChanged', sourceValue: {berkurang.selected}, target: model, targetProperty: 'berkurang')
        }
        errorLabel(path: 'bertambah', constraints: 'wrap')
        label('Isi:')
        panel {
            label(text: bind { model.informasi })
            button(id: 'listItemFaktur', action: showItemBarang, errorPath: 'items')
        }
        errorLabel(path: 'items', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                if (model.id != null) {
                    if (!DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                        return
                    }
                }
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            })
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {
                table.isRowSelected
            }, actionPerformed: controller.clear)
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind {
                table.isRowSelected
            }, actionPerformed: {
                if (DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
                    controller.delete()
                }
            })
        }

    }
}

PromptSupport.setPrompt("Nomor...", nomorSearch)
PromptSupport.setPrompt("Gudang...", gudangSearch)
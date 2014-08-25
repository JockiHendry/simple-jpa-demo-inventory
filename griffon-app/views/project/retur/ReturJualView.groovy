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
    action(id: 'showBarangRetur', name: 'Klik Disini Untuk Melihat Atau Mengisi Item Retur...', closure: controller.showBarangRetur)
    action(id: 'showKlaimRetur', name: 'Klik Disini Untuk Melihat Atau Mengisi Klaim Penukaran Barang...', closure: controller.showKlaimRetur)
}

application(title: 'Retur Jual',
        preferredSize: [520, 340],
        pack: true,
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            textField(id: 'nomorSearch', columns: 20, text: bind('nomorSearch', target: model, mutual: true), action: search)
            textField(id: 'konsumenSearch', columns: 20, text: bind('konsumenSearch', target: model, mutual: true), action: search)
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
                glazedColumn(name: 'Keterangan', property: 'keterangan')
            }
        }

        panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
            label('Nomor:')
            textField(id: 'nomor', columns: 50, text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
            errorLabel(path: 'nomor', constraints: 'wrap')
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', dateVisible: true, timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Konsumen:')
            comboBox(id: 'konsumen', model: model.konsumen, templateRenderer: '${value.nama} - ${value.region}', errorPath: 'konsumen')
            errorLabel(path: 'konsumen', constraints: 'wrap')
            label('Potongan Piutang:')
            decimalTextField(id: 'potongan', columns: 20, bindTo: 'potongan', errorPath: 'potongan')
            errorLabel(path: 'potongan', constraints: 'wrap')
            label('Keterangan:')
            textField(id: 'keterangan', columns: 50, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
            errorLabel(path: 'keterangan', constraints: 'wrap')
            label('Items:')
            button(action: showBarangRetur, errorPath: 'items')
            errorLabel(path: 'items', constraints: 'wrap')
            label('Klaim:')
            button(action: showKlaimRetur, errorPath: 'listKlaimRetur')
            errorLabel(path: 'listKlaimRetur', constraints: 'wrap')
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
}

PromptSupport.setPrompt("Nomor Search", nomorSearch)
PromptSupport.setPrompt("Konsumen Search", konsumenSearch)
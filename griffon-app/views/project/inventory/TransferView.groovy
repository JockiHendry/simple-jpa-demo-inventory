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
package project.inventory

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import java.awt.FlowLayout

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
        textField(id: 'asalSearch', columns: 10, text: bind('asalSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'tujuanSearch', columns: 10, text: bind('tujuanSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }


    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.transferList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemBarang, enterKeyAction: showItemBarang) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y' ? 'D' : '' })
                }
                glazedColumn(name: 'Nomor', property: 'nomor')
                glazedColumn(name: 'Tanggal', property: 'tanggal') {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Asal', property: 'gudang') {
                    templateRenderer(exp: { it?.nama })
                }
                glazedColumn(name: 'Tujuan', property: 'tujuan') {
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
        label('Asal:')
        comboBox(id: 'gudang', model: model.gudang, templateRenderer: '${value}', errorPath: 'gudang')
        errorLabel(path: 'gudang', constraints: 'wrap')
        label('Tujuan:')
        comboBox(id: 'tujuan', model: model.tujuan, templateRenderer: '${value}', errorPath: 'tujuan')
        errorLabel(path: 'tujuan', constraints: 'wrap')
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
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.update.message"),
                            app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
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
                if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                        app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    controller.delete()
                }
            })
        }

    }
}

PromptSupport.setPrompt("Nomor...", nomorSearch)
PromptSupport.setPrompt("Asal...", asalSearch)
PromptSupport.setPrompt("Tujuan...", tujuanSearch)
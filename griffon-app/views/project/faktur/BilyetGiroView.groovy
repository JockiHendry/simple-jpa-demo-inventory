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
package project.faktur

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport
import org.joda.time.LocalDate
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'pilih', name: 'Pilih', mnemonic: KeyEvent.VK_P, closure: {
        if (model.popupMode) {
            SwingUtilities.getWindowAncestor(mainPanel).visible = false
        }
    })
}

def warningIcon = imageIcon('/warning.png')

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        textField(id: 'nomorSeriSearch', columns: 20, text: bind('nomorSeriSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.bilyetGiroList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                doubleClickAction: pilih, enterKeyAction: pilih) {
            glazedColumn(name: '', property: 'deleted', width: 20) {
                templateRenderer(exp: { it == 'Y' ? 'D' : '' })
            }
            glazedColumn(name: 'Nomor Seri', expression: {it}) {
                templateRenderer(exp: {it.nomorSeri}, horizontalTextPosition: SwingConstants.LEFT) {
                    condition(if_: {value.jatuhTempo.isBefore(LocalDate.now()) && it.tanggalPencairan == null},
                              then_: {it.icon = warningIcon}, else_: {it.icon = null})
                }
            }
            glazedColumn(name: 'Nominal', property: 'nominal', columnClass: Integer) {
                templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Jatuh Tempo', property: 'jatuhTempo') {
                templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
            }
            glazedColumn(name: 'Nama Bank', property: 'namaBank')
            glazedColumn(name: 'Tanggal Pencairan', property: 'tanggalPencairan') {
                templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
            }
        }
    }

    panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nomor Seri:')
        textField(id: 'nomorSeri', columns: 50, text: bind('nomorSeri', target: model, mutual: true), errorPath: 'nomorSeri')
        errorLabel(path: 'nomorSeri', constraints: 'wrap')
        label('Nominal:')
        decimalTextField(id: 'nominal', columns: 20, bindTo: 'nominal', errorPath: 'nominal')
        errorLabel(path: 'nominal', constraints: 'wrap')
        label('Jatuh Tempo:')
        dateTimePicker(id: 'jatuhTempo', localDate: bind('jatuhTempo', target: model, mutual: true), errorPath: 'jatuhTempo', timeVisible: false)
        errorLabel(path: 'jatuhTempo', constraints: 'wrap')
        label('Nama Bank:')
        textField(id: 'namaBank', columns: 50, text: bind('namaBank', target: model, mutual: true), errorPath: 'namaBank')
        errorLabel(path: 'namaBank', constraints: 'wrap')

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
            }, visible: bind { !model.deleted })
            button('Pilih', visible: bind('isRowSelected', source: table, converter: {it && model.popupMode && !model.deleted}), action: pilih)
            button('Cairkan', actionPerformed: controller.cairkan, visible: bind {table.isRowSelected && !model.deleted})
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind {table.isRowSelected && !model.deleted}, actionPerformed: {
                if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                        app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    controller.delete()
                }
            })
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {
                table.isRowSelected
            }, actionPerformed: controller.clear)
        }
    }
}

PromptSupport.setPrompt('Nomor Seri...', nomorSeriSearch)

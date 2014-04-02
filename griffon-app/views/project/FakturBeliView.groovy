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
package project

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

application(title: 'Faktur Beli',
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
            comboBox(id: 'statusSearch', model: model.statusSearch)
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'supplierSearch', columns: 10, text: bind('supplierSearch', target: model, mutual: true), actionPerformed: controller.search)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }

        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.fakturBeliList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                    glazedColumn(name: '', property: 'deleted', width: 20) {
                        templateRenderer(exp: {it=='Y'?'D':''})
                    }
                    glazedColumn(name: 'Nomor', property: 'nomor', width: 120)
                    glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                        templateRenderer(exp: {it.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Supplier', property: 'supplier') {
                        templateRenderer(exp: {it.nama})
                    }
                    glazedColumn(name: 'Keterangan', property: 'keterangan')
                    glazedColumn(name: 'Status', property: 'status')
                    glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer)
                    glazedColumn(name: 'Jumlah Diskon', expression: {it.diskon?.jumlah(it.total())}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Total', expression: {it.total()}, columnClass: Integer) {
                        templateRenderer(exp: {currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }

                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
            label('Nomor:')
            textField(id: 'nomor', columns: 20, text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
            errorLabel(path: 'nomor', constraints: 'wrap')
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Supplier:')
            comboBox(id: 'supplier', model: model.supplier, templateRenderer: '${value}', errorPath: 'supplier')
            errorLabel(path: 'supplier', constraints: 'wrap')
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
                mvcPopupButton(id: 'listItemFaktur', text: 'Klik Disini Untuk Melihat Atau Mengisi Item Faktur...',
                    errorPath: 'listItemFaktur', mvcGroup: 'itemFakturAsChild',
                    args: {[parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur]},
                    dialogProperties: [title: 'Item Faktur', size: new Dimension(900,420)],
                    onFinish: {m, v, c ->
                        model.listItemFaktur.clear()
                        model.listItemFaktur.addAll(m.itemFakturList)
                        controller.refreshInformasi()
                    }
                )
            }
            errorLabel(path: 'listItemFaktur', constraints: 'wrap')
            label('Keterangan:')
            textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
            errorLabel(path: 'keterangan', constraints: 'wrap')

            panel(constraints: 'span, growx, wrap', visible: bind {model.notDeleted}) {
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
}

PromptSupport.setPrompt("Cari Nomor...", nomorSearch)
PromptSupport.setPrompt("Cari Supplier...", supplierSearch)
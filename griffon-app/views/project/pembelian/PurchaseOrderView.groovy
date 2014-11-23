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
package project.pembelian

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport
import javax.swing.JOptionPane
import java.awt.Dimension
import java.awt.FlowLayout
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.*

actions {
    action(id: 'showItemFaktur', name: 'Klik Disini Untuk Melihat Atau Mengisi Item Purchase Order...', closure: controller.showItemFaktur)
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        comboBox(id: 'statusSearch', model: model.statusSearch)
        textField(id: 'nomorPOSearch', columns: 10, text: bind('nomorPOSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'nomorFakturSearch', columns: 10, text: bind('nomorFakturSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'supplierSearch', columns: 10, text: bind('supplierSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }


    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.purchaseOrderList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemFaktur, enterKeyAction: showItemFaktur) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y' ? 'D' : '' })
                }
                glazedColumn(name: 'Nomor PO', property: 'nomor', width: 140)
                glazedColumn(name: 'Nomor Faktur', expression: { it.fakturBeli?.nomor })
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Supplier', property: 'supplier') {
                    templateRenderer(exp: { it?.nama })
                }
                glazedColumn(name: 'Keterangan', property: 'keterangan')
                glazedColumn(name: 'Status', property: 'status')
                glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer, visible: bind { model.showFakturBeli })
                glazedColumn(name: 'Jumlah Diskon', visible: bind { model.showFakturBeli },
                        expression: { it.jumlahDiskon() }, columnClass: Integer) {
                    templateRenderer(exp: { !it ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Total', expression: { it.total() }, columnClass: Integer, visible: bind { model.showFakturBeli }) {
                    templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
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
        label('Supplier:')
        comboBox(id: 'supplier', model: model.supplier, templateRenderer: '${value}', errorPath: 'supplier')
        errorLabel(path: 'supplier', constraints: 'wrap')
        label('Diskon:', visible: bind { model.showFakturBeli })
        panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0), visible: bind { model.showFakturBeli }) {
            decimalTextField(id: 'diskonPotonganPersen', columns: 5, bindTo: 'diskonPotonganPersen', errorPath: 'diskonPotonganPersen')
            label('% dan Potongan Langsung Rp')
            decimalTextField(id: 'diskonPotonganLangsung', columns: 20, bindTo: 'diskonPotonganLangsung', errorPath: 'diskonPotonganLangsung')
        }
        errorLabel(path: 'diskon', constraints: 'wrap', visible: bind { model.showFakturBeli })
        label('Isi:')
        panel {
            label(text: bind { model.informasi })
            button(id: 'listItemFaktur', action: showItemFaktur, errorPath: 'listItemFaktur')
        }
        errorLabel(path: 'listItemFaktur', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')

        panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Dibuat:')
            label(text: bind { model.created })
            label(text: bind { model.createdBy })
            label('   Dimodifikasi:')
            label(text: bind { model.modified })
            label(text: bind { model.modifiedBy })
        }

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"),
                    visible: bind { model.allowAddPO },
                    actionPerformed: {
                if (model.id != null) {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.update.message"),
                            app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        return
                    }
                }
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form)?.requestFocusInWindow()
            })
            button(id: 'cetak', action: cetak, visible: bind('isRowSelected', source: table, converter: {it && model.allowAddPO}))
            mvcPopupButton(id: 'penerimaanBarang', text: 'Penerimaan Barang', mvcGroup: 'penerimaanBarang',
                args: {[purchaseOrder: view.table.selectionModel.selected[0], allowTambahProduk: model.allowTambahProduk]},
                dialogProperties: [title: 'Penerimaan Barang', size: new Dimension(900,420)],
                onFinish: {m, v, c ->
                    view.table.selectionModel.selected[0] = m.purchaseOrder
                },
                visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan })
            )
            mvcPopupButton(id: 'itemBarangAsChild', text: 'Sisa Belum Diterima', mvcGroup: 'itemBarangAsChild', onBeforeDisplay: controller.onShowSisaBarang,
                dialogProperties: [title: 'Penerimaan Barang', size: new Dimension(900,420)],
                visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan }))
            mvcPopupButton(id: 'fakturBeli', text: 'Faktur Pembelian', mvcGroup: 'fakturBeli',
                args: {[purchaseOrder: view.table.selectionModel.selected[0], allowTambahProduk: model.allowTambahProduk]},
                dialogProperties: [title: 'Faktur Pembelian'],
                onFinish: {m, v, c ->
                    view.table.selectionModel.selected[0] = m.purchaseOrder
                },
                visible: bind('isRowSelected', source: table, converter: { it && model.showFakturBeli })
            )
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

PromptSupport.setPrompt("Nomor PO...", nomorPOSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorFakturSearch)
PromptSupport.setPrompt("Supplier...", supplierSearch)
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

import java.awt.Dimension
import java.awt.FlowLayout

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'showPembayaran', name: 'Klik Disini Untuk Melihat Pembayaran Yang Telah Dilakukan...', closure: controller.showPembayaran)
}

application(title: 'hutang',
        preferredSize: [320, 240],
        pack: true,
        //location: [50,50],
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
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'supplierSearch', columns: 10, text: bind('supplierSearch', target: model, mutual: true), actionPerformed: controller.search)
            checkBox('Jatuh Tempo ', id: 'chkJatuhTempo', selected: bind('chkJatuhTempo', target: model, mutual: true))
            dateTimePicker(id: 'jatuhTempoSearch', localDate: bind('jatuhTempoSearch', target: model, mutual: true), timeVisible: false,
                visible: bind('chkJatuhTempo', source: model))
            comboBox(id: 'statusSearch', model: model.statusSearch)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }

        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.purchaseOrderList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                        doubleClickAction: showPembayaran, enterKeyAction: showPembayaran) {
                    glazedColumn(name: '', property: 'deleted', width: 20) {
                        templateRenderer(exp: {it=='Y'?'D':''})
                    }
                    glazedColumn(name: 'Nomor PO', property: 'nomor', width: 150)
                    glazedColumn(name: 'Nomor Faktur Beli', expression: {it.fakturBeli?.nomor})
                    glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                        templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Supplier', property: 'supplier') {
                        templateRenderer(exp: {it?.nama})
                    }
                    glazedColumn(name: 'Keterangan', property: 'keterangan')
                    glazedColumn(name: 'Status', property: 'status')
                    glazedColumn(name: 'Jatuh Tempo', expression: {it.fakturBeli?.jatuhTempo}) {
                        templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Jumlah Hutang', expression: {it.fakturBeli?.hutang?.jumlah}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Jumlah Dibayar', expression: {it.fakturBeli?.hutang?.jumlahDibayar()}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Sisa Hutang', expression: {it.fakturBeli?.hutang?.sisa()}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind {table.isRowSelected}, constraints: PAGE_END, focusCycleRoot: true) {
            label('Pembayaran:')
            button(id: 'listPembayaranHutang', action: showPembayaran, errorPath: 'listPembayaranHutang')
            errorLabel(path: 'listPembayaranHutang', constraints: 'wrap')
        }
    }

}

PromptSupport.setPrompt("Cari Faktur...", nomorSearch)
PromptSupport.setPrompt("Cari Supplier...", supplierSearch)
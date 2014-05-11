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
package project.penjualan

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport

import java.awt.FlowLayout

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'showPembayaran', name: 'Klik Disini Untuk Melihat Pembayaran Yang Telah Dilakukan...', closure: controller.showPembayaran)
}

application() {
    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), actionPerformed: controller.search)
            checkBox('Jatuh Tempo ', id: 'chkJatuhTempo', selected: bind('chkJatuhTempo', target: model, mutual: true))
            dateTimePicker(id: 'jatuhTempoSearch', localDate: bind('jatuhTempoSearch', target: model, mutual: true), timeVisible: false,
                visible: bind('chkJatuhTempo', source: model))
            comboBox(id: 'statusSearch', model: model.statusSearch)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }

        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.fakturJualList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                        doubleClickAction: showPembayaran, enterKeyAction: showPembayaran) {
                    glazedColumn(name: '', property: 'deleted', width: 20) {
                        templateRenderer(exp: {it=='Y'?'D':''})
                    }
                    glazedColumn(name: 'Nomor Faktur', property: 'nomor')
                    glazedColumn(name: 'Tanggal', property: 'tanggal') {
                        templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Konsumen', property: 'konsumen') {
                        templateRenderer(exp: {it?.nama})
                    }
                    glazedColumn(name: 'Keterangan', property: 'keterangan')
                    glazedColumn(name: 'Status', property: 'status')
                    glazedColumn(name: 'Jatuh Tempo', property: 'jatuhTempo') {
                        templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Jumlah Piutang', expression: {it.piutang?.jumlah}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Jumlah Dibayar', expression: {it.piutang?.jumlahDibayar()}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                    glazedColumn(name: 'Sisa Piutang', expression: {it.sisaPiutang()}, columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind {table.isRowSelected}, constraints: PAGE_END, focusCycleRoot: true) {
            label('Pembayaran:')
            button(id: 'listPembayaranHutang', action: showPembayaran, constraints: 'wrap')
        }
    }
}

PromptSupport.setPrompt("Nomor Faktur...", nomorSearch)
PromptSupport.setPrompt("Konsumen...", konsumenSearch)
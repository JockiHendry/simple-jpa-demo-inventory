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

actions {
    action(id: 'showBarangYangHarusDikirim', name: 'Klik Disini Untuk Melihat Item Untuk Dikirim...', closure: controller.showBarangYangHarusDikirim)
    action(id: 'cetakSummary', name: 'Cetak Summary...', closure: controller.cetakSummary)
    action(id: 'simpanSuratJalan', name: 'Simpan Surat Jalan', closure: controller.simpanSuratJalan)
    action(id: 'kirimSuratJalan', name: 'Order Di Surat Jalan Sudah Diantar...', closure: controller.kirimSuratJalan)
}

application() {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            comboBox(id: 'statusSearch', model: model.statusSearch)
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'salesSearch', columns: 10, text: bind('salesSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), actionPerformed: controller.search)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
            button(action: cetakSummary)
        }

        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.fakturJualOlehSalesList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showBarangYangHarusDikirim, enterKeyAction: showBarangYangHarusDikirim) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y'? 'D': ''})
                }
                glazedColumn(name: 'Nomor', property: 'nomor', width: 140)
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Konsumen', expression: { it.konsumen.nama })
                glazedColumn(name: 'Sales', expression: { it.konsumen.sales.nama })
                glazedColumn(name: 'Status', property: 'status')
                glazedColumn(name: 'Keterangan', expression: { it.pengeluaranBarang?.keterangan?: ''})
            }
        }

        panel(constraints: PAGE_END) {
            borderLayout()
            panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: CENTER, focusCycleRoot: true) {
                label('Nomor Faktur Jual:')
                label(id: 'nomorFakturJual', text: bind('nomorFakturJual', source: model), constraints: 'wrap')
                label('Nomor Surat Jalan:')
                label(id: 'nomorSuratJalan', text: bind('nomorSuratJalan', source: model), errorPath: 'nomorSuratJalan')
                errorLabel(path: 'nomorSuratJalan', constraints: 'wrap')
                label('Tanggal Kirim:')
                dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
                errorLabel(path: 'tanggal', constraints: 'wrap')
                label('Alamat Tujuan:')
                textField(id: 'alamatTujuan', columns: 60, text: bind('alamatTujuan', target: model, mutual: true), errorPath: 'alamatTujuan')
                errorLabel(path: 'alamatTujuan', constraints: 'wrap')
                label('Keterangan:')
                textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
                errorLabel(path: 'keterangan', constraints: 'wrap')
            }

            panel(constraints: PAGE_END) {
                flowLayout(alignment: FlowLayout.LEADING)
                button(action: simpanSuratJalan, visible: bind { model.allowBuatSuratJalan })
                button(action: kirimSuratJalan, visible: bind { model.allowKirim })
                button('Hapus Pengiriman', actionPerformed: controller.batalKirim, visible: bind { model.allowBatalKirim})
                button(action: showBarangYangHarusDikirim, visible: bind { table.isRowSelected })
                button('Cetak', actionPerformed: controller.cetak, visible: bind('isRowSelected', source: table, converter: {it && model.allowPrint}))
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind { table.isRowSelected }, actionPerformed: controller.clear)
            }
        }
    }

}

PromptSupport.setPrompt("Sales...", salesSearch)
PromptSupport.setPrompt("Konsumen...", konsumenSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorSearch)
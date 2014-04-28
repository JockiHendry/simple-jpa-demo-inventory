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
    action(id: 'showBarangYangHarusDikirim', name: 'Klik Disini Untuk Melihat Item Yang Dikirim...', closure: controller.showBarangYangHarusDikirim)
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
            textField(id: 'nomorFakturSearch', columns: 10, text: bind('nomorFakturSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'nomorSuratJalanSearch', columns: 10, text: bind('nomorSuratJalanSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), actionPerformed: controller.search)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }

        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.fakturJualOlehSalesList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showBarangYangHarusDikirim, enterKeyAction: showBarangYangHarusDikirim) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y'? 'D': ''})
                }
                glazedColumn(name: 'Nomor Faktur', property: 'nomor')
                glazedColumn(name: 'Nomor Surat Jalan', expression: { it.pengeluaranBarang?.nomor })
                glazedColumn(name: 'Tanggal', property: 'tanggal') {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Konsumen', expression: { it.konsumen.nama })
                glazedColumn(name: 'Sales', expression: { it.sales.nama })
                glazedColumn(name: 'Status', property: 'status')
                glazedColumn(name: 'Nama Supir', expression: { it.pengeluaranBarang?.namaSupir })
                glazedColumn(name: 'Alamat Tujuan', expression: { it.pengeluaranBarang?.alamatTujuan })
                glazedColumn(name: 'Keterangan', property: 'keterangan')
            }
        }

        panel(constraints: PAGE_END) {
            borderLayout()
            panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: CENTER, focusCycleRoot: true) {
                label('Nomor Faktur:')
                label(id: 'nomorFaktur', text: bind('nomorFaktur', source: model), constraints: 'wrap')
                label('Nomor Surat Jalan:')
                label(id: 'nomorSuratJalan', text: bind('nomorSuratJalan', source: model), errorPath: 'nomorSuratJalan')
                errorLabel(path: 'nomorSuratJalan', constraints: 'wrap')
                label('Tanggal Terima:')
                dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
                errorLabel(path: 'tanggal', constraints: 'wrap')
                label('Nama Penerima:')
                textField(id: 'namaPenerima', columns: 20, text: bind('namaPenerima', target: model, mutual: true), errorPath: 'namaPenerima')
                errorLabel(path: 'namaPenerima', constraints: 'wrap')
            }

            panel(constraints: PAGE_END) {
                flowLayout(alignment: FlowLayout.LEADING)
                button('Simpan Bukti Terima', actionPerformed: controller.save, visible: bind { model.allowSimpan })
                button('Hapus', actionPerformed: controller.hapus, visible: bind { model.allowHapus})
                button(action: showBarangYangHarusDikirim, visible: bind { table.isRowSelected })
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind { table.isRowSelected }, actionPerformed: controller.clear)
            }
        }
    }

}

PromptSupport.setPrompt("Nomor Surat Jalan...", nomorSuratJalanSearch)
PromptSupport.setPrompt("Konsumen...", konsumenSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorFakturSearch)
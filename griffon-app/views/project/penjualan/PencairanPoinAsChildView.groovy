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
import org.joda.time.LocalDate
import java.awt.Color
import java.awt.FlowLayout
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'showDaftarBarang', name: 'Isi Daftar Produk Yang Ditukar...', closure: controller.showDaftarBarang)
}

application() {
    panel(id: 'mainPanel') {
        borderLayout()

        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.pencairanPoinList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                glazedColumn(name: 'Tanggal', property: 'tanggal') {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Jenis Penukaran', expression: {it?.class?.simpleName})
                glazedColumn(name: 'Jumlah Poin', property: 'jumlahPoin', columnClass: Integer)
                glazedColumn(name: 'Rate Per Poin', property: 'rate', columnClass: Integer) {
                    templateRenderer(exp: { it==null ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Keterangan', property: 'keterangan')
            }
        }

        panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Jumlah Poin:')
            numberTextField(id: 'jumlahPoin', columns: 20, bindTo: 'jumlahPoin', errorPath: 'jumlahPoin')
            errorLabel(path: 'jumlahPoin', constraints: 'wrap')
            label('Jenis Pencairan Poin:')
            comboBox(id: 'jenisPencairanPoin', model: model.jenisPencairanPoin, actionPerformed: controller.onPerubahanJenisLaporan)
            errorLabel(path: 'jenisPencairanPoin', constraints: 'wrap')
            label('Daftar Barang:', visible: bind {model.daftarBarangVisible})
            button(action: showDaftarBarang, errorPath: 'listItemBarang', visible: bind {model.daftarBarangVisible})
            errorLabel(path: 'listItemBarang', constraints: 'wrap', visible: bind {model.daftarBarangVisible})
            label('Keterangan:')
            textField(id: 'keterangan', columns: 50, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
            errorLabel(path: 'keterangan', constraints: 'wrap')

            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                    controller.save()
                    form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
                }, visible: bind {table.isNotRowSelected})
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {table.isRowSelected},
                    actionPerformed: controller.clear)
            }
        }

    }

}

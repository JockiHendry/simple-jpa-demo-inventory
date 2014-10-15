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

import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'showItemBarang', name: 'Klik Disini Untuk Melihat Atau Mengisi Daftar Barang...', closure: controller.showItemBarang)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
}

panel(id: 'mainPanel') {
    borderLayout()

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.pengeluaranBarangList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                doubleClickAction: showItemBarang, enterKeyAction: showItemBarang) {
            glazedColumn(name: '', property: 'deleted', width: 20) {
                templateRenderer(exp: { it=='Y'?'D':''})
            }
            glazedColumn(name: 'Nomor', property: 'nomor')
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: {it?.toString('dd-MM-yyyy')})
            }
            glazedColumn(name: 'Alamat Tujuan', property: 'alamatTujuan')
            glazedColumn(name: 'Keterangan', property: 'keterangan')
            glazedColumn(name: 'Qty', expression: { it.jumlah() }, columnClass: Integer)
        }
    }

    panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nomor Surat Jalan Retur:')
        label(id: 'nomor', text: bind('nomor', source: model), errorPath: 'nomor', constraints: 'wrap')
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Alamat Tujuan:')
        textField(id: 'alamatTujuan', columns: 60, text: bind('alamatTujuan', target: model, mutual: true), errorPath: 'alamatTujuan')
        errorLabel(path: 'alamatTujuan', constraints: 'wrap')
        label('Isi:')
        button(id: 'items', action: showItemBarang, errorPath: 'items')
        errorLabel(path: 'items', constraints: 'wrap')
        label('Keterangan:')
        textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
        errorLabel(path: 'keterangan', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap', visible: bind {model.notDeleted}) {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"), visible: bind { !table.isRowSelected }, actionPerformed: {
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            })
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {table.isRowSelected}, actionPerformed: controller.clear)
            button(action: delete, visible: bind {table.isRowSelected})
        }
    }
}

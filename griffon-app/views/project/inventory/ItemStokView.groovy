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

import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.CENTER

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        comboBox(id: 'periodeItemStok', model: model.periodeItemStok,
            templateRenderer: "\${it.tanggalMulai.toString('MMMM YYYY')} (Jumlah: \${it.jumlah})")
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }


    panel(constraints: CENTER) {
        borderLayout()
        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.itemStokList, sortingStrategy: SINGLE_COLUMN) {
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer("\${it.toString('dd-MM-yyyy')}")
                }
                glazedColumn(name: 'Qty', property: 'jumlah', columnClass: Integer, width: 40)
                glazedColumn(name: 'Referensi Finance', expression: {it.referensiStok?.deskripsiFinance()?: ''})
                glazedColumn(name: 'Referensi Gudang', expression: {it.referensiStok?.deskripsiGudang()?: ''})
                glazedColumn(name: 'Pihak Terkait', expression: {it.referensiStok?.pihakTerkait?: ''})
                glazedColumn(name: 'Dibuat', expression: {it.referensiStok?.dibuatOleh?:''})
                glazedColumn(name: 'Diubah', expression: {it.referensiStok?.diubahOleh?:''})
                glazedColumn(name: 'Keterangan', property: 'keterangan')
            }
        }
    }

    taskPane(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END) {
        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.close.button"), actionPerformed: {
                SwingUtilities.getWindowAncestor(mainPanel)?.dispose()
            }, mnemonic: KeyEvent.VK_T)
        }
    }
}

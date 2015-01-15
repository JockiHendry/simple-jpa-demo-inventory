/*
 * Copyright 2015 Jocki Hendry.
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

import java.awt.FlowLayout
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER

actions {
    action(id: 'tampilkanReferensi', name: 'Tampilkan Referensi', closure: controller.tampilkanReferensi)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        comboBox(id: 'periodeItemStok', model: model.periodeItemStok,
            templateRenderer: {"${it.tanggalMulai.toString('MMMM YYYY')} (Perubahan: ${it.jumlah}, Saldo: ${it.saldo})"})
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        checkBox('Referensi Finance', selected: bind('showReferensiFinance', target: model, mutual: true))
        checkBox('Referensi Gudang', selected: bind('showReferensiGudang', target: model, mutual: true))
        checkBox('Pembuat', selected: bind('showPembuat', target: model, mutual: true))
        checkBox('Keterangan', selected: bind('showKeterangan', target: model, mutual: true))
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.itemStokList, sortingStrategy: SINGLE_COLUMN) {
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: {it.toString('dd-MM-yyyy')})
            }
            glazedColumn(name: 'Qty', property: 'jumlah', columnClass: Integer, width: 50)
            glazedColumn(name: 'Saldo', property: 'saldo', columnClass: Integer, width: 60)
            glazedColumn(name: 'Pihak Terkait', expression: {it.referensiStok?.pihakTerkait?: ''})
            glazedColumn(name: 'Referensi Finance', expression: {it.referensiStok?.deskripsiFinance()?: ''},
                visible: bind {model.showReferensiFinance})
            glazedColumn(name: 'Referensi Gudang', expression: {it.referensiStok?.deskripsiGudang()?: ''},
                visible: bind {model.showReferensiGudang})
            glazedColumn(name: 'Dibuat', expression: {it.referensiStok?.dibuatOleh?:''},
                visible: bind {model.showPembuat})
            glazedColumn(name: 'Diubah', expression: {it.referensiStok?.diubahOleh?:''},
                visible: bind {model.showPembuat})
            glazedColumn(name: 'Keterangan', property: 'keterangan', visible: bind {model.showKeterangan})
            menuItem(tampilkanReferensi)
        }
    }
}

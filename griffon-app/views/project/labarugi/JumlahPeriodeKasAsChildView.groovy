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
package project.labarugi

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import java.awt.*

actions {
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
}

panel(id: 'mainPanel') {
    borderLayout()

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.jumlahPeriodeKasList, sortingStrategy: SINGLE_COLUMN) {
            glazedColumn(name: 'Kategori Kas', property: 'kategoriKas')
            glazedColumn(name: 'Jenis Transaksi Kas', property: 'jenisTransaksiKas')
            glazedColumn(name: 'Saldo', property: 'saldo', columnClass: Integer) {
                templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
        }
    }

    panel(constraints: PAGE_END) {
        flowLayout(alignment: FlowLayout.LEADING)
        button(action: close)
    }

}

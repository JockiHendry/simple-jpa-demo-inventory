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
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT
import static javax.swing.SwingConstants.RIGHT

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        label(text: bind { model.informasi })
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.fakturJualOlehSalesList, sortingStrategy: SINGLE_COLUMN) {
            glazedColumn(name: '', property: 'deleted', width: 20) {
                templateRenderer(exp: { it == 'Y'? 'D': ''})
            }
            glazedColumn(name: 'Nomor', property: 'nomor', width: 140)
            glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Jatuh Tempo', expression: { it.jatuhTempo }) {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') }) {
                    condition(if_: {LocalDate.now().isAfter(it)}, then_property_: 'foreground', is_: Color.RED, else_is_: Color.BLACK)
                    condition(if_: {isSelected}, then_property_: 'foreground', is_: Color.WHITE)
                }
            }
            glazedColumn(name: 'Status', property: 'status')
            glazedColumn(name: 'Keterangan', property: 'keterangan')
            glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer, visible: bind { model.showNilaiUang })
            glazedColumn(name: 'Jumlah Diskon', visible: bind { model.showNilaiUang }, expression: { it.jumlahDiskon() }, columnClass: Integer) {
                templateRenderer(exp: { !it ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Total', expression: { it.total() }, columnClass: Integer, visible: bind { model.showNilaiUang }) {
                templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
        }
    }
}

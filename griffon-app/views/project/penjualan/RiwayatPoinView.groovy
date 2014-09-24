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

import javax.swing.JOptionPane
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'cetak', name: 'Cetak', closure: controller.cetak)
    action(id: 'search', name: 'Lihat Riwayat', closure: controller.search)
    action(id: 'cariKonsumen', name: 'Cari Konsumen', closure: controller.cariKonsumen, mnemonic: KeyEvent.VK_K)
}

application(title: 'Riwayat Poin',
        preferredSize: [520, 340],
        pack: true,
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Konsumen: ')
            label(text: bind {model.konsumenSearch?: '- kosong -'})
            button(action: cariKonsumen, id: 'cariKonsumen', errorPath: 'konsumenSearch')
            label('  Tanggal: ')
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            button(action: search)
            button(action: cetak)
        }

        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.riwayatPoinList, sortingStrategy: SINGLE_COLUMN) {
                glazedColumn(name: 'Tanggal', property: 'tanggal') {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Poin', property: 'poin', columnClass: Integer)
                glazedColumn(name: 'Referensi', property: 'referensi')
            }
        }
    }
}
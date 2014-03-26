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
package project

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.prompt.PromptSupport

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.*

application(title: 'Received Not Invoiced',
  preferredSize: [320, 240],
  pack: true,
  //location: [50,50],
  locationByPlatform: true,
  iconImage:   imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'supplierSearch', columns: 10, text: bind('supplierSearch', target: model, mutual: true), actionPerformed: controller.search)
            checkBox(id: 'tampilkanhanyaRNI', text: 'Hanya Tampilkan RNI?', selected: bind('tampilkanHanyaRNI', target: model, mutual: true))
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }


        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.penerimaanBarangList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                    glazedColumn(name: '', property: 'deleted', width: 20) {
                        templateRenderer(exp: { it == 'Y' ? 'D' : '' })
                    }
                    glazedColumn(name: 'Nomor', property: 'nomor', width: 120)
                    glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                        templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                    }
                    glazedColumn(name: 'Supplier', property: 'supplier') {
                        templateRenderer(exp: { it?.nama })
                    }
                    glazedColumn(name: 'Faktur', property: 'faktur') {
                        templateRenderer(exp: {it?.nomor?: '-'})
                    }
                    glazedColumn(name: 'Keterangan', property: 'keterangan')
                }
            }
        }

        panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), visible: bind {table.isRowSelected}, constraints: PAGE_END, focusCycleRoot: true) {
            label('Nomor Faktur:')
            textField(id: 'nomorFaktur', columns: 20, text: bind('nomorFaktur', target: model, mutual: true), errorPath: 'nomorFaktur')
            errorLabel(path: 'nomorFaktur', constraints: 'wrap')
            label('Isi:')
            panel {
                mvcPopupButton(id: 'listItemBarang', text: 'Barang Yang Dikirim...',
                    errorPath: 'listItemBarang', mvcGroup: 'itemBarangAsChild',
                    args: { [parent: view.table.selectionModel.selected[0], listItemBarang: model.listItemBarang] },
                    dialogProperties: [title: 'Daftar Barang', size: new Dimension(900, 420)]
                )
                button('Sisa Barang Yang Belum Diterima...', actionPerformed: controller.sisaBarang)
            }
            errorLabel(path: 'listItemBarang', constraints: 'wrap')

            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button('Asosiasi Ke Faktur Beli', visible: bind('isRowSelected', source: table, converter: { it && !model.faktur }), actionPerformed: {
                    controller.assign()
                    form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
                })
            }

        }
    }
}

PromptSupport.setPrompt("Cari Nomor...", nomorSearch)
PromptSupport.setPrompt("Cari Supplier...", supplierSearch)
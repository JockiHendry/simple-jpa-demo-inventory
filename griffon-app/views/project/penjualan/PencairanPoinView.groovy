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
import java.awt.event.KeyEvent
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.RIGHT

actions {
    action(id: 'showDaftarBarang', name: 'Isi Daftar Produk Yang Ditukar...', closure: controller.showDaftarBarang)
    action(id: 'cariKonsumen', name: 'Cari Konsumen', closure: controller.cariKonsumen, mnemonic: KeyEvent.VK_K)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        flowLayout(alignment: FlowLayout.LEADING)
        dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
        label(' s/d ')
        dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
        textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
        textField(id: 'konsumenSearch', columns: 10, text: bind('konsumenSearch', target: model, mutual: true), actionPerformed: controller.search)
        button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.pencairanPoinList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: '', property: 'deleted', width: 20) {
                templateRenderer(exp: { it == 'Y'? 'D': ''})
            }
            glazedColumn(name: 'Nomor', property: 'nomor', width: 140)
            glazedColumn(name: 'Tanggal', property: 'tanggal') {
                templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
            }
            glazedColumn(name: 'Konsumen', expression: { it.konsumen.nama })
            glazedColumn(name: 'Jenis Penukaran', expression: {it?.class?.simpleName})
            glazedColumn(name: 'Jumlah Poin', property: 'jumlahPoin', columnClass: Integer)
            glazedColumn(name: 'Nominal', property: 'nominal', columnClass: Integer) {
                templateRenderer(exp: { it==null ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Keterangan', property: 'keterangan')
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Tanggal:')
        dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
        errorLabel(path: 'tanggal', constraints: 'wrap')
        label('Konsumen:')
        panel {
            label(text: bind {model.konsumen?: '- kosong -'})
            button(action: cariKonsumen, id: 'cariKonsumen', errorPath: 'konsumen')
        }
        errorLabel(path: 'konsumen', constraints: 'wrap')
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

        panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            label('Dibuat:')
            label(text: bind { model.created })
            label(text: bind { model.createdBy })
            label('   Dimodifikasi:')
            label(text: bind { model.modified })
            label(text: bind { model.modifiedBy })
        }

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            }, visible: bind {table.isNotRowSelected})
            button('Cetak', actionPerformed: controller.cetak, visible: bind {table.isRowSelected})
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {table.isRowSelected},
                actionPerformed: controller.clear)
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind {table.isRowSelected},
                actionPerformed: {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                            app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        controller.delete()
                    }
            })

        }
    }

}

PromptSupport.setPrompt("Nomor...", nomorSearch)
PromptSupport.setPrompt("Konsumen...", konsumenSearch)
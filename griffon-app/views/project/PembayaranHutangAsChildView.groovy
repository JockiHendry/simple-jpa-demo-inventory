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

import javax.swing.JOptionPane
import java.awt.FlowLayout
import java.awt.event.KeyEvent

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.RIGHT

application(title: 'simple-jpa-demo-inventory',
        preferredSize: [320, 240],
        pack: true,
        //location: [50,50],
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: CENTER) {
            borderLayout()
            scrollPane(constraints: CENTER) {
                glazedTable(id: 'table', list: model.pembayaranHutangList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
                    glazedColumn(name: 'Tanggal', property: 'tanggal') {
                        templateRenderer(exp: {it.toString('dd-MM-yyyy')})
                    }
                    glazedColumn(name: 'Jumlah', property: 'jumlah', columnClass: Integer) {
                        templateRenderer(exp: {!it?'-':currencyFormat(it)}, horizontalAlignment: RIGHT)
                    }
                }
            }
        }

        taskPane(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END) {
            label('Tanggal:')
            dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
            errorLabel(path: 'tanggal', constraints: 'wrap')
            label('Jumlah:')
            numberTextField(id: 'jumlah', columns: 10, bindTo: 'jumlah', errorPath: 'jumlah')
            errorLabel(path: 'jumlah', constraints: 'wrap')

            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button('Simpan', actionPerformed: {
                    if (!view.table.selectionModel.selectionEmpty) {
                        JOptionPane.showMessageDialog(view.mainPanel, 'Pembayaran tidak dapat di-edit.  Hapus dan buat pembayaran baru bila perlu!', 'Edit Pembayaran', JOptionPane.ERROR_MESSAGE)
                        return
                    }
                    controller.save()
                    jumlah.requestFocusInWindow()
                }, visible: bind{model.editable}, mnemonic: KeyEvent.VK_S)
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind('isRowSelected', source: table, converter: {it && model.editable}), actionPerformed: controller.clear, mnemonic: KeyEvent.VK_B)
                button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind('isRowSelected', source: table, converter: {it && model.editable}), actionPerformed: {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                            app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        controller.delete()
                    }
                }, mnemonic: KeyEvent.VK_H)
                button(app.getMessage("simplejpa.dialog.close.button"), actionPerformed: {
                    SwingUtilities.getWindowAncestor(mainPanel)?.dispose()
                }, mnemonic: KeyEvent.VK_T)
            }
        }
    }
}

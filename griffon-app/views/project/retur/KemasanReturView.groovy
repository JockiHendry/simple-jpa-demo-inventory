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

import javax.swing.JOptionPane
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*

actions {
    action(id: 'save', name: app.getMessage('simplejpa.dialog.save.button'), closure: controller.save)
    action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'showItemBarang', name: 'Items', closure: controller.showItemBarang)
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
}

application(title: 'Kemasan Retur',
        preferredSize: [520, 340],
        pack: true,
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.kemasanReturList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged, doubleClickAction: showItemBarang, enterKeyAction: showItemBarang) {
                glazedColumn(name: 'Nomor', expression: {String.format("%03d", it.nomor)})
                glazedColumn(name: 'Qty', expression: {it.items.size()})
            }
        }

        panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
            label('Nomor:')
            numberTextField(id: 'nomor', columns: 20, bindTo: 'nomor', errorPath: 'nomor')
            errorLabel(path: 'nomor', constraints: 'wrap')
            label('Items:')
            button(action: showItemBarang, errorPath: 'items')
            errorLabel(path: 'items', constraints: 'wrap')
            panel(visible: bind { table.isRowSelected }, constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                label('Created:')
                label(text: bind { model.created })
                label(text: bind { model.createdBy })
                label('   Modified:')
                label(text: bind { model.modified })
                label(text: bind { model.modifiedBy })
            }
            panel(constraints: 'span, growx, wrap') {
                flowLayout(alignment: FlowLayout.LEADING)
                button(action: save)
                button(visible: bind { table.isRowSelected }, action: cancel)
                button(visible: bind { table.isRowSelected }, action: delete)
                button(action: close)
            }
        }
    }
}
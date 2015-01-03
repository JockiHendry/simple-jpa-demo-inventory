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
package project.main

import net.miginfocom.swing.MigLayout
import simplejpa.swing.DialogUtils
import java.awt.FlowLayout
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER

panel(id: 'mainPanel') {
    borderLayout()

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.pengaturanList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged) {
            glazedColumn(name: 'Key', expression: { it.keyPengaturan })
            glazedColumn(name: 'Nilai', property: 'nilai') {
                templateRenderer(exp: {it?: '-'})
            }
        }
    }

    panel(id: "form", layout: new MigLayout('hidemode 2', '[right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Key:')
        textField(id: 'key', columns: 50, text: bind('keyPengaturan', source: model), editable: false, constraints: 'wrap')

        label('Nilai:', visible: bind { model.genericValue })
        textField(id: 'nilai', columns: 50, text: bind('nilai', target: model, mutual: true), visible: bind { model.genericValue }, errorPath: 'nilai')
        errorLabel(path: 'nilai', visible: bind { model.genericValue }, constraints: 'wrap')

        label('Nilai:', visible: bind { model.booleanValue})
        checkBox(id: 'nilaiBoolean', selected: bind('nilaiBoolean', target: model, mutual: true), visible: bind { model.booleanValue }, errorPath: 'nilaiBoolean')
        errorLabel(path: 'nilaiBoolean', visible: bind { model.booleanValue }, constraints: 'wrap')

        label('Password Baru:', visible: bind { model.passwordValue })
        passwordField(id: 'passwordBaru', columns: 50, visible: bind { model.passwordValue }, errorPath: 'nilai')
        errorLabel(path: 'nilai', visible: bind { model.passwordValue }, constraints: 'wrap')
        label('Ulangi Password Baru:', visible: bind { model.passwordValue })
        passwordField(id: 'ulangiPasswordBaru', columns: 50, visible: bind { model.passwordValue }, errorPath: 'ulangiPasswordBaru')
        errorLabel(path: 'ulangiPasswordBaru', visible: bind { model.passwordValue }, constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button('Save', actionPerformed: {
                if (!DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                    return
                }
                controller.save()
                //form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            }, visible: bind{table.isRowSelected})
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind{table.isRowSelected}, actionPerformed: controller.clear)
        }
    }
}
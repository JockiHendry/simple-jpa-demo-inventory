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
package project.main

import net.miginfocom.swing.MigLayout

import java.awt.FlowLayout

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.SINGLE_COLUMN
import static javax.swing.SwingConstants.CENTER
import static javax.swing.SwingConstants.CENTER

panel(id: 'mainPanel', layout: new MigLayout('', '[right][left][left][left,grow]',''), focusCycleRoot: true) {

    label('<html><strong>PENTING:</strong> Untuk melakukan operasi ini, Anda harus menghubungi supervisor untuk memperoleh persetujuan!</html>',
        constraints: 'span,wrap')

    label('Password Supervisor:')
    passwordField(id: 'password', columns: 20, errorPath: 'password', actionPerformed: controller.proses)
    button('Proses', actionPerformed: controller.proses)
    errorLabel(path: 'password', constraints: 'wrap')

}

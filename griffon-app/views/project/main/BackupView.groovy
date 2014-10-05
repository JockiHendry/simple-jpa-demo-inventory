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

import javax.swing.BorderFactory
import javax.swing.JFileChooser

def fileChooser = fileChooser(fileSelectionMode: JFileChooser.DIRECTORIES_ONLY)

panel(id: 'mainPanel', border: BorderFactory.createEmptyBorder(5,5,5,5)) {
    borderLayout()
    panel(layout: new MigLayout('', '[left][left]', ''), constraints: PAGE_START) {
        label('Lokasi MySQL: ')
        textField(text: bind('basedir', source: model, mutual: true), columns: 50, constraints: 'wrap')

        label('Password Database: ')
        passwordField(id: 'databasePassword', columns: 20, constraints: 'wrap')

        label('Lokasi Tujuan: ')
        label(text: bind('lokasiTujuan', source: model), constraints: 'split 2')
        button('Pilih Lokasi Tujuan', constraints: 'gapleft 10px, wrap', actionPerformed: {
            if (fileChooser.showOpenDialog(view.mainPanel)==JFileChooser.APPROVE_OPTION) {
                model.lokasiTujuan = fileChooser.selectedFile
            }
        })

        label('Arguments: ')
        textField(text: bind('arguments', source: model, mutual: true), columns: 50, constraints: 'wrap')

        button('Mulai Backup', actionPerformed: controller.&mulai, constraints: 'gaptop 10px, wrap')
    }

    scrollPane(constraints: CENTER) {
        textArea(id: 'output', editable: false)
    }

}

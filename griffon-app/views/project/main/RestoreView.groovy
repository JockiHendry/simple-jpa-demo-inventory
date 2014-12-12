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
import javax.swing.filechooser.FileNameExtensionFilter

def fileChooser = fileChooser(fileSelectionMode: JFileChooser.FILES_ONLY,
    fileFilter: new FileNameExtensionFilter("Backup files", 'csv', 'sql', 'xls'),
    currentDirectory: bind('fileRestore', source: model, mutual: true))

panel(id: 'mainPanel', border: BorderFactory.createEmptyBorder(5,5,5,5)) {
    borderLayout()
    panel(layout: new MigLayout('', '[left][left]', ''), constraints: PAGE_START) {
        label('File Restore: ')
        label(text: bind('fileRestore', source: model), constraints: 'split 2')
        button('Pilih Lokasi File Backup', constraints: 'gapleft 10px, wrap', actionPerformed: {
            if (fileChooser.showOpenDialog(view.mainPanel)==JFileChooser.APPROVE_OPTION) {
                model.fileRestore = fileChooser.selectedFile
            }
        })

        label('Password Database: ')
        passwordField(id: 'databasePassword', columns: 20, constraints: 'wrap')

        panel(constraints: 'gaptop 10px, span 2, wrap') {
            button('Mulai Restore', actionPerformed: controller.&mulai)
            button('Refresh Stok', actionPerformed: controller.refreshStok)
            button('Refresh Jumlah Akan Dikirim', actionPerformed: controller.refreshJumlahAkanDikirim)
            button('Refresh Saldo Stok', actionPerformed: controller.refreshSaldoStok)
        }
    }

    scrollPane(constraints: CENTER) {
        textArea(id: 'output', editable: false)
    }
}

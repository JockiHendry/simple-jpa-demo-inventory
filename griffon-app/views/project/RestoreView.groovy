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

import javax.swing.BorderFactory
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

def fileChooser = fileChooser(fileSelectionMode: JFileChooser.FILES_ONLY,
    fileFilter: new FileNameExtensionFilter("Backup files", 'csv', 'sql', 'xls'),
    currentDirectory: bind('fileRestore', source: model, mutual: true))

application(title: 'simple-jpa-demo-inventory',
        preferredSize: [320, 240],
        pack: true,
        //location: [50,50],
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                imageIcon('/griffon-icon-32x32.png').image,
                imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel', border: BorderFactory.createEmptyBorder(5,5,5,5)) {
        borderLayout()
        panel(layout: new MigLayout('', '[left][left]', ''), constraints: PAGE_START) {
            label('File Restore: ')
            label(text: bind('fileRestore', source: model), constraints: 'split 2')
            button('Pilih Lokasi Tujuan', constraints: 'gapleft 10px, wrap', actionPerformed: {
                if (fileChooser.showOpenDialog(view.mainPanel)==JFileChooser.APPROVE_OPTION) {
                    model.fileRestore = fileChooser.selectedFile
                }
            })

            label('Password Database: ')
            passwordField(id: 'databasePassword', columns: 20, constraints: 'wrap')

            button('Mulai Restore', actionPerformed: controller.&mulai, constraints: 'gaptop 10px, wrap')
        }

        scrollPane(constraints: CENTER) {
            textArea(id: 'output', editable: false)
        }

    }

}

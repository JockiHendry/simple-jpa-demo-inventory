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

import groovy.ui.ConsoleTextEditor
import java.awt.FlowLayout

actions {
    action(id: 'cari', name: 'Cari', closure: controller.cari)
    action(id: 'simpan', name: 'Simpan', closure: controller.simpan)
    action(id: 'reset', name: 'Reset', closure: controller.reset)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints:PAGE_START) {
        flowLayout(alignment: FlowLayout.LEFT)
        comboBox(id: 'namaTemplateFaktur', model: model.namaTemplateFaktur)
        button(action: cari)
    }

    widget(new ConsoleTextEditor(), id: 'inputEditor', constraints: CENTER)

    panel(constraints: PAGE_END) {
        flowLayout(alignment: FlowLayout.LEFT)
        button(action: simpan)
        button(action: reset)
    }
}
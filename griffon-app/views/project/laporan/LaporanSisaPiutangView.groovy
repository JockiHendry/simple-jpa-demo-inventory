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
package project.laporan

import net.miginfocom.swing.MigLayout

application() {
    panel(id: 'mainPanel', layout: new MigLayout('hidemode 2', '[right][left,grow]', '')) {
        label('Nama Sales')
        textField(text: bind('salesSearch', target: model, mutual: true), columns: 20, constraints: 'wrap')

        label('Dan', constraints: 'wrap')
        label('Nama Konsumen')
        textField(text: bind('konsumenSearch', target: model, mutual: true), columns: 20, constraints: 'wrap')

        checkBox('Cetak Formulir', selected: bind('cetakFormulir', target: model, mutual: true), constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            button('OK', actionPerformed: controller.tampilkanLaporan)
            button('Batal', actionPerformed: controller.batal)
        }
    }
}

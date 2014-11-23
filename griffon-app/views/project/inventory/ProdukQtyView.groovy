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
package project.inventory

import net.miginfocom.swing.MigLayout
import javax.swing.SwingConstants
import java.awt.FlowLayout

actions {
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
    action(id: 'save', name: 'Simpan Perubahan', closure: controller.save)
}

panel(id: 'mainPanel', layout: new MigLayout('', '[right][left]',''), focusCycleRoot: true) {

    label('Qty Retur:')
    numberTextField(id: 'jumlahRetur', columns: 20, bindTo: 'jumlahRetur', errorPath: 'jumlahRetur', constraints: 'wrap')

    label('Qty Tukar:')
    numberTextField(id: 'jumlahTukar', columns: 20, bindTo: 'jumlahTukar', errorPath: 'jumlahTukar', constraints: 'wrap')

    separator(orientation: SwingConstants.HORIZONTAL, preferredSize: [1,1], constraints: 'growx,span 2,wrap')

    label('Qty Semua (S):')
    numberTextField(id: 'jumlahSemua', columns: 20, bindTo: 'jumlahSemua', editable: false, errorPath: 'jumlahSemua', constraints: 'wrap')

    label('Qty Akan Dikirim (K):')
    numberTextField(id: 'jumlahAkanDikirim', columns: 20, bindTo: 'jumlahAkanDikirim', errorPath: 'jumlahAkanDikirim', constraints: 'wrap')

    label('Qty Ready (S - K):')
    numberTextField(id: 'jumlahReady', columns: 20, bindTo: 'jumlahReady', editable: false, errorPath: 'jumlahReady', constraints: 'wrap')

    panel(constraints: 'span, growx, wrap') {
        flowLayout(alignment: FlowLayout.LEADING)
        button(action: save)
        button(action: close)
    }
}

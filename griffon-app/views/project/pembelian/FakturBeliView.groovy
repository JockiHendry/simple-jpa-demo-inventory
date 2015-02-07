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
package project.pembelian

import java.awt.event.KeyEvent
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*

actions {
    action(id: 'showItemFaktur', name: 'Item Faktur...', closure: controller.showItemFaktur, mnemonic: KeyEvent.VK_I)
    action(id: 'save', name: 'Simpan', closure: controller.save, mnemonic: KeyEvent.VK_S)
    action(id: 'delete', name: 'Hapus', closure: controller.delete, mnemonic: KeyEvent.VK_H)
    action(id: 'cariTanggal', name: 'Cari Tanggal', closure: controller.hitungJatuhTempo, mnemonic: KeyEvent.VK_T)
}

panel(id: 'mainPanel', layout: new MigLayout('', '[right][left][left,grow]', '')) {
    label('Nomor Faktur:')
    textField(id: 'nomor', columns: 20, text: bind('nomor', target: model, mutual: true), errorPath: 'nomor')
    errorLabel(path: 'nomor', constraints: 'wrap')
    label('Tanggal:')
    dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
    errorLabel(path: 'tanggal', constraints: 'wrap')
    label('Jatuh Tempo:')
    panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0)) {
        dateTimePicker(id: 'jatuhTempo', localDate: bind('jatuhTempo', target: model, mutual: true, value: LocalDate.now().plusDays(30)),
            errorPath: 'jatuhTempo', dateVisible: true, timeVisible: false)
        numberTextField(id: 'hariJatuhTempo', columns: 5, bindTo: 'hariJatuhTempo')
        label(' Hari ')
        button(action: cariTanggal)
    }
    errorLabel(path: 'jatuhTempo', constraints: 'wrap')
    label('Diskon:')
    panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0)) {
        decimalTextField(id: 'diskonPotonganPersen', columns: 5, bindTo: 'diskonPotonganPersen', errorPath: 'diskonPotonganPersen')
        label('% dan Potongan Langsung Rp')
        decimalTextField(id: 'diskonPotonganLangsung', columns: 20, bindTo: 'diskonPotonganLangsung', errorPath: 'diskonPotonganLangsung')
    }
    errorLabel(path: 'diskon', constraints: 'wrap')
    label('Isi:')
    panel {
        label(text: bind { model.informasi })
        button(action: showItemFaktur, errorPath: 'listItemFaktur')
    }
    errorLabel(path: 'listItemFaktur', constraints: 'wrap')
    label('Keterangan:')
    textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
    errorLabel(path: 'keterangan', constraints: 'wrap')

    panel(constraints: 'span, growx, wrap', visible: bind {model.notDeleted}) {
        flowLayout(alignment: FlowLayout.LEADING)
        button(action: save, visible: bind { model.showSave })
        button(action: delete)
    }
}

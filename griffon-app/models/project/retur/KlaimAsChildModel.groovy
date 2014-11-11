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

import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.retur.*
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.joda.time.*
import javax.swing.event.*
import simplejpa.swing.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class KlaimAsChildModel {

    ItemRetur parent

    @Bindable Boolean editable = true

    @Bindable Long id
    @Bindable Produk produk
    @Bindable BigDecimal jumlah
    BasicEventList<Klaim> klaimList = new BasicEventList<>()

    @Bindable Boolean produkVisible

    EnumComboBoxModel<JenisKlaim> jenisKlaim = new EnumComboBoxModel<JenisKlaim>(JenisKlaim)

}

enum JenisKlaim {

    TUKAR_BARU('Tukar Baru'),
    TUKAR_SERVIS('Tukar Hasil Servis'),
    POTONG_PIUTANG('Potong Piutang')

    String desc

    JenisKlaim(String desc) {
        this.desc = desc
    }

    @Override
    String toString() {
        desc
    }

}
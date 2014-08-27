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

import domain.pembelian.Supplier
import domain.retur.*
import domain.inventory.*
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.joda.time.*
import javax.swing.event.*
import simplejpa.swing.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class KlaimReturAsChildModel {

    @Bindable Long id

    def parent

    @Bindable Produk produk
    @Bindable Integer jumlah

    BasicEventList<KlaimRetur> klaimReturList = new BasicEventList<>()

    @Bindable boolean editable
    @Bindable boolean showReturOnly
    Supplier supplierSearch

}
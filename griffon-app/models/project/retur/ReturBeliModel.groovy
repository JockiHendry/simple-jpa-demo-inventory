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

import domain.retur.*
import domain.inventory.*
import domain.pembelian.*
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.joda.time.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class ReturBeliModel {

    ReturBeliViewMode mode
    @Bindable boolean showSave
    @Bindable boolean showPenukaran
    @Bindable boolean deleted = false
    Supplier forSupplier

    @Bindable Long id
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch
    @Bindable String nomorSearch
    @Bindable String supplierSearch
    EnumComboBoxModel statusSearch = new EnumComboBoxModel(StatusReturBeli)

    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    List<ItemBarang> items = []
    List<KlaimKemasan> listKlaimRetur = []
    @Bindable BigDecimal potongan
    BasicEventList<Supplier> supplierList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Supplier> supplier = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(supplierList)

    BasicEventList<ReturBeli> returBeliList = new BasicEventList<>()

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

}

enum StatusReturBeli {
    SEMUA("Semua"), SUDAH_DIPROSES("Sudah Diproses"), BELUM_DIPROSES("Belum Diproses")

    String text

    StatusReturBeli(String text) {
        this.text = text
    }

    @Override
    String toString() {
        text
    }

}
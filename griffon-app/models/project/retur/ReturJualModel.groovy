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

import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.retur.*
import domain.penjualan.*
import domain.penjualan.*
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.*
import javax.swing.event.*
import simplejpa.swing.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class ReturJualModel {

    ReturJualViewMode mode
    @Bindable boolean showSave
    @Bindable boolean showPenukaran

    @Bindable Long id

    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch
    @Bindable String nomorSearch
    @Bindable String konsumenSearch
    EnumComboBoxModel statusSearch = new EnumComboBoxModel(StatusReturJual)
    BasicEventList<Gudang> gudangList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Gudang> gudang = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(gudangList)

    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    List<ItemBarang> items = []
    List<KlaimRetur> listKlaimRetur = []
    @Bindable BigDecimal potongan
    BasicEventList<Konsumen> konsumenList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Konsumen> konsumen = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(konsumenList)

    BasicEventList<ReturJual> returJualList = new BasicEventList<>()

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

}

enum StatusReturJual {
    SEMUA("Semua"), SUDAH_DIPROSES("Sudah Diproses"), BELUM_DIPROSES("Belum Diproses")

    String text

    StatusReturJual(String text) {
        this.text = text
    }

    @Override
    String toString() {
        text
    }

}
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
package project.penjualan

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import domain.inventory.ItemBarang
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import domain.penjualan.PencairanPoin
import groovy.beans.Bindable
import org.jdesktop.swingx.combobox.EnumComboBoxModel
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.LocalDate

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class PencairanPoinAsChildModel {

    @Bindable Long id
    @Bindable @NotNull LocalDate tanggal
    @Bindable @NotNull @Min(1l) Integer jumlahPoin
    @Bindable String keterangan
    @Bindable BigDecimal rate
    List<ItemBarang> items = []
    EnumComboBoxModel<JenisPencairanPoin> jenisPencairanPoin = new EnumComboBoxModel<JenisPencairanPoin>(JenisPencairanPoin)

    BasicEventList pencairanPoinList = new BasicEventList()

    @Bindable boolean daftarBarangVisible

    Konsumen konsumen

}

enum JenisPencairanPoin {

    TUKAR_UANG('Tukar Uang'), TUKAR_BARANG('Tukar Barang'), POTONG_PIUTANG('Potong Piutang')

    String desc

    JenisPencairanPoin(String desc) {
        this.desc = desc
    }

    @Override
    String toString() {
        desc
    }

}
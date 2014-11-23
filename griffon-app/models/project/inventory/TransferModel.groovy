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

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Transfer
import org.joda.time.LocalDate

class TransferModel {

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    List<ItemBarang> items = []
    BasicEventList<Gudang> gudangList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Gudang> gudang = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(gudangList)
    @Bindable DefaultEventComboBoxModel<Gudang> tujuan = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(gudangList)

    BasicEventList<Transfer> transferList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String asalSearch
    @Bindable String tujuanSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch

    @Bindable String informasi

}
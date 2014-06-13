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
import domain.inventory.PenyesuaianStok
import groovy.beans.Bindable
import org.joda.time.LocalDate

class PenyesuaianStokModel {

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable boolean bertambah
    @Bindable boolean berkurang
    @Bindable String keterangan
    List<ItemBarang> items = []
    BasicEventList<Gudang> gudangList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Gudang> gudang = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(gudangList)

    BasicEventList<PenyesuaianStok> penyesuaianStokList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String gudangSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch

    @Bindable String informasi

}
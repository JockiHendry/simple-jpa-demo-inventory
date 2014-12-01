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

import ca.odell.glazedlists.BasicEventList
import domain.retur.ItemRetur
import domain.retur.ReturJualEceran
import org.jdesktop.swingx.combobox.EnumComboBoxModel
import org.joda.time.LocalDate

class ReturJualEceranModel {

    ReturJualViewMode mode
    @Bindable boolean showSave
    @Bindable boolean allowPenukaran
    @Bindable boolean deleted = false
    boolean excludeDeleted = false

    @Bindable Long id

    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch
    @Bindable String nomorSearch
    @Bindable String konsumenSearch
    EnumComboBoxModel statusSearch = new EnumComboBoxModel(StatusReturJual)

    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    List<ItemRetur> items = []
    @Bindable String namaKonsumen
    @Bindable Boolean bisaDijualKembali

    BasicEventList<ReturJualEceran> returJualList = new BasicEventList<>()

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

}
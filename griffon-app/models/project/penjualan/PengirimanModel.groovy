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
package project.penjualan

import ca.odell.glazedlists.BasicEventList
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import org.hibernate.validator.constraints.NotBlank
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.LocalDate
import util.SwingHelper

class PengirimanModel {

    @Bindable boolean allowBuatSuratJalan
    @Bindable boolean allowKirim
    @Bindable boolean allowPrint = true

    @Bindable String nomorFakturJual
    @Bindable String nomorSuratJalan
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    @Bindable @NotBlank String alamatTujuan

    BasicEventList<FakturJualOlehSales> fakturJualOlehSalesList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String salesSearch
    @Bindable String konsumenSearch
    ListComboBoxModel statusSearch = new ListComboBoxModel(SwingHelper.searchEnum(StatusFakturJual))

}
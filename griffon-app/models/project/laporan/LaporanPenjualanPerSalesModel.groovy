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
package project.laporan

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import domain.penjualan.Konsumen
import domain.penjualan.Sales
import org.joda.time.LocalDate

class LaporanPenjualanPerSalesModel {

    @Bindable LocalDate tanggalMulaiCari
    @Bindable LocalDate tanggalSelesaiCari
    BasicEventList<Sales> salesList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Sales> sales = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(salesList)
    @Bindable Konsumen konsumenSearch

    List result
    Map params = [:]

    boolean batal = false

}
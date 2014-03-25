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
package project

import ca.odell.glazedlists.BasicEventList
import domain.faktur.Faktur
import domain.inventory.ItemBarang
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import groovy.beans.Bindable
import org.joda.time.LocalDate

class ReceivedNotInvoicedModel {

    @Bindable Long id
    @Bindable String nomorFaktur
    @Bindable Faktur faktur
    List<ItemBarang> listItemBarang = []

    BasicEventList<PenerimaanBarang> penerimaanBarangList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String supplierSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch
    @Bindable boolean tampilkanHanyaRNI
}
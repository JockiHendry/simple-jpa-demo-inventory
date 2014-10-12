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
import domain.inventory.ItemBarang
import domain.penjualan.PengeluaranBarang
import domain.retur.ReturJual
import groovy.beans.Bindable
import org.hibernate.validator.constraints.NotBlank
import org.joda.time.LocalDate

class PengirimanReturModel {

    ReturJual parent

    @Bindable boolean allowKirim
    @Bindable boolean allowBatalKirim

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    @Bindable String alamatTujuan
    List<ItemBarang> listItemBarang = []

    BasicEventList<PengeluaranBarang> pengeluaranBarangList = new BasicEventList<>()

}
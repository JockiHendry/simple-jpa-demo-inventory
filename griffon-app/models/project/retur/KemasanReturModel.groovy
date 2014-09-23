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

import domain.pembelian.Supplier
import domain.retur.*
import domain.inventory.*
import ca.odell.glazedlists.*
import org.joda.time.LocalDate

class KemasanReturModel {

    ReturBeli parent
    Supplier supplierSearch

    @Bindable Long id

    @Bindable Integer nomor
    @Bindable LocalDate tanggal
    @Bindable String keterangan
    List<ItemBarang> items = []
    BasicEventList<KlaimKemasan> kemasanReturList = new BasicEventList<>()

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

}
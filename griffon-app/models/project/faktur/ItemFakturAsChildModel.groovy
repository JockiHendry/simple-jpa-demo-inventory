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
package project.faktur

import ca.odell.glazedlists.BasicEventList
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.Produk
import domain.penjualan.Konsumen

class ItemFakturAsChildModel {

    @Bindable boolean allowTambahProduk
    @Bindable boolean showHarga

    Faktur parent
    Konsumen konsumen

    @Bindable Produk produk
    @Bindable Integer jumlah
    @Bindable BigDecimal harga
    @Bindable BigDecimal diskonPotonganPersen
    @Bindable BigDecimal diskonPotonganLangsung
    @Bindable String keterangan

    BasicEventList<ItemFaktur> itemFakturList = new BasicEventList<>()

    @Bindable boolean editable
}
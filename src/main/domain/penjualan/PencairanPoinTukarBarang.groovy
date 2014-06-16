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
package domain.penjualan

import domain.event.PerubahanStok
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import griffon.util.ApplicationHolder

@DomainClass @Entity @Canonical
class PencairanPoinTukarBarang extends PencairanPoin {

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @NotEmpty @CollectionTable(name='PencairanPoin_Items')
    List<ItemBarang> listItemBarang = []

    @Override
    boolean valid() {
        konsumen.poinTerkumpul >= getJumlahPoin()
    }

    @Override
    void proses() {
        DaftarBarangSementara daftarBarangSementara = new DaftarBarangSementara(listItemBarang, -1)
        daftarBarangSementara.gudang = konsumen.sales.gudang
        daftarBarangSementara.keterangan = 'Penukaran Poin'
        ApplicationHolder.application?.event(new PerubahanStok(daftarBarangSementara, null))
    }

    Integer getJumlahPoin() {
        listItemBarang.sum { it.jumlah * (it.produk.poin?: 0) }
    }

    void tambah(ItemBarang itemBarang) {
        listItemBarang << itemBarang
    }
}


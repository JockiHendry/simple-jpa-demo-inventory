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
package domain.retur

import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.inventory.SebuahDaftarBarang
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotEmpty
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OrderColumn
import javax.validation.constraints.NotNull

@DomainClass @Entity @Canonical(excludes='items')
class Kemasan implements SebuahDaftarBarang {

    @NotNull
    Integer nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    String keterangan

    @ElementCollection(fetch=FetchType.EAGER) @NotEmpty @OrderColumn
    List<ItemBarang> items = []

    void tambah(ItemBarang itemBarang) {
        items << itemBarang
    }

    Integer jumlah() {
        items.sum { ItemBarang i -> i.jumlah }?: 0
    }

    @Override
    DaftarBarang toDaftarBarang() {
        def itemBarangs = items.collect { new ItemBarang(it.produk, it.jumlah) }
        DaftarBarangSementara hasil = new DaftarBarangSementara(itemBarangs)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil
    }

}

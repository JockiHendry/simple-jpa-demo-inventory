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
package domain.inventory

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity
class PenyesuaianStok implements SebuahDaftarBarang {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200)
    String keterangan

    @NotNull @ManyToOne
    Gudang gudang

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @NotEmpty
    @Fetch(FetchMode.SUBSELECT)
    List<ItemPenyesuaian> items = []

    @NotNull
    Boolean bertambah = true

    void tambah(ItemPenyesuaian itemPenyesuaian) {
        items << itemPenyesuaian
    }

    @Override
    DaftarBarang toDaftarBarang() {
        DaftarBarangSementara hasil = new DaftarBarangSementara(items.collect { new ItemBarang(it.produk, it.jumlah) },
            bertambah? 1:-1, false)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil.gudang = gudang
        hasil.keterangan = keterangan
        hasil
    }

}


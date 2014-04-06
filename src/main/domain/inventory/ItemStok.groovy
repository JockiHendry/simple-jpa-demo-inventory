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

import domain.faktur.Faktur
import domain.riwayat.DapatDibuatRiwayat
import groovy.transform.*
import org.w3c.dom.Attr
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*

import org.joda.time.*

@Embeddable @Canonical
class ItemStok implements DapatDibuatRiwayat {

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name='tanggalItemStok')
    LocalDate tanggal

    String nomorReferensi

    String jenisReferensi

    @NotNull
    Integer jumlah

    @Size(min=3, max=100)
    String keterangan

    @Override
    LocalDate tanggalRiwayat() {
        tanggal
    }
}


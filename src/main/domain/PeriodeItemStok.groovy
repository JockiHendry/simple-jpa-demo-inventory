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

package domain

import groovy.transform.*
import simplejpa.DomainClass
import type.Periode

import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(excludes="listItemStok")
class PeriodeItemStok {

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggalMulai

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggalSelesai

    @Min(0l)
    Integer jumlah = 0

    @NotNull
    Boolean arsip = Boolean.FALSE

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name="periodeItemStok_id")
    @OrderColumn(name='idx')
    List<ItemStok> listItemStok = []

    public boolean termasuk(LocalDate tanggal) {
        (tanggal.isEqual(tanggalMulai) || tanggal.isAfter(tanggalMulai)) &&
        (tanggal.isEqual(tanggalSelesai) || tanggal.isBefore(tanggalSelesai))
    }

    public boolean termasuk(Periode periode) {
        Interval i1 = new Interval(tanggalMulai.toDateMidnight(), tanggalSelesai.plusDays(1).toDateMidnight())
        Interval i2 = new Interval(periode.tanggalMulai.toDateMidnight(), periode.tanggalSelesai.plusDays(1).toDateMidnight())
        i1.overlaps(i2)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PeriodeItemStok that = (PeriodeItemStok) o

        if (tanggalMulai != that.tanggalMulai) return false
        if (tanggalSelesai != that.tanggalSelesai) return false

        return true
    }

    int hashCode() {
        int result
        result = tanggalMulai.hashCode()
        result = 31 * result + tanggalSelesai.hashCode()
        return result
    }
}


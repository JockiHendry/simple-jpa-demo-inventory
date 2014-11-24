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
package domain.faktur

import domain.event.BilyetGiroCleared
import domain.exception.DataTidakBolehDiubah
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import griffon.util.*

@DomainClass @Entity @Canonical
class BilyetGiro implements Comparable {

    @NotEmpty @Size(min=2, max=50)
    String nomorSeri

    @NotNull
    BigDecimal nominal

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate jatuhTempo

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggalPencairan

    @Size(min=2, max=50)
    String namaBank

    @Size(min=2, max=50)
    String diterimaDari

    boolean sudahJatuhTempo(LocalDate tanggal = LocalDate.now()) {
        jatuhTempo.isEqual(tanggal) || jatuhTempo.isBefore(tanggal)
    }

    void cairkan(LocalDate tanggalPencairan) {
        if (!sudahJatuhTempo(tanggalPencairan)) {
            throw new DataTidakBolehDiubah('Tidak boleh mencairkan bilyet giro yang belum efektif!')
        }
        this.tanggalPencairan = tanggalPencairan
        ApplicationHolder.application?.event(new BilyetGiroCleared(this))
    }

    boolean sudahDicairkan() {
        tanggalPencairan != null
    }

    @Override
    int compareTo(Object o) {
        if ((o != null) && (o instanceof BilyetGiro)) {
            if (!nomorSeri || !o.nomorSeri) {
                return -1
            } else {
                return nomorSeri.compareTo(o.nomorSeri)
            }
        }
        -1
    }
}


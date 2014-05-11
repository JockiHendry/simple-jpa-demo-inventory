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

import domain.exception.DataTidakBolehDiubah
import groovy.transform.*
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.joda.time.*


@Embeddable @Canonical
class Pembayaran {

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @NotNull @Min(0l)
    BigDecimal jumlah

    @NotNull
    Boolean potongan = Boolean.FALSE

    @ManyToOne
    BilyetGiro bilyetGiro

    public void melaluiGiro(String nomorSeri, LocalDate tanggalPenerbitan, LocalDate tanggalEfektif) {
        if (bilyetGiro) {
            throw new DataTidakBolehDiubah(this)
        }
        bilyetGiro = new BilyetGiro(nomorSeri, tanggalPenerbitan, tanggalEfektif)
    }

    public BigDecimal nominal() {
        if (bilyetGiro && !bilyetGiro.sudahDicairkan()) {
            return 0
        }
        jumlah
    }

}


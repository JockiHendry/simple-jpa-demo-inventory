/*
 * Copyright 2015 Jocki Hendry.
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

    @Embedded
    Referensi referensi

//    public void melaluiGiro(String nomorSeri, LocalDate tanggalPenerbitan, LocalDate tanggalEfektif) {
//        if (bilyetGiro) {
//            throw new DataTidakBolehDiubah(this)
//        }
//        bilyetGiro = new BilyetGiro(nomorSeri, tanggalPenerbitan, tanggalEfektif)
//    }

    public boolean matches(KRITERIA_PEMBAYARAN kriteria) {
        if (kriteria == KRITERIA_PEMBAYARAN.SEMUA) {
            return true
        } else if (kriteria == KRITERIA_PEMBAYARAN.TANPA_GIRO_BELUM_CAIR) {
            return !bilyetGiro || bilyetGiro.sudahDicairkan()
        } else if (kriteria == KRITERIA_PEMBAYARAN.TANPA_POTONGAN) {
            return !potongan
        } else if (kriteria == KRITERIA_PEMBAYARAN.HANYA_POTONGAN) {
            return potongan
        }
        throw new IllegalArgumentException("Kriteria tidak dikenali: $kriteria")
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Pembayaran)) return false

        Pembayaran that = (Pembayaran) o

        if (jumlah != that.jumlah) return false
        if (potongan != that.potongan) return false
        if (tanggal != that.tanggal) return false

        return true
    }

    int hashCode() {
        int result
        result = (tanggal != null ? tanggal.hashCode() : 0)
        result = 31 * result + (jumlah != null ? jumlah.hashCode() : 0)
        result = 31 * result + (potongan != null ? potongan.hashCode() : 0)
        return result
    }

}


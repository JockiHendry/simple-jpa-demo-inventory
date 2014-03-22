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
package domain.pembelian

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class Hutang {

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate jatuhTempo

    @NotNull
    Boolean lunas = Boolean.FALSE

    @NotNull @Min(0l)
    BigDecimal jumlah

    @ElementCollection @OrderColumn
    List<PembayaranHutang> listPembayaran = []

    BigDecimal sisa(LocalDate hinggaTanggal = LocalDate.now()) {
        jumlah - jumlahDibayar(hinggaTanggal)?: 0
    }

    BigDecimal jumlahDibayar(LocalDate hinggaTanggal = LocalDate.now()) {
        listPembayaran.findAll { it.tanggal.equals(hinggaTanggal) || it.tanggal.isBefore(hinggaTanggal) }
            .sum { it.jumlah }
    }

    void bayar(PembayaranHutang pembayaranHutang) {
        listPembayaran << pembayaranHutang
        if (sisa()==0) {
            lunas = true
        }
    }

    boolean sudahJatuhTempo(LocalDate padaTanggal = LocalDate.now()) {
        padaTanggal.equals(jatuhTempo) || padaTanggal.isAfter(jatuhTempo)
    }
}


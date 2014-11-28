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
package domain.labarugi

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class TransaksiKas {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=150)
    String pihakTerkait

    @ManyToOne @NotNull
    KategoriKas kategoriKas

    @NotNull @Min(0l)
    BigDecimal jumlah

    @NotNull @ManyToOne
    JenisTransaksiKas jenis

    @Size(min=2, max=150)
    String keterangan

    void tambahKas(boolean invers = false) {
        int pengali = invers? -1: 1
        kategoriKas.perubahanSaldo(tanggal.getMonthOfYear(), tanggal.getYear(), pengali * jumlah, jenis)
    }


}


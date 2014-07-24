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

import ast.Auditable
import domain.validation.InputPencairanPoin
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import javax.validation.groups.Default

@DomainClass @Entity @Canonical @Auditable
abstract class PencairanPoin {

    @NotEmpty(groups=[Default]) @Size(min=2, max=100, groups=[Default])
    String nomor

    @NotNull(groups=[InputPencairanPoin,Default]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @NotNull(groups=[InputPencairanPoin,Default]) @ManyToOne
    Konsumen konsumen

    @NotNull(groups=[InputPencairanPoin,Default]) @Min(value=1l, groups=[InputPencairanPoin,Default])
    Integer jumlahPoin

    @NotNull(groups=[Default]) @Min(value=1l, groups=[Default])
    BigDecimal rate

    String keterangan

    public PencairanPoin() {}

    public PencairanPoin(LocalDate tanggal, Integer jumlahPoin, BigDecimal rate) {
        this.tanggal = tanggal
        this.jumlahPoin = jumlahPoin
        this.rate = rate
    }

    BigDecimal getNominal() {
        jumlahPoin * rate
    }

    abstract boolean valid()

    abstract void proses()

    abstract void hapus()

}


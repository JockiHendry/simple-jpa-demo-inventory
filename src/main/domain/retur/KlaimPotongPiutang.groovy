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

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import java.text.NumberFormat

@DomainClass @Entity @Canonical
class KlaimPotongPiutang extends Klaim {

    @Min(0l) @NotNull
    BigDecimal jumlah

    @Override
    boolean equals(Object o) {
        if (id == null || o.id == null) return false
        if ((o instanceof KlaimPotongPiutang) && (id == o.id)) return true
        false
    }

    @Override
    String toString() {
        "Potong Piutang: ${jumlah? NumberFormat.currencyInstance.format(jumlah): 0}"
    }
}


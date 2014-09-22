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

import domain.inventory.DaftarBarang
import domain.inventory.Produk
import groovy.transform.*
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@MappedSuperclass @Canonical(excludes='listKlaimRetur')
abstract class Retur extends DaftarBarang {

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) @OrderColumn
    List<KlaimRetur> listKlaimRetur = []

    @NotNull
    Boolean sudahDiproses = false

    void tambah(KlaimRetur klaimRetur) {
        listKlaimRetur << klaimRetur
    }

    List<KlaimRetur> getKlaim(Class jenis, boolean hanyaBelumDiproses = false) {
        new ArrayList<KlaimRetur>(listKlaimRetur.findAll {
            (it.class == jenis) && (hanyaBelumDiproses? it.sudahDiproses==false: true)
        })
    }

    BigDecimal sisaPotongan() {
        getKlaim(KlaimPotongan, true).sum { KlaimPotongan k -> k.potongan }?: 0
    }

    void prosesSisaPotongan() {
        getKlaim(KlaimPotongan, true).each { proses(it) }
    }

    void proses(KlaimRetur klaimRetur) {
        klaimRetur.proses()
        sudahDiproses = listKlaimRetur.every { it.sudahDiproses }
    }

}


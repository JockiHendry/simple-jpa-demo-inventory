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

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @NotEmpty
    List<KlaimRetur> listKlaimRetur = []

    @NotNull
    Boolean sudahDiproses = false

    void periksaSelesaiDiproses() {
        if (!sudahDiproses) {
            sudahDiproses = listKlaimRetur.every { it.sudahDiproses }
        }
    }

    void tambahKlaimPotongan(BigDecimal jumlah) {
        listKlaimRetur << new KlaimRetur(potongan: jumlah)
    }

    void tambahKlaimTukar(Produk produk, int jumlah) {
        listKlaimRetur << new KlaimRetur(produk, jumlah)
    }

    List<KlaimRetur> getKlaimTukar(boolean hanyaBelumDiproses = false) {
        List<KlaimRetur> hasil = []
        hasil.addAll(listKlaimRetur.findAll {it.produk!=null && (hanyaBelumDiproses? it.sudahDiproses==false: true)})
        hasil
    }

    List<KlaimRetur> getKlaimPotongan(boolean hanyaBelumDiproses = false) {
        List<KlaimRetur> hasil = []
        hasil.addAll(listKlaimRetur.findAll {it.potongan!=null && (hanyaBelumDiproses? it.sudahDiproses==false: true)})
        hasil
    }

    BigDecimal sisaPotongan() {
        getKlaimPotongan(true).sum { it.potongan }?: 0
    }

    void prosesSisaPotongan() {
        getKlaimPotongan(true).each { it.sudahDiproses = true }
    }


}


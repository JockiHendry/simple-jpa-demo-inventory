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

package domain.inventory

import domain.general.ItemPeriodik
import groovy.transform.*
import org.joda.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.*

@Embeddable @Canonical @TupleConstructor(includeSuperProperties=true)
class ItemStok extends ItemPeriodik {

    @NotNull
    ReferensiStok referensiStok

    ItemStok() {}

    ItemStok(LocalDate tanggal, ReferensiStok referensiStok, Long jumlah, String keterangan) {
        this.tanggal = tanggal
        this.referensiStok = referensiStok
        this.jumlah = jumlah
        this.keterangan = keterangan
    }

    @Override
    long delta() {
        jumlah
    }

}


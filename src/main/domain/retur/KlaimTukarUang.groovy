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

import domain.inventory.Produk
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical @AutoClone
class KlaimTukarUang extends Klaim {

    @Min(0l) @NotNull
    BigDecimal jumlah

    @Override
    void merge(Klaim klaim) {
        if (bolehMerge(klaim)) {
            jumlah += klaim.jumlah
        }
    }

    @Override
    boolean bolehMerge(Klaim klaim) {
        klaim instanceof KlaimTukarUang
    }

    @Override
    BigDecimal informasiHarga() {
        jumlah
    }

    @Override
    Produk informasiProduk() {
        null
    }

    @Override
    Integer informasiQty() {
        null
    }

}


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
package domain.event

import domain.labarugi.KATEGORI_SISTEM
import griffon.core.*

@SuppressWarnings("GroovyUnusedDeclaration")
class TransaksiSistem extends Event {

    BigDecimal jumlah
    String nomorReferensi
    KATEGORI_SISTEM kategori
    boolean invers

    TransaksiSistem(BigDecimal jumlah, String nomorReferensi, KATEGORI_SISTEM kategori, boolean invers = false) {
        super(jumlah)
        this.jumlah = jumlah
        this.nomorReferensi = nomorReferensi
        this.kategori = kategori
        this.invers = invers
    }

}

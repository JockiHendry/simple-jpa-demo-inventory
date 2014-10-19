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
package domain.exception;

import domain.penjualan.Konsumen
import griffon.util.GriffonNameUtils

class MelebihiBatasKredit extends RuntimeException {

    Konsumen konsumen
    List<String> pesanKesalahan

    MelebihiBatasKredit(Konsumen konsumen, List<String> pesanKesalahan = []) {
        super("${konsumen.nama} tidak boleh melakukan kredit lagi!")
        this.konsumen = konsumen
        this.pesanKesalahan = pesanKesalahan
    }

    String getHTMLMessage() {
        StringBuilder result = new StringBuilder("<html>${konsumen.nama} tidak boleh melakukan kredit lagi!")
        if (!pesanKesalahan.empty) {
            result.append('<br><ul>')
            pesanKesalahan.each { result.append('<li>' + GriffonNameUtils.capitalize(it)) }
            result.append('</ul>')
        }
        result.append('</html>')
        result.toString()
    }
}

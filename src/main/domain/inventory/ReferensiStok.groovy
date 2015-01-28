/*
 * Copyright 2015 Jocki Hendry.
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

import groovy.transform.*
import javax.persistence.*
import griffon.util.*

@Embeddable @Canonical
class ReferensiStok {

    String classFinance

    String nomorFinance

    String classGudang

    String nomorGudang

    String pihakTerkait

    String dibuatOleh

    String diubahOleh

    String deskripsiClassFinance() {
        classFinance? GriffonNameUtils.getNaturalName(classFinance): ''
    }

    String deskripsiClassGudang() {
        classGudang? GriffonNameUtils.getNaturalName(classGudang): ''
    }

    String deskripsiFinance() {
        if (classFinance || nomorFinance) {
            return "${deskripsiClassFinance()}: ${nomorFinance}"
        }
        ""
    }

    String deskripsiGudang() {
        if (classGudang || nomorGudang) {
            return "${deskripsiClassGudang()}: ${nomorGudang}"
        }
        ""
    }

    String deskripsiSingkat() {
        nomorFinance?:(nomorGudang?:'')
    }

}


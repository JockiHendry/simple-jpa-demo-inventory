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

import domain.faktur.Faktur
import domain.inventory.ReferensiStok
import griffon.core.Event
import domain.inventory.DaftarBarang
import groovy.transform.Canonical

@Canonical
class PerubahanStok extends Event {

    boolean invers
    boolean pakaiYangSudahDipesan
    ReferensiStok referensiStok

    PerubahanStok(DaftarBarang daftarBarang, ReferensiStok referensiStok, boolean invers = false, boolean pakaiYangSudahDipesan = false) {
        super(daftarBarang)
        this.invers = invers
        this.referensiStok = referensiStok
        this.pakaiYangSudahDipesan = pakaiYangSudahDipesan
    }

}

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

package domain.repository

import domain.Gudang
import domain.exception.DataDuplikat
import domain.exception.GudangUtamaTidakKonsisten
import simplejpa.transaction.Transaction

@Transaction
class GudangRepository {

    public Gudang cariGudangUtama() {
        Gudang gudangUtama = findGudangByUtama(true)
        if (!gudangUtama) {
            throw new GudangUtamaTidakKonsisten()
        }
        gudangUtama
    }

    public Gudang buat(Gudang gudang) {
        if (findGudangByNama(gudang.nama)) {
            throw new DataDuplikat(gudang)
        }
        if (gudang.utama) {
            if (findGudangByUtama(true)) {
                throw new GudangUtamaTidakKonsisten("Tidak boleh ada lebih dari satu gudang utama")
            }
        }
        persist(gudang)
        gudang
    }

    public Gudang update(Gudang gudang) {
        if (gudang.utama && findGudangByIdNeAndUtama(gudang.id, true)) {
            throw new GudangUtamaTidakKonsisten("Tidak boleh ada lebih dari satu gudang utama")
        }
        return merge(gudang)
    }


}

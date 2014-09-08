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

package project.inventory

import domain.exception.DataDuplikat
import domain.exception.GudangUtamaTidakKonsisten
import domain.inventory.Gudang
import simplejpa.transaction.Transaction

@Transaction
class GudangRepository {

    private Gudang gudangUtama

    public Gudang cariGudangUtama() {
        if (!gudangUtama) {
            Gudang gudangUtama = findGudangByUtama(true)
            if (!gudangUtama) {
                throw new GudangUtamaTidakKonsisten()
            }
            this.gudangUtama = gudangUtama
        }
        gudangUtama
    }

    public List<Gudang> cari(String namaSearch) {
        findAllGudangByDsl {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
        }
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

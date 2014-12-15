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
package project.labarugi

import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.labarugi.*
import project.user.NomorService
import simplejpa.transaction.Transaction

@Transaction
class KasRepository {

    NomorService nomorService

    public List<Kas> cari(String namaSearch) {
        findAllKasByDsl([excludeDeleted: false, orderBy: 'nama']) {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
        }
    }

    public Kas cariUntukSistem() {
        Kas kasUntukSistem = findKasBySistem(true)
        if (!kasUntukSistem) {
            throw new IllegalStateException('Tidak menemukan kas untuk sistem!')
        }
        kasUntukSistem
    }

    public List<Kas> cariUntukLabaRugi() {
        findAllKasByLabaRugi(true)
    }

    public Kas buat(Kas kas) {
        if (findKasByNama(kas.nama)) {
            throw new DataDuplikat(kas)
        }
        persist(kas)
        kas
    }

    public Kas update(Kas kas) {
        Kas mergedKas = findKasById(kas.id)
        if (!mergedKas) {
            throw new DataTidakBolehDiubah(kas)
        }
        mergedKas.with {
            nama = kas.nama
            sistem = kas.sistem
            labaRugi = kas.labaRugi
        }
        mergedKas
    }

    public Kas hapus(Kas kas) {
        kas = findKasById(kas.id)
        if (!kas) {
            throw new DataTidakBolehDiubah(kas)
        }
        kas.deleted = 'Y'
        kas
    }

}
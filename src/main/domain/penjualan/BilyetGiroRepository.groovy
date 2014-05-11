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
package domain.penjualan

import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.faktur.BilyetGiro
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class BilyetGiroRepository {

    List<BilyetGiro> cari(String nomorSeriSearch, Boolean sudahDicairkanSearch = null) {
        findAllBilyetGiroByDsl([excludeDeleted: false]) {
            if (nomorSeriSearch) {
                nomorSeri like("%${nomorSeriSearch}%")
            }
            if (sudahDicairkanSearch) {
                and()
                sudahDicairkan notNull()
            }
        }
    }

    BilyetGiro buat(BilyetGiro bilyetGiro) {
        if (findBilyetGiroByNomorSeri(bilyetGiro.nomorSeri)) {
            throw new DataDuplikat(bilyetGiro)
        }
        persist(bilyetGiro)
        bilyetGiro
    }

    BilyetGiro update(BilyetGiro bilyetGiro) {
        BilyetGiro old = findBilyetGiroByNomorSeri(bilyetGiro.nomorSeri)
        if (old.sudahDicairkan()) {
            throw new DataTidakBolehDiubah(old)
        }
        old.nomorSeri = bilyetGiro.nomorSeri
        old.nominal = bilyetGiro.nominal
        old.jatuhTempo = bilyetGiro.jatuhTempo
        old
    }

    BilyetGiro cairkan(BilyetGiro bilyetGiro, LocalDate tanggalPencairan = LocalDate.now()) {
        bilyetGiro = findBilyetGiroById(bilyetGiro.id)
        bilyetGiro.cairkan(tanggalPencairan)
        bilyetGiro
    }

    BilyetGiro hapus(BilyetGiro bilyetGiro) {
        bilyetGiro = findBilyetGiroById(bilyetGiro.id)
        if (bilyetGiro.sudahDicairkan()) {
            throw new DataTidakBolehDiubah('Giro yang sudah dicairkan tidak boleh dihapus!', bilyetGiro)
        }
        bilyetGiro.deleted = 'Y'
        bilyetGiro
    }

}

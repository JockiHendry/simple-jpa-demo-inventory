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

import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.KategoriKas
import static project.labarugi.KategoriKasRepository.*

@SuppressWarnings("GroovyUnusedDeclaration")
class KategoriKasService {

    KategoriKasRepository kategoriKasRepository

    void serviceInit() {
        // Buat kategori pendapatan tukar barang bila perlu
        if (!kategoriKasRepository.getPendapatanTukarBarang()) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getPengeluaranTukarBarang()) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }

        // Buat kategori lain-lain bila perlu
        if (!kategoriKasRepository.getPendapatanLain()) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getPengeluaranLain()) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }
    }

}
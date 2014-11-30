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
import simplejpa.transaction.Transaction

@Transaction
class JenisTransaksiKasRepository {

    private JenisTransaksiKas jenisTransaksiKasUntukSistem

    public List<JenisTransaksiKas> cari(String namaSearch = null) {
        findAllJenisTransaksiKasByDsl([excludeDeleted: false, orderBy: 'nama']) {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
        }
    }

    public JenisTransaksiKas cariUntukSistem() {
        if (!jenisTransaksiKasUntukSistem) {
            jenisTransaksiKasUntukSistem = findJenisTransaksiKasBySistem(true)
            if (!jenisTransaksiKasUntukSistem) {
                throw new IllegalStateException('Tidak menemukan jenis transaksi kas untuk sistem!')
            }
        }
        jenisTransaksiKasUntukSistem
    }

    public JenisTransaksiKas buat(JenisTransaksiKas jenisTransaksiKas) {
        if (findJenisTransaksiKasByNama(jenisTransaksiKas.nama)) {
            throw new DataDuplikat(jenisTransaksiKas)
        }
        if (jenisTransaksiKas.sistem) {
            if (findJenisTransaksiKasBySistem(true)) {
                throw new IllegalStateException('Lebih dari satu jenis transaksi kas untuk sistem!')
            }
        }
        persist(jenisTransaksiKas)
        jenisTransaksiKas
    }

    public JenisTransaksiKas update(JenisTransaksiKas jenisTransaksiKas) {
        JenisTransaksiKas mergedJenisTransaksiKas = findJenisTransaksiKasById(jenisTransaksiKas.id)
        if (!mergedJenisTransaksiKas) {
            throw new DataTidakBolehDiubah(jenisTransaksiKas)
        }
        mergedJenisTransaksiKas.with {
            nama = jenisTransaksiKas.nama
            sistem = jenisTransaksiKas.sistem
        }
        mergedJenisTransaksiKas
    }

}
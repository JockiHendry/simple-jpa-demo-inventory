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
package domain.exception

import domain.inventory.Gudang;

class StokTidakCukup extends RuntimeException {

    Integer jumlahYangDibutuhkan
    Integer jumlahTersedia
    String namaProduk
    Gudang gudang
    JENIS_STOK jenisStok
    String pesan

    StokTidakCukup(String namaProduk, Integer jumlahYangDibutuhkan, Integer jumlahTersedia, Gudang gudang = null, JENIS_STOK jenisStok = JENIS_STOK.STOK_BIASA) {
        // Inisialisasi variabel
        this.namaProduk = namaProduk
        this.jumlahYangDibutuhkan = jumlahYangDibutuhkan
        this.jumlahTersedia = jumlahTersedia
        this.gudang = gudang
        this.jenisStok = jenisStok

        // Buat pesan kesalahan
        StringBuilder pesan = new StringBuilder(jenisStok.keterangan)
        pesan.append(' untuk ')
        pesan.append(namaProduk)
        pesan.append(' tidak tersedia')
        if (gudang) {
            pesan.append(' di ')
            pesan.append(gudang.nama)
        }
        pesan.append(' ( ')
        pesan.append(jumlahYangDibutuhkan)
        pesan.append(' melebihi jumlah tersedia: ')
        pesan.append(jumlahTersedia)
        pesan.append(' )')
        this.pesan = pesan
    }

    @Override
    String getMessage() {
        pesan
    }

    enum JENIS_STOK {
        STOK_BIASA ('Stok'),
        STOK_TUKAR ('Stok tukar')

        String keterangan

        JENIS_STOK(String keterangan) {
            this.keterangan = keterangan
        }

        @Override
        String toString() {
            this.keterangan
        }
    }

}

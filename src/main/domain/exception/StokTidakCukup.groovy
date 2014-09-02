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

    int jumlahYangDibutuhkan
    int jumlahTersedia
    String namaProduk
    Gudang gudang

    StokTidakCukup(String namaProduk, int jumlahYangDibutuhkan, int jumlahTersedia, Gudang gudang = null) {
        super("${namaProduk} sejumlah ${jumlahYangDibutuhkan} tidak tersedia di gudang [${gudang?.nama}]; yang tersedia adalah ${jumlahTersedia}")
        this.namaProduk = namaProduk
        this.jumlahYangDibutuhkan = jumlahYangDibutuhkan
        this.jumlahTersedia = jumlahTersedia
        this.gudang = gudang
    }

}

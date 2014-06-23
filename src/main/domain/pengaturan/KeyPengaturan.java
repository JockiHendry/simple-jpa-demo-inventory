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
package domain.pengaturan;

public enum KeyPengaturan {

    SUPERVISOR_PASSWORD("Supervisor Password", JenisNilai.PASSWORD, new byte[] {-126, 124, -53, 14, -22, -118, 112, 108, 76, 52, -95, 104, -111, -8, 78, 123}),
    MASA_JATUH_TEMPO("Jatuh Tempo (Hari)", JenisNilai.INTEGER, 30),
    UKURAN_HURUF_TABEL("Ukuran Huruf Tabel", JenisNilai.INTEGER, 0),
    BONUS_POINT_RATE("Bonus Point Rate", JenisNilai.INTEGER, 2000),
    LEVEL_MINIMUM_STOK("Level Minimum Untuk Stok", JenisNilai.INTEGER, 0),
    CREDIT_LIMIT_DEFAULT("Credit Limit Default", JenisNilai.INTEGER, 10000000)
    ;

    public String description;
    public JenisNilai jenisNilai;
    public Object defaultValue;

    KeyPengaturan(String description, JenisNilai jenisNilai, Object defaultValue) {
        this.description = description;
        this.jenisNilai = jenisNilai;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return description;
    }

}

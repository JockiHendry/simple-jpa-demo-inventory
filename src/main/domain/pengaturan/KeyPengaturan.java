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

    SUPERVISOR_PASSWORD("Supervisor Password", JenisNilai.PASSWORD, "MD5Of(12345)"),
    MASA_JATUH_TEMPO_HUTANG("Jatuh Tempo Hutang (Hari)", JenisNilai.INTEGER, 30),
    ;

    private String description;
    private JenisNilai jenisNilai;
    private Object defaultValue;

    KeyPengaturan(String description, JenisNilai jenisNilai, Object defaultValue) {
        this.description = description;
        this.jenisNilai = jenisNilai;
        this.defaultValue = defaultValue;
    }

    public JenisNilai getJenisNilai() {
        return jenisNilai;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return description;
    }

}

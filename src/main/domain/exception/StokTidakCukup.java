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
package domain.exception;

public class StokTidakCukup extends RuntimeException {

    private int jumlahYangDibutuhkan;
    private int jumlahTersedia;
    private String namaProduk;

    public StokTidakCukup(String namaProduk, int jumlahYangDibutuhkan, int jumlahTersedia) {
        super(namaProduk + " sejumlah " + jumlahYangDibutuhkan + " tidak tersedia; yang tersedia adalah " + jumlahTersedia);
        this.jumlahYangDibutuhkan = jumlahYangDibutuhkan;
        this.jumlahTersedia = jumlahTersedia;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public int getJumlahYangDibutuhkan() {
        return jumlahYangDibutuhkan;
    }

    public void setJumlahYangDibutuhkan(int jumlahYangDibutuhkan) {
        this.jumlahYangDibutuhkan = jumlahYangDibutuhkan;
    }

    public int getJumlahTersedia() {
        return jumlahTersedia;
    }

    public void setJumlahTersedia(int jumlahTersedia) {
        this.jumlahTersedia = jumlahTersedia;
    }

}

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
package domain.penjualan;

public enum StatusFakturJual {
    DIBUAT("Dibuat", true, true, true), DIANTAR("Diantar", false, true, true), DITERIMA("Diterima", false, true, false), LUNAS("Lunas", false, false, false);

    StatusFakturJual(String desc, boolean bolehDiubah, boolean piutangBolehDiubah, boolean pengeluaranBolehDiUbah) {
        this.desc = desc;
        this.bolehDiubah = bolehDiubah;
        this.piutangBolehDiubah = piutangBolehDiubah;
        this.pengeluaranBolehDiubah = pengeluaranBolehDiUbah;
    }

    @Override
    public String toString() {
        return desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean getBolehDiubah() {
        return bolehDiubah;
    }

    public boolean isBolehDiubah() {
        return bolehDiubah;
    }

    public void setBolehDiubah(boolean bolehDiubah) {
        this.bolehDiubah = bolehDiubah;
    }

    public boolean getPiutangBolehDiubah() {
        return piutangBolehDiubah;
    }

    public boolean isPiutangBolehDiubah() {
        return piutangBolehDiubah;
    }

    public void setPiutangBolehDiubah(boolean piutangBolehDiubah) {
        this.piutangBolehDiubah = piutangBolehDiubah;
    }

    public boolean getPengeluaranBolehDiubah() {
        return pengeluaranBolehDiubah;
    }

    public boolean isPengeluaranBolehDiubah() {
        return pengeluaranBolehDiubah;
    }

    public void setPengeluaranBolehDiubah(boolean pengeluaranBolehDiubah) {
        this.pengeluaranBolehDiubah = pengeluaranBolehDiubah;
    }

    private String desc;
    private boolean bolehDiubah;
    private boolean piutangBolehDiubah;
    private boolean pengeluaranBolehDiubah;
}

/*
 * Copyright 2015 Jocki Hendry.
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
package domain.pembelian;

public enum StatusPurchaseOrder {

    DIBUAT("Dibuat", true, true, true),
    FAKTUR_DITERIMA("Hanya Terima Faktur", false, true, true),
    BARANG_DITERIMA("Hanya Terima Barang", false, true, true),
    OK("Diterima Penuh", false, false, true),
    LUNAS("Lunas", false, false, false)

    String desc
    boolean bolehDiubah;
    boolean fakturBolehDiubah;
    boolean penerimaanBolehDiubah;

    StatusPurchaseOrder(String desc, boolean bolehDiubah, boolean fakturBolehDiubah, boolean penerimaanBolehDiubah) {
        this.desc = desc
        this.bolehDiubah = bolehDiubah
        this.fakturBolehDiubah = fakturBolehDiubah
        this.penerimaanBolehDiubah = penerimaanBolehDiubah
    }

    @Override
    public String toString() {
        return desc
    }

}
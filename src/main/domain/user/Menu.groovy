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
package domain.user;

public enum Menu {
    PENERIMAAN_BARANG("Penerimaan Barang"),
    PENGELUARAN_BARANG("Pengeluaran Barang"),
    BUKTI_TERIMA("Bukti Terima"),
    PURCHASE_ORDER("Purchase Order"),
    FAKTUR_BELI("Faktur Beli"),
    FAKTUR_JUAL("Faktur Jual"),
    HUTANG("Hutang"),
    PIUTANG("Piutang"),
    GIRO("Giro"),
    POIN("Poin"),
    PRODUK("Produk"),
    TRANSFER("Transfer"),
    PENYESUAIAN_STOK("Penyesuaian"),
    RETUR_JUAL("Retur Jual"),
    RETUR_BELI("Retur Beli"),
    LAPORAN("Laporan"),
    MAINTENANCE("Maintenance"),
    PESAN("Pesan")

    String naturalName

    Menu(String naturalName) {
        this.naturalName = naturalName
    }

    @Override
    public String toString() {
        naturalName
    }


}

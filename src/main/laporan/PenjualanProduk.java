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
package laporan;

import domain.inventory.Gudang;
import domain.inventory.Produk;

public class PenjualanProduk {

    private Produk produk;
    private Gudang gudang;
    private long jumlahPenjualan;

    public PenjualanProduk(Produk produk, Gudang gudang, long jumlahPenjualan) {
        this.produk = produk;
        this.gudang = gudang;
        this.jumlahPenjualan = jumlahPenjualan;
    }

    public PenjualanProduk(Produk produk, long jumlahPenjualan) {
        this.produk = produk;
        this.jumlahPenjualan = jumlahPenjualan;
    }

    public Produk getProduk() {
        return produk;
    }

    public void setProduk(Produk produk) {
        this.produk = produk;
    }

    public Gudang getGudang() {
        return gudang;
    }

    public void setGudang(Gudang gudang) {
        this.gudang = gudang;
    }

    public long getJumlahPenjualan() {
        return jumlahPenjualan;
    }

    public void setJumlahPenjualan(long jumlahPenjualan) {
        this.jumlahPenjualan = jumlahPenjualan;
    }

    @Override
    public String toString() {
        return "PenjualanProduk{" +
            "produk=" + produk +
            ", gudang=" + gudang +
            ", jumlahPenjualan=" + jumlahPenjualan +
            '}';
    }
}

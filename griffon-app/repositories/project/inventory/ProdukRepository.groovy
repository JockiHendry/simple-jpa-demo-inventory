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

package project.inventory

import domain.exception.DataDuplikat
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.pembelian.Supplier
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.StatusFakturJual
import domain.retur.ReturJual
import simplejpa.transaction.Transaction

@Transaction
class ProdukRepository {

    public List<Produk> cari(String namaSearch, boolean hanyaRetur = false, Supplier supplierSearch = null) {
        findAllProdukByDslFetchComplete([excludeDeleted: false, orderBy: 'nama']) {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
            if (hanyaRetur) {
                and()
                jumlahRetur gt(0)
            }
            if (supplierSearch) {
                and()
                supplier eq(supplierSearch)
            }
        }
    }

    /**
     * Menghapus data item stok lama dan memberi tanda bahwa operasi untuk masa waktu stok tersebut
     * tidak boleh dilakukan lagi.  Setelah pengarsipan, data <code>PeriodeItemStok</code> menjadi
     * tidak memiliki <code>ItemStok</code> (dan tidak dapat dimodifikasi lagi).
     *
     * @param deltaTahun adalah rentang waktu tahun yang akan diarsip, paling cepat adalah 3 tahun yang lalu.
     */
    public arsipItemStok(int deltaTahun) {
        if (deltaTahun < 3) {
            throw new IllegalArgumentException('Masa pengarsipan paling cepat adalah 3 tahun yang lalu')
        }

        List daftarProduk = findAllProduk()
        for (Produk p: daftarProduk) {
            p.daftarStok.each { Gudang g, StokProduk s ->
                for (PeriodeItemStok pi: s.periodeUntukArsip(deltaTahun)) {
                    pi.arsip = true
                    pi.listItem.clear()
                }
            }
        }
    }

    public Produk buat(Produk produk) {
        if (findProdukByNama(produk.nama)) {
            throw new DataDuplikat(produk)
        }
        persist(produk)
        produk
    }

    public Produk update(Produk produk) {
        Produk p = findProdukByIdFetchComplete(produk.id)
        p.nama = produk.nama
        p.hargaDalamKota = produk.hargaDalamKota
        p.hargaLuarKota = produk.hargaLuarKota
        p.satuan = produk.satuan
        p.supplier = produk.supplier
        p.poin = produk.poin
        p.levelMinimum = produk.levelMinimum
        p
    }

    public void refreshJumlahAkanDikirim() {
        def pengiriman = [:]
        findAllFakturJualByStatus(StatusFakturJual.DIBUAT).each { FakturJual f ->
            f.listItemFaktur.each { ItemFaktur i ->
                if (pengiriman.containsKey(i.produk)) {
                    pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                } else {
                    pengiriman[i.produk] = i.jumlah
                }
            }
        }
        findAllReturJualBySudahDiproses(false).each { ReturJual r ->
            r.yangHarusDitukar().items.each { ItemBarang i ->
                if (pengiriman.containsKey(i.produk)) {
                    pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                } else {
                    pengiriman[i.produk] = i.jumlah
                }
            }
        }
        findAllProduk().each { Produk p ->
            p.jumlahAkanDikirim = pengiriman[p]?: 0
        }
    }

    public Produk aturJumlahRetur(Produk produk, Integer jumlahRetur) {
        Produk p = findProdukByIdFetchComplete(produk.id)
        p.jumlahRetur = jumlahRetur
        p
    }

}

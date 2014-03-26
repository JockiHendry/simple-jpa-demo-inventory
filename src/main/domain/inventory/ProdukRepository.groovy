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

package domain.inventory

import domain.Container
import domain.faktur.Faktur
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.exception.DataDuplikat
import domain.pembelian.PenerimaanBarang
import simplejpa.transaction.Transaction

@Transaction
class ProdukRepository {

    public List<Produk> cari(String nama) {
        findAllProdukByDslFetchComplete([excludeDeleted: false]) {
            if (nama) {
                nama like("%${nama}%")
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
                    pi.listItemStok.clear()
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
        Produk p = findProdukById(produk.id)
        p.nama = produk.nama
        p.harga = produk.harga
        p
    }

    public void perubahanStok(Produk produk, int jumlah, DaftarBarang daftarBarang, String keterangan = null) {
        if (keterangan==null) keterangan = daftarBarang.keterangan
        Produk p = findProdukById(produk.id)
        p.perubahanStok(jumlah, daftarBarang, keterangan)
    }

}

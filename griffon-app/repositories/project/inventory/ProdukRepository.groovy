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

package project.inventory

import domain.exception.DataDuplikat
import domain.inventory.Gudang
import domain.inventory.ItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.pembelian.Supplier
import org.joda.time.LocalDate
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
     * @param deltaTahun adalah rentang waktu tahun yang akan diarsip.
     */
    public arsipItemStok(int deltaTahun) {
        List daftarProduk = findAllProduk()
        for (Produk p: daftarProduk) {
            p.daftarStok.each { Gudang g, StokProduk s ->
                s.arsip(deltaTahun)
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
        p.ongkosKirimBeli = produk.ongkosKirimBeli
        p.satuan = produk.satuan
        p.supplier = produk.supplier
        p.poin = produk.poin
        p.levelMinimum = produk.levelMinimum
        p.keterangan = produk.keterangan
        p
    }

    public Produk updateQty(Produk produk) {
        Produk p = findProdukByIdFetchComplete(produk.id)
        p.jumlahRetur = produk.jumlahRetur
        p.jumlahTukar = produk.jumlahTukar
        p
    }

    public List<ItemStok> cariSeluruhPenerimaan(Produk produk, Gudang gudang, LocalDate sampaiTanggal) {
        executeQuery('''
            SELECT i
            FROM StokProduk s LEFT JOIN s.listPeriodeRiwayat p LEFT JOIN p.listItem i
            WHERE s.produk = :produk AND s.gudang = :gudang AND i.jumlah > 0
            AND (i.referensiStok IS NULL OR i.referensiStok.classGudang<> 'Transfer')
            AND i.tanggal < :sampaiTanggal
            ORDER BY i.tanggal DESC, i.referensiStok.nomorGudang DESC
        ''', [:], [gudang: gudang, produk: produk, sampaiTanggal: sampaiTanggal])
    }

    public List cariSeluruhPenerimaan(Gudang gudang, LocalDate sampaiTanggal) {
        executeQuery('''
            SELECT s.produk, i
            FROM StokProduk s LEFT JOIN s.listPeriodeRiwayat p LEFT JOIN p.listItem i
            WHERE s.gudang = :gudang AND i.jumlah > 0
            AND (i.referensiStok IS NULL OR i.referensiStok.classGudang<> 'Transfer') AND i.tanggal < :sampaiTanggal
            ORDER BY i.tanggal DESC
        ''', [:], [gudang: gudang, sampaiTanggal: sampaiTanggal])
    }

    public List cariQtyTerakhir(LocalDate sebelumTanggal) {
        executeQuery('''
            SELECT s.produk, SUM(i.jumlah)
            FROM StokProduk s JOIN s.listPeriodeRiwayat p JOIN p.listItem i ON i.tanggal < :tanggal
            GROUP BY s.produk
        ''', [:], [tanggal: sebelumTanggal])
    }

    public List<ItemStok> cariSeluruhPerubahan(Produk produk, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        executeQuery('''
            SELECT i
            FROM StokProduk s LEFT JOIN s.listPeriodeRiwayat p LEFT JOIN p.listItem i
            WHERE s.produk = :produk AND i.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
            ORDER BY i.tanggal
        ''', [:], [produk: produk, tanggalMulai: tanggalMulai, tanggalSelesai: tanggalSelesai])
    }

    public List cariSeluruhPerubahan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        executeQuery('''
            SELECT s.produk, i
            FROM StokProduk s LEFT JOIN s.listPeriodeRiwayat p LEFT JOIN p.listItem i
            WHERE i.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
            ORDER BY i.tanggal
        ''', [:], [tanggalMulai: tanggalMulai, tanggalSelesai: tanggalSelesai])
    }

}

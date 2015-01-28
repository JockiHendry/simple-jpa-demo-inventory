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
package domain.labarugi

import domain.inventory.Gudang
import domain.inventory.ItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import groovy.transform.Canonical
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.inventory.ProdukRepository
import simplejpa.SimpleJpaUtil

@Canonical
class CacheGlobal {

    List daftarQtyTerakhir
    List daftarPenerimaan
    List daftarPerubahan
    LocalDate tanggalMulai = LocalDate.now(), tanggalSelesai = LocalDate.now()

    ProdukRepository produkRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Produk')
    GudangRepository gudangRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang')

    void perbaharui(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        this.tanggalMulai = tanggalMulai
        this.tanggalSelesai = tanggalSelesai
        daftarQtyTerakhir = produkRepository.cariQtyTerakhir(tanggalMulai)
        daftarPenerimaan = produkRepository.cariSeluruhPenerimaan(gudangRepository.cariGudangUtama(), tanggalMulai)
        if (tanggalSelesai != null) {
            daftarPerubahan = produkRepository.cariSeluruhPerubahan(tanggalMulai, tanggalSelesai)
        }
    }

    Integer cariQtyTerakhir(Produk produk) {
        Integer qtyTerakhir
        if (daftarQtyTerakhir == null) {
            produkRepository.withTransaction {
                produk = findProdukById(produk.id)
                StokProduk stokProduk = produk.stok(gudangRepository.cariGudangUtama())
                qtyTerakhir = stokProduk.saldoKumulatifSebelum(tanggalMulai)
                // Tambahkan juga dengan jumlah yang tersedia di gudang lain (yang bukan gudang utama)
                for (Gudang gudang : gudangRepository.cariBukanGudangUtama()) {
                    qtyTerakhir += produk.stok(gudang).saldoKumulatifSebelum(tanggalMulai)
                }
            }
        } else {
            def q = daftarQtyTerakhir.find { it[0] == produk }
            if (q) {
                qtyTerakhir = q[1]
            }
        }
        qtyTerakhir?: 0
    }

    List<ItemStok> cariPenerimaan(Produk produk) {
        List<ItemStok> hasil
        if (daftarPenerimaan) {
            hasil = daftarPenerimaan.findAll { it[0] == produk }.collect { it[1] }
        } else {
            hasil = produkRepository.cariSeluruhPenerimaan(produk, gudangRepository.cariGudangUtama(), tanggalMulai)
        }
        hasil?: []
    }

    List<ItemStok> cariPerubahan(Produk produk) {
        List<ItemStok> hasil
        if (daftarPerubahan) {
            hasil = daftarPerubahan.findAll { it[0] == produk }.collect { it[1] }
        } else {
            hasil = produkRepository.cariSeluruhPerubahan(produk, tanggalMulai, tanggalSelesai)
        }
        hasil?: []
    }


}

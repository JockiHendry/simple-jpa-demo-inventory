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
package project.labarugi

import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemPenyesuaian
import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.labarugi.NilaiInventory
import domain.pembelian.PurchaseOrder
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import simplejpa.transaction.Transaction

@Transaction
class LabaRugiService {

    GudangRepository gudangRepository

    NilaiInventory hitungInventory(LocalDate sampaiTanggal, Produk produk) {
        produk = findProdukById(produk.id)
        StokProduk stokProduk = produk.stok(gudangRepository.cariGudangUtama())
        int qtyTersedia = stokProduk.saldoKumulatifSebelum(sampaiTanggal)

        // Tambahkan juga dengan jumlah yang tersedia di gudang lain (yang bukan gudang utama)
        gudangRepository.findAllGudangByUtama(false).each { Gudang gudang ->
            qtyTersedia += produk.stok(gudang).saldoKumulatifSebelum(sampaiTanggal)
        }

        // Hitung nilai inventory dengan menggunakan metode FIFO
        NilaiInventory nilaiInventory = new NilaiInventory()
        for (int i = stokProduk.listPeriodeRiwayat.size() - 1; i >= 0; i--) {
            for (ItemStok itemStok: stokProduk.listPeriodeRiwayat[i].cariPenambahanInventory().reverse()) {
                if (nilaiInventory.qty() + itemStok.jumlah >= qtyTersedia) {
                    nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok.pihakTerkait, qtyTersedia - nilaiInventory.qty(), cariHarga(produk, itemStok))
                    break
                } else {
                    nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok.pihakTerkait, itemStok.jumlah, cariHarga(produk, itemStok))
                }
            }
        }

        nilaiInventory
    }

    BigDecimal cariHarga(Produk produk, ItemStok itemStok) {
        if (itemStok.referensiStok.classFinance == PurchaseOrder.simpleName) {
            PurchaseOrder po = findPurchaseOrderByNomor(itemStok.referensiStok.nomorFinance)
            Faktur f = po.fakturBeli?: po
            for (ItemFaktur i: f.listItemFaktur) {
                if (i.produk == produk) {
                    return i.diskon?.hasil(i.harga)?: i.harga
                }
            }
        } else if (itemStok.referensiStok.classGudang == PenyesuaianStok.simpleName) {
            PenyesuaianStok ps = findPenyesuaianStokByNomor(itemStok.referensiStok.nomorGudang)
            for (ItemPenyesuaian i: ps.items) {
                if (i.produk == produk) {
                    return i.harga?: produk.hargaDalamKota
                }
            }
        }
        throw new IllegalStateException("Tidak menemukan referensi harga ${produk.nama} untuk ${itemStok}!")
    }

}

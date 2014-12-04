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
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.inventory.Transfer
import domain.labarugi.NilaiInventory
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJual
import laporan.NilaiInventoryProduk
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.inventory.GudangRepository
import simplejpa.transaction.Transaction

@Transaction
class LabaRugiService {

    final Logger log = LoggerFactory.getLogger(LabaRugiService)

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
        if (qtyTersedia > 0) {
            for (PeriodeItemStok p : stokProduk.listPeriodeRiwayat.reverse()) {
                for (ItemStok itemStok : p.cariPenambahanInventory().reverse()) {
                    if (nilaiInventory.qty() + itemStok.jumlah >= qtyTersedia) {
                        nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok?.pihakTerkait, qtyTersedia - nilaiInventory.qty(), cariHarga(produk, itemStok))
                        break
                    } else {
                        nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok?.pihakTerkait, itemStok.jumlah, cariHarga(produk, itemStok))
                    }
                }
            }
        }

        nilaiInventory
    }

    BigDecimal cariHarga(Produk produk, ItemStok itemStok) {
        if (itemStok.referensiStok?.classFinance == PurchaseOrder.simpleName) {
            PurchaseOrder po = findPurchaseOrderByNomor(itemStok.referensiStok.nomorFinance)
            Faktur f = po.fakturBeli?: po
            for (ItemFaktur i: f.listItemFaktur) {
                if (i.produk == produk) {
                    return i.diskon?.hasil(i.harga)?: i.harga
                }
            }
        } else if (itemStok.referensiStok?.classGudang == PenyesuaianStok.simpleName) {
            PenyesuaianStok ps = findPenyesuaianStokByNomor(itemStok.referensiStok.nomorGudang)
            for (ItemPenyesuaian i: ps.items) {
                if (i.produk == produk) {
                    return i.harga?: produk.hargaDalamKota
                }
            }
        }
        log.warn "Tidak menemukan referensi harga ${produk.nama} untuk ${itemStok}! Yang dipakai adalah harga default ${produk.hargaDalamKota}!"
        produk.hargaDalamKota
    }

    BigDecimal hitungPenjualanKotor(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal hasil = 0
        List<FakturJual> fakturJuals = findAllFakturJualByTanggalBetween(tanggalMulai, tanggalSelesai)
        for (FakturJual fakturJual: fakturJuals) {
            hasil += fakturJual.nilaiPenjualan()
        }
        hasil
    }

    BigDecimal hitungHPP(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal hasil = 0
        List<Produk> produks = findAllProduk()
        for (Produk produk: produks) {
            hasil += hitungHPP(tanggalMulai, tanggalSelesai, produk)
        }
        hasil
    }

    BigDecimal hitungHPP(LocalDate tanggalMulai, LocalDate tanggalSelesai, Produk produk, NilaiInventoryProduk informasi = null) {
        NilaiInventory nilaiInventory = hitungInventory(tanggalMulai, produk)
        if (informasi) {
            informasi.produk = produk
            informasi.nilaiAwal = nilaiInventory.nilai()
        }
        List<ItemStok> itemStoks = produk.semuaItemStok(tanggalMulai, tanggalSelesai)
        long jumlahPenjualan = 0
        for (ItemStok itemStok: itemStoks) {
            // Abaikan transfer karena tidak mempengaruhi laba rugi
            if (itemStok.referensiStok.classGudang != Transfer.simpleName) {
                if (itemStok.jumlah > 0) {
                    nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok.pihakTerkait, itemStok.jumlah, cariHarga(produk, itemStok))
                } else {
                    jumlahPenjualan += itemStok.jumlah
                }
            }
        }
        BigDecimal hasil = nilaiInventory.kurang(Math.abs(jumlahPenjualan))
        if (informasi) {
            informasi.nilaiHPP = hasil
            informasi.nilaiAkhir = informasi.nilaiAwal - informasi.nilaiHPP
        }
        hasil
    }

}

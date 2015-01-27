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
package project.labarugi

import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.inventory.ItemPenyesuaian
import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.Produk
import domain.inventory.Transfer
import domain.labarugi.CacheGlobal
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.KATEGORI_SISTEM
import domain.labarugi.Kas
import domain.labarugi.KategoriKas
import domain.labarugi.NilaiInventory
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import laporan.ItemLabaRugi
import laporan.NilaiInventoryProduk
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.inventory.GudangRepository
import project.inventory.ProdukRepository
import simplejpa.transaction.Transaction
import static project.labarugi.KategoriKasRepository.KATEGORI_LAIN
import static project.labarugi.KategoriKasRepository.KATEGORI_TUKAR_BARANG

@Transaction
class LabaRugiService {

    final Logger log = LoggerFactory.getLogger(LabaRugiService)

    ProdukRepository produkRepository
    GudangRepository gudangRepository
    KategoriKasRepository kategoriKasRepository
    KasRepository kasRepository

    @SuppressWarnings("GroovyUnusedDeclaration")
    void serviceInit() {
        // Buat kategori pendapatan tukar barang bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }

        // Buat kategori lain-lain bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }
    }

    NilaiInventory hitungInventory(Produk produk, CacheGlobal cacheGlobal = new CacheGlobal()) {
        int qtyTerakhir = cacheGlobal.cariQtyTerakhir(produk)
        NilaiInventory nilaiInventory = new NilaiInventory(produk: produk)
        if (qtyTerakhir > 0) {
            List daftarItemStok = cacheGlobal.cariPenerimaan(produk)
            for (ItemStok itemStok : daftarItemStok) {
                if ((nilaiInventory.qty() + itemStok.jumlah) >= qtyTerakhir) {
                    nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok?.pihakTerkait, qtyTerakhir - nilaiInventory.qty(),
                        cariHarga(produk, itemStok), itemStok.referensiStok?.deskripsiSingkat())
                    break
                } else {
                    nilaiInventory.tambah(itemStok.tanggal, itemStok.referensiStok?.pihakTerkait, itemStok.jumlah,
                        cariHarga(produk, itemStok), itemStok.referensiStok?.deskripsiSingkat())
                }
            }
        }
        nilaiInventory
    }

    BigDecimal cariHarga(Produk produk, ItemStok itemStok) {
        if (itemStok.referensiStok?.classFinance == PurchaseOrder.simpleName) {
            PurchaseOrder po = findPurchaseOrderByNomor(itemStok.referensiStok.nomorFinance)
            if (po) {
                Faktur f = po.fakturBeli ?: po
                for (ItemFaktur i : f.listItemFaktur) {
                    if (i.produk == produk) {
                        return i.diskon?.hasil(i.harga) ?: i.harga
                    }
                }
            }
        } else if (itemStok.referensiStok?.classGudang == PenyesuaianStok.simpleName) {
            PenyesuaianStok ps = findPenyesuaianStokByNomor(itemStok.referensiStok.nomorGudang)
            if (ps) {
                for (ItemPenyesuaian i : ps.items) {
                    if (i.produk == produk) {
                        return i.harga ?: null
                    }
                }
            }
        }
        log.warn "Tidak menemukan referensi harga ${produk.nama} untuk ${itemStok}!"
        return null
    }

    List hitungPenjualan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal penjualanSales = 0, penjualanEceran = 0, potonganPiutang = 0
        List<FakturJual> fakturJuals = findAllFakturJualByTanggalBetween(tanggalMulai, tanggalSelesai)
        for (FakturJual fakturJual: fakturJuals) {
            if (fakturJual instanceof FakturJualOlehSales) {
                penjualanSales += fakturJual.nilaiPenjualan()
                if (fakturJual.piutang) {
                    potonganPiutang += fakturJual.piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN)
                }
            } else if (fakturJual instanceof FakturJualEceran) {
                penjualanEceran += fakturJual.nilaiPenjualan()
            }
        }
        [penjualanSales, penjualanEceran, potonganPiutang]
    }

    List hitungHPP(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal hasil = 0, ongkosKirim = 0
        CacheGlobal cacheGlobal = new CacheGlobal()
        cacheGlobal.perbaharui(tanggalMulai, tanggalSelesai)
        List<Produk> produks = findAllProduk()
        for (Produk produk: produks) {
            NilaiInventoryProduk info = new NilaiInventoryProduk()
            hasil += hitungHPP(produk, info, cacheGlobal)
            ongkosKirim += ((info.qtyPenjualan?:0) * (produk.ongkosKirimBeli?:0))
        }
        [hasil, ongkosKirim]
    }

    BigDecimal hitungHPP(Produk produk, NilaiInventoryProduk informasi = null, CacheGlobal cacheGlobal = new CacheGlobal()) {
        NilaiInventory nilaiInventory = hitungInventory(produk, cacheGlobal)
        if (informasi) {
            informasi.produk = produk
            informasi.nilaiAwal = nilaiInventory.nilai()
        }
        List<ItemStok> itemStoks = cacheGlobal.cariPerubahan(produk)
        long jumlahPenjualan = 0
        for (ItemStok itemStok: itemStoks) {
            // Abaikan transfer karena tidak mempengaruhi laba rugi
            if (itemStok.referensiStok?.classGudang != Transfer.simpleName) {
                if (itemStok.jumlah > 0) {
                    BigDecimal harga = cariHarga(produk, itemStok)
                    nilaiInventory.tambah(itemStok.tanggal, itemStok?.referensiStok?.pihakTerkait, itemStok.jumlah, harga)
                    if (informasi) {
                        informasi.nilaiAwal += ((itemStok.jumlah?:0) * (harga?:0))
                    }
                } else {
                    jumlahPenjualan += itemStok.jumlah
                }
            }
        }
        BigDecimal hasil = nilaiInventory.kurang(Math.abs(jumlahPenjualan))
        if (informasi) {
            informasi.nilaiHPP = hasil
            informasi.nilaiAkhir = informasi.nilaiAwal - informasi.nilaiHPP
            informasi.qtyPenjualan = Math.abs(jumlahPenjualan)
        }
        hasil
    }

    List<ItemLabaRugi> laporanLabaRugi(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        List<ItemLabaRugi> hasil = []
        def (penjualanSales, penjualanEceran, potonganPiutang) = hitungPenjualan(tanggalMulai, tanggalSelesai)
        def (hpp, ongkosKirimBeli) = hitungHPP(tanggalMulai, tanggalSelesai)

        hasil << new ItemLabaRugi('Pendapatan Piutang Penjualan Sales (Gross)', penjualanSales, null)
        hasil << new ItemLabaRugi('Pendapatan Penjualan Eceran', penjualanEceran, null)
        hasil << new ItemLabaRugi('Pendapatan Operasional', totalPendapatan(tanggalMulai, tanggalSelesai), null)
        hasil << new ItemLabaRugi('Harga Pokok Penjualan (HPP)', null, hpp)
        hasil << new ItemLabaRugi('Ongkos Kirim Pembelian', null, ongkosKirimBeli)
        hasil << new ItemLabaRugi('Potongan Piutang', null, potonganPiutang)
        hasil << new ItemLabaRugi('Pengeluaran Operasional', null, totalPengeluaran(tanggalMulai, tanggalSelesai))

        hasil
    }

    long totalPendapatan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        long hasil = 0
        for (Kas kas: kasRepository.cariUntukLabaRugi()) {
            hasil += kas.jumlah(tanggalMulai, tanggalSelesai, JENIS_KATEGORI_KAS.PENDAPATAN, true)
        }
        hasil
    }

    long totalPengeluaran(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        long hasil = 0
        for (Kas kas: kasRepository.cariUntukLabaRugi()) {
            hasil += kas.jumlah(tanggalMulai, tanggalSelesai, JENIS_KATEGORI_KAS.PENGELUARAN, true)
        }
        hasil
    }

}

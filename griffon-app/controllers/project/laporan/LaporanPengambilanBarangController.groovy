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
package project.laporan

import domain.inventory.ItemBarang
import domain.inventory.ItemPenyesuaian
import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.inventory.Transfer
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import domain.retur.ReturJual
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import laporan.PengambilanBarang
import laporan.PengambilanBarangSummary
import listener.InventoryEventListenerService
import org.joda.time.LocalDate
import project.inventory.ProdukRepository
import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanPengambilanBarangController {

    LaporanPengambilanBarangModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    boolean periksaReferensi(ItemStok itemStok) {
        if (itemStok.keterangan == InventoryEventListenerService.KETERANGAN_INVERS_HAPUS) {
            return false
        }
        ReferensiStok referensiStok = itemStok.referensiStok
        if (referensiStok.classGudang) {
            List hasil = produkRepository."findAll${referensiStok.classGudang}ByNomor"(referensiStok.nomorGudang, [excludeDeleted: false])
            if (hasil.empty) return false
            if (hasil.any { it.deleted == 'Y' }) return false
        }
        if (referensiStok.classFinance) {
            List hasil = produkRepository."findAll${referensiStok.classFinance}ByNomor"(referensiStok.nomorFinance, [excludeDeleted: false])
            if (hasil.empty) return false
            if (hasil.any { it.deleted == 'Y' }) return false
        }
        true
    }

    void prosesLaporanPengambilanBarang(boolean summary = false) {
        produkRepository.withTransaction {
            // Faktur jual oleh sales
            List daftarFakturJualSales = executeQuery('''
                SELECT f FROM FakturJualOlehSales f JOIN FETCH f.pengeluaranBarang p
                WHERE p IS NOT NULL AND p.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
                AND p.gudang.utama = TRUE
                ''',  [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])
            for (FakturJualOlehSales f: daftarFakturJualSales) {
                for (ItemBarang i : f.pengeluaranBarang.items) {
                    ReferensiStok ref = new ReferensiStokBuilder(f).refer(f.pengeluaranBarang).buat()
                    if (summary) {
                        model.result << new PengambilanBarangSummary(f.pengeluaranBarang.tanggal, i.produk, i.jumlah)
                    } else {
                        model.result << new PengambilanBarang(f.pengeluaranBarang.tanggal, i.produk, i.jumlah, f.konsumen.nama, ref)
                    }
                }
            }

            // Faktur jual eceran
            List daftarFakturJualEceran = executeQuery('''
                SELECT f FROM FakturJualEceran f JOIN FETCH f.pengeluaranBarang p
                WHERE p IS NOT NULL AND p.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
                ''',  [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])
            for (FakturJualEceran f: daftarFakturJualEceran) {
                for (ItemBarang i : f.pengeluaranBarang.items) {
                    ReferensiStok ref = new ReferensiStokBuilder(f).refer(f.pengeluaranBarang).buat()
                    if (summary) {
                        model.result << new PengambilanBarangSummary(f.pengeluaranBarang.tanggal, i.produk, 0, i.jumlah)
                    } else {
                        model.result << new PengambilanBarang(f.pengeluaranBarang.tanggal, i.produk, i.jumlah, f.namaPembeli, ref)
                    }
                }
            }

            // Penyesuaian stok
            List daftarPenyesuaianStok = executeQuery('''
                SELECT f FROM PenyesuaianStok f WHERE f.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
                AND f.gudang.utama = TRUE AND f.bertambah = FALSE
            ''', [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])
            for (PenyesuaianStok p: daftarPenyesuaianStok) {
                for (ItemPenyesuaian i: p.items) {
                    ReferensiStok ref = new ReferensiStokBuilder(p).buat()
                    if (summary) {
                        model.result << new PengambilanBarangSummary(p.tanggal, i.produk, 0, 0, 0, i.jumlah)
                    } else {
                        model.result << new PengambilanBarang(p.tanggal, i.produk, i.jumlah, '[Internal]', ref)
                    }
                }
            }

            // Transfer
            List daftarTransfer = executeQuery('''
                SELECT f FROM Transfer f WHERE f.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
                AND f.gudang.utama = TRUE
            ''', [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])
            for (Transfer p: daftarTransfer) {
                for (ItemBarang i: p.items) {
                    ReferensiStok ref = new ReferensiStokBuilder(p).buat()
                    if (summary) {
                        model.result << new PengambilanBarangSummary(p.tanggal, i.produk, 0, 0, 0, 0, i.jumlah)
                    } else {
                        model.result << new PengambilanBarang(p.tanggal, i.produk, i.jumlah, '[Internal]', ref)
                    }
                }
            }

            // Retur jual
            List daftarReturJual = executeQuery('''
                SELECT r FROM ReturJual r WHERE r.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
                AND r.pengeluaranBarang.gudang.utama = TRUE
            ''',  [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])
            for (ReturJual r: daftarReturJual) {
                for (ItemBarang i : r.pengeluaranBarang.items) {
                    ReferensiStok ref = new ReferensiStokBuilder(r).refer(r.pengeluaranBarang).buat()
                    if (summary) {
                        model.result << new PengambilanBarangSummary(r.pengeluaranBarang.tanggal, i.produk, 0, 0, i.jumlah)
                    } else {
                        String keterangan
                        if (r instanceof ReturJualOlehSales) {
                            keterangan = r.konsumen.nama
                        } else if (r instanceof ReturJualEceran) {
                            keterangan = r.namaKonsumen
                        }
                        model.result << new PengambilanBarang(r.pengeluaranBarang.tanggal, i.produk, i.jumlah, keterangan, ref)
                    }
                }
            }

        }
    }

    def tampilkanLaporan = {
        model.result = []
        if (model.cetakSummary?.booleanValue()) {
            model.params.fileLaporan = 'report/laporan_summary_pengambilan_barang.jasper'
            prosesLaporanPengambilanBarang(true)
        } else {
            model.params.fileLaporan = 'report/laporan_pengambilan_barang.jasper'
            prosesLaporanPengambilanBarang()
        }
        model.params.'tanggalMulaiCari' = model.tanggalMulaiCari
        model.params.'tanggalSelesaiCari' = model.tanggalSelesaiCari
        close()
    }

    def reset = {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel).visible = false }
    }

}

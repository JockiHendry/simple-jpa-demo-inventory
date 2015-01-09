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

import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.Produk
import domain.inventory.Transfer
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import laporan.PengambilanBarang
import laporan.PengambilanBarangSummary
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

    def tampilkanLaporan = {
        model.result = []
        if (model.cetakSummary?.booleanValue()) {
            model.params.fileLaporan = 'report/laporan_summary_pengambilan_barang.jasper'
            produkRepository.withTransaction {
                for (Produk produk: findAllProduk()) {
                    for (ItemStok itemStok : produk.semuaItemStok(model.tanggalMulaiCari, model.tanggalSelesaiCari)) {
                        if (itemStok.jumlah < 0) {
                            PengambilanBarangSummary p = new PengambilanBarangSummary(tanggal: itemStok.tanggal, produk: produk)
                            Long jumlah = -1 * itemStok.jumlah
                            if (itemStok.referensiStok.classFinance == FakturJualOlehSales.simpleName) {
                                p.qtyJualSales = jumlah
                            } else if (itemStok.referensiStok.classFinance == FakturJualEceran.simpleName) {
                                p.qtyJualEceran = jumlah
                            } else if (itemStok.referensiStok.classGudang == PenyesuaianStok.simpleName) {
                                p.qtyPenyesuaian = jumlah
                            } else if (itemStok.referensiStok.classGudang == Transfer.simpleName) {
                                p.qtyTransfer = jumlah
                            } else {
                                p.qtyRetur = jumlah
                            }
                            model.result << p
                        }
                    }
                }
            }
        } else {
            model.params.fileLaporan = 'report/laporan_pengambilan_barang.jasper'
            produkRepository.withTransaction {
                for (Produk produk : findAllProduk()) {
                    for (ItemStok itemStok : produk.semuaItemStok(model.tanggalMulaiCari, model.tanggalSelesaiCari)) {
                        if (itemStok.jumlah < 0) {
                            model.result << new PengambilanBarang(itemStok.tanggal, produk, -1 * itemStok.jumlah, itemStok.keterangan, itemStok.referensiStok)
                        }
                    }
                }
            }
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

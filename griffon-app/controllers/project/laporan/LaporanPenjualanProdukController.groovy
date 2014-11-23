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
package project.laporan

import domain.inventory.Gudang
import laporan.PenjualanProduk
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.penjualan.FakturJualRepository
import simplejpa.swing.DialogUtils
import javax.swing.SwingUtilities
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanPenjualanProdukController {

    LaporanPenjualanProdukModel model
    def view
    FakturJualRepository fakturJualRepository
    GudangRepository gudangRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        String kriteriaNama = ''
        if (model.produkSearch) {
            kriteriaNama = " AND i.produk.nama LIKE '%${model.produkSearch}%' "
        }

        List result = fakturJualRepository.executeQuery("""
            SELECT new laporan.PenjualanProduk(i.produk, f.konsumen.sales.gudang, SUM(i.jumlah))
            FROM FakturJualOlehSales f LEFT JOIN f.listItemFaktur i
            WHERE f.deleted != 'Y' AND f.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai $kriteriaNama
            GROUP BY i.produk, f.konsumen.sales.gudang
            ORDER BY i.produk
        """, [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])

        List penjualanEceran = fakturJualRepository.executeQuery("""
            SELECT new laporan.PenjualanProduk(i.produk, SUM(i.jumlah))
            FROM FakturJualEceran f LEFT JOIN f.listItemFaktur i
            WHERE f.deleted != 'Y' AND f.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai $kriteriaNama
            GROUP BY i.produk
        """, [:], [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari])

        // Gabungkan penjualan eceran sebagai gudang utama pada hasil
        Gudang gudangUtama = gudangRepository.cariGudangUtama()
        for (PenjualanProduk penjualanProduk: penjualanEceran) {
            penjualanProduk.gudang = gudangUtama
            PenjualanProduk hasilCari = result.find {
                PenjualanProduk p -> p.produk == penjualanProduk.produk && p.gudang == penjualanProduk.gudang
            }
            if (hasilCari) {
                hasilCari.jumlahPenjualan += penjualanProduk.jumlahPenjualan
            } else {
                result.add(penjualanProduk)
            }
        }

        model.result = result
        model.params.'tanggalMulaiCari' = model.tanggalMulaiCari
        model.params.'tanggalSelesaiCari' = model.tanggalSelesaiCari
        close()
    }

    def cariProduk = {
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: false]
            def dialogProps = [title: 'Cari Produk...', preferredSize: new Dimension(900, 600)]
            DialogUtils.showMVCGroup('produk', args, view, dialogProps) { m, v, c ->
                if (!v.table.selectionModel.isSelectionEmpty()) {
                    model.produkSearch = v.view.table.selectionModel.selected[0].nama
                }
            }
        }
    }

    def reset = {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        model.produkSearch = null
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

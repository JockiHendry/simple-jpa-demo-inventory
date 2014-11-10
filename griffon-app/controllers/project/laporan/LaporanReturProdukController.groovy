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

import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.retur.ItemRetur
import domain.retur.Kemasan
import domain.retur.ReturBeli
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import laporan.ReturProduk
import org.joda.time.LocalDate
import project.inventory.ProdukRepository
import simplejpa.swing.DialogUtils

import javax.swing.SwingUtilities
import java.awt.Dimension

class LaporanReturProdukController {

    LaporanReturProdukModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        String kriteriaNama = ''
        Map params = [tanggalMulai: model.tanggalMulaiCari, tanggalSelesai: model.tanggalSelesaiCari]
        if (model.produkSearch) {
            kriteriaNama = " AND i.produk = :produkSearch "
            params.produkSearch = model.produkSearch
        }

        Map<Produk, ReturProduk> returProduk = [:]

        produkRepository.executeQuery("""
            SELECT r FROM ReturJualOlehSales r LEFT JOIN r.items i
            WHERE r.deleted != 'Y' AND r.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai $kriteriaNama
        """, [:], params).each { ReturJualOlehSales r ->
            r.items.each { ItemRetur i ->
                if (model.produkSearch && (i.produk != model.produkSearch)) return
                ReturProduk rp = returProduk[i.produk]
                if (rp) {
                    rp.jumlahReturJualSales += i.jumlah
                } else {
                    returProduk[i.produk] = new ReturProduk(i.produk, i.jumlah, 0, 0)
                }
            }
        }

        produkRepository.executeQuery("""
            SELECT r FROM ReturJualEceran r LEFT JOIN r.items i
            WHERE r.deleted != 'Y' AND r.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai $kriteriaNama
        """, [:], params).each { ReturJualEceran r ->
            r.items.each { ItemRetur i ->
                if (model.produkSearch && (i.produk != model.produkSearch)) return
                ReturProduk rp = returProduk[i.produk]
                if (rp) {
                    rp.jumlahReturJualEceran += i.jumlah
                } else {
                    returProduk[i.produk] = new ReturProduk(i.produk, 0, i.jumlah, 0)
                }
            }
        }

        produkRepository.executeQuery("""
            SELECT r FROM ReturBeli r LEFT JOIN r.items k LEFT JOIN k.items i
            WHERE r.deleted != 'Y' $kriteriaNama AND r.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai
        """, [:], params).each { ReturBeli r ->
            r.items.each { Kemasan k ->
                k.items.each { ItemBarang i ->
                    if (model.produkSearch && (i.produk != model.produkSearch)) return
                    ReturProduk rp = returProduk[i.produk]
                    if (rp) {
                        rp.jumlahReturBeli += i.jumlah
                    } else {
                        returProduk[i.produk] = new ReturProduk(i.produk, 0, 0, i.jumlah)
                    }
                }
            }
        }

        model.result = returProduk.values().sort { r1, r2 -> r1.produk.nama.compareTo(r2.produk.nama) }
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
                    model.produkSearch = v.view.table.selectionModel.selected[0]
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

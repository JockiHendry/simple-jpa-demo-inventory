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

import domain.labarugi.CacheGlobal
import domain.penjualan.FakturJualOlehSales
import laporan.ProfitSales
import project.labarugi.LabaRugiService
import project.penjualan.FakturJualRepository
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.SwingUtilities
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanPenjualanPerSalesController {

    LaporanPenjualanPerSalesModel model
    def view
    FakturJualRepository fakturJualRepository
    LabaRugiService labaRugiService

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        execInsideUISync {
            model.salesList.clear()
        }
        List sales = fakturJualRepository.findAllSales([orderBy: 'nama'])
        execInsideUISync {
            model.salesList.addAll(sales)
        }
    }

    def tampilkanLaporan = {
        model.result = fakturJualRepository.findAllFakturJualOlehSalesByDslFetchPengeluaranBarang([orderBy: 'konsumen__sales__nama,tanggal,nomor']) {
            tanggal between(model.tanggalMulaiCari, model.tanggalSelesaiCari)
            if (model.sales.selectedItem) {
                and()
                konsumen__sales eq(model.sales.selectedItem)
            }
            if (model.konsumenSearch) {
                and()
                konsumen eq(model.konsumenSearch)
            }
        }
        if (model.profitSales) {
            model.params.fileLaporan = "report/laporan_profit_sales.jasper"
            fakturJualRepository.withTransaction {
                CacheGlobal cacheGlobal = new CacheGlobal()
                cacheGlobal.perbaharui(model.tanggalMulaiCari, model.tanggalSelesaiCari)
                Map daftarNilaiInventory = [:]
                model.result = model.result.collect { FakturJualOlehSales f ->
                    BigDecimal hargaModal = 0, ongkosKirim = 0
                    f.barangYangHarusDikirim().items.each {
                        if (!daftarNilaiInventory.containsKey(it.produk)) {
                            daftarNilaiInventory[it.produk] = labaRugiService.hitungInventory(it.produk, cacheGlobal)
                        }
                        hargaModal +=  daftarNilaiInventory[it.produk].kurang(it.jumlah)
                        ongkosKirim += ((it.produk.ongkosKirimBeli?:0) * it.jumlah)
                    }
                    new ProfitSales(f.tanggal, f.konsumen, f.nomor, f.totalSetelahRetur(), hargaModal, ongkosKirim)
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
        model.sales.selectedItem = null
        model.konsumenSearch = null
    }

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, view, dialogProps) { m, v, c ->
                if (!v.table.selectionModel.isSelectionEmpty()) {
                    model.konsumenSearch = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel).visible = false }
    }

}

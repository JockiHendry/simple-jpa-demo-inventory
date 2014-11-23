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

import domain.penjualan.Konsumen
import domain.penjualan.Region
import domain.penjualan.Sales
import org.joda.time.LocalDate
import project.penjualan.KonsumenRepository
import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanSisaPiutangController {

    LaporanSisaPiutangModel model
    def view
    KonsumenRepository konsumenRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        List<Konsumen> daftarKonsumen = konsumenRepository.findAllKonsumen([orderBy: 'nama'])
        List<Sales> daftarSales = konsumenRepository.findAllSales([orderBy: 'nama'])
        List<Region> daftarRegion = konsumenRepository.findAllRegion([orderBy: 'nama'])
        execInsideUISync {
            model.konsumenSearch.values = daftarKonsumen
            model.salesList.clear()
            model.salesList.addAll(daftarSales)
            model.regionList.clear()
            model.regionList.addAll(daftarRegion)
        }
    }

    def tampilkanLaporan = {
        String jpql = '''
    SELECT DISTINCT k FROM Konsumen k JOIN FETCH k.listFakturBelumLunas f JOIN FETCH f.piutang
    WHERE k.listFakturBelumLunas IS NOT EMPTY
        AND f.tanggal BETWEEN :tanggalMulaiSearch AND :tanggalSelesaiSearch
        AND f.deleted != 'Y'
'''
        if (model.konsumenSearch.selectedValues?.size() > 0) {
            String ids = model.konsumenSearch.selectedValues.collect { it.id }.join(',')
            jpql += " AND k.id IN ($ids)"
        }

        if (model.sales.selectedItem) {
            jpql += " AND k.sales = :salesSearch"
        }

        if (model.region.selectedItem) {
            jpql += " AND (k.region = :regionSearch OR k.region.bagianDari = :regionSearch)"
        }

        jpql += " ORDER BY k.nama "

        Map params = [tanggalMulaiSearch: model.tanggalMulaiCari, tanggalSelesaiSearch: model.tanggalSelesaiCari]
        if (model.sales.selectedItem) {
            params.salesSearch = model.sales.selectedItem
        }
        if (model.region.selectedItem) {
            params.regionSearch = model.region.selectedItem
        }
        model.result = konsumenRepository.executeQuery(jpql, [:], params)

        if (model.cetakFormulir?.booleanValue()) {
            model.params.fileLaporan = 'report/formulir_sisa_piutang.jasper'
        }

        model.params.tanggalMulaiCari = model.tanggalMulaiCari
        model.params.tanggalSelesaiCari = model.tanggalSelesaiCari

        close()
    }

    def reset = {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        model.sales.selectedItem = null
        model.region.selectedItem = null
        model.konsumenSearch.clearSelectedValues()
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

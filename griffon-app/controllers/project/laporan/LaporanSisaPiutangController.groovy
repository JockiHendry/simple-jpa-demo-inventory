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

import org.joda.time.LocalDate
import project.penjualan.KonsumenRepository
import javax.swing.SwingUtilities

class LaporanSisaPiutangController {

    LaporanSisaPiutangModel model
    def view
    KonsumenRepository konsumenRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        String jpql = '''
    SELECT DISTINCT k FROM Konsumen k JOIN FETCH k.listFakturBelumLunas f JOIN FETCH f.piutang
    WHERE k.listFakturBelumLunas IS NOT EMPTY
        AND f.tanggal BETWEEN :tanggalMulaiSearch AND :tanggalSelesaiSearch
        AND f.deleted != 'Y'
    ORDER BY k.nama
'''
        if (model.konsumenSearch) {
            jpql += " AND k.nama LIKE '%${model.konsumenSearch}%'"
        }
        if (model.salesSearch) {
            jpql += " AND k.sales.nama LIKE '%${model.salesSearch}%'"
        }
        if (model.regionSearch) {
            jpql += " AND (k.region.nama LIKE '%${model.regionSearch}%' OR k.region.bagianDari.nama LIKE '%${model.regionSearch}%')"
        }

        model.result = konsumenRepository.executeQuery(jpql, [:], [tanggalMulaiSearch: model.tanggalMulaiCari, tanggalSelesaiSearch: model.tanggalSelesaiCari])

        if (model.cetakFormulir?.booleanValue()) {
            model.params.fileLaporan = 'report/formulir_sisa_piutang.jasper'
        }

        model.params.tanggalMulaiCari = model.tanggalMulaiCari
        model.params.tanggalSelesaiCari = model.tanggalSelesaiCari

        //
        // TODO: Fix this! Hibernate complain with the following query, it has something to do with named graph.
        //
//        model.result = konsumenRepository.findAllKonsumenByDslFetchFakturBelumLunas {
//            listFakturBelumLunas isNotEmpty()
//            if (model.konsumenSearch) {
//                and()
//                nama like("%${model.konsumenSearch}%")
//            }
//            if (model.salesSearch) {
//                and()
//                sales__nama like("%${model.salesSearch}%")
//            }
//        }
        close()
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

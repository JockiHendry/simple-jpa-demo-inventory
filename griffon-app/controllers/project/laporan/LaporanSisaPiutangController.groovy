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

import project.penjualan.KonsumenRepository
import javax.swing.SwingUtilities

class LaporanSisaPiutangController {

    LaporanSisaPiutangModel model
    def view
    KonsumenRepository konsumenRepository

    def tampilkanLaporan = {
        String jpql = 'SELECT DISTINCT k FROM Konsumen k JOIN FETCH k.listFakturBelumLunas f JOIN FETCH f.piutang WHERE k.listFakturBelumLunas IS NOT EMPTY '
        if (model.konsumenSearch) {
            jpql += " AND k.nama LIKE '%${model.konsumenSearch}%'"
        }
        if (model.salesSearch) {
            jpql += " AND k.sales.nama LIKE '%${model.salesSearch}%'"
        }
        model.result = konsumenRepository.executeQuery(jpql)

        if (model.cetakFormulir?.booleanValue()) {
            model.params.fileLaporan = 'report/formulir_sisa_piutang.jasper'
        }

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

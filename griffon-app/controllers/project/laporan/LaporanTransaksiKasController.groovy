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

import domain.inventory.Periode
import domain.labarugi.KategoriKas
import domain.labarugi.SaldoKas
import laporan.SaldoKategoriKas
import org.joda.time.LocalDate
import project.labarugi.KategoriKasRepository
import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanTransaksiKasController {

    LaporanTransaksiKasModel model
    def view
    KategoriKasRepository kategoriKasRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        Periode periodeCari = new Periode(model.tanggalMulaiCari, model.tanggalSelesaiCari)
        def result = []
        kategoriKasRepository.findAllKategoriKas().each { KategoriKas k ->
            if (k.dipakaiDiLaporan) {
                for (SaldoKas saldoKas : k.listSaldoKas) {
                    if (saldoKas.periode.overlaps(periodeCari)) {
                        result << new SaldoKategoriKas(k.nama, k.jenis, saldoKas.jenis, saldoKas.saldo)
                    }
                }
            }
        }

        model.result = result
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
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

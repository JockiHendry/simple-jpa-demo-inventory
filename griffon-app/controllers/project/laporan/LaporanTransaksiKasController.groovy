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
import domain.labarugi.JumlahPeriodeKas
import domain.labarugi.Kas
import domain.labarugi.PeriodeKas
import laporan.SaldoKategoriKas
import org.joda.time.LocalDate
import project.labarugi.KasRepository
import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanTransaksiKasController {

    LaporanTransaksiKasModel model
    def view
    KasRepository kasRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
    }

    def tampilkanLaporan = {
        model.tanggalMulaiCari = model.tanggalMulaiCari.dayOfMonth().withMinimumValue()
        model.tanggalSelesaiCari = model.tanggalSelesaiCari.dayOfMonth().withMaximumValue()
        Periode periodeCari = new Periode(model.tanggalMulaiCari, model.tanggalSelesaiCari)
        def result = []
        kasRepository.findAllKas().each { Kas kas ->
            for (PeriodeKas p: kas.listPeriodeRiwayat) {
                if (p.termasuk(periodeCari)) {
                    for (JumlahPeriodeKas j: p.jumlahPeriodik) {
                        result << new SaldoKategoriKas(kas.nama, j.kategoriKas.nama, j.kategoriKas.jenis, j.jenisTransaksiKas, j.saldo)
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
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel).visible = false }
    }

}

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

import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Sales
import laporan.PenjualanSales
import org.joda.time.LocalDate
import project.penjualan.FakturJualRepository
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

class LaporanPenjualanTriwulanController {

    LaporanPenjualanTriwulanModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
    }

    def tampilkanLaporan = {

        LocalDate tanggalMulai = model.tanggalMulaiCari.withDayOfMonth(1).minusMonths(1).minusDays(1)
        LocalDate tanggalSelesai = model.tanggalMulaiCari.plusMonths(1).minusDays(1)
        LocalDate bulan1 = tanggalMulai
        LocalDate bulan2 = tanggalMulai.plusMonths(1)
        LocalDate bulan3 = tanggalMulai.plusMonths(2)

        List fakturs = fakturJualRepository.findAllFakturJualOlehSalesByDslFetchItems([orderBy: 'tanggal']) {
            tanggal between(tanggalMulai, tanggalSelesai)
        }

        Map tmp = [:]
        fakturs.groupBy { FakturJualOlehSales f -> f.konsumen.sales }.each { Sales s, List<FakturJualOlehSales> daftarFaktur ->
            if (!tmp.containsKey(s)) {
                tmp[s] = new PenjualanSales(s.nama)
            }
            PenjualanSales p = tmp[s]
            for (FakturJualOlehSales f: daftarFaktur) {
                switch (f.tanggal.getMonthOfYear()) {
                    case bulan1.getMonthOfYear():
                        p.bulan1 = (p.bulan1?:0) + (f.totalSetelahRetur()?:0)
                        break
                    case bulan2.getMonthOfYear():
                        p.bulan2 = (p.bulan2?:0) + (f.totalSetelahRetur()?:0)
                        break
                    case bulan3.getMonthOfYear():
                        p.bulan3 = (p.bulan3?:0) + (f.totalSetelahRetur()?:0)
                        break
                }
            }
        }

        model.result = tmp.values().toList().sort { it.namaSales }
        model.params.'tanggalMulaiCari' = tanggalMulai
        model.params.'tanggalSelesaiCari' = tanggalSelesai
        model.params.bulan1 = bulan1.toString('MMMM YYYY')
        model.params.bulan2 = bulan2.toString('MMMM YYYY')
        model.params.bulan3 = bulan3.toString('MMMM YYYY')
        model.params.'icon_up' = ImageIO.read(getResourceAsStream('report/icon_up.png'))
        model.params.'icon_down' = ImageIO.read(getResourceAsStream('report/icon_down.png'))
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

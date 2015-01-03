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
import domain.inventory.Produk
import laporan.PengambilanBarang
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
        produkRepository.withTransaction {
            for (Produk produk : findAllProduk()) {
                for (ItemStok itemStok : produk.semuaItemStok(model.tanggalMulaiCari, model.tanggalSelesaiCari)) {
                    if (itemStok.jumlah < 0) {
                        model.result << new PengambilanBarang(itemStok.tanggal, produk, -1 * itemStok.jumlah, itemStok.keterangan, itemStok.referensiStok)
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

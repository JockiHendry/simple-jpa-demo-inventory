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
import project.inventory.ProdukRepository
import javax.swing.SwingUtilities

class LaporanStokGudangController {

    LaporanStokGudangModel model
    def view
    ProdukRepository produkRepository

    def tampilkanLaporan = {
        model.result = produkRepository.findAllStokProdukByDsl([orderBy: 'gudang__nama,produk__nama']) {
            if (model.produkSearch) {
                produk__nama like("%${model.produkSearch}%")
            }
            if (model.gudangSearch) {
                and()
                gudang__nama like("%${model.gudangSearch}%")
            }
        }
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

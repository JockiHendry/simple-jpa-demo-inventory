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

import domain.inventory.Gudang
import project.inventory.ProdukRepository

import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanSuratJalanController {

    LaporanSuratJalanModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        init()
    }

    def init = {
        execInsideUISync {
            model.gudangList.clear()
        }
        List gudang = produkRepository.findAllGudangByUtama(false)
        execInsideUISync {
            model.gudangList.addAll(gudang)
        }
    }

    def tampilkanLaporan = {
        if (model.gudang.selectedItem == null) {
            model.errors['gudang'] = 'Gudang harus dipilih!'
            return
        }
        model.result = produkRepository.findAllStokProdukByDsl([orderBy: 'produk__nama']) {
            gudang eq(model.gudang.selectedItem)
            and()
            jumlah gt(0)
        }
        Gudang gudang = model.gudang.selectedItem
        model.params.platNo = "${gudang.nama} - ${gudang.keterangan}"
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

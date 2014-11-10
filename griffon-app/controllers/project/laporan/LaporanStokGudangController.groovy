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
import project.inventory.ProdukRepository
import simplejpa.swing.DialogUtils

import javax.swing.SwingUtilities
import java.awt.Dimension

class LaporanStokGudangController {

    LaporanStokGudangModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        List gudang = produkRepository.findAllGudang([orderBy: 'nama'])
        execInsideUISync {
            model.gudangList.clear()
            model.gudangList.addAll(gudang)
        }
    }

    def tampilkanLaporan = {
        model.result = produkRepository.findAllStokProdukByDsl([orderBy: 'gudang__nama,produk__nama']) {
            if (model.produkSearch) {
                produk eq(model.produkSearch)
            }
            if (model.gudang.selectedItem) {
                and()
                gudang eq(model.gudang.selectedItem)
            }
        }
        close()
    }

    def cariProduk = {
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: false]
            def dialogProps = [title: 'Cari Produk...', preferredSize: new Dimension(900, 600)]
            DialogUtils.showMVCGroup('produk', args, view, dialogProps) { m, v, c ->
                if (!v.table.selectionModel.isSelectionEmpty()) {
                    model.produkSearch = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def reset = {
        model.produkSearch = null
        model.gudang.selectedItem = null
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

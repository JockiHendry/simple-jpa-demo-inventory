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

import domain.inventory.Produk
import domain.labarugi.CacheGlobal
import org.joda.time.LocalDate
import project.inventory.ProdukRepository
import project.labarugi.LabaRugiService
import simplejpa.swing.DialogUtils
import javax.swing.SwingUtilities
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanDaftarInventoryController {

    LaporanDaftarInventoryModel model
    def view
    LabaRugiService labaRugiService
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.tanggalSearch = LocalDate.now()
    }

    def tampilkanLaporan = {
        labaRugiService.withTransaction {
            List daftarProduk = labaRugiService.findAllProdukByDsl() {
                jumlah gt(0)
                if (model.produkSearch) {
                    and()
                    nama like("%${model.produkSearch}%")
                }
            }
            model.result = []
            CacheGlobal cacheGlobal = new CacheGlobal()
            cacheGlobal.perbaharui(model.tanggalSearch, null)
            for (Produk produk : daftarProduk) {
                model.result << labaRugiService.hitungInventory(produk, cacheGlobal)
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
                    model.produkSearch = v.view.table.selectionModel.selected[0].nama
                }
            }
        }
    }

    def reset = {
        model.tanggalSearch = LocalDate.now()
        model.produkSearch = null
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel).visible = false }
    }


}

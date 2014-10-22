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

import domain.inventory.ItemStok
import domain.inventory.Periode
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import org.joda.time.LocalDate
import project.inventory.ProdukRepository
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.validation.groups.Default
import java.awt.Dimension

class KartuStokController {

    KartuStokModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        execInsideUISync {
            model.gudangList.clear()
        }
        List gudang = produkRepository.findAllGudang()
        execInsideUISync {
            model.gudangList.addAll(gudang)
        }
    }

    def showProduk = {
        execInsideUISync {
            def args = [popup: true, allowTambahProduk: false, showReturOnly: false]
            def dialogProps = [title: 'Cari Produk', preferredSize: new Dimension(900, 600)]
            Produk produk = null
            DialogUtils.showMVCGroup('produk', args, app, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada produk yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
                    return
                } else {
                    produk = v.view.table.selectionModel.selected[0]
                }
                model.produkSearch = produk
            }
        }
    }


    def tampilkanLaporan = {
        if (!produkRepository.validate(model, Default, model)) return
        if (!model.gudangSearch.selectedItem) {
            model.errors['gudangSearch'] = 'Gudang harus dipilih'
            return
        }

        produkRepository.withTransaction {
            Produk produk = findProdukById(model.produkSearch.id)
            Periode periode = new Periode(model.tanggalMulaiCari, model.tanggalSelesaiCari)
            List<PeriodeItemStok> periodeItemStok = produk.stok(model.gudangSearch.selectedItem).periode(periode)
            model.result = []
            for (PeriodeItemStok p : periodeItemStok) {
                for (ItemStok itemStok : p.listItem) {
                    if (itemStok.tanggal.isBefore(model.tanggalMulaiCari)) continue
                    if (itemStok.tanggal.isAfter(model.tanggalSelesaiCari)) continue
                    model.result << itemStok
                }
            }
        }

        model.params.tanggalMulaiCari = model.tanggalMulaiCari
        model.params.tanggalSelesaiCari = model.tanggalSelesaiCari
        model.params.namaProduk = model.produkSearch.nama

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

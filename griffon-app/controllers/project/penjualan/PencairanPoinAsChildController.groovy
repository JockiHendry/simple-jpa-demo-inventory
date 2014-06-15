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
package project.penjualan

import domain.Container
import domain.exception.PencairanPoinTidakValid
import domain.penjualan.KonsumenRepository
import domain.penjualan.PencairanPoin
import domain.penjualan.PencairanPoinTukarUang
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PencairanPoinAsChildController {

    PencairanPoinAsChildModel model
    def view

    KonsumenRepository repo

    void mvcGroupInit(Map args) {
        repo = Container.app.konsumenRepository
        model.konsumen = repo.findKonsumenByIdFetchPencairanPoin(args.'konsumen'.id)
        init()
    }

    def init = {
        execInsideUISync {
            model.pencairanPoinList.clear()
            model.pencairanPoinList.addAll(model.konsumen.listPencairanPoin)
        }
    }

    def save = {
        if (!repo.validate(model, Default, model)) return

        try {
            PencairanPoin pencairanPoin
            def jenisPencarian = model.jenisPencairanPoin.selectedItem

            if (jenisPencarian == JenisPencairanPoin.TUKAR_UANG) {

                // Proses tukar uang
                pencairanPoin = repo.cairkanPoinTukarUang(model.konsumen, model.tanggal, model.jumlahPoin)

            } else if (jenisPencarian == JenisPencairanPoin.TUKAR_BARANG) {

                // Proses tukar barang
                if (model.items.isEmpty()) {
                    model.errors['listItemBarang'] = ' Harus di-isi.'
                    return
                }
                pencairanPoin = repo.cairkanPoinTukarBarang(model.konsumen, model.tanggal, model.jumlahPoin, model.items)

            } else if (jenisPencarian == JenisPencairanPoin.POTONG_PIUTANG) {

                // Proses potong piutang
                pencairanPoin = repo.cairkanPoinPotongPiutang(model.konsumen, model.tanggal, model.jumlahPoin)

            } else {

                // Tidak didukung
                model.errors['jenisPencairanPoin'] = 'Tidak didukung untuk saat ini!'
                return
            }

            execInsideUISync {
                model.pencairanPoinList << pencairanPoin
                view.table.changeSelection(model.pencairanPoinList.size() - 1, 0, false, false)
                clear()
            }

        } catch (Exception ex) {
            model.errors['jumlahPoin'] = ex.message
        }
    }

    def showDaftarBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.items, editable: true, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang Yang Ditukar', size: new Dimension(900,420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def onPerubahanJenisLaporan = {
        if (model.jenisPencairanPoin.selectedItem == JenisPencairanPoin.TUKAR_BARANG) {
            model.daftarBarangVisible = true
        } else {
            model.daftarBarangVisible = false
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.tanggal = null
            model.jumlahPoin = null
            model.keterangan = null
            model.rate = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PencairanPoin selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.tanggal = selected.tanggal
                model.jumlahPoin = selected.jumlahPoin
                model.keterangan = selected.keterangan
                model.rate = selected.rate
            }
        }
    }

}

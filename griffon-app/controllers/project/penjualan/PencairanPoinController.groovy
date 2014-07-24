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

import ast.NeedSupervisorPassword
import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.penjualan.KonsumenRepository
import domain.penjualan.PencairanPoin
import domain.penjualan.PencairanPoinPotongPiutang
import domain.penjualan.PencairanPoinRepository
import domain.penjualan.PencairanPoinTukarBarang
import domain.penjualan.PencairanPoinTukarUang
import domain.validation.InputPencairanPoin
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension
import java.text.DateFormat

class PencairanPoinController {

    PencairanPoinModel model
    def view

    PencairanPoinRepository repo

    void mvcGroupInit(Map args) {
        repo = Container.app.pencairanPoinRepository
        init()
        search()
    }

    def init = {
        Container.app.nomorService.refreshAll()
        execInsideUISync {
            model.konsumenList.clear()
        }
        List konsumen = repo.findAllKonsumen()
        execInsideUISync {
            model.konsumenList.addAll(konsumen)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
        }
    }

    def search = {
        List result = repo.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.konsumenSearch)
        execInsideUISync {
            model.pencairanPoinList.clear()
            model.pencairanPoinList.addAll(result)
        }
    }

    def save = {
        def jenisPencarian = model.jenisPencairanPoin.selectedItem
        PencairanPoin pencairanPoin

        if (jenisPencarian == JenisPencairanPoin.TUKAR_UANG) {
            pencairanPoin = new PencairanPoinTukarUang(tanggal: model.tanggal, konsumen: model.konsumen.selectedItem,
                jumlahPoin: model.jumlahPoin)
        } else if (jenisPencarian == JenisPencairanPoin.TUKAR_BARANG) {
            pencairanPoin = new PencairanPoinTukarBarang(tanggal: model.tanggal, konsumen: model.konsumen.selectedItem,
                jumlahPoin: 1)
            pencairanPoin.listItemBarang.addAll(model.items)
        } else if (jenisPencarian == JenisPencairanPoin.POTONG_PIUTANG) {
            pencairanPoin = new PencairanPoinPotongPiutang(tanggal: model.tanggal, konsumen: model.konsumen.selectedItem,
                jumlahPoin: model.jumlahPoin)
        } else {
            model.errors['jenisPencairanPoin'] = 'Tidak didukung untuk saat ini!'
            return
        }

        if (!repo.validate(pencairanPoin, InputPencairanPoin, model)) {
            JOptionPane.showMessageDialog(null, "Errosr = " + model.errors)
            return
        }

        try {
            pencairanPoin = repo.buat(pencairanPoin)
            execInsideUISync {
                model.pencairanPoinList << pencairanPoin
                view.table.changeSelection(model.pencairanPoinList.size() - 1, 0, false, false)
                clear()
            }

        } catch (Exception ex) {
            model.errors['jumlahPoin'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PencairanPoin pencairanPoin = view.table.selectionModel.selected[0]
            pencairanPoin = repo.hapus(pencairanPoin)

            execInsideUISync {
                view.table.selectionModel.selected[0] = pencairanPoin
                clear()
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.mainPanel, "Pencairan poin tidak boleh dihapus: ${ex.message}", 'Penghapusan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def cetak = {
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: model.jenisPencairanPoin.selectedItem.fileLaporan]
            def dialogProps = [title: 'Preview Bukti Pencairan Poin', preferredSize: new Dimension(920, 400)]
            DialogUtils.showMVCGroup('previewEscp', args, app, view, dialogProps)
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
            model.nomor = null
            model.tanggal = null
            model.jumlahPoin = null
            model.keterangan = null
            model.rate = null
            model.konsumen.selectedItem = null
            model.items.clear()
            model.createdBy = null
            model.modifiedBy = null

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
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.jumlahPoin = selected.jumlahPoin
                model.keterangan = selected.keterangan
                model.rate = selected.rate
                model.konsumen.selectedItem = selected.konsumen
                model.items.clear()
                if (selected instanceof PencairanPoinTukarUang) {
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.TUKAR_UANG
                } else if (selected instanceof PencairanPoinTukarBarang) {
                    model.items.addAll(selected.listItemBarang)
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.TUKAR_BARANG
                } else if (selected instanceof PencairanPoinPotongPiutang) {
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.POTONG_PIUTANG
                }
                model.createdBy = (selected.createdBy && selected.createdDate)?
                    "${selected.createdBy} (${DateFormat.getDateTimeInstance().format(selected.createdDate)})": "-"
                model.modifiedBy = (selected.modifiedBy && selected.modifiedDate)?
                    "${selected.modifiedBy} (${DateFormat.getDateTimeInstance().format(selected.modifiedDate)})": "-"
            }
        }
    }

}

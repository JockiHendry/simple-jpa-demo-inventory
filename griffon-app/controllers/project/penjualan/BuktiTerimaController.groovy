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
package project.penjualan

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.penjualan.BuktiTerima
import domain.penjualan.FakturJualOlehSales
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class BuktiTerimaController {

    BuktiTerimaModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        search()
    }

    def search = {
        List result = fakturJualRepository.cariFakturJualUntukBuktiTerima(model.nomorFakturSearch, model.nomorSuratJalanSearch, model.konsumenSearch)
        execInsideUISync {
            model.fakturJualOlehSalesList.clear()
            model.fakturJualOlehSalesList.addAll(result)
        }
    }

    def save = {
        BuktiTerima buktiTerima = new BuktiTerima(tanggalTerima: model.tanggal, namaPenerima: model.namaPenerima, namaSupir: model.namaSupir)
        if (!fakturJualRepository.validate(buktiTerima, Default, model)) return
        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin barang yang dikirim telah diterima untuk faktur ini?', 'Konfirmasi Pengiriman', JOptionPane.QUESTION_MESSAGE)) {
            return
        }
        try {
            FakturJualOlehSales faktur = view.table.selectionModel.selected[0]
            faktur = fakturJualRepository.proses(faktur, [buktiTerima: buktiTerima])
            execInsideUIAsync {
                model.fakturJualOlehSalesList.remove(faktur)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def hapus = {
        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin akan mengembalikan faktur ini menjadi belum diantar?', 'Konfirmasi Pembatalan Pengantaran', JOptionPane.QUESTION_MESSAGE)) {
            return
        }
        try {
            FakturJualOlehSales faktur = view.table.selectionModel.selected[0]
            fakturJualRepository.hapus(faktur)
            execInsideUISync {
                model.fakturJualOlehSalesList.remove(faktur)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBarangYangHarusDikirim = {
        execInsideUISync {
            def args = [editable: false, listItemBarang: view.table.selectionModel.selected[0].barangYangHarusDikirim().items, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang Yang Telah Dikirim', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.nomorFaktur = null
            model.nomorSuratJalan = null
            model.tanggal = null
            model.namaPenerima = null
            model.namaSupir = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                model.allowSimpan = false
                model.allowHapus = false
                clear()
            } else {
                FakturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.nomorFaktur = selected.nomor
                model.nomorSuratJalan = selected.pengeluaranBarang?.nomor
                model.allowSimpan = true
                model.allowHapus = true
            }
        }
    }

}

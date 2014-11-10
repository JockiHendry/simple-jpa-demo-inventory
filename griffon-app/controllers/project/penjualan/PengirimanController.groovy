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
import domain.exception.DataTidakBolehDiubah
import domain.inventory.DaftarBarangSementara
import domain.penjualan.FakturJualOlehSales
import project.user.NomorService
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PengirimanController {

    PengirimanModel model
    def view
    FakturJualRepository fakturJualRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.nomorSuratJalan = nomorService.getCalonNomor(NomorService.TIPE.PENGELUARAN_BARANG)
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = StatusFakturJual.DIBUAT
        }
    }

    def search = {
        List result = fakturJualRepository.cariFakturJualUntukPengiriman(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
                model.nomorSearch, model.salesSearch, model.konsumenSearch, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturJualOlehSalesList.clear()
            model.fakturJualOlehSalesList.addAll(result)
        }
    }

    def simpanSuratJalan = {
        if (!fakturJualRepository.validate(model, Default, model)) return
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin ingin menyimpan data surat untuk faktur ini?', 'Konfirmasi Pengiriman', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        try {
            FakturJualOlehSales faktur = view.table.selectionModel.selected[0]
            faktur = fakturJualRepository.buatSuratJalan(faktur, model.alamatTujuan, model.tanggal, model.keterangan)
            execInsideUISync {
                view.table.selectionModel.selected[0] = faktur
                cetak(faktur)
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, ex.message, 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def kirimSuratJalan = {
        if (!fakturJualRepository.validate(model, Default, model)) return
        FakturJualOlehSales faktur = view.table.selectionModel.selected[0]
        if (faktur.pengeluaranBarang == null) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Untuk memproses pengiriman, buat surat jalan terlebih dahulu!', 'Kesalahan', JOptionPane.ERROR_MESSAGE)
            return
        }
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin ingin order surat jalan telah diantar untuk faktur ini?', 'Konfirmasi Pengiriman', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        try {
            faktur = fakturJualRepository.kirimSuratJalan(faktur)
            execInsideUISync {
                model.fakturJualOlehSalesList.remove(faktur)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def batalKirim = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin akan membatalkan pengiriman barang untuk faktur ini?', 'Konfirmasi Pembatalan Pengiriman', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
        try {
            FakturJualOlehSales faktur = view.table.selectionModel.selected[0]
            fakturJualRepository.batalKirim(faktur)
            execInsideUISync {
                model.fakturJualOlehSalesList.remove(faktur)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBarangYangHarusDikirim = {
        execInsideUISync {
            def args = [editable: false, listItemBarang: view.table.selectionModel.selected[0].barangYangHarusDikirim().items, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang Yang Harus Dikirim', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps)
        }
    }

    def cetak = { e ->
        FakturJualOlehSales selected = e instanceof FakturJualOlehSales? e: view.table.selectionModel.selected[0]
        if (selected.pengeluaranBarang != null) {
            execInsideUISync {
                def args = [dataSource: selected, template: 'surat_jalan.json']
                def dialogProps = [title: 'Preview Surat Jalan', preferredSize: new Dimension(970, 700)]
                DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
            }
        } else {
            JOptionPane.showMessageDialog(view.mainPanel, 'Surat jalan belum dibuat sehingga tidak bisa dicetak!', 'Percetakan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def cetakSummary = {
        DaftarBarangSementara daftarBarang = fakturJualRepository.hitungBarangYangHarusDikirim()
        execInsideUISync {
            def args = [dataSource: daftarBarang, template: 'daftar_barang_kirim.json']
            def dialogProps = [title: 'Preview Daftar Barang Untuk Dikirim', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.nomorFakturJual = null
            model.nomorSuratJalan = nomorService.getCalonNomor(NomorService.TIPE.PENGELUARAN_BARANG)
            model.tanggal = null
            model.keterangan = null
            model.alamatTujuan = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                model.allowKirim = false
                model.allowBatalKirim = false
                clear()
            } else {
                FakturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.nomorFakturJual = selected.nomor
                if (selected.pengeluaranBarang) {
                    model.allowBuatSuratJalan = false
                    model.allowKirim = (selected.status == StatusFakturJual.DIBUAT) && (selected.deleted != 'Y')
                    model.allowBatalKirim = (selected.status == StatusFakturJual.DIANTAR)
                    model.nomorSuratJalan = selected.pengeluaranBarang.nomor
                    model.tanggal = selected.pengeluaranBarang.tanggal
                    model.alamatTujuan = selected.pengeluaranBarang.alamatTujuan
                    model.keterangan = selected.pengeluaranBarang.keterangan
                } else {
                    model.nomorSuratJalan = null
                    model.tanggal = null
                    model.alamatTujuan = null
                    model.keterangan = null
                    if (selected.status.pengeluaranBolehDiubah && selected.deleted != 'Y') {
                        model.allowBuatSuratJalan = true
                        model.allowKirim = false
                        model.allowBatalKirim = false
                        model.alamatTujuan = selected.konsumen.alamat
                    }
                }
                model.allowPrint = (selected.deleted != 'Y')
            }
        }
    }

}

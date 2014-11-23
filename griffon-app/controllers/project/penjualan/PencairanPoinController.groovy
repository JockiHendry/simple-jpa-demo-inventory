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
import domain.penjualan.PencairanPoin
import domain.penjualan.PencairanPoinPotongPiutang
import domain.penjualan.PencairanPoinTukarBarang
import domain.penjualan.PencairanPoinTukarUang
import domain.validation.InputPencairanPoin
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class PencairanPoinController {

    PencairanPoinModel model
    def view
    PencairanPoinRepository pencairanPoinRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            nomorService.refreshAll()
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
        }
    }

    def search = {
        List result = pencairanPoinRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.konsumenSearch)
        execInsideUISync {
            model.pencairanPoinList.clear()
            model.pencairanPoinList.addAll(result)
        }
    }

    def save = {
        def jenisPencarian = model.jenisPencairanPoin.selectedItem
        PencairanPoin pencairanPoin

        if (jenisPencarian == JenisPencairanPoin.TUKAR_UANG) {
            pencairanPoin = new PencairanPoinTukarUang(tanggal: model.tanggal, konsumen: model.konsumen, jumlahPoin: model.jumlahPoin, keterangan: model.keterangan)
        } else if (jenisPencarian == JenisPencairanPoin.TUKAR_BARANG) {
            pencairanPoin = new PencairanPoinTukarBarang(tanggal: model.tanggal, konsumen: model.konsumen, jumlahPoin: 1, keterangan: model.keterangan)
            pencairanPoin.listItemBarang.addAll(model.items)
        } else if (jenisPencarian == JenisPencairanPoin.POTONG_PIUTANG) {
            pencairanPoin = new PencairanPoinPotongPiutang(tanggal: model.tanggal, konsumen: model.konsumen, jumlahPoin: model.jumlahPoin, keterangan: model.keterangan, fakturJualOlehSales: model.fakturPotongPiutang)
        } else {
            model.errors['jenisPencairanPoin'] = 'Tidak didukung untuk saat ini!'
            return
        }

        if (!pencairanPoinRepository.validate(pencairanPoin, InputPencairanPoin, model)) {
            return
        }

        try {
            pencairanPoin = pencairanPoinRepository.buat(pencairanPoin)
            execInsideUISync {
                model.pencairanPoinList << pencairanPoin
                view.table.changeSelection(model.pencairanPoinList.size() - 1, 0, false, false)
                clear()
                cetak(pencairanPoin)
            }

        } catch (Exception ex) {
            model.errors['jumlahPoin'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PencairanPoin pencairanPoin = view.table.selectionModel.selected[0]
            pencairanPoin = pencairanPoinRepository.hapus(pencairanPoin)

            execInsideUISync {
                view.table.selectionModel.selected[0] = pencairanPoin
                clear()
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.mainPanel, "Pencairan poin tidak boleh dihapus: ${ex.message}", 'Penghapusan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: model.jenisPencairanPoin.selectedItem.fileLaporan]
            if (e instanceof PencairanPoin) args.dataSource = e
            def dialogProps = [title: 'Preview Bukti Pencairan Poin', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def showDaftarBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.items, editable: true, allowTambahProduk: false]
            def dialogProps = [title: 'Daftar Barang Yang Ditukar', size: new Dimension(900,420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
            }
        }
    }

    def showFakturPotongPiutang = {
        if (!model.konsumen) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus memiliki konsumen terlebih dahulu!', 'Kesalahan Urutan Pengisian Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [konsumen: model.konsumen]
            def dialogProps = [title: 'Faktur Belum Lunas', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('fakturJualOlehSalesAsChild', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada faktur yang dipilih!', 'Cari Produk', JOptionPane.ERROR_MESSAGE)
                    return
                } else {
                    model.fakturPotongPiutang = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada konsumen yang dipilih!', 'Cari Konsumen', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.konsumen = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def onPerubahanJenisLaporan = {
        model.daftarBarangVisible = (model.jenisPencairanPoin.selectedItem == JenisPencairanPoin.TUKAR_BARANG)
        model.potongPiutangVisible = (model.jenisPencairanPoin.selectedItem == JenisPencairanPoin.POTONG_PIUTANG)
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.jumlahPoin = null
            model.keterangan = null
            model.rate = null
            model.konsumen = null
            model.items.clear()
            model.fakturPotongPiutang = null
            model.created = null
            model.createdBy = null
            model.modified = null
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
                model.konsumen = selected.konsumen
                model.items.clear()
                if (selected instanceof PencairanPoinTukarUang) {
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.TUKAR_UANG
                } else if (selected instanceof PencairanPoinTukarBarang) {
                    model.items.addAll(selected.listItemBarang)
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.TUKAR_BARANG
                } else if (selected instanceof PencairanPoinPotongPiutang) {
                    model.jenisPencairanPoin.selectedItem = JenisPencairanPoin.POTONG_PIUTANG
                    model.fakturPotongPiutang = null
                }
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
            }
        }
    }

}

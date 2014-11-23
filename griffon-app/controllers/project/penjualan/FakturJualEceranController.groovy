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
import domain.exception.StokTidakCukup
import domain.faktur.Diskon
import domain.penjualan.*
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import util.SwingHelper
import java.awt.Dimension
import java.text.NumberFormat

@SuppressWarnings("GroovyUnusedDeclaration")
class FakturJualEceranController {

    FakturJualEceranModel model
    def view
    FakturJualRepository fakturJualRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        model.mode = args.containsKey('mode')? args.'mode': FakturEceranViewMode.ALL
        switch (model.mode) {
            case FakturEceranViewMode.FAKTUR:
                model.showPenerimaan = false
                model.showFakturJual = true
                model.showNilaiUang = true
                model.allowAddFakturJual = true
                model.statusSearch.selectedItem = SwingHelper.SEMUA
                break
            case FakturEceranViewMode.PENGELUARAN:
                model.showPenerimaan = true
                model.showFakturJual = false
                model.showNilaiUang = false
                model.allowAddFakturJual = false
                model.statusSearch.selectedItem = StatusFakturJual.DIBUAT
                break
            case FakturEceranViewMode.ALL:
                model.showPenerimaan = true
                model.showFakturJual = true
                model.showNilaiUang = true
                model.allowAddFakturJual = true
                model.statusSearch.selectedItem = SwingHelper.SEMUA
                break
        }

        init()
        search()
    }

    def init = {
        model.nomor = nomorService.getCalonNomorFakturJual()
        model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
        model.tanggalSelesaiSearch = LocalDate.now()
    }

    def search = {
        List result = fakturJualRepository.cariFakturJualEceran(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.namaPembeliSearch, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturJualEceranList.clear()
            model.fakturJualEceranList.addAll(result)
        }
    }

    def save = {
        FakturJualEceran fakturJualEceran = new FakturJualEceran(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            namaPembeli: model.namaPembeli, keterangan: model.keterangan, diskon: new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung))
        model.listItemFaktur.each { fakturJualEceran.tambah(it) }

        if (!fakturJualRepository.validate(fakturJualEceran, Default, model)) return

        try {
            if (fakturJualEceran.id == null) {
                fakturJualEceran = fakturJualRepository.buat(fakturJualEceran)
                execInsideUISync {
                    model.fakturJualEceranList << fakturJualEceran
                    view.table.changeSelection(model.fakturJualEceranList.size() - 1, 0, false, false)
                    clear()
                    cetak(fakturJualEceran)
                }
            } else {
                fakturJualEceran = fakturJualRepository.update(fakturJualEceran)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = fakturJualEceran
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (StokTidakCukup ex) {
            model.errors['listItemFaktur'] = ex.message
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            FakturJualEceran fakturJualEceran = view.table.selectionModel.selected[0]
            fakturJualEceran = fakturJualRepository.hapus(fakturJualEceran)

            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualEceran
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def antar = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin akan melakukan pengantaran barang untuk faktur ini?', 'Konfirmasi Pengantaran', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return
        }
        try {
            FakturJualEceran fakturJualEceran = view.table.selectionModel.selected[0]
            fakturJualEceran = fakturJualRepository.antar(fakturJualEceran)
            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualEceran
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def batalAntar = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin akan membatalkan pengantaran barang untuk faktur ini?', 'Konfirmasi Pengantaran', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return
        }
        try {
            FakturJualEceran fakturJualEceran = view.table.selectionModel.selected[0]
            fakturJualEceran = fakturJualRepository.batalAntar(fakturJualEceran)
            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualEceran
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def bayar = {
        FakturJualEceran fakturJualEceran = view.table.selectionModel.selected[0]
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin akan barang sudah diterima dan faktur sudah dibayar?', 'Konfirmasi Pembayaran', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return
        }
        try {
            fakturJualEceran = fakturJualRepository.bayar(fakturJualEceran)
            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualEceran
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum { it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it.total() }?: 0
        model.informasi = "Qty ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"
    }

    def showItemFaktur = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur,
                        allowTambahProduk: false, showHarga: model.showFakturJual]
            def dialogProps = [title: 'Detail Item', preferredSize: new Dimension(900, 620)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], template: 'faktur_jual_eceran.json']
            if (e instanceof FakturJual) args.dataSource = e
            def dialogProps = [title: 'Preview Faktur Jual', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomorFakturJual()
            model.tanggal = null
            model.namaPembeli = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.status = null
            model.listItemFaktur.clear()
            model.created = null
            model.createdBy = null
            model.modified = null
            model.modifiedBy = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
            refreshInformasi()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                FakturJualEceran selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.namaPembeli = selected.namaPembeli
                model.keterangan = selected.keterangan
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.status = selected.status
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(selected.listItemFaktur)
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                refreshInformasi()
                model.allowPrint = (selected.deleted != 'Y')
                model.allowAntar = (selected.deleted != 'Y')
            }
        }
    }

}
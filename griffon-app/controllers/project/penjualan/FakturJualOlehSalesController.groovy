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
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.Diskon
import domain.pengaturan.NamaTemplateFaktur
import domain.penjualan.*
import domain.validation.InputPenjualanOlehSales
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import domain.exception.DataDuplikat
import util.SwingHelper
import java.awt.Dimension
import java.text.NumberFormat

@SuppressWarnings("GroovyUnusedDeclaration")
class FakturJualOlehSalesController {

    FakturJualOlehSalesModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        if (args.containsKey('nomorSearch')) model.nomorSearch = args.nomorSearch
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = SwingHelper.SEMUA
        }
    }

    def search = {
        List result = fakturJualRepository.cariFakturJualOlehSales(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.salesSearch, model.konsumenSearch, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturJualOlehSalesList.clear()
            model.fakturJualOlehSalesList.addAll(result)
        }
    }

    def save = {
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(id: model.id, tanggal: model.tanggal,
            keterangan: model.keterangan, diskon: new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung),
            konsumen: model.konsumen, kirimDariGudangUtama: model.kirimDariGudangUtama)
        model.listItemFaktur.each { fakturJualOlehSales.tambah(it) }

        if (!fakturJualRepository.validate(fakturJualOlehSales, InputPenjualanOlehSales, model)) return

        try {
            if (fakturJualOlehSales.id == null) {
                try {
                    fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, false, !model.listBonus.empty? model.listBonus: [])
                } catch (MelebihiBatasKredit ex) {
                    DialogUtils.showMVCGroup('supervisorPassword', [pesan: ex.getHTMLMessage()], view, ['title': 'Password Supervisor', 'size': new Dimension(500, 150)]) { m, v, c ->
                        if (m.ok) {
                            fakturJualOlehSales.bonusPenjualan = null
                            fakturJualOlehSales = fakturJualRepository.buat(fakturJualOlehSales, true, (!model.listBonus.empty)? model.listBonus: [])
                        } else {
                            throw new MelebihiBatasKredit(fakturJualOlehSales.konsumen)
                        }
                    }
                }
                execInsideUISync {
                    model.fakturJualOlehSalesList << fakturJualOlehSales
                    view.table.changeSelection(model.fakturJualOlehSalesList.size() - 1, 0, false, false)
                    clear()
                    cetak(fakturJualOlehSales)
                }
            } else {
                fakturJualOlehSales = fakturJualRepository.update(fakturJualOlehSales)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = fakturJualOlehSales
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (StokTidakCukup ex) {
            model.errors['listItemFaktur'] = ex.message
        } catch (MelebihiBatasKredit ex) {
            model.errors['konsumen'] = ex.message
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            FakturJualOlehSales fakturJualOlehSales = view.table.selectionModel.selected[0]
            fakturJualOlehSales = fakturJualRepository.hapus(fakturJualOlehSales)

            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualOlehSales
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBonus = {
        execInsideUISync {
            def args = [editable: false, listItemBarang: model.listBonus, allowTambahProduk: false]
            if (view.table.selectionModel.selected[0] == null) {
                args.'editable' = true
            }

            def dialogProps = [title: 'Detail Bonus', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, view, dialogProps) { m, v, c ->
                model.listBonus.clear()
                model.listBonus.addAll(m.itemBarangList)
                refreshInformasi()
            }
        }
    }

    def showItemFaktur = {
        if (!model.konsumen) {
            DialogUtils.message(view.mainPanel, 'Anda harus memilih konsumen terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur,
                        konsumen: model.konsumen, allowTambahProduk: false, showHarga: model.showFakturJual]
            def dialogProps = [title: 'Detail Item', preferredSize: new Dimension(900, 620)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def showRetur = {
        execInsideUISync {
            def args = [fakturJualOlehSales: view.table.selectionModel.selected[0]]
            def dialogProps = [title: 'Retur Faktur', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('retur', args, view, dialogProps) { m, v, c ->
                view.table.selectionModel.selected[0] = m.fakturJualOlehSales
            }
        }
    }

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    DialogUtils.message(view.mainPanel, 'Tidak ada konsumen yang dipilih!', 'Cari Konsumen', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.konsumen = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], namaTemplateFaktur: NamaTemplateFaktur.FAKTUR_JUAL_SALES, showParameter: true]
            if (e instanceof FakturJual) args.dataSource = e
            def dialogProps = [title: 'Preview Faktur Jual', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def cetakSuratJalan = {
        FakturJualOlehSales selected = fakturJualRepository.findFakturJualOlehSalesByIdFetchPengeluaranBarang(view.table.selectionModel.selected[0].id)
        if (selected.pengeluaranBarang != null) {
            execInsideUISync {
                def args = [dataSource: selected, namaTemplateFaktur: NamaTemplateFaktur.SURAT_JALAN]
                def dialogProps = [title: 'Preview Surat Jalan', preferredSize: new Dimension(970, 700)]
                DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
            }
        } else {
            DialogUtils.message(view.mainPanel, 'Surat jalan belum dibuat sehingga tidak bisa dicetak!', 'Percetakan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum{ it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it?.total() ?: 0 }?: 0
        model.informasi = "Qty ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"

        jumlahItem = model.listBonus.toArray().sum { it.jumlah }?: 0
        model.informasiBonus = "Qty ${jumlahItem}"
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.konsumen = null
            model.kirimDariGudangUtama = Boolean.FALSE
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.status = null
            model.listItemFaktur.clear()
            model.listBonus.clear()
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
                FakturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.konsumen = selected.konsumen
                model.kirimDariGudangUtama = selected.kirimDariGudangUtama
                model.keterangan = selected.keterangan
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.status = selected.status
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(selected.listItemFaktur)
                model.listBonus.clear()
                if (selected.bonusPenjualan) {
                    model.listBonus.addAll(selected.bonusPenjualan.items)
                }
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                refreshInformasi()
                model.allowPrint = (selected.deleted != 'Y')
            }
        }
    }

}
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
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.Diskon
import domain.penjualan.*
import domain.validation.InputPenjualanOlehSales
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import domain.exception.DataDuplikat
import util.SwingHelper
import java.awt.Dimension
import java.text.DateFormat
import java.text.NumberFormat

class FakturJualOlehSalesController {

    FakturJualOlehSalesModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.konsumenList.clear()
        }
        List konsumen = fakturJualRepository.findAllKonsumen()
        execInsideUISync {
            model.konsumenList.addAll(konsumen)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
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
            konsumen: model.konsumen.selectedItem)
        model.listItemFaktur.each { fakturJualOlehSales.tambah(it) }

        if (!fakturJualRepository.validate(fakturJualOlehSales, InputPenjualanOlehSales, model)) return

        try {
            if (fakturJualOlehSales.id == null) {
                if (!model.listBonus.empty) {
                    fakturJualRepository.buat(fakturJualOlehSales, false, model.listBonus)
                } else {
                    fakturJualRepository.buat(fakturJualOlehSales, false)
                }
                execInsideUISync {
                    model.fakturJualOlehSalesList << fakturJualOlehSales
                    view.table.changeSelection(model.fakturJualOlehSalesList.size() - 1, 0, false, false)
                    clear()
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
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
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
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBonus = {
        execInsideUISync {
            def args = [editable: false, listItemBarang: model.listBonus, allowTambahProduk: false]
            if (view.table.selectionModel.selected[0] == null) {
                args.'editable' = true
            }

            def dialogProps = [title: 'Detail Bonus', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listBonus.clear()
                model.listBonus.addAll(m.itemBarangList)
                refreshInformasi()
            }
        }
    }

    def showItemFaktur = {
        if (!model.konsumen.selectedItem) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Anda harus memilih konsumen terlebih dahulu!', 'Urutan Input Data', JOptionPane.ERROR_MESSAGE)
            return
        }
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur,
                        konsumen: model.konsumen.selectedItem, allowTambahProduk: false, showHarga: model.showFakturJual]
            def dialogProps = [title: 'Detail Item', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum{ it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it.total() }?: 0
        model.informasi = "Qty ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"

        jumlahItem = model.listBonus.toArray().sum { it.jumlah }?: 0
        model.informasiBonus = "Qty ${jumlahItem}"
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.konsumen.selectedItem = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.status = null
            model.listItemFaktur.clear()
            model.listBonus.clear()
            model.createdBy = null
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
                model.konsumen.selectedItem = selected.konsumen
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
                model.createdBy = (selected.createdBy && selected.createdDate)?
                    "${selected.createdBy} (${DateFormat.getDateTimeInstance().format(selected.createdDate)})": "-"
                model.modifiedBy = (selected.modifiedBy && selected.modifiedDate)?
                    "${selected.modifiedBy} (${DateFormat.getDateTimeInstance().format(selected.modifiedDate)})": "-"
                refreshInformasi()
            }
        }
    }

}
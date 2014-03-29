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
package project

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.faktur.Diskon
import domain.pembelian.*
import org.joda.time.LocalDate

import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import domain.Container

import java.text.NumberFormat

class FakturBeliController {

    FakturBeliModel model
    def view

    FakturBeliRepository fakturBeliRepository

    void mvcGroupInit(Map args) {
        fakturBeliRepository = Container.app.fakturBeliRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.supplierList.clear()
        }
        List supplier = fakturBeliRepository.findAllSupplier()
        execInsideUISync {
            model.supplierList.addAll(supplier)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = Container.SEMUA
        }
    }

    def search = {
        List result = fakturBeliRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.supplierSearch, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturBeliList.clear()
            model.fakturBeliList.addAll(result)
            refreshInformasi()
        }
    }

    def save = {
        FakturBeli fakturBeli = new FakturBeli(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            keterangan: model.keterangan, supplier: model.supplier.selectedItem)
        model.listItemFaktur.each { fakturBeli.tambah(it) }
        fakturBeli.diskon = new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung)

        if (!fakturBeliRepository.validate(fakturBeli, Default, model)) return

        try {
            if (fakturBeli.id == null) {
                fakturBeliRepository.buat(fakturBeli)
                execInsideUISync {
                    model.fakturBeliList << fakturBeli
                    view.table.changeSelection(model.fakturBeliList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                fakturBeli = fakturBeliRepository.update(fakturBeli)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = fakturBeli
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            FakturBeli fakturBeli = view.table.selectionModel.selected[0]
            fakturBeli = fakturBeliRepository.hapus(fakturBeli)
            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturBeli
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.sum { it.jumlah }?: 0
        def total = model.listItemFaktur.sum { it.total() }?: 0
        model.informasi = "Jumlah ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.notDeleted = true
            model.nomor = null
            model.tanggal = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.supplier.selectedItem = null
            model.listItemFaktur.clear()

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
                FakturBeli selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.notDeleted = (selected.deleted == 'N')
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.supplier.selectedItem = selected.supplier
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(selected.listItemFaktur)
                refreshInformasi()
            }
        }
    }

}
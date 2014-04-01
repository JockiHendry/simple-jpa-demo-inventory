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
import com.google.common.base.Strings
import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.exception.FakturTidakDitemukan
import domain.inventory.ItemBarang
import domain.pembelian.FakturBeli
import domain.pembelian.PenerimaanBarang
import domain.pembelian.PenerimaanBarangRepository
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils

import javax.swing.event.ListSelectionEvent
import java.awt.Dimension

class ReceivedNotInvoicedController {

    ReceivedNotInvoicedModel model
    def view

    PenerimaanBarangRepository penerimaanBarangRepository

    void mvcGroupInit(Map args) {
        penerimaanBarangRepository = Container.app.penerimaanBarangRepository
        init()
        search()
    }

    def init = {
        model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
        model.tanggalSelesaiSearch = LocalDate.now()
        model.tampilkanHanyaRNI = true
    }

    def search = {
        List result
        if (model.tampilkanHanyaRNI) {
            result = penerimaanBarangRepository.cariReceivedNotInvoiced(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.supplierSearch)
        } else {
            result = penerimaanBarangRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.supplierSearch)
        }
        execInsideUISync {
            model.penerimaanBarangList.clear()
            model.penerimaanBarangList.addAll(result)
        }
    }

    def assign = {
        if (Strings.isNullOrEmpty(model.nomorFaktur)) {
            model.errors['nomorFaktur'] = 'Nomor faktur beli harus di-isi'
            return
        }
        try {
            PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
            penerimaanBarang = Container.app.receivedNotInvoicedService.assign(penerimaanBarang, model.nomorFaktur)
            execInsideUISync {
                if (model.tampilkanHanyaRNI) {
                    model.penerimaanBarangList.remove(view.table.selectionModel.selected[0])
                } else {
                    view.table.selectionModel.selected[0] = penerimaanBarang
                }
                clear()
            }
        } catch (FakturTidakDitemukan | DataTidakKonsisten | DataTidakBolehDiubah ex) {
            model.errors['nomorFaktur'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def hapusAssignment = {
        try {
            PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
            penerimaanBarang = Container.app.receivedNotInvoicedService.hapusAssignment(penerimaanBarang)
            execInsideUISync {
                view.table.selectionModel.selected[0] = penerimaanBarang
            }
            clear()
        } catch (DataTidakBolehDiubah ex) {
            model.errors['nomorFaktur'] = ex.message
        }
    }

    def sisaBarang = {
        PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
        FakturBeli fakturBeli = view.table.selectionModel.selected[0].faktur
        List<ItemBarang> hasil
        if (!fakturBeli) {
            hasil = penerimaanBarang.listItemBarang
        } else {
            hasil = Container.app.receivedNotInvoicedService.sisaBelumDiterima(fakturBeli)
        }
        DialogUtils.showMVCGroup('itemBarangAsChild',
            [editable: false, listItemBarang: hasil],
            app, view,
            [title: 'Daftar Barang', size: new Dimension(900, 420)])
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.faktur = null
            model.nomorFaktur = null
            model.listItemBarang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PenerimaanBarang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.faktur = selected.faktur
                model.nomorFaktur = selected.faktur?.nomor
                model.listItemBarang.clear()
                model.listItemBarang.addAll(selected.listItemBarang)
            }
        }
    }

}

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
package project.pembelian

import ast.NeedSupervisorPassword
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.faktur.Diskon
import domain.pembelian.PurchaseOrder
import domain.pengaturan.NamaTemplateFaktur
import project.user.NomorService
import domain.validation.InputPurchaseOrder
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import util.SwingHelper
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import java.awt.Dimension
import java.text.NumberFormat

@SuppressWarnings("GroovyUnusedDeclaration")
class PurchaseOrderController {

    PurchaseOrderModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        if (args.containsKey('nomorSearch')) model.nomorPOSearch = args.nomorSearch
        model.mode = args.containsKey('mode')? args.'mode': POViewMode.ALL
        switch (model.mode) {
            case POViewMode.FAKTUR_BELI:
                model.showPenerimaan = false
                model.showFakturBeli = true
                model.allowTambahProduk = false
                model.allowAddPO = false
                break
            case POViewMode.PENERIMAAN:
                model.showPenerimaan = true
                model.showFakturBeli = false
                model.allowTambahProduk = false
                model.allowAddPO = false
                break
            case POViewMode.ALL:
                model.showPenerimaan = true
                model.showFakturBeli = true
                model.allowTambahProduk = true
                model.allowAddPO = true
        }

        init()
        search()
    }

    def init = {
        execInsideUISync {
            List supplier = purchaseOrderRepository.findAllSupplier([orderBy: 'nama'])
            model.supplierList.clear()
            model.supplierList.addAll(supplier)
            model.tanggalMulaiSearch = LocalDate.now().minusWeeks(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            nomorService.refreshAll()
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PURCHASE_ORDER)
            model.statusSearch.selectedItem = SwingHelper.SEMUA
        }
    }

    def search = {
        List result = []
        if (model.mode == POViewMode.ALL) {
            result = purchaseOrderRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
                model.nomorPOSearch, model.nomorFakturSearch, model.supplierSearch, model.statusSearch.selectedItem)
        } else if (model.mode == POViewMode.FAKTUR_BELI) {
            result = purchaseOrderRepository.cariFakturBeli(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
                model.nomorPOSearch, model.nomorFakturSearch, model.supplierSearch, model.statusSearch.selectedItem)
        } else if (model.mode == POViewMode.PENERIMAAN) {
            result = purchaseOrderRepository.cariPenerimaan(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
                model.nomorPOSearch, model.nomorFakturSearch, model.supplierSearch, model.statusSearch.selectedItem)
        }
        execInsideUISync {
            model.purchaseOrderList.clear()
            model.purchaseOrderList.addAll(result)
        }
    }

    def save = {
        PurchaseOrder purchaseOrder = new PurchaseOrder(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            keterangan: model.keterangan, supplier: model.supplier.selectedItem,
            diskon: new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung))
        model.listItemFaktur.each { purchaseOrder.tambah(it) }

        if (!purchaseOrderRepository.validate(purchaseOrder, InputPurchaseOrder, model)) return

        try {
            if (purchaseOrder.id == null) {
                purchaseOrder = purchaseOrderRepository.buat(purchaseOrder)
                execInsideUISync {
                    model.purchaseOrderList << purchaseOrder
                    view.table.changeSelection(model.purchaseOrderList.size() - 1, 0, false, false)
                    clear()
                    cetak(purchaseOrder)
                }
            } else {
                purchaseOrder = purchaseOrderRepository.update(purchaseOrder)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = purchaseOrder
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Purchase order tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PurchaseOrder purchaseOrder = view.table.selectionModel.selected[0]
            purchaseOrder = purchaseOrderRepository.hapus(purchaseOrder)
            execInsideUISync {
                view.table.selectionModel.selected[0] = purchaseOrder
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], namaTemplateFaktur: NamaTemplateFaktur.PURCHASE_ORDER]
            if (e instanceof PurchaseOrder ) args.dataSource = e
            def dialogProps = [title: 'Preview Purchase Order', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum { it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it.total() }?: 0
        if (model.showFakturBeli) {
            model.informasi = "Jumlah ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"
        } else {
            model.informasi = "Jumlah ${jumlahItem}"
        }
    }

    def onShowSisaBarang = { def button, Map args ->
        PurchaseOrder selected = view.table.selectionModel.selected[0]
        args.'listItemBarang' = selected.sisaBelumDiterima()
        args.'editable' = false
    }

    def showItemFaktur = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur,
                        editable: model.allowAddPO, showHarga: model.showFakturBeli]
            def dialogProps = [title: 'Detail Item', preferredSize: new Dimension(900, 620)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PURCHASE_ORDER)
            model.tanggal = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.supplier.selectedItem = null
            model.created = null
            model.createdBy = null
            model.modified = null
            model.modifiedBy = null
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
                PurchaseOrder selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.supplier.selectedItem = selected.supplier
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(selected.listItemFaktur)
                model.created = selected.createdDate
                model.createdBy = selected.createdBy ? '(' + selected.createdBy + ')' : null
                model.modified = selected.modifiedDate
                model.modifiedBy = selected.modifiedBy ? '(' + selected.modifiedBy + ')' : null
                refreshInformasi()
            }
        }
    }

}

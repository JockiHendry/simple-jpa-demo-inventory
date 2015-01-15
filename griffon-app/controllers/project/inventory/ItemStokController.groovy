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
package project.inventory

import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.PeriodeItemStok
import domain.inventory.Transfer
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.PencairanPoinTukarBarang
import domain.retur.ReturBeli
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales
import project.main.MainGroupModel
import project.main.MainGroupView

@SuppressWarnings("GroovyUnusedDeclaration")
class ItemStokController {

    ItemStokModel model
    def view

    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.showReferensiFinance = true
        model.showReferensiGudang = false
        model.showPembuat = false
        model.showKeterangan = true
        init()
    }

    def init = {
        execInsideUISync {
            model.periodeItemStokList.clear()
        }
        List periodeItemStok = model.parent.listPeriodeRiwayat.sort { it.tanggalMulai }
        execInsideUISync {
            model.periodeItemStokList.addAll(periodeItemStok)
            model.periodeItemStok.selectedItem = model.periodeItemStokList.isEmpty()? null: model.periodeItemStokList[0]
        }
    }

    def search = {
        if (model.periodeItemStok.selectedItem) {
            execInsideUISync { model.itemStokList.clear()}
            List data = []
            produkRepository.withTransaction {
                model.parent = produkRepository.findStokProdukById(model.parent.id)
                PeriodeItemStok p = produkRepository.findPeriodeItemStokById(model.periodeItemStok.selectedItem.id)
                data.addAll(model.parent.findAllItemPeriodik(p))
            }
            execInsideUISync { model.itemStokList.addAll(data) }
        }
    }

    def tampilkanReferensi = {
        ItemStok itemStok = view.table.selectionModel.selected[0]
        if (itemStok) {
            MainGroupView mainView = app.getMvcGroupManager()['mainGroup'].view
            MainGroupModel mainModel = app.getMvcGroupManager()['mainGroup'].model
            String nomorReferensi, mvcGroup
            if ((itemStok.referensiStok.classFinance == FakturJualOlehSales.simpleName) && mainModel.fakturJualVisible) {
                mvcGroup = 'fakturJualOlehSales'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == FakturJualEceran.simpleName) && mainModel.fakturJualVisible) {
                mvcGroup = 'fakturJualEceran'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == ReturJualOlehSales.simpleName) && mainModel.returJualVisible) {
                mvcGroup = 'returJualOlehSales'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == ReturJualEceran.simpleName) && mainModel.returJualVisible) {
                mvcGroup = 'returJualEceran'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == PurchaseOrder.simpleName) && mainModel.purchaseOrderVisible) {
                mvcGroup = 'purchaseOrder'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == PencairanPoinTukarBarang.simpleName) && mainModel.poinVisible) {
                mvcGroup = 'pencairanPoin'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classFinance == ReturBeli.simpleName) && mainModel.returBeliVisible) {
                mvcGroup = 'returBeli'
                nomorReferensi = itemStok.referensiStok.nomorFinance
            } else if ((itemStok.referensiStok.classGudang == PenyesuaianStok.simpleName) && mainModel.penyesuaianStokVisible) {
                mvcGroup = 'penyesuaianStok'
                nomorReferensi = itemStok.referensiStok.nomorGudang
            } else if ((itemStok.referensiStok.classGudang == Transfer.simpleName) && mainModel.transferVisible) {
                mvcGroup = 'transfer'
                nomorReferensi = itemStok.referensiStok.nomorGudang
            }
            if (mvcGroup) {
                mainView.mainTab.addMVCTab(mvcGroup, [nomorSearch: nomorReferensi], "Referensi $nomorReferensi")
            }
        }
    }

}

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
package project.inventory

import domain.inventory.PeriodeItemStok

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
                data.addAll(model.parent.cariItemStok(p))
            }
            execInsideUISync { model.itemStokList.addAll(data) }
        }
    }

}

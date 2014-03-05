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

import com.google.common.base.Strings
import domain.*
import domain.container.Application
import domain.exception.DataDuplikat
import domain.repository.ProdukRepository
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class ProdukController {

    ProdukModel model
    def view

    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        produkRepository = Application.instance.produkRepository
        search()
    }

    def search = {
        List produkResult
        if (Strings.isNullOrEmpty(model.namaSearch)) {
            produkResult = produkRepository.findAllProdukFetchComplete()
        } else {
            produkResult = produkRepository.findAllProdukByNamaLikeFetchComplete("%${model.namaSearch}%")
        }
        execInsideUISync {
            model.produkList.clear()
            model.produkList.addAll(produkResult)
            model.namaSearch = null
        }
    }

    def save = {
        Produk produk = new Produk(id: model.id, nama: model.nama, harga: model.harga)
        if (!produkRepository.validate(produk, Default, model)) return

        try {
            if (produk.id==null) {
                produkRepository.buat(produk)
                execInsideUISync {
                    model.produkList << produk
                    view.table.changeSelection(model.produkList.size()-1, 0, false, false)
                    clear()
                }
            } else {
                produk = produkRepository.merge(produk)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = produk
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Produk produk = view.table.selectionModel.selected[0]
		produkRepository.remove(produk)
        execInsideUISync {
            model.produkList.remove(produk)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
			model.nama = null
			model.harga = null
			model.daftarStok.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Produk selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
				model.nama = selected.nama
				model.harga = selected.harga
				model.daftarStok.clear()
				model.daftarStok.addAll(selected.daftarStok)
            }
        }
    }

}
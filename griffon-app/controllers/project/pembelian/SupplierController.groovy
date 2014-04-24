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

package project.pembelian

import domain.Container
import domain.pembelian.Supplier
import domain.pembelian.SupplierRepository

import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

import domain.exception.DataDuplikat

class SupplierController {

    SupplierModel model
    def view

    SupplierRepository supplierRepository

    void mvcGroupInit(Map args) {
        supplierRepository = Container.app.supplierRepository
        search()
    }

    def search = {
        List result = supplierRepository.cari(model.namaSearch)
        execInsideUISync {
            model.supplierList.clear()
            model.supplierList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        Supplier supplier = new Supplier(id: model.id, 'nama': model.nama, 'alamat': model.alamat, 'nomorTelepon': model.nomorTelepon)

        if (!supplierRepository.validate(supplier, Default, model)) return

        try {
            if (supplier.id == null) {
                supplierRepository.buat(supplier)
                execInsideUISync {
                    model.supplierList << supplier
                    view.table.changeSelection(model.supplierList.size()-1, 0, false, false)
                    clear()
                }
            } else {
                supplier = supplierRepository.merge(supplier)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = supplier
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Supplier supplier = view.table.selectionModel.selected[0]
		supplierRepository.remove(supplier)
        execInsideUISync {
            model.supplierList.remove(supplier)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
			model.nama = null
			model.alamat = null
			model.nomorTelepon = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Supplier selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
				model.nama = selected.nama
				model.alamat = selected.alamat
				model.nomorTelepon = selected.nomorTelepon
            }
        }
    }

}
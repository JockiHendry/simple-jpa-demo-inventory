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

import domain.penjualan.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import domain.Container

class SalesController {

    SalesModel model
    def view

    SalesRepository salesRepository

    void mvcGroupInit(Map args) {
        salesRepository = Container.app.salesRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.gudangList.clear()
        }
        List gudang = salesRepository.findAllGudang()
        execInsideUISync {
            model.gudangList.addAll(gudang)
        }
    }

    def search = {
        List result = salesRepository.cari(model.namaSearch)
        execInsideUISync {
            model.salesList.clear()
            model.salesList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        Sales sales = new Sales(id: model.id, 'nama': model.nama, 'nomorTelepon': model.nomorTelepon, 'gudang': model.gudang.selectedItem)

        if (!salesRepository.validate(sales, Default, model)) return

        try {
            if (sales.id == null) {
                salesRepository.buat(sales)
                execInsideUISync {
                    model.salesList << sales
                    view.table.changeSelection(model.salesList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                sales = salesRepository.merge(sales)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = sales
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Sales sales = view.table.selectionModel.selected[0]
        salesRepository.remove(sales)

        execInsideUISync {
            model.salesList.remove(sales)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.nomorTelepon = null
            model.gudang.selectedItem = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Sales selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.nomorTelepon = selected.nomorTelepon
                model.gudang.selectedItem = selected.gudang
            }
        }
    }

}
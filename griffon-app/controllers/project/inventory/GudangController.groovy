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

import domain.Container
import domain.exception.DataDuplikat
import domain.exception.GudangUtamaTidakKonsisten
import domain.inventory.Gudang
import domain.inventory.GudangRepository

import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class GudangController {

    GudangModel model
    def view

    GudangRepository gudangRepository

    void mvcGroupInit(Map args) {
        gudangRepository = Container.app.gudangRepository
        search()
    }

    def search = {
        List gudangResult = gudangRepository.cari(model.namaSearch)
        execInsideUISync {
            model.gudangList.clear()
            model.gudangList.addAll(gudangResult)
            model.namaSearch = null
        }
    }

    def save = {
        Gudang gudang = new Gudang(id: model.id, nama: model.nama, utama: model.utama, keterangan: model.keterangan)
        if (!gudangRepository.validate(gudang, Default, model)) return

        try {
            if (gudang.id==null) {
                gudangRepository.buat(gudang)
                execInsideUISync {
                    model.gudangList << gudang
                    view.table.changeSelection(model.gudangList.size()-1, 0, false, false)
                    clear()
                }
            }  else {
                gudang = gudangRepository.update(gudang)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = gudang
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (GudangUtamaTidakKonsisten ex) {
            model.errors['utama'] = ex.message
        }
    }

    def delete = {
        Gudang gudang = view.table.selectionModel.selected[0]
		gudangRepository.remove(gudang)
        execInsideUISync {
            model.gudangList.remove(gudang)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
			model.nama = null
			model.utama = false
			model.keterangan = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Gudang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
				model.nama = selected.nama
				model.utama = selected.utama
				model.keterangan = selected.keterangan
            }
        }
    }

}
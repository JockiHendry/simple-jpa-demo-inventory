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

import domain.inventory.*
import project.inventory.SatuanModel

import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import com.google.common.base.Strings
import domain.exception.DataDuplikat
import domain.Container

class SatuanController {

    SatuanModel model
    def view

    SatuanRepository satuanRepository

    void mvcGroupInit(Map args) {
        satuanRepository = Container.app.satuanRepository
        search()
    }

    def search = {
        List result
        if (Strings.isNullOrEmpty(model.namaSearch)) {
            result = satuanRepository.findAllSatuan()
        } else {
            result = satuanRepository.findAllSatuanByNamaLike("%${model.namaSearch}%")
        }
        execInsideUISync {
            model.satuanList.clear()
            model.satuanList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        Satuan satuan = new Satuan(id: model.id, nama: model.nama, singkatan: model.singkatan)
        if (!satuanRepository.validate(satuan, Default, model)) return

        try {
            if (satuan.id == null) {
                satuanRepository.buat(satuan)
                execInsideUISync {
                    model.satuanList << satuan
                    view.table.changeSelection(model.satuanList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                satuan = satuanRepository.merge(satuan)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = satuan
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Satuan satuan = view.table.selectionModel.selected[0]
        satuanRepository.remove(satuan)
        execInsideUISync {
            model.satuanList.remove(satuan)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.singkatan = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Satuan selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.singkatan = selected.singkatan
            }
        }
    }

}
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

@SuppressWarnings("GroovyUnusedDeclaration")
class RegionController {

    RegionModel model
    def view

    RegionRepository regionRepository

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.bagianDariList.clear()
        }
        List region = regionRepository.findAllRegion()
        execInsideUISync {
            model.bagianDariList.addAll(region)
        }
    }

    def search = {
        List result = regionRepository.cari(model.namaSearch)
        execInsideUISync {
            model.regionList.clear()
            model.regionList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        Region region = new Region(id: model.id, nama: model.nama, bagianDari: model.bagianDari.selectedItem)

        if (!regionRepository.validate(region, Default, model)) return

        try {
            if (region.id == null) {
                regionRepository.buat(region)
                execInsideUISync {
                    model.regionList << region
                    view.table.changeSelection(model.regionList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                region = regionRepository.merge(region)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = region
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        Region region = view.table.selectionModel.selected[0]
        regionRepository.remove(region)

        execInsideUISync {
            model.regionList.remove(region)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.bagianDari.selectedItem = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Region selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.bagianDari.selectedItem = selected.bagianDari
            }
        }
    }

}
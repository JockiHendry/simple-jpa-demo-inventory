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

import domain.Container
import domain.pengaturan.JenisNilai
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.PengaturanRepository

import javax.swing.event.ListSelectionEvent

class PengaturanController {

    PengaturanModel model
    def view

    PengaturanRepository pengaturanRepository

    void mvcGroupInit(Map args) {
        pengaturanRepository = Container.app.pengaturanRepository
        refresh()
    }

    def refresh = {
        List result = []
        pengaturanRepository.cache.each { KeyPengaturan k, def v ->
            def nilai = v
            if (nilai!=null) {
                if (k.jenisNilai == JenisNilai.PASSWORD) {
                    nilai = '[password]'
                }
            }
            result << new PengaturanDTO(k,nilai)
        }
        execInsideUISync {
            model.pengaturanList.clear()
            model.pengaturanList.addAll(result)
        }
    }

    def save = {
        try {
            PengaturanDTO pengaturanDTO = view.table.selectionModel.selected[0]
            def nilai = model.nilai

            if (pengaturanDTO.keyPengaturan.jenisNilai == JenisNilai.PASSWORD) {
                if (!Arrays.equals(view.passwordBaru.getPassword(),view.ulangiPasswordBaru.getPassword())) {
                    model.errors['ulangiPasswordBaru'] = 'Password yang dimasukkan tidak sama!'
                    return
                }
                nilai = String.valueOf(view.passwordBaru.getPassword())
            }

            pengaturanRepository.save(pengaturanDTO.keyPengaturan, nilai)
            refresh()
            execInsideUISync { clear() }
        } catch (IllegalArgumentException ex) {
            model.errors['nilai'] = ex.message
        }
    }

    def clear = {
        execInsideUISync {
            model.keyPengaturan = null
            model.nilai = null
            model.passwordValue = false
            model.genericValue = false
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PengaturanDTO selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.keyPengaturan = selected.keyPengaturan

                model.passwordValue = false
                model.genericValue = false
                if (model.keyPengaturan.jenisNilai == JenisNilai.PASSWORD) {
                    model.passwordValue = true
                } else {
                    model.genericValue = true
                    model.nilai = selected.nilai
                }
            }
        }
    }
}

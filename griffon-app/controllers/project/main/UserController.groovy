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
package project.main

import domain.user.*
import project.user.UserRepository
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import domain.exception.DataDuplikat

@SuppressWarnings("GroovyUnusedDeclaration")
class UserController {

    UserModel model
    def view
    UserRepository userRepository

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.hakAkses.clearSelectedValues()
            List hakAkses = []
            Menu.values().each { hakAkses << it }
            model.hakAkses.values = new ArrayList<>(hakAkses)
        }
    }

    def search = {
        List result = userRepository.cari(model.namaSearch)
        execInsideUISync {
            model.userList.clear()
            model.userList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        User user = new User(id: model.id, nama: model.nama)
        user.hakAkses.clear()
        user.hakAkses.addAll(model.hakAkses.selectedValues)

        String password = null
        if (view.password.getPassword().length > 0) {
            password = new String(view.password.getPassword())
        }

        if (!userRepository.validate(user, Default, model)) return

        try {
            if (user.id == null) {
                userRepository.buat(user, password)
                execInsideUISync {
                    model.userList << user
                    view.table.changeSelection(model.userList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                user = userRepository.update(user, password)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = user
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        User user = view.table.selectionModel.selected[0]
        userRepository.remove(user)

        execInsideUISync {
            model.userList.remove(user)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.loginTerakhir = null
            model.hakAkses.clearSelectedValues()

            model.errors.clear()
            view.password.text = null
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                User selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.loginTerakhir = selected.loginTerakhir
                model.hakAkses.clearSelectedValues()
                model.hakAkses.setSelectedValues(new ArrayList(selected.hakAkses))
                view.password.text = null
            }
        }
    }

}
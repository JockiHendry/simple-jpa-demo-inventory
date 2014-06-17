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

import domain.Container
import domain.util.Pesan
import domain.util.PesanRepository

import javax.swing.JOptionPane

class PesanController {

    PesanModel model
    def view

    PesanRepository pesanRepository

    void mvcGroupInit(Map args) {
        pesanRepository = Container.app.pesanRepository
        refresh()
    }

    def refresh = {
        List result = pesanRepository.refresh()
        execInsideUISync {
            model.pesanList.clear()
            model.pesanList.addAll(result)
        }
    }

    def hapus = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin ingin menghapus pesan ini?', 'Konfirmasi Hapus', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return
        }
        Pesan pesan = view.table.selectionModel.selected[0]
        pesanRepository.remove(pesan)
        execInsideUISync {
            model.pesanList.remove(pesan)
            view.table.selectionModel.clearSelection()
        }
    }

}

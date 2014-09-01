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
import simplejpa.swing.DialogUtils
import javax.swing.*
import java.awt.Dimension

class RiwayatPoinController {

    RiwayatPoinModel model
    def view
    KonsumenRepository konsumenRepository

    def cariKonsumen = {
        execInsideUISync {
            def args = [popup: true]
            def dialogProps = [title: 'Cari Konsumen...', preferredSize: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('konsumen', args, app, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(view.mainPanel, 'Tidak ada konsumen yang dipilih!', 'Cari Konsumen', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.konsumenSearch = v.view.table.selectionModel.selected[0]
                }
            }
        }
    }

    def search = {
        if (model.konsumenSearch == null) {
            model.errors['konsumenSearch'] = 'Konsumen harus di-isi!'
            return
        }
        List<RiwayatPoin> result = konsumenRepository.cariRiwayatPoin(model.konsumenSearch, model.tanggalMulaiSearch, model.tanggalSelesaiSearch)
        if (!result.empty) {
            execInsideUISync {
                model.riwayatPoinList.clear()
                model.riwayatPoinList.addAll(result)
            }
        }
    }

}
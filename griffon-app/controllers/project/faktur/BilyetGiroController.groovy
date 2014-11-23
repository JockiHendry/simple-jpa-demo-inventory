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
package project.faktur

import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.faktur.BilyetGiro
import project.penjualan.BilyetGiroService
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

@SuppressWarnings("GroovyUnusedDeclaration")
class BilyetGiroController {

    BilyetGiroModel model
    def view
    BilyetGiroRepository bilyetGiroRepository
    BilyetGiroService bilyetGiroService

    void mvcGroupInit(Map args) {
        model.popupMode = args.'popupMode'?: false
    }

    def search = {
        List bilyetGiro = bilyetGiroRepository.cari(model.nomorSeriSearch, null, model.popupMode)
        execInsideUISync {
            model.bilyetGiroList.clear()
            model.bilyetGiroList.addAll(bilyetGiro)
        }
    }

    def save = {
        BilyetGiro bilyetGiro = new BilyetGiro(id: model.id, nomorSeri: model.nomorSeri, namaBank: model.namaBank,
                                               nominal: model.nominal, jatuhTempo: model.jatuhTempo)

        if (!bilyetGiroRepository.validate(bilyetGiro, Default, model)) return

        try {
            if (bilyetGiro.id == null) {
                bilyetGiro = bilyetGiroRepository.buat(bilyetGiro)
                execInsideUISync {
                    model.bilyetGiroList << bilyetGiro
                    view.table.changeSelection(model.bilyetGiroList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                bilyetGiro = bilyetGiroRepository.update(bilyetGiro)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = bilyetGiro
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomorSeri'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Bilyet giro tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def cairkan = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin sudah mencairkan bilyet giro ini?', 'Pencairan Bilyet Giro', JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
            return
        }
        try {
            BilyetGiro bilyetGiro = view.table.selectionModel.selected[0]
            bilyetGiro = bilyetGiroRepository.cairkan(bilyetGiro)
            execInsideUIAsync {
                view.table.selectionModel.selected[0] = bilyetGiro
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(view.mainPanel, ex.message, 'Kesalahan Pencairan Giro', JOptionPane.ERROR_MESSAGE)
        }
    }

    def delete = {
        try {
            BilyetGiro bilyetGiro = view.table.selectionModel.selected[0]
            bilyetGiro = bilyetGiroService.hapus(bilyetGiro)

            execInsideUISync {
                view.table.selectionModel.selected[0] = bilyetGiro
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, ex.message, 'Kesalahan Hapus Giro', JOptionPane.ERROR_MESSAGE)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomorSeri = null
            model.jatuhTempo = null
            model.tanggalPencairan = null
            model.nominal = null
            model.namaBank = null
            model.deleted = false
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                BilyetGiro selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomorSeri = selected.nomorSeri
                model.nominal = selected.nominal
                model.namaBank = selected.namaBank
                model.jatuhTempo = selected.jatuhTempo
                model.tanggalPencairan = selected.tanggalPencairan
                model.deleted = (selected.deleted != 'N')
            }
        }
    }

}

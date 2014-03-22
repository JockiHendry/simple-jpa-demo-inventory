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
import domain.exception.DataTidakBolehDiubah
import domain.inventory.Periode
import domain.pembelian.*
import domain.validation.TanpaGudang
import org.joda.time.LocalDate

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import com.google.common.base.Strings
import domain.exception.DataDuplikat

class PenerimaanBarangController {

    PenerimaanBarangModel model
    def view

    PenerimaanBarangRepository penerimaanBarangRepository

    void mvcGroupInit(Map args) {
        penerimaanBarangRepository = Container.app.penerimaanBarangRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.supplierList.clear()
        }
        List supplier = penerimaanBarangRepository.findAllSupplier()
        execInsideUISync {
            model.supplierList.addAll(supplier)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
        }
    }

    def search = {
        List result
        result = penerimaanBarangRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.supplierSearch)
        execInsideUISync {
            model.penerimaanBarangList.clear()
            model.penerimaanBarangList.addAll(result)
        }
    }

    def save = {
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(id: model.id, nomor: model.nomor,
            tanggal: model.tanggal, keterangan: model.keterangan, supplier: model.supplier.selectedItem)
        penerimaanBarang.listItemBarang.addAll(model.listItemBarang)

        if (!penerimaanBarangRepository.validate(penerimaanBarang, TanpaGudang, model)) return

        try {
            if (penerimaanBarang.id == null) {
                penerimaanBarangRepository.buat(penerimaanBarang)
                execInsideUISync {
                    model.penerimaanBarangList << penerimaanBarang
                    view.table.changeSelection(model.penerimaanBarangList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                penerimaanBarang = penerimaanBarangRepository.update(penerimaanBarang)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = penerimaanBarang
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Penerimaan barang tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def delete = {
        try {
            PenerimaanBarang penerimaanBarang = view.table.selectionModel.selected[0]
            penerimaanBarang = penerimaanBarangRepository.hapus(penerimaanBarang)
            execInsideUISync {
                view.table.selectionModel.selected[0] = penerimaanBarang
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Penerimaan barang tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = null
            model.tanggal = null
            model.keterangan = null
            model.supplier.selectedItem = null
            model.listItemBarang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PenerimaanBarang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.supplier.selectedItem = selected.supplier
                model.listItemBarang.clear()
                model.listItemBarang.addAll(selected.listItemBarang)
            }
        }
    }

}
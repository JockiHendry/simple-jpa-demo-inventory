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

import ast.NeedSupervisorPassword
import domain.exception.DataDuplikat
import domain.inventory.Transfer
import project.user.NomorService
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class TransferController {

    TransferModel model
    def view
    TransferRepository transferRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        nomorService.refreshAll()
        execInsideUISync {
            model.transferList.clear()
        }
        List gudang = transferRepository.findAllGudang([orderBy: 'nama'])
        execInsideUISync {
            model.gudangList.addAll(gudang)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.asalSearch = null
            model.tujuanSearch = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.TRANSFER)
        }
    }

    def search = {
        List result = transferRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch,
            model.asalSearch, model.tujuanSearch)
        execInsideUISync {
            model.transferList.clear()
            model.transferList.addAll(result)
        }
    }

    def save = {
        Transfer transfer = new Transfer(id: model.id, nomor: model.nomor, tanggal: model.tanggal, gudang: model.gudang.selectedItem,
            tujuan: model.tujuan.selectedItem, keterangan: model.keterangan)
        model.items.each { transfer.tambah(it) }

        if (!transferRepository.validate(transfer, Default, model)) return

        try {
            if (transfer.id == null) {
                transferRepository.buat(transfer)
                execInsideUISync {
                    model.transferList << transfer
                    view.table.changeSelection(model.transferList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                transferRepository.update(transfer)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = transfer
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    @NeedSupervisorPassword
    def delete = {
        Transfer transfer = view.table.selectionModel.selected[0]
        transfer = transferRepository.hapus(transfer)
        execInsideUISync {
            view.table.selectionModel.selected[0] = transfer
            clear()
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.items.toArray().sum { it.jumlah }?: 0
        model.informasi = "Jumlah ${jumlahItem}"
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.items]
            def dialogProps = [title: 'Detail Item', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.items.clear()
                model.items.addAll(m.itemBarangList)
                refreshInformasi()
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.TRANSFER)
            model.tanggal = null
            model.keterangan = null
            model.gudang.selectedItem = null
            model.tujuan.selectedItem = null
            model.items.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
            refreshInformasi()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Transfer selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.gudang.selectedItem = selected.gudang
                model.tujuan.selectedItem = selected.tujuan
                model.items.clear()
                model.items.addAll(selected.items)
                refreshInformasi()
            }
        }
    }


}

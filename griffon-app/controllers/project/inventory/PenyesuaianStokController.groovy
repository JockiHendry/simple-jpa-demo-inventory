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
import domain.exception.StokTidakCukup
import domain.inventory.PenyesuaianStok
import project.user.NomorService
import org.joda.time.LocalDate
import simplejpa.swing.DialogUtils
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

class PenyesuaianStokController {

    PenyesuaianStokModel model
    def view
    PenyesuaianStokRepository penyesuaianStokRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        init()
        search()
    }

    def init = {
        nomorService.refreshAll()
        execInsideUISync {
            model.gudangList.clear()
        }
        List gudang = penyesuaianStokRepository.findAllGudang([orderBy: 'nama'])
        execInsideUISync {
            model.gudangList.addAll(gudang)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.gudangSearch = null
            model.nomorSearch = null
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENYESUAIAN_STOK)
        }
    }

    def search = {
        List result = penyesuaianStokRepository.cari(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.gudangSearch)
        execInsideUISync {
            model.penyesuaianStokList.clear()
            model.penyesuaianStokList.addAll(result)
        }
    }

    def save = {
        PenyesuaianStok penyesuaianStok = new PenyesuaianStok(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            gudang: model.gudang.selectedItem, keterangan: model.keterangan)
        model.items.each { penyesuaianStok.tambah(it) }

        if (model.bertambah || model.berkurang) {
            penyesuaianStok.bertambah = model.bertambah
        } else {
            model.errors['bertambah'] = 'Harus dipilih'
            return
        }

        if (!penyesuaianStokRepository.validate(penyesuaianStok, Default, model)) return

        try {
            if (penyesuaianStok.id == null) {
                penyesuaianStokRepository.buat(penyesuaianStok)
                execInsideUISync {
                    model.penyesuaianStokList << penyesuaianStok
                    view.table.changeSelection(model.penyesuaianStokList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                penyesuaianStokRepository.update(penyesuaianStok)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = penyesuaianStok
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (StokTidakCukup ex) {
            model.errors['items'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        PenyesuaianStok penyesuaianStok = view.table.selectionModel.selected[0]
        penyesuaianStok = penyesuaianStokRepository.hapus(penyesuaianStok)
        execInsideUISync {
            view.table.selectionModel.selected[0] = penyesuaianStok
            clear()
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.items.toArray().sum { it.jumlah }?: 0
        model.informasi = "Jumlah ${jumlahItem}"
    }

    def showItemBarang = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemBarang: model.items, allowTambahProduk: false]
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
            model.nomor = nomorService.getCalonNomor(NomorService.TIPE.PENYESUAIAN_STOK)
            model.tanggal = null
            model.keterangan = null
            model.gudang.selectedItem = null
            model.bertambah = false
            model.berkurang = false
            view.bertambah.selected = model.bertambah
            view.berkurang.selected = model.berkurang
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
                PenyesuaianStok selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.keterangan = selected.keterangan
                model.gudang.selectedItem = selected.gudang
                model.bertambah = selected.bertambah
                model.berkurang = !selected.bertambah
                view.bertambah.selected = model.bertambah
                view.berkurang.selected = model.berkurang
                model.items.clear()
                model.items.addAll(selected.items)
                refreshInformasi()
            }
        }
    }

}

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
package project.labarugi

import ast.NeedSupervisorPassword
import domain.exception.DataDuplikat
import domain.labarugi.*
import project.user.NomorService
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class TransaksiKasController {

    TransaksiKasModel model
    def view
    KasRepository kasRepository
    NomorService nomorService

    void mvcGroupInit(Map args) {
        model.kas = args.kas
        List periodeKas = model.kas.listPeriodeRiwayat.sort { it.tanggalMulai }
        List kategoriKasResult = kasRepository.findAllKategoriKas([orderBy: 'jenis,nama'])
        List jenisTransaksiKasResult = kasRepository.findAllJenisTransaksiKas([orderBy: 'nama'])
        execInsideUISync {
            model.periodeKasList.addAll(periodeKas)
            model.periodeKas.selectedItem = model.periodeKasList.isEmpty()? null: model.periodeKasList[0]
            model.kategoriKasList.clear()
            model.kategoriKasList.addAll(kategoriKasResult)
            model.jenisTransaksiKasList.clear()
            model.jenisTransaksiKasList.addAll(jenisTransaksiKasResult)
        }
    }

    def search = {
        if (model.periodeKas.selectedItem) {
            execInsideUISync { model.transaksiKasList.clear() }
            List data = []
            kasRepository.withTransaction {
                model.kas = kasRepository.findKasById(model.kas.id)
                PeriodeKas p = kasRepository.findPeriodeKasById(model.periodeKas.selectedItem.id)
                data.addAll(model.kas.findAllItemPeriodik(p).findAll {
                    if ((model.pihakTerkaitSearch?.trim()?.length() > 0) && !it.pihakTerkait?.toLowerCase()?.contains(model.pihakTerkaitSearch.toLowerCase())) {
                        return false
                    }
                    if ((model.kategoriKasSearch?.trim()?.length() > 0) && !it.kategoriKas?.nama?.toLowerCase()?.contains(model.kategoriKasSearch.toLowerCase())) {
                        return false
                    }
                    true
                })
            }
            execInsideUISync { model.transaksiKasList.addAll(data) }
        }
    }

    def save = {
        TransaksiKas transaksiKas = new TransaksiKas(tanggal: model.tanggal, pihakTerkait: model.pihakTerkait, kategoriKas: model.kategoriKas.selectedItem,
            jumlah: model.jumlah, jenis: model.jenisTransaksiKas.selectedItem, keterangan: model.keterangan)
        if (!kasRepository.validate(transaksiKas, Default, model)) return

        try {
            kasRepository.withTransaction {
                model.kas = findKasById(model.kas.id)
                model.kas.tambah(transaksiKas)
            }
            execInsideUISync {
                model.transaksiKasList << transaksiKas
                view.table.changeSelection(model.transaksiKasList.size() - 1, 0, false, false)
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
        execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
    }

    def summary = {
        if (model.periodeKas.selectedItem) {
            execInsideUISync {
                def args = [parent: model.periodeKas.selectedItem]
                def props = [title: 'Saldo Periodik', preferredSize: new Dimension(900, 620)]
                DialogUtils.showMVCGroup('jumlahPeriodeKasAsChild', args, view, props)
            }
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        TransaksiKas transaksiKas = view.table.selectionModel.selected[0]
        kasRepository.withTransaction {
            model.kas = findKasById(model.kas.id)
            model.kas.hapus(transaksiKas)
        }
        execInsideUISync {
            model.transaksiKasList.remove(transaksiKas)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.tanggal = null
            model.pihakTerkait = null
            model.kategoriKas.selectedItem = null
            model.jumlah = null
            model.jenisTransaksiKas.selectedItem = null
            model.keterangan = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                TransaksiKas selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.tanggal = selected.tanggal
                model.pihakTerkait = selected.pihakTerkait
                model.kategoriKas.selectedItem = selected.kategoriKas
                model.jumlah = selected.jumlah
                model.jenisTransaksiKas.selectedItem = selected.jenis
                model.keterangan = selected.keterangan
            }
        }
    }

}
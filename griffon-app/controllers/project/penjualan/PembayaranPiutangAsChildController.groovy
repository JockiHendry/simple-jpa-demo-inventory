/*
 * Copyright 2015 Jocki Hendry.
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

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.exception.FakturTidakDitemukan
import domain.exception.HargaSelisih
import domain.faktur.Pembayaran
import domain.faktur.Referensi
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class PembayaranPiutangAsChildController {

    PembayaranPiutangAsChildModel model
    def view
    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        model.faktur = args.'faktur'
        model.editable = true
        execInsideUISync {
            model.pembayaranList.clear()
            model.pembayaranList.addAll(args.'listPembayaran')
        }
    }

    def save = {
        if (!view.table.selectionModel.selectionEmpty) {
            DialogUtils.message(view.mainPanel, 'Pembayaran tidak dapat di-edit.  Hapus dan buat pembayaran baru bila perlu!', 'Edit Pembayaran', JOptionPane.ERROR_MESSAGE)
            return
        }
        Pembayaran pembayaran = new Pembayaran(tanggal: model.tanggal, jumlah: model.jumlah, potongan: model.potongan, keteranganPembayaran: model.keteranganPembayaran)
        if (model.jenisReferensi.selectedItem && model.nomorReferensi) {
            pembayaran.referensi = new Referensi(model.jenisReferensi.selectedItem.clazz, model.nomorReferensi)
        }
        if (!fakturJualRepository.validate(pembayaran, Default, model)) return

        try {
            model.faktur = fakturJualRepository.bayar(model.faktur, pembayaran, model.bilyetGiro)
            execInsideUISync {
                model.pembayaranList << pembayaran
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Pembayaran tidak dapat dilakukan lagi!', 'Pembayaran gagal disimpan', JOptionPane.ERROR_MESSAGE)
        } catch (IllegalArgumentException | HargaSelisih ex) {
            model.errors['jumlah'] = ex.message
        } catch (FakturTidakDitemukan ex) {
            model.errors['nomorReferensi'] = ex.message
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        try {
            Pembayaran pembayaranHutang = view.table.selectionModel.selected[0]
            model.faktur = fakturJualRepository.hapusPembayaran(model.faktur, pembayaranHutang)
            execInsideUISync {
                model.pembayaranList.remove(pembayaranHutang)
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Pembayaran tidak dapat dihapus lagi!', 'Pembayaran gagal dihapus', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBilyetGiro = {
        execInsideUISync {
            def args = [popupMode: true]
            def dialogProps = [title: 'Cari Bilyet Giro', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('bilyetGiro', args, view, dialogProps) { m, v, c ->
                if (v.table.selectionModel.isSelectionEmpty()) {
                    DialogUtils.message(view.mainPanel, 'Tidak ada bilyet giro yang dipilih!', 'Cari Bilyet Giro', JOptionPane.ERROR_MESSAGE)
                } else {
                    model.bilyetGiro = v.table.selectionModel.selected[0]
                }
            }
        }
    }

    def hapusBilyetGiro = {
        execInsideUISync {
            model.bilyetGiro = null
        }
    }

    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose() }
    }

    def clear = {
        execInsideUISync {
            model.tanggal = null
            model.jumlah = null
            model.bilyetGiro = null
            model.potongan = false
            model.keteranganPembayaran = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Pembayaran selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.tanggal = selected.tanggal
                model.jumlah = selected.jumlah
                model.potongan = selected.potongan as boolean
                model.bilyetGiro = selected.bilyetGiro
                model.keteranganPembayaran = selected.keteranganPembayaran
            }
        }
    }

}

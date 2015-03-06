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
package project.pembelian

import ast.NeedSupervisorPassword
import domain.exception.BarangSelisih
import domain.exception.DataTidakBolehDiubah
import domain.exception.HargaSelisih
import domain.faktur.Diskon
import domain.pembelian.*
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import java.awt.Dimension
import java.text.NumberFormat

@SuppressWarnings("GroovyUnusedDeclaration")
class FakturBeliController {

    FakturBeliModel model
    def view

    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        model.purchaseOrder = args.'purchaseOrder'
        model.fakturBeli = model.purchaseOrder.fakturBeli
        model.editable = args.containsKey('editable')? args.'editable': false
        model.allowTambahProduk = args.containsKey('allowTambahProduk')? args.'allowTambahProduk': true
        refreshInformasi()

        // init data
        model.errors.clear()
        if (model.fakturBeli) {
            model.fakturBeli.with {
                model.id = id
                model.notDeleted = (deleted == 'N')
                model.nomor = nomor
                model.tanggal = tanggal
                model.jatuhTempo = jatuhTempo
                model.keterangan = keterangan
                model.diskonPotonganLangsung = diskon?.potonganLangsung
                model.diskonPotonganPersen = diskon?.potonganPersen
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(listItemFaktur)
            }
            refreshInformasi()
        }

    }

    def save = {
        FakturBeli fakturBeli = new FakturBeli(nomor: model.nomor, tanggal: model.tanggal, jatuhTempo: model.jatuhTempo, keterangan: model.keterangan)
        fakturBeli.diskon = new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung)
        model.listItemFaktur.each { fakturBeli.tambah(it) }

        if (!purchaseOrderRepository.validate(fakturBeli, Default, model)) return

        try {
            model.purchaseOrder = purchaseOrderRepository.tambah(model.purchaseOrder, fakturBeli)
            DialogUtils.message(view.mainPanel, 'Faktur beli berhasil disimpan!', 'Informasi', JOptionPane.INFORMATION_MESSAGE)
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (BarangSelisih | HargaSelisih ex) {
            if (DialogUtils.confirm(view.mainPanel, "${ex.message}.\nAnda yakin ingin melanjutkan?", 'Konfirmasi')) {
                model.purchaseOrder = purchaseOrderRepository.tambah(model.purchaseOrder, fakturBeli, false)
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        try {
            if (model.fakturBeli) {
                model.purchaseOrder = purchaseOrderRepository.hapusFaktur(model.purchaseOrder)
                execInsideUISync {
                    clear()
                }
            }
        } catch (DataTidakBolehDiubah ex) {
            DialogUtils.message(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum { it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it.total() }?: 0
        model.informasi = "Qty ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"
    }

    def hitungJatuhTempo = {
        if (!model.hariJatuhTempo) return
        model.jatuhTempo = model.tanggal.plusDays(model.hariJatuhTempo)
    }

    def showItemFaktur = {
        execInsideUISync {
            def args = [parent: model.fakturBeli, listItemFaktur: model.listItemFaktur, allowTambahProduk: model.allowTambahProduk, editable: true]
            def dialogProps = [title: 'Item Faktur', preferredSize: new Dimension(900,420)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.notDeleted = true
            model.nomor = null
            model.tanggal = null
            model.jatuhTempo = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.listItemFaktur.clear()

            model.errors.clear()
            refreshInformasi()
        }
    }

}
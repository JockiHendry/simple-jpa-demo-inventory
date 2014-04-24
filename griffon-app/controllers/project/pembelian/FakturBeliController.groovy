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
package project.pembelian

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.faktur.Diskon
import domain.pembelian.*
import project.pembelian.FakturBeliModel

import javax.swing.*
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import domain.Container

import java.text.NumberFormat

class FakturBeliController {

    FakturBeliModel model
    def view

    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        purchaseOrderRepository = Container.app.purchaseOrderRepository
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
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur beli berhasil disimpan!', 'Informasi', JOptionPane.INFORMATION_MESSAGE)
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (DataTidakKonsisten ex) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, "${ex.message}.\nAnda yakin ingin melanjutkan?", 'Konfirmasi', JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.purchaseOrder = purchaseOrderRepository.tambah(model.purchaseOrder, fakturBeli, false)
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            if (model.fakturBeli) {
                model.purchaseOrder = purchaseOrderRepository.hapus(model.purchaseOrder, model.fakturBeli)
                execInsideUISync {
                    clear()
                }
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur beli tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
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
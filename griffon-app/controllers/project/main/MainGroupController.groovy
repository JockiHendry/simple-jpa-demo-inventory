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

import domain.user.User
import simplejpa.SimpleJpaUtil
import util.BusyLayerUI
import javax.swing.JButton
import java.awt.*
import java.awt.event.*

class MainGroupController {

    MainGroupModel model
    def view
    def groupId

    void mvcGroupInit(Map args) {
        User currentUser = SimpleJpaUtil.instance.user
        if (currentUser) {
            model.status = "<html>Aplikasi demo inventory dengan Griffon dan plugin simple-jpa |  Selamat datang, <b>${currentUser.nama}</b>.</html>"
            model.penerimaanBarangVisible = currentUser.bolehAkses(domain.user.Menu.PENERIMAAN_BARANG)
            model.pengeluaranBarangVisible = currentUser.bolehAkses(domain.user.Menu.PENGELUARAN_BARANG)
            model.buktiTerimaVisible = currentUser.bolehAkses(domain.user.Menu.BUKTI_TERIMA)
            model.purchaseOrderVisible = currentUser.bolehAkses(domain.user.Menu.PURCHASE_ORDER)
            model.fakturBeliVisible = currentUser.bolehAkses(domain.user.Menu.FAKTUR_BELI)
            model.fakturJualVisible = currentUser.bolehAkses(domain.user.Menu.FAKTUR_JUAL)
            model.hutangVisible = currentUser.bolehAkses(domain.user.Menu.HUTANG)
            model.piutangVisible = currentUser.bolehAkses(domain.user.Menu.PIUTANG)
            model.giroVisible = currentUser.bolehAkses(domain.user.Menu.GIRO)
            model.poinVisible = currentUser.bolehAkses(domain.user.Menu.POIN)
            model.produkVisible = currentUser.bolehAkses(domain.user.Menu.PRODUK)
            model.transferVisible = currentUser.bolehAkses(domain.user.Menu.TRANSFER)
            model.penyesuaianStokVisible = currentUser.bolehAkses(domain.user.Menu.PENYESUAIAN_STOK)
            model.returJualVisible = currentUser.bolehAkses(domain.user.Menu.RETUR_JUAL)
            model.returBeliVisible = currentUser.bolehAkses(domain.user.Menu.RETUR_BELI)
            model.laporanVisible = currentUser.bolehAkses(domain.user.Menu.LAPORAN)
            model.maintenanceVisible = currentUser.bolehAkses(domain.user.Menu.MAINTENANCE)
            model.pesanVisible = currentUser.bolehAkses(domain.user.Menu.PESAN)
        }
        app.addApplicationEventListener(this)
        if (model.pesanVisible) {
            view.mainTab.addMVCTab('pesan', [:])
        }
    }

    void onUpdatePesan(boolean visible) {
        JButton pesanNotifikasi = view.pesanNotifikasi
        if (model.pesanVisible) {
            pesanNotifikasi.visible = visible
        } else {
            pesanNotifikasi.visible = false
        }
    }

    def switchPage = { ActionEvent event, Map arguments = [:] ->
        execInsideUISync {
            BusyLayerUI.instance.show()

            def groupId = event.actionCommand
            view.mainTab.addMVCTab(groupId, arguments)

            BusyLayerUI.instance.hide()
        }
    }

}
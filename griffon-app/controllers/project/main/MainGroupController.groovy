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
import org.jdesktop.swingx.JXLoginPane
import util.BusyLayerUI
import java.awt.*
import java.awt.event.*
import griffon.util.GriffonNameUtils
import domain.Container
import domain.user.Menu

class MainGroupController {

    MainGroupModel model
    def view
    def groupId

    void mvcGroupInit(Map args) {
        execInsideUISync {
            if (Environment.current != Environment.TEST) {
                JXLoginPane panel = new JXLoginPane(Container.app.userLoginService)
                JXLoginPane.Status status = JXLoginPane.showLoginDialog(app.windowManager.getStartingWindow(), panel)
                if (status != JXLoginPane.Status.SUCCEEDED) {
                    app.shutdown()
                }

                User currentUser = Container.app.currentUser
                model.status = "Aplikasi demo inventory dengan Griffon dan plugin simple-jpa |  Selamat datang, ${currentUser.nama}."
                model.penerimaanBarangVisible = currentUser.bolehAkses(Menu.PENERIMAAN_BARANG)
                model.pengeluaranBarangVisible = currentUser.bolehAkses(Menu.PENGELUARAN_BARANG)
                model.buktiTerimaVisible = currentUser.bolehAkses(Menu.BUKTI_TERIMA)
                model.purchaseOrderVisible = currentUser.bolehAkses(Menu.PURCHASE_ORDER)
                model.fakturBeliVisible = currentUser.bolehAkses(Menu.FAKTUR_BELI)
                model.fakturJualVisible = currentUser.bolehAkses(Menu.FAKTUR_JUAL)
                model.hutangVisible = currentUser.bolehAkses(Menu.HUTANG)
                model.piutangVisible = currentUser.bolehAkses(Menu.PIUTANG)
                model.giroVisible = currentUser.bolehAkses(Menu.GIRO)
                model.produkVisible = currentUser.bolehAkses(Menu.PRODUK)
                model.transferVisible = currentUser.bolehAkses(Menu.TRANSFER)
                model.laporanVisible = currentUser.bolehAkses(Menu.LAPORAN)
                model.maintenanceVisible = currentUser.bolehAkses(Menu.MAINTENANCE)
            }
        }
    }

    def switchPage = { ActionEvent event, Map arguments = [:] ->

        execInsideUISync {
            BusyLayerUI.instance.show()

            // destroying previous MVCGroup before switching to a new one
            if (groupId) {
                app.mvcGroupManager.destroyMVCGroup(groupId)
            }

            groupId = event.actionCommand

            // destroying current MVCGroup if it was not destroyed properly before
            if (app.mvcGroupManager.findConfiguration(groupId)) {
                app.mvcGroupManager.destroyMVCGroup(groupId)
            }

            def (m, v, c) = app.mvcGroupManager.createMVCGroup(groupId, groupId, arguments)

            view.mainPanel.removeAll()
            view.mainPanel.add(v.mainPanel, BorderLayout.CENTER)
            view.mainPanel.revalidate()
            view.mainPanel.repaint()
            BusyLayerUI.instance.hide()                        
            view.mainFrame.title = "${app.config.application.title} ${app.metadata.getApplicationVersion()}: ${GriffonNameUtils.getNaturalName(groupId)}"
        }
    }

}
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

import project.main.MainGroupModel
import project.pembelian.POViewMode
import project.penjualan.FakturEceranViewMode
import project.retur.ReturJualViewMode
import util.BusyLayerUI

import javax.swing.*
import javax.swing.border.*
import java.awt.*
import java.awt.event.*

def popupMaintenance = {
    maintenancePopup.show(maintenanceButton, 0, maintenanceButton.getHeight())
}

def popupPengeluaranBarang = {
    pengeluaranBarangPopup.show(pengeluaranBarangButton, 0, pengeluaranBarangButton.getHeight())
}

def popupPenjualan = {
    penjualanPopup.show(fakturJualButton, 0, fakturJualButton.getHeight())
}



def switchPageWithArguments = { Map arguments ->
    return { ActionEvent event ->
        controller.switchPage(event, arguments)
    }
}

actions {
    action(id: 'penerimaanBarang', name: 'Terima Barang', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_T,
        smallIcon: imageIcon('/menu_penerimaan_barang.png'), closure: switchPageWithArguments([mode: POViewMode.PENERIMAAN]))
    action(id: 'pengeluaranBarang', name: 'Antar Barang', actionCommandKey: 'pengeluaranBarang', mnemonic: KeyEvent.VK_A,
        smallIcon: imageIcon('/menu_pengeluaran.png'), closure: popupPengeluaranBarang)
    action(id: 'pengeluaranBarangEceran', name: 'Eceran', actionCommandKey: 'fakturJualEceran', mnemonic: KeyEvent.VK_E,
        smallIcon: imageIcon('/menu_pengeluaran_eceran.png'), closure: switchPageWithArguments([mode: FakturEceranViewMode.PENGELUARAN]))
    action(id: 'pengiriman', name: 'Kirim Dalam Kota', actionCommandKey: 'pengiriman', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_pengeluaran_sales.png'), closure: controller.switchPage)
    action(id: 'pengeluaranReturJual', name: 'Retur Jual', actionCommandKey: 'returJual', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_pengeluaran_retur_jual.png'), closure: switchPageWithArguments([mode: ReturJualViewMode.PENGELUARAN]))
    action(id: 'buktiTerima', name: 'Bukti Terima', actionCommandKey: 'buktiTerima', mnemonic: KeyEvent.VK_T,
        smallIcon: imageIcon('/menu_bukti_terima.png'), closure: controller.switchPage)

    action(id: 'purchaseOrder', name: 'Purchase Order', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_purchaseorder.png'), closure: switchPageWithArguments([mode: POViewMode.ALL]))
    action(id: 'fakturBeli', name: 'Pembelian', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_pembelian.png'), closure: switchPageWithArguments([mode: POViewMode.FAKTUR_BELI]))
    action(id: 'fakturJual', name: 'Penjualan', actionCommandKey: 'penjualan', mnemonic: KeyEvent.VK_J,
        smallIcon: imageIcon('/menu_penjualan.png'), closure: popupPenjualan)
    action(id: 'fakturJualEceran', name: 'Eceran', actionCommandKey: 'fakturJualEceran', mnemonic: KeyEvent.VK_E,
        smallIcon: imageIcon('/menu_penjualan_eceran.png'), closure: switchPageWithArguments([mode: FakturEceranViewMode.FAKTUR]))
    action(id: 'fakturJualOlehSales', name: 'Sales', actionCommandKey: 'fakturJualOlehSales', mnemonic: KeyEvent.VK_S,
        smallIcon: imageIcon('/menu_penjualan_sales.png'), closure: controller.switchPage)
    action(id: 'hutang', name: 'Hutang', actionCommandKey: 'hutang', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_hutang.png'), closure: controller.switchPage)
    action(id: 'piutang', name: 'Piutang', actionCommandKey: 'piutang', mnemonic: KeyEvent.VK_I,
        smallIcon: imageIcon('/menu_piutang.png'), closure: controller.switchPage)
    action(id: 'bilyetGiro', name: 'Giro', actionCommandKey: 'bilyetGiro', mnemonic: KeyEvent.VK_G,
        smallIcon: imageIcon('/menu_giro.png'), closure: controller.switchPage)
    action(id: 'pencairanPoin', name: 'Poin', actionCommandKey: 'pencairanPoin', mnemonic: KeyEvent.VK_O,
        smallIcon: imageIcon('/menu_pencairan_poin.png'), closure: controller.switchPage)

    action(id: 'produk', name: 'Produk', actionCommandKey: 'produk', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_produk.png'), closure: controller.switchPage)
    action(id: 'transfer', name: 'Transfer', actionCommandKey: 'transfer', mnemonic: KeyEvent.VK_F,
        smallIcon: imageIcon('/menu_transfer.png'), closure: controller.switchPage)
    action(id: 'penyesuaianStok', name: 'Penyesuaian', actionCommandKey: 'penyesuaianStok', mnemonic: KeyEvent.VK_Y,
        smallIcon: imageIcon('/menu_penyesuaianstok.png'), closure: controller.switchPage)

    action(id: 'returJual', name: 'Retur Jual', actionCommandKey: 'returJual', mnemonic: KeyEvent.VK_U,
        smallIcon: imageIcon('/menu_retur_jual.png'), closure: controller.switchPage)

    action(id: 'laporan', name: 'Laporan', actionCommandKey: 'laporan', mnemonic: KeyEvent.VK_L,
        smallIcon: imageIcon('/menu_laporan.png'), closure: controller.switchPage)
    action(id: 'pesan', name: 'Notifikasi', actionCommandKey: 'pesan', mnemonic: KeyEvent.VK_N,
        smallIcon: imageIcon('/menu_notifikasi.png'), closure: controller.switchPage)

    action(id: 'maintenance', name: 'Maintenance', actionCommandKey: 'maintenance', mnemonic: KeyEvent.VK_M,
        smallIcon: imageIcon('/menu_maintenance.png'), closure: popupMaintenance)
    action(id: 'konsumen', name: 'Konsumen', actionCommandKey: 'konsumen', mnemonic: KeyEvent.VK_K,
        smallIcon: imageIcon('/menu_maintenance_konsumen.png'), closure: controller.switchPage)
    action(id: 'sales', name: 'Sales', actionCommandKey: 'sales', mnemonic: KeyEvent.VK_L,
        smallIcon: imageIcon('/menu_maintenance_sales.png'), closure: controller.switchPage)
    action(id: 'region', name: 'Region', actionCommandKey: 'region', mnemonic: KeyEvent.VK_E,
        smallIcon: imageIcon('/menu_maintenance_region.png'), closure: controller.switchPage)
    action(id: 'gudang', name: 'Gudang', actionCommandKey: 'gudang', mnemonic: KeyEvent.VK_G,
        smallIcon: imageIcon('/menu_maintenance_gudang.png'), closure: controller.switchPage)
    action(id: 'supplier', name: 'Supplier', actionCommandKey: 'supplier', mnemonic: KeyEvent.VK_S,
        smallIcon: imageIcon('/menu_maintenance_supplier.png'), closure: controller.switchPage)
    action(id: 'satuan', name: 'Satuan', actionCommandKey: 'satuan', mnemonic: KeyEvent.VK_A,
        smallIcon: imageIcon('/menu_maintenance_satuan.png'), closure: controller.switchPage)
    action(id: 'pengaturan', name: 'Pengaturan', actionCommandKey: 'pengaturan', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_maintenance_pengaturan.png'), closure: controller.switchPage)
    action(id: 'backup', name: 'Backup', actionCommandKey: 'backup', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_maintenance_backup.png'), closure: controller.switchPage)
    action(id: 'restore', name: 'Restore', actionCommandKey: 'restore', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_maintenance_restore.png'), closure: controller.switchPage)
    action(id: 'user', name: 'User', actionCommandKey: 'user', mnemonic: KeyEvent.VK_U,
        smallIcon: imageIcon('/menu_maintenance_user.png'), closure: controller.switchPage)
}

application(id: 'mainFrame',
        title: "${app.config.application.title} ${app.metadata.getApplicationVersion()}",
        extendedState: JFrame.MAXIMIZED_BOTH,
        pack: true,
        iconImage: imageIcon('/icon.png').image,
        locationByPlatform: true) {

    popupMenu(id: "maintenancePopup") {
        menuItem(action: konsumen)
        menuItem(action: sales)
        menuItem(action: region)
        separator(border: BorderFactory.createEmptyBorder(3,0,3,0))
        menuItem(action: supplier)
        menuItem(action: gudang)
        menuItem(action: satuan)
        separator(border: BorderFactory.createEmptyBorder(3,0,3,0))
        menuItem(action: pengaturan)
        menuItem(action: backup)
        menuItem(action: restore)
        menuItem(action: user)
    }

    popupMenu(id: 'pengeluaranBarangPopup') {
        menuItem(action: pengeluaranBarangEceran)
        menuItem(action: pengiriman)
        menuItem(action: pengeluaranReturJual)
    }

    popupMenu(id: 'penjualanPopup') {
        menuItem(action: fakturJualEceran)
        menuItem(action: fakturJualOlehSales)
    }

    borderLayout()
    jxlayer(UI: BusyLayerUI.instance, constraints: BorderLayout.CENTER) {
        panel() {
            borderLayout()

            toolBar(id: 'toolbar', constraints: BorderLayout.PAGE_START) {
                buttonGroup(id: 'buttons')
                toggleButton(buttonGroup: buttons, action: penerimaanBarang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.penerimaanBarangVisible})
                toggleButton(buttonGroup: buttons, action: pengeluaranBarang, id: 'pengeluaranBarangButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.pengeluaranBarangVisible})
                toggleButton(buttonGroup: buttons, action: buktiTerima, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.buktiTerimaVisible})
                separator()
                toggleButton(buttonGroup: buttons, action: purchaseOrder, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.purchaseOrderVisible})
                toggleButton(buttonGroup: buttons, action: fakturBeli, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.fakturBeliVisible})
                toggleButton(buttonGroup: buttons, action: fakturJual, id: 'fakturJualButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.fakturJualVisible})
                toggleButton(buttonGroup: buttons, action: hutang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.hutangVisible})
                toggleButton(buttonGroup: buttons, action: piutang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.piutangVisible})
                toggleButton(buttonGroup: buttons, action: bilyetGiro, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.giroVisible})
                toggleButton(buttonGroup: buttons, action: pencairanPoin, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.pencairanPoinVisible})
                separator()
                toggleButton(buttonGroup: buttons, action: produk, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.produkVisible})
                toggleButton(buttonGroup: buttons, action: transfer, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.transferVisible})
                toggleButton(buttonGroup: buttons, action: penyesuaianStok, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.penyesuaianStokVisible})
                separator()
                toggleButton(buttonGroup: buttons, action: returJual, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.returJualVisible})
                separator()
                toggleButton(buttonGroup: buttons, action: laporan, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.laporanVisible})
                toggleButton(buttonGroup: buttons, action: pesan, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.pesanVisible})
                toggleButton(buttonGroup: buttons, action: maintenance, id: 'maintenanceButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    visible: bind {model.maintenanceVisible})
            }

            panel(id: "mainPanel", constraints: BorderLayout.CENTER) {
                borderLayout()
            }

            statusBar(constraints: BorderLayout.PAGE_END, border: BorderFactory.createBevelBorder(BevelBorder.LOWERED)) {
                label(text:bind {model.status})
            }
        }
    }
}

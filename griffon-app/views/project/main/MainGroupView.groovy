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



package project.main

import org.jdesktop.swingx.JXStatusBar
import project.pembelian.POViewMode
import project.penjualan.FakturEceranViewMode
import project.retur.ReturBeliViewMode
import project.retur.ReturJualViewMode
import util.BusyLayerUI
import simplejpa.swing.MainTabbedPane
import util.ScrollableToolBar
import javax.swing.*
import javax.swing.border.*
import java.awt.*
import java.awt.event.*

def popupMaintenance = {
    maintenancePopup.show(maintenanceButton, 0, maintenanceButton.getHeight())
}

def popupPenerimaanBarang = {
    penerimaanBarangPopup.show(penerimaanBarangButton, 0, penerimaanBarangButton.getHeight())
}

def popupPengeluaranBarang = {
    pengeluaranBarangPopup.show(pengeluaranBarangButton, 0, pengeluaranBarangButton.getHeight())
}

def popupPenjualan = {
    penjualanPopup.show(fakturJualButton, 0, fakturJualButton.getHeight())
}

def popupPoin = {
    poinPopup.show(poinButton, 0, poinButton.getHeight())
}

def popupReturJual = {
    returJualPopup.show(returJualButton, 0, returJualButton.getHeight())
}

actions {
    action(id: 'penerimaanBarang', name: 'Terima Barang', smallIcon: imageIcon('/menu_penerimaan_barang.png'),
        mnemonic: KeyEvent.VK_T, closure: popupPenerimaanBarang)
    action(id: 'penerimaanPembelian', name: 'Terima Pembelian', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_penerimaan_po.png'), closure: controller.switchPage.rcurry([mode: POViewMode.PENERIMAAN]))
    action(id: 'penerimaanReturBeli', name: 'Retur Beli', actionCommandKey: 'returBeli', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_penerimaan_retur_beli.png'), closure: controller.switchPage.rcurry([mode: ReturBeliViewMode.PENERIMAAN]))
    action(id: 'pengeluaranBarang', name: 'Antar Barang', actionCommandKey: 'pengeluaranBarang', mnemonic: KeyEvent.VK_A,
        smallIcon: imageIcon('/menu_pengeluaran.png'), closure: popupPengeluaranBarang)
    action(id: 'pengeluaranBarangEceran', name: 'Antar Eceran', actionCommandKey: 'fakturJualEceran', mnemonic: KeyEvent.VK_E,
        smallIcon: imageIcon('/menu_pengeluaran_eceran.png'), closure: controller.switchPage.rcurry([mode: FakturEceranViewMode.PENGELUARAN]))
    action(id: 'pengiriman', name: 'Antar Dalam Kota', actionCommandKey: 'pengiriman', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_pengeluaran_sales.png'), closure: controller.switchPage)
    action(id: 'pengeluaranReturJualEceran', name: 'Antar Retur Jual Eceran', actionCommandKey: 'returJualEceran', mnemonic: KeyEvent.VK_C,
        smallIcon: imageIcon('/menu_pengeluaran_retur_jual_sales.png'), closure: controller.switchPage.rcurry([mode: ReturJualViewMode.PENGELUARAN]))
    action(id: 'pengeluaranReturJualSales', name: 'Antar Retur Jual Sales', actionCommandKey: 'returJualOlehSales', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_pengeluaran_retur_jual_eceran.png'), closure: controller.switchPage.rcurry([mode: ReturJualViewMode.PENGELUARAN]))
    action(id: 'buktiTerima', name: 'Bukti Terima', actionCommandKey: 'buktiTerima', mnemonic: KeyEvent.VK_T,
        smallIcon: imageIcon('/menu_bukti_terima.png'), closure: controller.switchPage)

    action(id: 'purchaseOrder', name: 'Purchase Order', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_purchaseorder.png'), closure: controller.switchPage.rcurry([mode: POViewMode.ALL]))
    action(id: 'fakturBeli', name: 'Pembelian', actionCommandKey: 'purchaseOrder', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_pembelian.png'), closure: controller.switchPage.rcurry([mode: POViewMode.FAKTUR_BELI]))
    action(id: 'fakturJual', name: 'Penjualan', actionCommandKey: 'penjualan', mnemonic: KeyEvent.VK_J,
        smallIcon: imageIcon('/menu_penjualan.png'), closure: popupPenjualan)
    action(id: 'fakturJualEceran', name: 'Eceran', actionCommandKey: 'fakturJualEceran', mnemonic: KeyEvent.VK_E,
        smallIcon: imageIcon('/menu_penjualan_eceran.png'), closure: controller.switchPage.rcurry([mode: FakturEceranViewMode.FAKTUR]))
    action(id: 'fakturJualOlehSales', name: 'Sales', actionCommandKey: 'fakturJualOlehSales', mnemonic: KeyEvent.VK_S,
        smallIcon: imageIcon('/menu_penjualan_sales.png'), closure: controller.switchPage)
    action(id: 'hutang', name: 'Hutang', actionCommandKey: 'hutang', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_hutang.png'), closure: controller.switchPage)
    action(id: 'piutang', name: 'Piutang', actionCommandKey: 'piutang', mnemonic: KeyEvent.VK_I,
        smallIcon: imageIcon('/menu_piutang.png'), closure: controller.switchPage)
    action(id: 'bilyetGiro', name: 'Giro', actionCommandKey: 'bilyetGiro', mnemonic: KeyEvent.VK_G,
        smallIcon: imageIcon('/menu_giro.png'), closure: controller.switchPage)
    action(id: 'poin', name: 'Poin', smallIcon: imageIcon('/menu_poin.png'), closure: popupPoin)
    action(id: 'pencairanPoin', name: 'Pencairan', actionCommandKey: 'pencairanPoin', mnemonic: KeyEvent.VK_O,
        smallIcon: imageIcon('/menu_pencairan_poin.png'), closure: controller.switchPage)
    action(id: 'riwayatPoin', name: 'Riwayat', actionCommandKey: 'riwayatPoin', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_riwayat_poin.png'), closure: controller.switchPage)

    action(id: 'produk', name: 'Produk', actionCommandKey: 'produk', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_produk.png'), closure: controller.switchPage)
    action(id: 'transfer', name: 'Transfer', actionCommandKey: 'transfer', mnemonic: KeyEvent.VK_F,
        smallIcon: imageIcon('/menu_transfer.png'), closure: controller.switchPage)
    action(id: 'penyesuaianStok', name: 'Penyesuaian', actionCommandKey: 'penyesuaianStok', mnemonic: KeyEvent.VK_Y,
        smallIcon: imageIcon('/menu_penyesuaianstok.png'), closure: controller.switchPage)

    action(id: 'returJual', name: 'Retur Jual', actionCommandKey: 'returJual', mnemonic: KeyEvent.VK_U,
        smallIcon: imageIcon('/menu_retur_jual.png'), closure: popupReturJual)
    action(id: 'returJualEceran', name: 'Retur Jual Eceran', actionCommandKey: 'returJualEceran', mnemonic: KeyEvent.VK_E,
            smallIcon: imageIcon('/menu_penjualan_eceran.png'), closure: controller.switchPage)
    action(id: 'returJualOlehSales', name: 'Retur Jual Sales', actionCommandKey: 'returJualOlehSales', mnemonic: KeyEvent.VK_S,
            smallIcon: imageIcon('/menu_penjualan_sales.png'), closure: controller.switchPage)
    action(id: 'returBeli', name: 'Retur Beli', actionCommandKey: 'returBeli', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_retur_beli.png'), closure: controller.switchPage)

    action(id: 'servis', name: 'Servis', actionCommandKey: 'servis', mnemonic: KeyEvent.VK_V,
        smallIcon: imageIcon('/menu_servis.png'), closure: controller.switchPage)
    action(id: 'penerimaanServis', name: 'Penerimaan Servis', actionCommandKey: 'penerimaanServis',
        smallIcon: imageIcon('/menu_penerimaan_servis.png'), closure: controller.switchPage)
    action(id: 'kas', name: 'Kas', actionCommandKey: 'kas', mnemonic: KeyEvent.VK_H,
        smallIcon: imageIcon('/menu_transaksi_kas.png'), closure: controller.switchPage)

    action(id: 'laporan', name: 'Laporan', actionCommandKey: 'laporan', mnemonic: KeyEvent.VK_L,
        smallIcon: imageIcon('/menu_laporan.png'), closure: controller.switchPage)

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
    action(id: 'kategoriKas', name: 'Kategori Kas', actionCommandKey: 'kategoriKas', mnemonic: KeyEvent.VK_I,
        smallIcon: imageIcon('/menu_maintenance_kategori_kas.png'), closure: controller.switchPage)
    action(id: 'jenisTransaksiKas', name: 'Jenis Transaksi', actionCommandKey: 'jenisTransaksiKas', mnemonic: KeyEvent.VK_J,
        smallIcon: imageIcon('/menu_maintenance_jenis_transaksi.png'), closure: controller.switchPage)
    action(id: 'pengaturan', name: 'Pengaturan', actionCommandKey: 'pengaturan', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_maintenance_pengaturan.png'), closure: controller.switchPage)
    action(id: 'backup', name: 'Backup', actionCommandKey: 'backup', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_maintenance_backup.png'), closure: controller.switchPage)
    action(id: 'restore', name: 'Restore', actionCommandKey: 'restore', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_maintenance_restore.png'), closure: controller.switchPage)
    action(id: 'user', name: 'User', actionCommandKey: 'user', mnemonic: KeyEvent.VK_U,
        smallIcon: imageIcon('/menu_maintenance_user.png'), closure: controller.switchPage)

    action(id: 'pesan', name: '<html><strong>Ada pesan notifikasi yang belum dibaca!</strong></html>',
        actionCommandKey: 'pesan', smallIcon: imageIcon('/warning.png'), closure: controller.switchPage)
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
        menuItem(action: kategoriKas)
        menuItem(action: jenisTransaksiKas)
        separator(border: BorderFactory.createEmptyBorder(3,0,3,0))
        menuItem(action: pengaturan)
        menuItem(action: backup)
        menuItem(action: restore)
        menuItem(action: user)
    }

    popupMenu(id: 'penerimaanBarangPopup') {
        menuItem(action: penerimaanPembelian)
        menuItem(action: penerimaanReturBeli)
    }

    popupMenu(id: 'pengeluaranBarangPopup') {
        menuItem(action: pengeluaranBarangEceran)
        menuItem(action: pengiriman)
        menuItem(action: pengeluaranReturJualSales)
        menuItem(action: pengeluaranReturJualEceran)
    }

    popupMenu(id: 'penjualanPopup') {
        menuItem(action: fakturJualEceran)
        menuItem(action: fakturJualOlehSales)
    }

    popupMenu(id: 'returJualPopup') {
        menuItem(action: returJualEceran)
        menuItem(action: returJualOlehSales)
    }

    popupMenu(id: 'poinPopup') {
        menuItem(action: pencairanPoin)
        menuItem(action: riwayatPoin)
    }

    borderLayout()
    jxlayer(UI: BusyLayerUI.instance, constraints: BorderLayout.CENTER) {
        panel() {
            borderLayout()

            container(new ScrollableToolBar(), id: 'toolbar', constraints: BorderLayout.PAGE_START) {
                button(action: penerimaanBarang, id: 'penerimaanBarangButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.penerimaanBarangVisible})
                button(action: pengeluaranBarang, id: 'pengeluaranBarangButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.pengeluaranBarangVisible})
                button(action: buktiTerima, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.buktiTerimaVisible})
                button(action: purchaseOrder, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.purchaseOrderVisible})
                button(action: fakturBeli, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.fakturBeliVisible})
                button(action: fakturJual, id: 'fakturJualButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.fakturJualVisible})
                button(action: hutang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.hutangVisible})
                button(action: piutang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.piutangVisible})
                button(action: bilyetGiro, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.giroVisible})
                button(action: poin, id: 'poinButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.poinVisible})
                button(action: produk, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.produkVisible})
                button(action: transfer, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.transferVisible})
                button(action: penyesuaianStok, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.penyesuaianStokVisible})
                button(action: returJual, id: 'returJualButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.returJualVisible})
                button(action: returBeli, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.returBeliVisible})
                button(action: servis, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.servisVisible})
                button(action: penerimaanServis, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.penerimaanServisVisible})
                button(action: kas, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.kasVisible})
                button(action: laporan, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.laporanVisible})
                button(action: maintenance, id: 'maintenanceButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER,
                    borderPainted: false, visible: bind {model.maintenanceVisible})
            }

            widget(new MainTabbedPane(), id: "mainTab", constraints: BorderLayout.CENTER)

            statusBar(id: 'statusBar', constraints: BorderLayout.PAGE_END, border: BorderFactory.createBevelBorder(BevelBorder.LOWERED)) {
                label(text:bind {model.status}, constraints: new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL))
                hyperlink(id: 'pesanNotifikasi', visible: false, action: pesan)
            }
        }
    }
}

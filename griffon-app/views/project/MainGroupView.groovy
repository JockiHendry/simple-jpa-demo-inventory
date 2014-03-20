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



package project

import util.BusyLayerUI

import javax.swing.*
import javax.swing.border.*
import java.awt.*
import java.awt.event.*

def popupMaintenance = {
    maintenancePopup.show(maintenanceButton, 0, maintenanceButton.getHeight())
}

actions {
    action(id: 'penerimaanBarang', name: 'Terima Barang', actionCommandKey: 'penerimaanBarang', mnemonic: KeyEvent.VK_T,
        smallIcon: imageIcon('/menu_penerimaan_barang.png'), closure: controller.switchPage)

    action(id: 'produk', name: 'Produk', actionCommandKey: 'produk', mnemonic: KeyEvent.VK_P,
        smallIcon: imageIcon('/menu_produk.png'), closure: controller.switchPage)

    action(id: 'maintenance', name: 'Maintenance', actionCommandKey: 'maintenance', mnemonic: KeyEvent.VK_M,
        smallIcon: imageIcon('/menu_maintenance.png'), closure: popupMaintenance)
    action(id: 'gudang', name: 'Gudang', actionCommandKey: 'gudang', mnemonic: KeyEvent.VK_G,
        smallIcon: imageIcon('/menu_maintenance_gudang.png'), closure: controller.switchPage)
    action(id: 'supplier', name: 'Supplier', actionCommandKey: 'supplier', mnemonic: KeyEvent.VK_S,
        smallIcon: imageIcon('/menu_maintenance_supplier.png'), closure: controller.switchPage)
    action(id: 'backup', name: 'Backup', actionCommandKey: 'backup', mnemonic: KeyEvent.VK_B,
        smallIcon: imageIcon('/menu_maintenance_backup.png'), closure: controller.switchPage)
    action(id: 'restore', name: 'Restore', actionCommandKey: 'restore', mnemonic: KeyEvent.VK_R,
        smallIcon: imageIcon('/menu_maintenance_restore.png'), closure: controller.switchPage)
}

application(id: 'mainFrame',
        title: "${app.config.application.title} ${app.metadata.getApplicationVersion()}",
        extendedState: JFrame.MAXIMIZED_BOTH,
        pack: true,
        locationByPlatform: true) {

    popupMenu(id: "maintenancePopup") {
        menuItem(action: supplier)
        menuItem(action: gudang)
        separator(border: BorderFactory.createEmptyBorder(3,0,3,0))
        menuItem(action: backup)
        menuItem(action: restore)
    }

    borderLayout()
    jxlayer(UI: BusyLayerUI.instance, constraints: BorderLayout.CENTER) {
        panel() {
            borderLayout()

            toolBar(constraints: BorderLayout.PAGE_START) {
                buttonGroup(id: 'buttons')
                toggleButton(buttonGroup: buttons, action: penerimaanBarang, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER)
                separator()
                toggleButton(buttonGroup: buttons, action: produk, verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER)
                toggleButton(buttonGroup: buttons, action: maintenance, id: 'maintenanceButton', verticalTextPosition: SwingConstants.BOTTOM, horizontalTextPosition: SwingConstants.CENTER)
            }

            panel(id: "mainPanel", constraints: BorderLayout.CENTER) {
                cardLayout(id: "cardLayout")
            }

            statusBar(constraints: BorderLayout.PAGE_END, border: BorderFactory.createBevelBorder(BevelBorder.LOWERED)) {
                label('Aplikasi demo inventory dengan Griffon dan plugin simple-jpa.')
            }
        }
    }
}

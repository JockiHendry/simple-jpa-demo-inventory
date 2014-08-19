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
import domain.Container
import domain.pengaturan.KeyPengaturan
import domain.user.Menu
import domain.user.User
import org.jdesktop.swingx.JXLoginPane
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil
import util.HttpUtil
import util.SplashScreen
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import java.awt.Font

/*
 * This script is executed inside the UI thread, so be sure to  call
 * long running code in another thread.
 *
 * You have the following options
 * - execOutsideUI { // your code }
 * - execFuture { // your code }
 * - Thread.start { // your code }
 *
 * You have the following options to run code again inside the UI thread
 * - execInsideUIAsync { // your code }
 * - execInsideUISync { // your code }
 */

execOutsideUI {
    // Create listener
    ServiceManager serviceManager = app.serviceManager
    serviceManager.findService('BilyetGiroEventListener')
    serviceManager.findService('InventoryEventListener')

    // Create repository
    PengaturanRepository pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan')
    pengaturanRepository.refreshAll()
    Container.app.nomorService.refreshAll()

    // Mengubah ukuran huruf bila diperlukan
    execInsideUISync {
        def ukuranHuruf = pengaturanRepository.getValue(KeyPengaturan.UKURAN_HURUF_TABEL)
        if (ukuranHuruf > 0) {
            UIManager.put('Table.font', new FontUIResource(new Font('SansSerif', Font.PLAIN, ukuranHuruf)))
            UIManager.put('Table.rowHeight', ukuranHuruf + 1)
        }
    }

    // Clearing bilyet giro yang jatuh tempo
    Container.app.bilyetGiroClearingService.periksaJatuhTempo()
}

SplashScreen.instance.dispose()

if (Environment.current != Environment.TEST) {
    JXLoginPane panel = new JXLoginPane(Container.app.userLoginService)
    JXLoginPane.Status status = JXLoginPane.showLoginDialog(app.windowManager.getStartingWindow(), panel)
    if (status != JXLoginPane.Status.SUCCEEDED) {
        app.shutdown()
    }
    MVCGroup mainGroup = app.mvcGroupManager.findGroup('mainGroup')

    User currentUser = Container.app.currentUser
    mainGroup.model.status = "Aplikasi demo inventory dengan Griffon dan plugin simple-jpa |  Selamat datang, ${currentUser.nama}."
    mainGroup.model.penerimaanBarangVisible = currentUser.bolehAkses(Menu.PENERIMAAN_BARANG)
    mainGroup.model.pengeluaranBarangVisible = currentUser.bolehAkses(Menu.PENGELUARAN_BARANG)
    mainGroup.model.buktiTerimaVisible = currentUser.bolehAkses(Menu.BUKTI_TERIMA)
    mainGroup.model.purchaseOrderVisible = currentUser.bolehAkses(Menu.PURCHASE_ORDER)
    mainGroup.model.fakturBeliVisible = currentUser.bolehAkses(Menu.FAKTUR_BELI)
    mainGroup.model.fakturJualVisible = currentUser.bolehAkses(Menu.FAKTUR_JUAL)
    mainGroup.model.hutangVisible = currentUser.bolehAkses(Menu.HUTANG)
    mainGroup.model.piutangVisible = currentUser.bolehAkses(Menu.PIUTANG)
    mainGroup.model.giroVisible = currentUser.bolehAkses(Menu.GIRO)
    mainGroup.model.produkVisible = currentUser.bolehAkses(Menu.PRODUK)
    mainGroup.model.transferVisible = currentUser.bolehAkses(Menu.TRANSFER)
    mainGroup.model.penyesuaianStokVisible = currentUser.bolehAkses(Menu.PENYESUAIAN_STOK)
    mainGroup.model.laporanVisible = currentUser.bolehAkses(Menu.LAPORAN)
    mainGroup.model.maintenanceVisible = currentUser.bolehAkses(Menu.MAINTENANCE)

}

execOutsideUI {
    HttpUtil.instance.sendNotification(Container.app.currentUser?.nama, "Startup...")
}
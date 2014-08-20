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
import domain.pengaturan.KeyPengaturan
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
    // Create listener and init services
    ServiceManager serviceManager = app.serviceManager
    serviceManager.findService('BilyetGiroEventListener')
    serviceManager.findService('InventoryEventListener')
    serviceManager.findService('Nomor')

    // Create repository
    PengaturanRepository pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan')
    pengaturanRepository.refreshAll()

    // Mengubah ukuran huruf bila diperlukan
    execInsideUISync {
        def ukuranHuruf = pengaturanRepository.getValue(KeyPengaturan.UKURAN_HURUF_TABEL)
        if (ukuranHuruf > 0) {
            UIManager.put('Table.font', new FontUIResource(new Font('SansSerif', Font.PLAIN, ukuranHuruf)))
            UIManager.put('Table.rowHeight', ukuranHuruf + 1)
        }
    }

    // Clearing bilyet giro yang jatuh tempo
    serviceManager.findService('BilyetGiroClearing').periksaJatuhTempo()
}

SplashScreen.instance.dispose()

execOutsideUI {
    HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Startup...")
}
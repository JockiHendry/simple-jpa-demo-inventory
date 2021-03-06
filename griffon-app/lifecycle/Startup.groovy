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

import domain.general.User
import domain.pengaturan.KeyPengaturan
import org.joda.time.LocalDateTime
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil
import simplejpa.swing.DialogUtils
import util.HttpUtil
import util.SplashScreen
import javax.swing.JOptionPane
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
    serviceManager.findService('ReturJualEventListener')
    serviceManager.findService('LabaRugiEventListener')
    serviceManager.findService('Nomor')
    serviceManager.findService('LabaRugi')

    // Create repository
    PengaturanRepository pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('pengaturan')
    pengaturanRepository.refreshAll()
    SimpleJpaUtil.instance.repositoryManager.findRepository('TemplateFaktur').refreshAll()

    // Mengubah ukuran huruf bila diperlukan
    execInsideUISync {
        def ukuranHuruf = pengaturanRepository.getValue(KeyPengaturan.UKURAN_HURUF_TABEL)
        if (ukuranHuruf > 0) {
            UIManager.put('Table.font', new FontUIResource(new Font('SansSerif', Font.PLAIN, ukuranHuruf)))
            UIManager.put('Table.rowHeight', ukuranHuruf + 1)
        }
    }
}

SplashScreen.instance.dispose()

execOutsideUI {
    HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Version ${app?.metadata?.getApplicationVersion()} Startup...")
}

Thread.startDaemon('PemeriksaTanggal', {
    //noinspection GroovyInfiniteLoopStatement
    while (true) {
        User user = SimpleJpaUtil.instance.user
        if (user && LocalDateTime.now().isBefore(user.loginTerakhir)) {
            DialogUtils.message(null, "Waktu sistem (${LocalDateTime.now().toString('dd-MM-YYYY HH:mm:ss')}) tidak boleh sebelum waktu login (${user.loginTerakhir.toString('dd-MM-YYYY HH:mm:ss')})",
                'Kesalahan Tanggal', JOptionPane.ERROR_MESSAGE)
            app.shutdown()
        }
        Thread.sleep(2000)
    }
})
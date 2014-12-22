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
import daemon.ScheduledTask
import project.daemon.DaemonService
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil
import util.HttpUtil
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

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

// Start daemon
HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Daemon ${app?.metadata?.getApplicationVersion()} Startup...")
DaemonService daemonService = serviceManager.findService('Daemon')
ScheduledThreadPoolExecutor scheduler = Executors.newScheduledThreadPool(2)
scheduler.continueExistingPeriodicTasksAfterShutdownPolicy = true
scheduler.executeExistingDelayedTasksAfterShutdownPolicy = true

//
// Task     : Periksa Jatuh Tempo
// Jadwal   : Setiap 7 jam.
//
scheduler.scheduleAtFixedRate(new ScheduledTask().action { daemonService.periksaJatuhTempo() }, 1, 7 * 60, TimeUnit.MINUTES)
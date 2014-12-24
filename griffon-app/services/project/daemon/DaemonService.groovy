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
package project.daemon

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.penjualan.BilyetGiroService
import project.penjualan.PiutangService
import project.user.PesanRepository
import simplejpa.AuditableUser
import simplejpa.SimpleJpaUtil
import util.HttpUtil

class DaemonService {

    final Logger log = LoggerFactory.getLogger(DaemonService)

    BilyetGiroService bilyetGiroService
    PiutangService piutangService
    PesanRepository pesanRepository

    @SuppressWarnings("GroovyUnusedDeclaration")
    void serviceInit() {
        // Setup user
        SimpleJpaUtil.instance.user = new AuditableUser() {
            @Override
            String getUserName() {
                "daemon"
            }
        }
    }

    void periksaJatuhTempo() {
        HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Mulai melakukan pemeriksaan jatuh tempo.")
        log.info "Mulai melakukan pemeriksaan jatuh tempo."
        bilyetGiroService.periksaJatuhTempo()
        piutangService.periksaJatuhTempo()
        log.info "Selesai melakukan pemeriksaan jatuh tempo."
        HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Selesai melakukan pemeriksaan jatuh tempo.")
    }

    void refreshPesan() {
        HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Mulai men-refresh pesan.")
        log.info "Mulai melakukan refresh pesan."
        pesanRepository.refresh()
        log.info "Selesai melakukan refresh pesan."
        HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, "Selesai men-refresh pesan.")
    }

}

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
package project.penjualan

import domain.faktur.BilyetGiro
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class BilyetGiroClearingService {

    final Logger log = LoggerFactory.getLogger(BilyetGiroClearingService)

    public void periksaJatuhTempo() {
        log.debug "Memeriksa bilyet giro yang sudah jatuh tempo..."
        findAllBilyetGiroByDsl {
            tanggalPencairan isNull()
            and()
            jatuhTempo lt(LocalDate.now())
        }.each { BilyetGiro bg ->
            log.info "Memproses bilyet giro yang jatuh tempo: $bg"
            bg.cairkan(LocalDate.now())
        }
        log.debug "Pemeriksaan selesai!"
    }

}

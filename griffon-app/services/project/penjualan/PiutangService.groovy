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

import domain.penjualan.FakturJualOlehSales
import domain.user.PesanPiutangJatuhTempo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.user.PesanRepository
import simplejpa.transaction.Transaction

class PiutangService {

    final Logger log = LoggerFactory.getLogger(PiutangService)

    PesanRepository pesanRepository
    FakturJualRepository fakturJualRepository

    void periksaJatuhTempo() {
        log.debug "Memeriksa piutang yang sudah jatuh tempo..."
        fakturJualRepository.cariPiutang(null, null, null, null, FakturJualRepository.StatusPiutangSearch.AKAN_JATUH_TEMPO).each { FakturJualOlehSales f ->
            log.info "Memproses faktur jual yang akan jatuh tempo: ${f.nomor}"
            pesanRepository.buat(new PesanPiutangJatuhTempo(f))
        }
        log.debug "Pemeriksaan selesai!"
    }

}

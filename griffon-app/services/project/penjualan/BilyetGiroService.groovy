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

import domain.exception.DataTidakBolehDiubah
import domain.faktur.BilyetGiro
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJualOlehSales
import domain.general.PesanGiroJatuhTempo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.faktur.BilyetGiroRepository
import project.user.PesanRepository
import simplejpa.transaction.Transaction

@Transaction
class BilyetGiroService {

    final Logger log = LoggerFactory.getLogger(BilyetGiroService)

    PesanRepository pesanRepository
    BilyetGiroRepository bilyetGiroRepository

    public void periksaJatuhTempo() {
        log.debug "Memeriksa bilyet giro yang sudah jatuh tempo..."
        bilyetGiroRepository.cariJatuhTempo().each { BilyetGiro bg ->
            log.info "Memproses bilyet giro yang jatuh tempo: $bg"
            pesanRepository.buat(new PesanGiroJatuhTempo(bg))
        }
        log.debug "Pemeriksaan selesai!"
    }

    public List<FakturJualOlehSales> cariFakturJualYangDibayarDengan(BilyetGiro bilyetGiro) {
        executeQuery("SELECT DISTINCT f FROM FakturJualOlehSales f, IN(f.piutang.listPembayaran) b WHERE b.bilyetGiro = :bilyetGiro AND f.deleted <> 'Y' ORDER BY f.nomor",
            [:], [bilyetGiro: bilyetGiro])
    }

    public List<PurchaseOrder> cariPurchaseOrderYangDibayarDengan(BilyetGiro bilyetGiro) {
        executeQuery("SELECT DISTINCT p FROM PurchaseOrder p, IN(p.fakturBeli.hutang.listPembayaran) b WHERE b.bilyetGiro = :bilyetGiro AND p.deleted <> 'Y' ORDER BY p.nomor",
            [:], [bilyetGiro: bilyetGiro])
    }

    public BilyetGiro hapus(BilyetGiro bilyetGiro) {
        bilyetGiro = findBilyetGiroById(bilyetGiro.id)
        if (bilyetGiro.sudahDicairkan()) {
            throw new DataTidakBolehDiubah('Giro yang sudah dicairkan tidak boleh dihapus!', bilyetGiro)
        }
        List refFakturJual = cariFakturJualYangDibayarDengan(bilyetGiro)
        List refPurchaseOrder = cariPurchaseOrderYangDibayarDengan(bilyetGiro)
        if (!refFakturJual.empty || !refPurchaseOrder.empty) {
            throw new DataTidakBolehDiubah("Giro tidak boleh dihapus karena dipakai sebagai referensi pembayaran:\n" +
                "${refFakturJual.collect{it.nomor}.join(', ')}" + "${refPurchaseOrder.collect{it.nomor}.join(', ')}", bilyetGiro)
        }
        bilyetGiro.deleted = 'Y'
        bilyetGiro
    }

}

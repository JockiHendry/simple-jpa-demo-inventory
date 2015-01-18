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
package listener

import domain.event.BayarPiutang
import domain.exception.FakturTidakDitemukan
import domain.faktur.Referensi
import domain.penjualan.FakturJualOlehSales
import domain.retur.ReturJual
import domain.retur.ReturJualOlehSales
import project.retur.ReturJualRepository
import simplejpa.transaction.Transaction

@SuppressWarnings("GroovyUnusedDeclaration")
@Transaction
class ReturJualEventListenerService {

    ReturJualRepository returJualRepository

    void onBayarPiutang(BayarPiutang bayarPiutang) {
        Referensi referensi = bayarPiutang.pembayaran.referensi

        // Apakah perlu memproses event ini?
        if ((!referensi) || (referensi?.namaClass != ReturJual.simpleName)) return

        ReturJualOlehSales retur = findReturJualOlehSalesByNomor(referensi.nomor)
        if (!retur) {
            throw new FakturTidakDitemukan(referensi.nomor)
        }
        FakturJualOlehSales faktur = bayarPiutang.faktur
        if (bayarPiutang.hapus) {
            retur.hapusReferensiFaktur(faktur.nomor)
        } else {
            retur.fakturPotongPiutang << new Referensi(FakturJualOlehSales, faktur.nomor)
        }
    }

}

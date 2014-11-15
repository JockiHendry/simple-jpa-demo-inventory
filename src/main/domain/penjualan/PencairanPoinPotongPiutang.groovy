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
package domain.penjualan

import domain.faktur.Referensi
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*

@DomainClass @Entity @Canonical
class PencairanPoinPotongPiutang extends PencairanPoin {

    @Transient
    FakturJualOlehSales fakturJualOlehSales

    @Override
    boolean valid() {
        konsumen.jumlahPiutang() >= getNominal()
    }

    @Override
    void proses() {
        if (fakturJualOlehSales) {
            konsumen.potongPiutang(getNominal(),fakturJualOlehSales, new Referensi(PencairanPoin, nomor))
        } else {
            konsumen.potongPiutang(getNominal(), new Referensi(PencairanPoin, nomor))
        }
    }

    @Override
    void hapus() {
        throw new UnsupportedOperationException('Untuk saat ini, proses hapus pencairan poin untuk potong piutang belum di-implementasi-kan!')
    }
}


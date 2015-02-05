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
package domain.penjualan

import domain.event.TransaksiSistem
import domain.labarugi.KATEGORI_SISTEM
import simplejpa.DomainClass
import javax.persistence.*
import org.joda.time.*
import griffon.util.*

@DomainClass @Entity
class PencairanPoinTukarUang extends PencairanPoin {

    public PencairanPoinTukarUang() {}

    @SuppressWarnings("GroovyUnusedDeclaration")
    public PencairanPoinTukarUang(LocalDate tanggal, Integer jumlahPoin, BigDecimal rate) {
        super(tanggal, jumlahPoin, rate)
    }

    @Override
    boolean valid() {
        true
    }

    @Override
    void proses() {
        ApplicationHolder.application?.event(new TransaksiSistem(getNominal(), nomor, KATEGORI_SISTEM.PENGELUARAN_LAIN))
    }

    @Override
    void hapus() {
        ApplicationHolder.application?.event(new TransaksiSistem(getNominal(), nomor, KATEGORI_SISTEM.PENGELUARAN_LAIN, true))
    }

}


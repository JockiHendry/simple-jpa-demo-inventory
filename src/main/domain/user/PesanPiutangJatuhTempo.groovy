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
package domain.user

import domain.penjualan.FakturJualOlehSales
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class PesanPiutangJatuhTempo extends Pesan {

    @NotNull @ManyToOne
    FakturJualOlehSales faktur

    PesanPiutangJatuhTempo() {}

    PesanPiutangJatuhTempo(FakturJualOlehSales faktur) {
        this.tanggal = LocalDateTime.now()
        this.faktur = faktur
        this.pesan = "Faktur <span class='info'>${faktur.nomor}</span> akan segera jatuh tempo pada tanggal <span class='info'>${faktur.jatuhTempo.toString('dd-MM-YYYY')}</span>."
    }

    @Override
    boolean masihBerlaku() {
        !faktur.piutang.lunas
    }

    @Override
    String jenisPesan() {
        "Piutang Jatuh Tempo"
    }
}


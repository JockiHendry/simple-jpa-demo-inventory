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
package domain.retur

import domain.faktur.Referensi
import domain.inventory.Gudang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class ReturJualOlehSales extends ReturJual {

    @NotNull @ManyToOne
    Gudang gudang

    @NotNull @ManyToOne
    Konsumen konsumen

    Set<KlaimPotongPiutang> getKlaimsPotongPiutang(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimPotongPiutang, hanyaBelumDiproses)
    }

    BigDecimal jumlahPotongPiutang() {
        getKlaimsPotongPiutang().sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    void potongPiutang() {
        if (konsumen==null) {
            throw new UnsupportedOperationException("Konsumen untuk [$this] harus di-isi sebelum melakukan pemotongan piutang!")
        }
        Referensi referensi = new Referensi(nomor, ReturJual)
        getKlaimsPotongPiutang(true).each { KlaimPotongPiutang k ->
            konsumen.potongPiutang(k.jumlah, referensi)
            proses(k)
        }
    }

    BigDecimal sisaPotongPiutang() {
        getKlaimsPotongPiutang(true).sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    void prosesKlaimPotongPiutang() {
        getKlaimsPotongPiutang(true).each { it.proses() }
    }

    PengeluaranBarang tukar() {
        super.tukar(gudang, konsumen.nama)
    }
}


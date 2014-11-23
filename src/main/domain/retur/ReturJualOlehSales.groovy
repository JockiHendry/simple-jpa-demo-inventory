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

import domain.exception.FakturTidakDitemukan
import domain.faktur.Referensi
import domain.inventory.BolehPesanStok
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical
class ReturJualOlehSales extends ReturJual implements BolehPesanStok {

    @NotNull @ManyToOne
    Gudang gudang

    @NotNull @ManyToOne
    Konsumen konsumen

    List<KlaimPotongPiutang> getKlaimsPotongPiutang(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimPotongPiutang, hanyaBelumDiproses)
    }

    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name='returjual_fakturs')
    Set<Referensi> fakturPotongPiutang = [] as Set

    void tambah(Referensi referensi) {
        fakturPotongPiutang << referensi
    }

    BigDecimal jumlahPotongPiutang() {
        getKlaimsPotongPiutang().sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    void potongPiutang() {
        if (konsumen==null) {
            throw new UnsupportedOperationException("Konsumen untuk [$this] harus di-isi sebelum melakukan pemotongan piutang!")
        }
        Referensi referensi = new Referensi(ReturJual, nomor)
        getKlaimsPotongPiutang(true).each { KlaimPotongPiutang k ->
            Set daftarFaktur = konsumen.potongPiutang(k.jumlah, referensi)
            fakturPotongPiutang.addAll(daftarFaktur)
            proses(k)
        }
    }

    BigDecimal sisaPotongPiutang() {
        getKlaimsPotongPiutang(true).sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    PengeluaranBarang tukar() {
        super.tukar(gudang, konsumen.nama, gudang.utama)
    }

    @Override
    boolean isBolehPesanStok() {
        gudang.utama
    }

    @Override
    List<ItemBarang> yangDipesan() {
        yangHarusDitukar().items
    }

    void hapusReferensiFaktur(String nomor) {
        Referensi referensi = fakturPotongPiutang.find { it.nomor == nomor }
        if (!referensi) {
            throw new FakturTidakDitemukan(nomor)
        }
        fakturPotongPiutang.remove(referensi)
    }

}


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

import domain.exception.DataTidakKonsisten
import domain.faktur.Referensi
import domain.inventory.BolehPesanStok
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import project.user.NomorService
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import griffon.util.*

@NamedEntityGraphs([
    @NamedEntityGraph(name='ReturJualOlehSales.PengeluaranBarang', attributeNodes=[
        @NamedAttributeNode('pengeluaranBarang'),
    ])
])
@DomainClass @Entity @Canonical
class ReturJualOlehSales extends ReturJual implements BolehPesanStok {

    @NotNull @ManyToOne
    Gudang gudang

    @NotNull @ManyToOne
    Konsumen konsumen

    List<KlaimPotongPiutang> getKlaimsPotongPiutang(boolean hanyaBelumDiproses = false) {
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

    PengeluaranBarang tukar(PengeluaranBarang pengeluaranBarang) {
        // Periksa apakah data pengeluaran barang sesuai dengan retur
        if (!pengeluaranBarang.gudang) {
            pengeluaranBarang.gudang = gudang
        } else if (pengeluaranBarang.gudang != gudang) {
            throw new DataTidakKonsisten('Gudang pada pengiriman harus sama dengan gudang yang tertera di retur!')
        }
        if (!pengeluaranBarang.bagianDari(yangHarusDitukar())) {
            throw new DataTidakKonsisten('Barang yang dikirim harus bagian dari barang yang tertera di retur!')
        }

        if (!pengeluaranBarang.nomor) {
            pengeluaranBarang.nomor = ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG)
        }
        // Periksa bahwa setiap item barang harus sesuai jumlah pada item klaim
        Set<KlaimTukar> seluruhKlaim = getKlaimsTukar(true)
        List<KlaimTukar> yangDiKlaim = []
        pengeluaranBarang.items.each { ItemBarang i ->
            KlaimTukar k = seluruhKlaim.find { (it.produk == i.produk) && (it.jumlah == i.jumlah) }
            if (!k) {
                throw new DataTidakKonsisten("Tidak ada rencana klaim ${i.produk.nama} dengan jumlah persis berupa ${i.jumlah}!")
            } else {
                yangDiKlaim << k
            }
        }

        // Tambahkan pengeluaran barang pada retur ini dan proses klaim yang bersangkutan
        this.pengeluaranBarang << pengeluaranBarang
        pengeluaranBarang.diterima(LocalDate.now(), konsumen.nama, '[Retur Jual]')
        yangDiKlaim.each { proses(it) }

        pengeluaranBarang
    }

    @Override
    boolean isValid() {
        gudang.utama
    }

    @Override
    List<ItemBarang> yangDipesan() {
        yangHarusDitukar().items
    }

}


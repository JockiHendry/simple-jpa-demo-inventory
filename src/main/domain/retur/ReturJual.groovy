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

import domain.event.PerubahanStok
import domain.exception.DataTidakBolehDiubah
import domain.inventory.ItemBarang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import project.user.NomorService
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import griffon.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang')
class ReturJual extends Retur {

    @NotNull @ManyToOne
    Konsumen konsumen

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PengeluaranBarang pengeluaranBarang

    PengeluaranBarang tukar() {
        if (pengeluaranBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        List<KlaimRetur> klaimTukar = getKlaimTukar(true)
        if (klaimTukar.empty) {
            throw new UnsupportedOperationException("Tidak ada penukaran yang dapat dilakukan untuk retur jual [$nomor]")
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: gudang,
            keterangan: "Retur Jual [$nomor]"
        )
        getKlaimTukar(true).each {
            pengeluaranBarang.tambah(new ItemBarang(it.produk, it.jumlah))
            it.sudahDiproses = true
        }
        periksaSelesaiDiproses()
        pengeluaranBarang.diterima(LocalDate.now(), konsumen.nama)
        this.pengeluaranBarang = pengeluaranBarang
        pengeluaranBarang
    }

    void potongPiutang() {
        if (konsumen==null) {
            throw new UnsupportedOperationException("Konsumen untuk [$this] harus di-isi sebelum melakukan pemotongan piutang!")
        }
        getKlaimPotongan(true).each {
            konsumen.potongPiutang(it.potongan)
            it.sudahDiproses = true
        }
        periksaSelesaiDiproses()
    }

    @Override
    int faktor() {
        1
    }
}


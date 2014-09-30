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

import domain.exception.DataTidakBolehDiubah
import domain.faktur.Referensi
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import project.user.NomorService
import simplejpa.DomainClass
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
        List<KlaimTukar> klaimTukar = getKlaim(KlaimTukar, true)
        if (klaimTukar.empty) {
            throw new UnsupportedOperationException("Tidak ada penukaran yang dapat dilakukan untuk retur jual [$nomor]")
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: gudang,
            keterangan: "Retur Jual [$nomor]"
        )
        DaftarBarangSementara hasil = new DaftarBarangSementara() + klaimTukar.collect { it as ItemBarang }
        hasil.items.each { pengeluaranBarang.tambah(it) }
        klaimTukar.each { proses(it) }
        pengeluaranBarang.diterima(LocalDate.now(), konsumen.nama, '[Retur Jual]')
        this.pengeluaranBarang = pengeluaranBarang
        pengeluaranBarang
    }

    void potongPiutang() {
        if (konsumen==null) {
            throw new UnsupportedOperationException("Konsumen untuk [$this] harus di-isi sebelum melakukan pemotongan piutang!")
        }
        Referensi referensi = new Referensi(nomor, ReturJual.simpleName)
        getKlaim(KlaimPotongan, true).each { KlaimPotongan k ->
            konsumen.potongPiutang(k.potongan, referensi)
            proses(k)
        }
    }

    @Override
    int faktor() {
        1
    }

}


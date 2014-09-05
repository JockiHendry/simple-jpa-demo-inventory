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

import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.validation.PenjualanDiterima
import domain.validation.PenjualanOlehSales
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*

import javax.validation.groups.Default

@DomainClass @Entity @Canonical
class PengeluaranBarang extends DaftarBarang {

    @Size(max=150, groups=[Default,PenjualanOlehSales]) @NotNull(groups=[PenjualanOlehSales])
    String alamatTujuan

    @Embedded @NotNull(groups=[PenjualanDiterima])
    BuktiTerima buktiTerima

    @Override
    int faktor() {
        -1
    }

    void diterima(BuktiTerima buktiTerima) {
        this.buktiTerima = buktiTerima
    }

    void diterima(LocalDate tanggal, String namaPenerima, String namaSupir) {
        this.buktiTerima = new BuktiTerima(tanggal, namaPenerima, namaSupir)
    }

    void batalDiterima() {
        this.buktiTerima = null
    }

    boolean sudahDiterima() {
        this.buktiTerima != null
    }

    PengeluaranBarang plus(DaftarBarangSementara daftarBarangSementara) {
        PengeluaranBarang hasil = new PengeluaranBarang(nomor: this.nomor, tanggal: this.tanggal, keterangan: this.keterangan,
            gudang: this.gudang, alamatTujuan: this.alamatTujuan, buktiTerima: this.buktiTerima)
        (daftarBarangSementara + this).items.each {
            hasil.tambah(it)
        }
        hasil
    }

}


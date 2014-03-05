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

package domain

import domain.container.Application
import domain.exception.DataDuplikat
import groovy.transform.*
import simplejpa.DomainClass
import type.Periode

import javax.persistence.*
import javax.validation.constraints.*

import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='listItemStok')
class StokProduk {

    @NotNull @ManyToOne
    Gudang gudang

    @Min(0l)
    Integer jumlah = 0

    @NotNull @ManyToOne
    Produk produk

    @OneToMany(cascade=CascadeType.ALL) @JoinColumn(name="stokProduk_id")
    Set<PeriodeItemStok> daftarPeriodeItemStok = new HashSet<>()

    /**
     * Membuat sebuah <code>PeriodeItemStok</code> dimana tanggal tertentu merupakan bagian
     * dari periode tersebut.  Untuk saat ini, periode dianggap sebagai periode bulanan dimana
     * selalu dimulai dari tanggal pertama hingga tanggal terakhir dari bulan tersebut.
     *
     * @param tanggal adalah tanggal yang dijadikan sebagai acuan.
     * @return sebuah <code>PeriodeItemStok</code> baru.
     */
    public PeriodeItemStok buatPeriode(LocalDate tanggal) {
        PeriodeItemStok p = new PeriodeItemStok(
            tanggal.withDayOfMonth(1),
            tanggal.withDayOfMonth(1).plusMonths(1).minusDays(1),
            0, Boolean.FALSE)
        if (daftarPeriodeItemStok.contains(p)) {
            throw new DataDuplikat(p)
        }
        daftarPeriodeItemStok.add(p)
        p
    }
    /**
     * Method ini akan mengembalikan <code>PeriodeItemStok</code> untuk tanggal yang diminta.
     * Bila <code>PeriodeItemStok</code> untuk tanggal tersebut tidak ditemukan, maka method ini
     * akan membuat sebuah <code>PeriodeItemStok</code> baru untuk tanggal tersebut.
     *
     * @param tanggal adalah tanggal yang dicari.
     */
    public PeriodeItemStok periode(LocalDate tanggal) {
        PeriodeItemStok result
        for (PeriodeItemStok p: daftarPeriodeItemStok) {
            if (p.termasuk(tanggal)) {
                result = p
                break
            }
        }

        // Buat PeriodeItemStok baru bila belum ada sebelumnya
        if (result==null) {
            result = buatPeriode(tanggal)
        }

        result
    }

    /**
     * Method ini akan mengembalikan <code>PeriodeItemStok</code> yang masuk dalam periode yang dicari.
     * Bila tidak ada <code>PeriodeItemStok</code> yang ditemukan, maka method ini <strong>TIDAK</code> akan
     * membuat <code>PeriodeItemStok</code> baru.
     *
     * @param periode adalah <code>Periode</code> yang dicari.
     * @return sebuah <code>List</code> berisi <code>PeriodeItemStok</code> yang masuk dalam periode yang dicari,
     *  atau sebuah <code>List</code> kosong bila tidak ada <code>PeriodeItemStok</code> yang ditemukan.
     */
    public List<PeriodeItemStok> periode(Periode periode) {
        List result = []
        for (PeriodeItemStok p: daftarPeriodeItemStok) {
            if (p.termasuk(periode)) {
                result << p
            }
        }
        result
    }

    /**
     * Method ini akan mengembalikan <code>PeriodeItemStok</code> yang sudah boleh di-arsip dengan batas
     * berupa <code>tahun</code> yang lalu.  Batas paling kecil untuk nilai yang boleh diarsip adalah
     * tiga tahun yang lalu.
     *
     * @param deltaTahun adalah jumlah batas yang akan diarsip (minimal 3 tahun yang lalu).
     * @return <code>List</code> berisi <code>PeriodeItemStok</code> yang memenuhi kriteria untuk di-arsip.
     */
    public List<PeriodeItemStok> periodeUntukArsip(int deltaTahun) {
        if (deltaTahun < 3) {
            throw new IllegalArgumentException('Masa pengarsipan paling cepat adalah 3 tahun yang lalu')
        }
        List result = []
        int tahunSekarang = LocalDate.now().year
        for (PeriodeItemStok p: daftarPeriodeItemStok) {
            if ((tahunSekarang - p.tanggalSelesai.year) >= deltaTahun) {
                result << p
            }
        }
        result
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        StokProduk that = (StokProduk) o

        if (gudang != that.gudang) return false
        if (produk != that.produk) return false

        return true
    }

    int hashCode() {
        int result
        result = gudang.hashCode()
        result = 31 * result + produk.hashCode()
        return result
    }
}


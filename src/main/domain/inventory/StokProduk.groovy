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

package domain.inventory

import domain.exception.DataDuplikat
import groovy.transform.*
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical
class StokProduk {

    @NotNull @ManyToOne
    Gudang gudang

    @NotNull @ManyToOne
    Produk produk

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name='riwayat_id') @OrderColumn
    List<PeriodeItemStok> listPeriodeRiwayat = []

    @Min(0l)
    Integer jumlah = 0

    /**
     * Membuat sebuah <code>PeriodeItemStok</code> dimana tanggal tertentu merupakan bagian
     * dari periode tersebut.  Untuk saat ini, periode dianggap sebagai periode bulanan dimana
     * selalu dimulai dari tanggal pertama hingga tanggal terakhir dari bulan tersebut.
     *
     * @param tanggal adalah tanggal yang dijadikan sebagai acuan.
     * @return sebuah <code>PeriodeItemStok</code> baru.
     */

    PeriodeItemStok periode(LocalDate tanggal) {
        PeriodeItemStok result
        for (PeriodeItemStok p: listPeriodeRiwayat) {
            if (p.termasuk(tanggal)) {
                result = p
                break
            }
        }
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
    List<PeriodeItemStok> periode(Periode periode) {
        List<PeriodeItemStok> result = []
        for (PeriodeItemStok p: listPeriodeRiwayat) {
            if (p.termasuk(periode)) {
                result << p
            }
        }
        result
    }

    PeriodeItemStok buatPeriode(LocalDate tanggal) {
        PeriodeItemStok p = new PeriodeItemStok()
        p.tanggalMulai = tanggal.withDayOfMonth(1)
        p.tanggalSelesai = tanggal.withDayOfMonth(1).plusMonths(1).minusDays(1)
        p.jumlah = 0
        p.arsip = Boolean.FALSE
        if (listPeriodeRiwayat.contains(p)) {
            throw new DataDuplikat(p)
        }
        listPeriodeRiwayat.add(p)
        p

    }

    /**
     * Method ini akan mengembalikan <code>PeriodeItemStok</code> yang sudah boleh di-arsip dengan batas
     * berupa <code>tahun</code> yang lalu.  Batas paling kecil untuk nilai yang boleh diarsip adalah
     * tiga tahun yang lalu.
     *
     * @param deltaTahun adalah jumlah batas yang akan diarsip (minimal 3 tahun yang lalu).
     * @return <code>List</code> berisi <code>PeriodeItemStok</code> yang memenuhi kriteria untuk di-arsip.
     */
    List<PeriodeItemStok> periodeUntukArsip(int deltaTahun) {
        if (deltaTahun < 3) {
            throw new IllegalArgumentException('Masa pengarsipan paling cepat adalah 3 tahun yang lalu')
        }
        List result = []
        int tahunSekarang = LocalDate.now().year
        for (PeriodeItemStok p: listPeriodeRiwayat) {
            if ((tahunSekarang - p.tanggalSelesai.year) >= deltaTahun) {
                result << p
            }
        }
        result
    }

    void tambah(ItemStok item) {
        periode(item.tanggalRiwayat()).tambah(item)
        this.jumlah += item.jumlah
    }

    long saldoKumulatifSebelum(PeriodeItemStok periodeItemStok) {
        long saldo = 0
        for (PeriodeItemStok p: listPeriodeRiwayat) {
            if (p.termasuk(periodeItemStok.tanggalMulai)) break
            saldo += p.jumlah
        }
        saldo
    }

    long saldoKumulatifSebelum(LocalDate tanggal) {
        long saldo = 0
        PeriodeItemStok periode = periode(tanggal)
        saldo += saldoKumulatifSebelum(periode)
        periode.listItem.each { ItemStok i ->
            if (i.tanggal.isBefore(tanggal)) {
                saldo += i.jumlah
            }
        }
        saldo
    }

    List<ItemStok> cariItemStok(PeriodeItemStok periodeItemStok) {
        long saldo = saldoKumulatifSebelum(periodeItemStok)
        List<ItemStok> hasil = []
        hasil << new ItemStok(periodeItemStok.tanggalMulai, null, null, 'Saldo Awal', saldo)
        for (ItemStok itemStok: periodeItemStok.listItem) {
            saldo += itemStok.jumlah
            itemStok.saldo = saldo
            hasil << itemStok
        }
        hasil
    }

}


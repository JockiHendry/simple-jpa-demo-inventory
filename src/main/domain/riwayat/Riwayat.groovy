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
package domain.riwayat

import domain.exception.DataDuplikat
import domain.inventory.ItemStok
import domain.inventory.Periode
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

import javax.persistence.CascadeType
import javax.persistence.JoinColumn
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Transient
import javax.validation.constraints.Min

@MappedSuperclass
abstract class Riwayat<T extends PeriodeRiwayat, V extends DapatDibuatRiwayat> {

    @Transient
    public Class<T> periodeRiwayatClass

    public Riwayat(Class<T> periodeRiwayatClass) {
        this.periodeRiwayatClass = periodeRiwayatClass
    }

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name='riwayat_id') @OrderColumn
    List<T> listPeriodeRiwayat = []

    @Min(0l)
    Integer jumlah = 0

    /**
     * Membuat sebuah <code>PeriodeRiwayat</code> dimana tanggal tertentu merupakan bagian
     * dari periode tersebut.  Untuk saat ini, periode dianggap sebagai periode bulanan dimana
     * selalu dimulai dari tanggal pertama hingga tanggal terakhir dari bulan tersebut.
     *
     * @param tanggal adalah tanggal yang dijadikan sebagai acuan.
     * @return sebuah <code>PeriodeRiwayat</code> baru.
     */

    T periode(LocalDate tanggal) {
        T result
        for (T p: listPeriodeRiwayat) {
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
     * Method ini akan mengembalikan <code>PeriodeRiwayat</code> yang masuk dalam periode yang dicari.
     * Bila tidak ada <code>PeriodeRiwayat</code> yang ditemukan, maka method ini <strong>TIDAK</code> akan
     * membuat <code>PeriodeRiwayat</code> baru.
     *
     * @param periode adalah <code>Periode</code> yang dicari.
     * @return sebuah <code>List</code> berisi <code>PeriodeRiwayat</code> yang masuk dalam periode yang dicari,
     *  atau sebuah <code>List</code> kosong bila tidak ada <code>PeriodeRiwayat</code> yang ditemukan.
     */
    List<T> periode(Periode periode) {
        List<T> result = []
        for (T p: listPeriodeRiwayat) {
            if (p.termasuk(periode)) {
                result << p
            }
        }
        result
    }

    T buatPeriode(LocalDate tanggal) {
        T p = periodeRiwayatClass.newInstance()
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
     * Method ini akan mengembalikan <code>PeriodeRiwayat</code> yang sudah boleh di-arsip dengan batas
     * berupa <code>tahun</code> yang lalu.  Batas paling kecil untuk nilai yang boleh diarsip adalah
     * tiga tahun yang lalu.
     *
     * @param deltaTahun adalah jumlah batas yang akan diarsip (minimal 3 tahun yang lalu).
     * @return <code>List</code> berisi <code>PeriodeRiwayat</code> yang memenuhi kriteria untuk di-arsip.
     */
    List<T> periodeUntukArsip(int deltaTahun) {
        if (deltaTahun < 3) {
            throw new IllegalArgumentException('Masa pengarsipan paling cepat adalah 3 tahun yang lalu')
        }
        List result = []
        int tahunSekarang = LocalDate.now().year
        for (T p: listPeriodeRiwayat) {
            if ((tahunSekarang - p.tanggalSelesai.year) >= deltaTahun) {
                result << p
            }
        }
        result
    }

    void tambah(V item) {
        periode(item.tanggalRiwayat()).tambah(item)
        this.jumlah += item.efekPadaJumlah()
    }
}

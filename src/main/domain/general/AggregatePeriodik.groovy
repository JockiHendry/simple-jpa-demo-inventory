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
package domain.general

import domain.exception.DataDuplikat
import domain.inventory.Periode
import domain.inventory.PeriodeItemStok
import groovy.transform.*
import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*

@MappedSuperclass @Canonical
abstract class AggregatePeriodik {

    @Min(0l)
    Integer jumlah = 0

    abstract List getListNilaiPeriodik()

    abstract NilaiPeriodik buatNilaiPeriodik()

    NilaiPeriodik periode(LocalDate tanggal) {
        NilaiPeriodik result
        for (NilaiPeriodik p: getListNilaiPeriodik()) {
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

    List<NilaiPeriodik> periode(Periode periode) {
        List<NilaiPeriodik> result = []
        for (NilaiPeriodik p: getListNilaiPeriodik()) {
            if (p.termasuk(periode)) {
                result << p
            }
        }
        result
    }

    NilaiPeriodik buatPeriode(LocalDate tanggal) {
        NilaiPeriodik p = buatNilaiPeriodik()
        p.tanggalMulai = tanggal.withDayOfMonth(1)
        p.tanggalSelesai = tanggal.withDayOfMonth(1).plusMonths(1).minusDays(1)
        p.jumlah = 0
        p.arsip = Boolean.FALSE
        if (getListNilaiPeriodik().contains(p)) {
            throw new DataDuplikat(p)
        }
        getListNilaiPeriodik().add(p)
        p
    }

    List<NilaiPeriodik> periodeUntukArsip(int deltaTahun) {
        if (deltaTahun < 3) {
            throw new IllegalArgumentException('Masa pengarsipan paling cepat adalah 3 tahun yang lalu')
        }
        List result = []
        int tahunSekarang = LocalDate.now().year
        for (PeriodeItemStok p: getListNilaiPeriodik()) {
            if ((tahunSekarang - p.tanggalSelesai.year) >= deltaTahun) {
                result << p
            }
        }
        result
    }

    long saldoKumulatifSebelum(NilaiPeriodik nilaiPeriodik) {
        long saldo = 0
        for (NilaiPeriodik p: getListNilaiPeriodik()) {
            if (p.termasuk(nilaiPeriodik.tanggalMulai)) break
            saldo += p.jumlah
        }
        saldo
    }

    long saldoKumulatifSebelum(LocalDate tanggal) {
        long saldo = 0
        NilaiPeriodik periode = periode(tanggal)
        saldo += saldoKumulatifSebelum(periode)
        periode.getListItemPeriodik().each { ItemPeriodik i ->
            if (i.tanggal.isBefore(tanggal)) {
                saldo += i.jumlah
            }
        }
        saldo
    }

    void tambah(ItemPeriodik item) {
        periode(item.tanggal).tambah(item)
        this.jumlah += item.jumlah
    }

}


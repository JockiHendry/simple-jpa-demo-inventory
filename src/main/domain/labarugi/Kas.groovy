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
package domain.labarugi

import domain.general.AggregatePeriodik
import domain.general.NilaiPeriodik
import domain.inventory.Periode
import groovy.transform.*
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@DomainClass @Entity @Canonical(excludes='listPeriodeRiwayat')
class Kas extends AggregatePeriodik {

    @NotBlank @Size(min=2, max=100)
    String nama

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) @JoinColumn @OrderColumn
    List<PeriodeKas> listPeriodeRiwayat = []

    Boolean sistem = Boolean.FALSE

    @Override
    List getListNilaiPeriodik() {
        listPeriodeRiwayat
    }

    @Override
    NilaiPeriodik buatNilaiPeriodik() {
        new PeriodeKas()
    }

    long jumlah(LocalDate tanggalMulai, LocalDate tanggalSelesai, JENIS_KATEGORI_KAS jenisKategoriKas, boolean dipakaiDiLaporan = false) {
        jumlah(new Periode(tanggalMulai, tanggalSelesai), jenisKategoriKas, dipakaiDiLaporan)
    }

    long jumlah(Periode periode, JENIS_KATEGORI_KAS jenisKategoriKas, boolean dipakaiDiLaporan = false) {
        long hasil = 0
        listPeriodeRiwayat.each { PeriodeKas p ->
            if (!p.termasuk(periode)) return
            if ((p.tanggalMulai.compareTo(periode.tanggalMulai) >= 0) && (p.tanggalSelesai.compareTo(periode.tanggalSelesai) <= 0)) {
                hasil += p.jumlah(jenisKategoriKas, dipakaiDiLaporan)
            } else {
                p.listTransaksiKas.each { TransaksiKas t ->
                    if (t.kategoriKas.dipakaiDiLaporan && periode.termasuk(t.tanggal) && (t.kategoriKas.jenis == jenisKategoriKas)) {
                        hasil += t.jumlah
                    }
                }
            }
        }
        hasil
    }

}


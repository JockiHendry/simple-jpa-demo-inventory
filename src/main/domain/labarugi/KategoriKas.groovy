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

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity  @Canonical
class KategoriKas implements Comparable {

    @NotNull @Size(min=3, max=100)
    String nama

    @NotNull @Enumerated
    JENIS_KATEGORI_KAS jenis

    @NotNull
    Boolean sistem = Boolean.FALSE

    @NotNull
    Boolean dipakaiDiLaporan = Boolean.TRUE

    @ElementCollection(fetch=FetchType.EAGER)
    Set<SaldoKas> listSaldoKas = [] as Set

    void perubahanSaldo(int bulan, int tahun, BigDecimal jumlahBerubah, JenisTransaksiKas jenis) {
        SaldoKas saldoKas = listSaldoKas.find { (it.bulan == bulan) && (it.tahun == tahun) && (it.jenis == jenis) }
        if (saldoKas) {
            saldoKas.saldo += jumlahBerubah
        } else {
            saldoKas = new SaldoKas(bulan, tahun, jumlahBerubah, jenis)
            listSaldoKas << saldoKas
        }
    }

    BigDecimal saldo(int bulan, int tahun, JenisTransaksiKas jenisTransaksiKas = null) {
        listSaldoKas.sum { SaldoKas j ->
            if ((j.bulan == bulan) && (j.tahun == tahun) && (jenisTransaksiKas? (j.jenis == jenisTransaksiKas): true)) {
                return j.saldo
            }
            0
        }?: 0
    }

    BigDecimal saldoTerakhir() {
        listSaldoKas.sum { it.saldo }?: 0
    }

    @Override
    String toString() {
        "${jenis.teks} - $nama"
    }

    @Override
    int compareTo(Object o) {
        if (o && (o instanceof KategoriKas)) {
            return nama.compareTo(o.nama)
        }
        1
    }
}


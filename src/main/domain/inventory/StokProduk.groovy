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

import domain.general.AggregatePeriodik
import domain.general.NilaiPeriodik
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical @TupleConstructor(includeSuperProperties=true)
class StokProduk extends AggregatePeriodik {

    @NotNull @ManyToOne
    Gudang gudang

    @NotNull @ManyToOne
    Produk produk

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name='riwayat_id') @OrderColumn
    List<PeriodeItemStok> listPeriodeRiwayat = []

    List getListNilaiPeriodik() {
        listPeriodeRiwayat
    }

    NilaiPeriodik buatNilaiPeriodik() {
        new PeriodeItemStok()
    }

    List<ItemStok> cariItemStok(PeriodeItemStok periodeItemStok, boolean hitungSaldo = true) {
        long saldo = 0
        List<ItemStok> hasil = []
        if (hitungSaldo) {
            saldo = saldoKumulatifSebelum(periodeItemStok)
            hasil << new ItemStok(tanggal: periodeItemStok.tanggalMulai, keterangan: 'Saldo Awal', saldo: saldo)
        }
        for (ItemStok itemStok: periodeItemStok.listItem) {
            if (hitungSaldo) {
                saldo += itemStok.jumlah
                itemStok.saldo = saldo
            }
            hasil << itemStok
        }
        hasil
    }

    List<ItemStok> cariItemStok(Periode periode) {
        List<ItemStok> hasil = []
        for (PeriodeItemStok periodeItemStok: listPeriodeRiwayat) {
            if (periodeItemStok.termasuk(periode)) {
                hasil.addAll(cariItemStok(periodeItemStok, false).findAll { periode.termasuk(it.tanggal) })
            }
        }
        hasil
    }

}


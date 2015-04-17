/*
 * Copyright 2015 Jocki Hendry.
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
package domain.faktur

import domain.exception.DataTidakBolehDiubah
import domain.exception.HargaSelisih
import groovy.transform.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical
class KewajibanPembayaran {

    @NotNull @Min(0l)
    BigDecimal jumlah

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @CollectionTable(name='kewajibanpembayaran_items')
    @Fetch(FetchMode.SUBSELECT)
    List<Pembayaran> listPembayaran = []

    BigDecimal sisa(KRITERIA_PEMBAYARAN kriteria = KRITERIA_PEMBAYARAN.SEMUA) {
        jumlah - jumlahDibayar(kriteria)
    }

    BigDecimal jumlahDibayar(KRITERIA_PEMBAYARAN kriteria = KRITERIA_PEMBAYARAN.SEMUA) {
        listPembayaran.findAll{ it.matches(kriteria) }.sum { it.jumlah } ?: 0
    }

    BigDecimal jumlahPotongan(String jenisReferensi) {
        jenisReferensi? (listPembayaran.findAll { it.potongan && it.referensi?.namaClass == jenisReferensi }.sum { it.jumlah } ?: 0):
            (listPembayaran.findAll { it.potongan && !it.referensi }.sum { it.jumlah }?: 0)
    }

    boolean isLunas() {
        sisa().compareTo(BigDecimal.ZERO) == 0
    }

    void bayar(Pembayaran pembayaran) {
        // Sisa pembayaran termasuk giro yang belum jatuh tempo
        def sisa = sisa()

        if (pembayaran.jumlah > sisa) {
            throw new HargaSelisih('Pembayaran lebih dari sisa pembayaran!', pembayaran.jumlah, sisa)
        }
        listPembayaran << pembayaran
    }

    void hapus(Pembayaran pembayaran) {
        if (!listPembayaran.contains(pembayaran)) {
            throw new DataTidakBolehDiubah("Tidak menemukan pembayaran $pembayaran")
        }
        listPembayaran.remove(pembayaran)
    }

}

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
package domain.faktur

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Entity @Canonical
class KewajibanPembayaran {

    @NotNull @Min(0l)
    BigDecimal jumlah

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @CollectionTable(name='kewajibanpembayaran_items')
    List<Pembayaran> listPembayaran = []

    BigDecimal sisa() {
        jumlah - jumlahDibayar()
    }

    BigDecimal jumlahDibayar() {
        listPembayaran.sum { it.nominal() }?: 0
    }

    boolean isLunas() {
        sisa().compareTo(BigDecimal.ZERO) == 0
    }

    void bayar(Pembayaran pembayaran) {
        // Sisa pembayaran termasuk giro yang belum jatuh tempo
        def sisa = jumlah - (listPembayaran.sum { it.jumlah }?: 0)

        if (pembayaran.jumlah > sisa) {
            throw new IllegalArgumentException("Pembayaran ${pembayaran.jumlah} melebihi sisa sebesar ${sisa}")
        }
        listPembayaran << pembayaran
    }

    void hapus(Pembayaran pembayaran) {
        if (!listPembayaran.contains(pembayaran)) {
            throw new IllegalArgumentException("Tidak menemukan pembayaran $pembayaran")
        }
        listPembayaran.remove(pembayaran)
    }

}


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

import domain.general.ItemPeriodik
import groovy.transform.*
import javax.persistence.*
import javax.validation.constraints.*

@Embeddable @Canonical @TupleConstructor(includeSuperProperties=true) @EqualsAndHashCode(callSuper=true)
class TransaksiKas extends ItemPeriodik {

    @Size(min=2, max=150)
    String pihakTerkait

    @ManyToOne @NotNull
    KategoriKas kategoriKas

    @NotNull @ManyToOne
    JenisTransaksiKas jenis

    @Override
    long delta() {
        (kategoriKas.jenis == JENIS_KATEGORI_KAS.PENGELUARAN)? -jumlah: jumlah
    }

    BigDecimal debit() {
        (kategoriKas.jenis == JENIS_KATEGORI_KAS.PENDAPATAN)? jumlah: 0
    }

    BigDecimal kredit() {
        (kategoriKas.jenis == JENIS_KATEGORI_KAS.PENGELUARAN)? jumlah: 0
    }

}


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
package domain.labarugi

import domain.general.ItemPeriodik
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@DomainClass @Canonical @TupleConstructor(includeSuperProperties=true)
@Entity
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof TransaksiKas)) return false
        TransaksiKas that = (TransaksiKas) o
        if (tanggal != that.tanggal) return false
        if (jumlah != that.jumlah) return false
        if (keterangan != that.keterangan) return false
        if (saldo != that.saldo) return false
        if (jenis.nama != that.jenis.nama) return false
        if (kategoriKas.nama != that.kategoriKas.nama) return false
        if (pihakTerkait != that.pihakTerkait) return false
        true
    }

    int hashCode() {
        int result
        result = (tanggal?.hashCode()?: 0)
        result = 31 * result + (jumlah?.hashCode()?: 0)
        result = 31 * result + (keterangan?.hashCode()?: 0)
        result = 31 * result + (saldo?.hashCode()?: 0)
        result = 31 * result + (jenis?.nama?.hashCode()?: 0)
        result = 31 * result + (pihakTerkait?.hashCode()?: 0)
        result = 31 * result + (kategoriKas?.nama?.hashCode()?: 0)
        result
    }

}


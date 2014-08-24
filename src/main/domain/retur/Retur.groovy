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
package domain.retur

import domain.inventory.Produk
import groovy.transform.*
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@MappedSuperclass @Canonical(excludes='items')
abstract class Retur {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200)
    String keterangan

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @NotEmpty
    List<BarangRetur> items = []

    @NotNull
    Boolean sudahDiklaim = Boolean.FALSE

    @Min(0l)
    BigDecimal potongan = BigDecimal.ZERO

    @Min(0l)
    BigDecimal potonganCair = BigDecimal.ZERO

    public Boolean getSudahDiklaim() {
        (items.every { it.sudahDiKlaim() }? Boolean.TRUE: Boolean.FALSE) && (sisaPotongan() == BigDecimal.ZERO)
    }

    void tambah(BarangRetur barangRetur) {
        items << barangRetur
        if (barangRetur.tukar) {
            barangRetur.jumlahDiKlaim = 0
        } else {
            barangRetur.jumlahDiKlaim = barangRetur.jumlah
        }
    }

    void prosesKlaim(Produk produk, int jumlah, String nomorKlaim) {
        def listItems = items.findAll { it.produk == produk  && !it.sudahDiKlaim() }
        if (listItems.empty) {
            throw new UnsupportedOperationException("Tidak menemukan [$produk] untuk di-prosesKlaim di [$this]")
        }
        listItems.each {
            it.jumlahDiKlaim += jumlah
            if (it.nomorKlaim) {
                it.nomorKlaim += ',' + nomorKlaim
            } else {
                it.nomorKlaim = nomorKlaim
            }
        }
    }

    void prosesPotongan(BigDecimal jumlah) {
        if (jumlah > sisaPotongan()) {
            throw new IllegalArgumentException("Tidak dapat melakukan potongan [$jumlah] untuk sisa potongan [${sisaPotongan()}]")
        }
        potonganCair += jumlah
    }

    List<BarangRetur> getBelumDiklaim() {
        def hasil = []
        hasil.addAll(items.findAll{ !it.sudahDiKlaim() })
        hasil
    }

    List<BarangRetur> getBarangDitukar() {
        def hasil = []
        hasil.addAll(items.findAll{ it.tukar })
        hasil
    }

    BigDecimal sisaPotongan() {
        potongan - potonganCair
    }

}


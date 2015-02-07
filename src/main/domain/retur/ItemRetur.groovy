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
package domain.retur

import domain.exception.DataTidakBolehDiubah
import domain.inventory.Produk
import domain.validation.InputReturJual
import groovy.transform.Canonical
import simplejpa.DomainClass
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

@DomainClass @Entity @Canonical
class ItemRetur {

    @NotNull(groups=[Default,InputReturJual]) @ManyToOne
    Produk produk

    @NotNull(groups=[Default,InputReturJual]) @Min(value=1l, groups=[Default,InputReturJual])
    Integer jumlah

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    Set<Klaim> klaims = [] as Set

    Boolean isSudahDiproses() {
        klaims.every { it.sudahDiproses }
    }

    List<Klaim> getKlaims(Class clazz, boolean hanyaBelumDiproses = false) {
        new ArrayList<Klaim>(
            klaims.findAll { (it.getClass() == clazz) && (hanyaBelumDiproses? !it.sudahDiproses: true) }
        )
    }

    Integer jumlahBarangDitukar(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimTukar, hanyaBelumDiproses).sum { KlaimTukar k -> k.jumlah }?: 0
    }

    Integer jumlahBarangDiservis(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimServis, hanyaBelumDiproses).sum { KlaimServis k -> k.jumlah }?: 0
    }

    Integer jumlahPotongPiutang(boolean  hanyaBelumDiproses = false) {
        getKlaims(KlaimPotongPiutang, hanyaBelumDiproses).sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Integer qtyPotongPiutang() {
        int hasil = 0
        if (jumlahPotongPiutang() > 0) {
            hasil = jumlah - (jumlahBarangDitukar() + jumlahBarangDiservis())
        }
        (hasil > 0)? hasil: 0
    }

    void tambahKlaim(Klaim klaim) {
        if (klaims.contains(klaim)) {
            throw new DataTidakBolehDiubah("Sudah ada klaim yang sama untuk ${klaim}")
        }
        klaims << klaim
    }

    void merge(ItemRetur itemReturLain) {
        if (itemReturLain?.produk != produk) {
            throw new IllegalArgumentException("Item untuk produk ${produk.nama} tidak dapat digabung dengan item untuk produk ${itemReturLain?.produk?.nama}")
        }
        jumlah += itemReturLain.jumlah
        Set<Klaim> sudahDiMerge = [] as Set
        itemReturLain.klaims.toArray().each { Klaim k ->
            // Cari apakah sudah ada klaim serupa
            Klaim klaimSudahAda = klaims.find { it.class == k.class }
            if (klaimSudahAda && klaimSudahAda.bolehMerge(k)) {
                klaimSudahAda.merge(k)
                sudahDiMerge << k
            } else if (!sudahDiMerge.contains(k)) {
                klaims << k
            }
        }
    }

    void leftShift(ItemRetur itemReturLain) {
        merge(itemReturLain)
    }

    void hapusSemuaKlaimPotongPiutang() {
        klaims.toArray().findAll { it instanceof KlaimPotongPiutang }.each { klaims.remove(it) }
    }

    String getDescription() {
        List barangDitukar = getKlaims(KlaimTukar).findAll { KlaimTukar k -> k.produk != produk }
        if (barangDitukar.empty) {
            return produk.nama
        } else {
            return "${produk.nama} (TUKAR: ${barangDitukar.collect{ KlaimTukar k -> k.produk.nama }.join(',')})"
        }
    }

}

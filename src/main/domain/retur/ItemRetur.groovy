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

import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.validation.InputReturJual
import groovy.transform.Canonical
import org.hibernate.validator.constraints.NotEmpty
import simplejpa.DomainClass
import javax.persistence.AttributeOverride
import javax.persistence.AttributeOverrides
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

@DomainClass @Entity @Canonical
class ItemRetur {

    @NotNull(groups=[Default,InputReturJual]) @ManyToOne
    Produk produk

    @NotNull(groups=[Default,InputReturJual]) @Min(value=1l, groups=[Default,InputReturJual])
    Integer jumlah

    @NotEmpty(groups=[Default]) @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    Set<Klaim> klaims = [] as Set

    Boolean isSudahDiproses() {
        klaims.every { it.sudahDiproses }
    }

    Set<Klaim> getKlaims(Class clazz, boolean hanyaBelumDiproses = false) {
        new HashSet<Klaim>(
            klaims.findAll { (it.getClass() == clazz) && (hanyaBelumDiproses? !it.sudahDiproses: true) }
        )
    }

    Integer jumlahBarangDitukar(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimTukar, hanyaBelumDiproses).sum { KlaimTukar k -> k.jumlah }?: 0
    }

    Integer jumlahPotongPiutang(boolean  hanyaBelumDiproses = false) {
        getKlaims(KlaimPotongPiutang, hanyaBelumDiproses).sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    void tambahKlaim(Klaim klaim) {
        klaims << klaim
    }

}

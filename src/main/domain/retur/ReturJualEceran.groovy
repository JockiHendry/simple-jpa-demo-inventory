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

import domain.inventory.DaftarBarang
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import project.inventory.GudangRepository
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@DomainClass @Entity @Canonical
class ReturJualEceran extends ReturJual {

    @NotEmpty @Size(min=2, max=100)
    String namaKonsumen

    @Override
    void tambah(ItemRetur itemRetur) {
        if (itemRetur.klaims.find { it instanceof KlaimPotongPiutang }) {
            throw new IllegalArgumentException('Retur jual eceran tidak boleh memiliki klaim potong piutang!')
        }
        super.tambah(itemRetur)
    }

    PengeluaranBarang tukar() {
        super.tukar((SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang') as GudangRepository).cariGudangUtama(), namaKonsumen)
    }

    @Override
    DaftarBarang toDaftarBarang() {
        DaftarBarang hasil = super.toDaftarBarang()
        hasil.gudang = (SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang') as GudangRepository).cariGudangUtama()
        hasil
    }
}


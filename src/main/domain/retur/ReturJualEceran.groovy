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

import domain.inventory.BolehPesanStok
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import project.inventory.GudangRepository
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil

import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class ReturJualEceran extends ReturJual implements BolehPesanStok {

    @NotEmpty @Size(min=2, max=100)
    String namaKonsumen

    @Override
    void tambah(ItemRetur itemRetur) {
        if (itemRetur.klaims.find { !(it instanceof KlaimTukar) }) {
            throw new IllegalArgumentException('Retur jual eceran tidak boleh memiliki klaim selain klaim tukar!')
        }
        super.tambah(itemRetur)
    }

    PengeluaranBarang tukar() {
        super.tukar((SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang') as GudangRepository).cariGudangUtama(), namaKonsumen)
    }

    @Override
    boolean isValid() {
        true
    }

    @Override
    List<ItemBarang> yangDipesan() {
        yangHarusDitukar().items
    }

}


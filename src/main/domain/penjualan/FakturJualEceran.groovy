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
package domain.penjualan

import domain.faktur.Faktur
import domain.pengaturan.KeyPengaturan
import domain.penjualan.state.FakturJualEceranDiantar
import domain.penjualan.state.FakturJualEceranDibuat
import domain.penjualan.state.FakturJualEceranLunas
import domain.penjualan.state.FakturJualEceranMulai
import domain.penjualan.state.FakturJualEceranSingkatMulai
import domain.penjualan.state.OperasiFakturJual
import domain.inventory.ItemBarang
import project.pengaturan.PengaturanRepository
import groovy.transform.*
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@NamedEntityGraph(name='FakturJualEceran.Complete', attributeNodes=[
    @NamedAttributeNode('listItemFaktur'),
    @NamedAttributeNode('pengeluaranBarang')
])
@DomainClass @Entity @Canonical @EqualsAndHashCode(callSuper=true)
class FakturJualEceran extends FakturJual {

    @NotEmpty @Size(min=2, max=100)
    String namaPembeli

    @Override
    boolean isBolehPesanStok() {
        true
    }

    @Override
    List<ItemBarang> yangDipesan() {
        toDaftarBarang().items
    }

    @Override
    BigDecimal nilaiPenjualan() {
        total()
    }

    @Override
    OperasiFakturJual getOperasiFakturJual() {
        PengaturanRepository pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan') as PengaturanRepository
        switch (status) {
            case null:
                if (pengaturanRepository.getValue(KeyPengaturan.WORKFLOW_GUDANG)) {
                    return new FakturJualEceranMulai()
                } else {
                    return new FakturJualEceranSingkatMulai()
                }
            case StatusFakturJual.DIBUAT: return new FakturJualEceranDibuat()
            case StatusFakturJual.DIANTAR: return new FakturJualEceranDiantar()
            case StatusFakturJual.LUNAS: return new FakturJualEceranLunas()
        }
        null
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Faktur faktur = (Faktur) o

        if (nomor != faktur.nomor) return false

        return true
    }

    int hashCode() {
        return nomor.hashCode()
    }

}


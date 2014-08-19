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
package domain.penjualan

import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.faktur.Faktur
import project.inventory.GudangRepository
import domain.inventory.ItemBarang
import project.user.NomorService
import groovy.transform.*
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import griffon.util.*

@NamedEntityGraph(name='FakturJualEceran.Complete', attributeNodes=[
    @NamedAttributeNode('listItemFaktur'),
    @NamedAttributeNode('pengeluaranBarang')
])
@DomainClass @Entity @Canonical @EqualsAndHashCode(callSuper=true)
class FakturJualEceran extends FakturJual {

    @NotEmpty @Size(min=2, max=100)
    String namaPembeli

    public void antar() {
        if (!status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: this.tanggal,
            gudang: (SimpleJpaUtil.instance.repositoryManager.findRepository('GudangRepository') as GudangRepository).cariGudangUtama()
        )
        listItemFaktur.each {
            pengeluaranBarang.tambah(new ItemBarang(produk: it.produk, jumlah: it.jumlah))
        }
        tambah(pengeluaranBarang)
    }

    public void batalAntar() {
        if (status!=StatusFakturJual.DIANTAR) {
            throw new DataTidakBolehDiubah(this)
        }
        hapusPengeluaranBarang()
    }

    public void bayar() {
        if (status != StatusFakturJual.DIANTAR) {
            throw new DataTidakBolehDiubah(this)
        }
        status = StatusFakturJual.LUNAS
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


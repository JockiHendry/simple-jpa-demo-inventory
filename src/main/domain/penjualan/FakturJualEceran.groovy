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
import domain.inventory.ItemBarang
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical @EqualsAndHashCode(callSuper=true)
class FakturJualEceran extends FakturJual {

    @NotEmpty @Size(min=2, max=100)
    String namaPembeli

    public void antar() {
        if (!status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: Container.app.nomorService.buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: this.tanggal,
            gudang: Container.app.gudangRepository.cariGudangUtama()
        )
        listItemFaktur.each {
            pengeluaranBarang.tambah(new ItemBarang(produk: it.produk, jumlah: it.jumlah))
        }
        tambah(pengeluaranBarang)
    }

    public void bayar() {
        if (status != StatusFakturJual.DIANTAR) {
            throw new DataTidakBolehDiubah(this)
        }
        status = StatusFakturJual.LUNAS
    }
}


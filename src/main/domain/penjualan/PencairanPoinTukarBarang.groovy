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

import domain.event.PerubahanStok
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.validation.InputPencairanPoin
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.validator.constraints.*
import griffon.util.*

import javax.validation.groups.Default

@DomainClass @Entity @Canonical
class PencairanPoinTukarBarang extends PencairanPoin {

    @NotEmpty(groups=[InputPencairanPoin,Default])
    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @CollectionTable(name='PencairanPoin_Items')
    List<ItemBarang> listItemBarang = []

    @Override
    boolean valid() {
        konsumen.poinTerkumpul >= getJumlahPoin()
    }

    @Override
    void proses() {
        DaftarBarangSementara daftarBarangSementara = new DaftarBarangSementara(listItemBarang, -1)
        daftarBarangSementara.gudang = konsumen.sales.gudang
        daftarBarangSementara.keterangan = 'Penukaran Poin'
        ReferensiStok ref = new ReferensiStokBuilder(daftarBarangSementara, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(daftarBarangSementara, ref))
    }

    @Override
    void hapus() {
        DaftarBarangSementara daftarBarangSementara = new DaftarBarangSementara(listItemBarang, -1)
        daftarBarangSementara.gudang = konsumen.sales.gudang
        daftarBarangSementara.keterangan = 'Pembatalan Penukaran Poin'
        ReferensiStok ref = new ReferensiStokBuilder(daftarBarangSementara, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(daftarBarangSementara, ref, true))
    }

    Integer getJumlahPoin() {
        listItemBarang.sum { it.jumlah * (it.produk.poin?: 0) }
    }

    void tambah(ItemBarang itemBarang) {
        listItemBarang << itemBarang
    }
}


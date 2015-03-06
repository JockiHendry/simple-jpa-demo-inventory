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

package domain.faktur

import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.inventory.SebuahDaftarBarang
import domain.validation.InputPurchaseOrder
import groovy.transform.*
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import javax.validation.groups.Default

@MappedSuperclass @Canonical(excludes='listItemFaktur')
abstract class Faktur implements Comparable, SebuahDaftarBarang {

    @NotEmpty(groups=[Default]) @Size(min=2, max=100, groups=[Default])
    String nomor

    @NotNull(groups=[Default,InputPurchaseOrder]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Embedded
    Diskon diskon

    @Size(min=2, max=200, groups=[Default,InputPurchaseOrder])
    String keterangan

    @ElementCollection @OrderColumn @NotEmpty(groups=[Default,InputPurchaseOrder])
    List<ItemFaktur> listItemFaktur = []

    public void tambah(ItemFaktur itemFaktur) {
        listItemFaktur << itemFaktur
    }

    public BigDecimal subtotal() {
        listItemFaktur.sum { it.total() }?: 0
    }

    public BigDecimal total() {
        def total = subtotal()
        if (diskon) {
            total = diskon.hasil(total)
        }
        total
    }

    public BigDecimal totalSebelumDiskon() {
        listItemFaktur.sum { it.totalSebelumDiskon() }?: 0
    }

    public BigDecimal jumlahDiskonTanpaDiskonItem() {
        diskon?.jumlah(totalSebelumDiskon())?: 0
    }

    public BigDecimal jumlahDiskonItem() {
        listItemFaktur.sum { it.jumlahDiskon() }?: 0
    }

    public BigDecimal jumlahDiskon() {
        jumlahDiskonItem() + jumlahDiskonTanpaDiskonItem()
    }

    @Override
    DaftarBarang toDaftarBarang() {
        def items = listItemFaktur.collect { new ItemBarang(it.produk, it.jumlah) }
        DaftarBarangSementara hasil = new DaftarBarangSementara(items)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil
    }

    public void ubahItems(List<ItemFaktur> items) {
        if (items.size() != listItemFaktur.size()) return
        for (int i=0; i < listItemFaktur.size(); i++) {
            ItemFaktur item = items[i]
            if (listItemFaktur[i].produk == item.produk) {
                listItemFaktur[i].harga = item.harga
            }
        }
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

    @Override
    int compareTo(Object o) {
        if (o == null) return -1
        if (!(o instanceof Faktur)) return -1
        if (nomor.compareTo(o.nomor) != 0) {
            if (!o.tanggal) return -1
            return tanggal.compareTo(o.tanggal)
        } else {
            return 0
        }
    }
}


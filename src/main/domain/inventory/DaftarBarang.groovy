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
package domain.inventory

import domain.faktur.Faktur
import domain.validation.TanpaGudang
import groovy.transform.Canonical
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.joda.time.LocalDate
import javax.persistence.ElementCollection
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OrderColumn
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.groups.Default

@MappedSuperclass @Canonical(excludes='items')
abstract class DaftarBarang implements SebuahDaftarBarang {

    @NotBlank(groups=[Default,TanpaGudang]) @Size(min=2, max=100, groups=[Default,TanpaGudang])
    String nomor

    @NotNull(groups=[Default,TanpaGudang]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200, groups=[Default,TanpaGudang])
    String keterangan

    @NotNull(groups=[Default]) @ManyToOne
    Gudang gudang

    @ElementCollection(fetch=FetchType.EAGER) @OrderColumn @NotEmpty(groups=[Default])
    List<ItemBarang> items = []

    abstract int faktor()

    void tambah(ItemBarang itemBarang) {
        items << itemBarang
    }

    int jumlah() {
        items.sum { it.jumlah }?: 0
    }

    int jumlah(Produk produk) {
        normalisasi().find { it.produk == produk}?.jumlah?: 0
    }

    List<ItemBarang> normalisasi() {
        List hasil = []
        items.groupBy { it.produk }.each { k, v ->
            hasil << new ItemBarang(k, v.sum { it.jumlah })
        }
        hasil
    }

    boolean isiSamaDengan(DaftarBarang daftarBarangLain) {
        normalisasi().toSet() == daftarBarangLain.normalisasi().toSet()
    }

    boolean isiSamaDengan(Faktur faktur) {
        // faktur.toDaftarBarang() akan menghasilkan daftar barang yang sudah dinormalisasi.
        normalisasi().toSet() == faktur.toDaftarBarang().items.toSet()
    }

    @Override
    DaftarBarang toDaftarBarang() {
        this
    }

    DaftarBarangSementara plus(SebuahDaftarBarang sebuahDaftarBarang) {
        DaftarBarang daftarBarang = sebuahDaftarBarang.toDaftarBarang()
        DaftarBarang hasil = plus(daftarBarang.items)
        hasil.nomor = daftarBarang.nomor
        hasil.tanggal = daftarBarang.tanggal
        hasil.keterangan = daftarBarang.keterangan
        hasil.gudang = daftarBarang.gudang
        hasil
    }

    DaftarBarangSementara plus(Collection<ItemBarang> daftarLain) {
        List<ItemBarang> hasil = new ArrayList<>(items.collect { new ItemBarang(it.produk, it.jumlah)})
        daftarLain.each { ItemBarang itemLain ->
            ItemBarang itemBarang = hasil.find {it.produk == itemLain.produk}
            if (itemBarang) {
                itemBarang.jumlah += itemLain.jumlah
            } else {
                hasil << itemLain
            }
        }
        new DaftarBarangSementara(hasil, faktor())
    }

    DaftarBarangSementara minus(SebuahDaftarBarang sebuahDaftarBarang) {
        DaftarBarang daftarBarang = sebuahDaftarBarang.toDaftarBarang()
        DaftarBarang hasil = minus(daftarBarang.items)
        hasil.nomor = daftarBarang.nomor
        hasil.tanggal = daftarBarang.tanggal
        hasil.keterangan = daftarBarang.keterangan
        hasil.gudang = daftarBarang.gudang
        hasil
    }

    DaftarBarangSementara minus(Collection<ItemBarang> daftarLain) {
        List<ItemBarang> hasil = new ArrayList<>(normalisasi())
        daftarLain.each { ItemBarang itemBarang ->
            ItemBarang lhs = hasil.find { it.produk == itemBarang.produk }
            if (lhs) {
                lhs.jumlah -= itemBarang.jumlah
                if (lhs.jumlah == 0) {
                    hasil.remove(lhs)
                }
            }
        }
        new DaftarBarangSementara(hasil, faktor())
    }

    Integer toPoin() {
        items.sum { it.jumlah * (it.produk.poin?:0) }?: 0
    }
}

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
import domain.pengaturan.KeyPengaturan
import domain.penjualan.Sales
import org.joda.time.LocalDate
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@NamedEntityGraphs([
    @NamedEntityGraph(name='Produk.Complete', attributeNodes = [
        @NamedAttributeNode(value='daftarStok', subgraph='stokProduk')
    ], subgraphs = [
        @NamedSubgraph(
            name = 'stokProduk',
            attributeNodes=[@NamedAttributeNode(value='listPeriodeRiwayat')]
        )
    ]),
    @NamedEntityGraph(name='Produk.StokProduk', attributeNodes = [
        @NamedAttributeNode(value='daftarStok', subgraph='stokProduk')
    ])
])
@DomainClass @Entity
class Produk implements Comparable {

    Produk() {}

    Produk(String nama) {
        this.nama = nama
    }

    Produk(String nama, Integer jumlah) {
        this.nama = nama
        this.jumlah = jumlah
    }

    Produk(String nama, Integer jumlah, BigDecimal hargaDalamKota, BigDecimal hargaLuarKota) {
        this.nama = nama
        this.jumlah = jumlah
        this.hargaDalamKota = hargaDalamKota
        this.hargaLuarKota = hargaLuarKota
    }

    @NotBlank @Size(min=3, max=150)
    String nama

    @NotNull
    BigDecimal hargaDalamKota

    @NotNull
    BigDecimal hargaLuarKota

    @Min(0l)
    Integer jumlah = 0

    @Min(0l)
    Integer jumlahAkanDikirim = 0

    @NotNull @ManyToOne
    Satuan satuan

    @NotNull @Min(0l)
    Integer poin = 0

    @Min(0l)
    Integer levelMinimum

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy='produk') @MapKey(name='gudang')
    Map<Gudang, StokProduk> daftarStok = [:]

    public StokProduk stok(Gudang gudang) {
        StokProduk result = daftarStok[gudang]
        if (!result) {
            result = new StokProduk(gudang: gudang, produk: this)
            daftarStok[gudang] = result
        }
        result
    }

    public List<StokProduk> stokSemuaGudang() {
        daftarStok.values().toList().sort { it.gudang.nama }
    }

    public void perubahanStok(int jumlah, Faktur faktur, Gudang gudang, String keterangan) {
        ItemStok itemStok = new ItemStok(LocalDate.now(), faktur?.nomor?:'-', faktur?.class?.simpleName?:'-', jumlah, keterangan)
        perubahanStok(gudang, itemStok)
    }

    public void perubahanStok(int jumlah, Transfer transfer, Gudang gudang, String keterangan) {
        ItemStok itemStok = new ItemStok(LocalDate.now(), transfer.nomor, 'Transfer', jumlah, keterangan)
        perubahanStok(gudang, itemStok)
    }

    public void perubahanStok(Gudang gudang, ItemStok itemStok) {
        stok(gudang).tambah(itemStok)
        this.jumlah += itemStok.jumlah
    }

    public boolean tersediaUntuk(int jumlahYangDibutuhkan) {
        if ((jumlah - jumlahAkanDikirim) < jumlahYangDibutuhkan) {
            return false
        }
        true
    }

    public BigDecimal hargaUntuk(Sales sales) {
        sales.dalamKota()? hargaDalamKota: hargaLuarKota
    }

    public Integer getLevelMinimum() {
        levelMinimum?: SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan').getValue(KeyPengaturan.LEVEL_MINIMUM_STOK)
    }

    public boolean periksaLevel() {
        jumlah > getLevelMinimum()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Produk produk = (Produk) o

        if (nama != produk.nama) return false

        return true
    }

    int hashCode() {
        return nama.hashCode()
    }

    String toString() {
        "$nama"
    }

    @Override
    int compareTo(Object o) {
        if (!(o instanceof Produk)) return -1
        nama.compareTo(o.nama)
    }
}


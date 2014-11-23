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

import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.SebuahDaftarBarang
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import groovy.transform.*
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import project.user.NomorService
import simplejpa.DomainClass
import simplejpa.SimpleJpaUtil
import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*
import griffon.util.*

@DomainClass @Entity @Canonical(excludes='penerimaanBarang')
class ReturBeli implements SebuahDaftarBarang {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200)
    String keterangan

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) @OrderColumn
    List<Kemasan> items = []

    @NotNull @ManyToOne
    Supplier supplier

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PenerimaanBarang penerimaanBarang

    @Min(0l)
    BigDecimal nilaiPotonganHutang

    @NotNull
    Boolean sudahDiterima = false

    void tambah(Kemasan kemasan) {
        if (items.find { (it instanceof Kemasan) && (it.nomor == kemasan.nomor)}) {
            throw new DataDuplikat(kemasan)
        }
        if (kemasan.nomor == null) {
            kemasan.nomor = items.size() + 1
        }
        items << kemasan
    }

    void hapus(Kemasan kemasan) {
        items.remove(kemasan)
    }

    PenerimaanBarang terima() {
        if (this.penerimaanBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: SimpleJpaUtil.instance.repositoryManager.findRepository('Gudang').cariGudangUtama(),
            keterangan: "Retur Beli [$nomor]"
        )
        toDaftarBarang().items.each { penerimaanBarang.tambah(it) }
        this.penerimaanBarang = penerimaanBarang
        sudahDiterima = true
        penerimaanBarang
    }

    @Override
    DaftarBarang toDaftarBarang() {
        DaftarBarangSementara tmp = items.sum { Kemasan k -> k.toDaftarBarang() }?: new DaftarBarangSementara()
        DaftarBarangSementara hasil = new DaftarBarangSementara(tmp.normalisasi().sort { it.produk.nama }, -1)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil.keterangan = keterangan
        hasil
    }

}


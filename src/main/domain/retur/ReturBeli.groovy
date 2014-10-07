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
import domain.inventory.DaftarBarangSementara
import domain.inventory.ItemBarang
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import groovy.transform.*
import project.user.NomorService
import simplejpa.DomainClass

import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*
import griffon.util.*

@DomainClass @Entity @Canonical(excludes='penerimaanBarang')
class ReturBeli extends Retur {

    @NotNull @ManyToOne
    Supplier supplier

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PenerimaanBarang penerimaanBarang

    void tambah(KlaimKemasan kemasanRetur) {
        if (listKlaimRetur.find { (it instanceof KlaimKemasan) && (it.nomor == kemasanRetur.nomor)}) {
            throw new DataDuplikat(kemasanRetur)
        }
        if (kemasanRetur.nomor == null) {
            kemasanRetur.nomor = listKlaimRetur.size() + 1
        }
        listKlaimRetur << kemasanRetur
        def itemsBaru = (toDaftarBarang() + kemasanRetur.items).items
        items.clear()
        items.addAll(itemsBaru)
    }

    void hapus(KlaimKemasan kemasanRetur) {
        if (listKlaimRetur.remove(kemasanRetur)) {
            def itemsBaru = (toDaftarBarang() - kemasanRetur.items).items
            items.clear()
            items.addAll(itemsBaru)
        }
    }

    PenerimaanBarang tukar() {
        if (this.penerimaanBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        PenerimaanBarang penerimaanBarang = new PenerimaanBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: gudang,
            keterangan: "Retur Beli [$nomor]"
        )
        List<KlaimKemasan> listKlaimKemasan = getKlaim(KlaimKemasan, true)
        DaftarBarangSementara hasil = new DaftarBarangSementara()
        listKlaimKemasan.each {
            hasil = hasil + it.items
            proses(it)
        }
        hasil.items.each { penerimaanBarang.tambah(it) }
        this.penerimaanBarang = penerimaanBarang
        penerimaanBarang
    }

    @Override
    int faktor() {
        -1
    }

}


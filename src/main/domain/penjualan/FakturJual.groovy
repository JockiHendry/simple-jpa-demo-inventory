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

import domain.exception.StokTidakCukup
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.BolehPesanStok
import domain.penjualan.state.OperasiFakturJual
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang')
abstract class FakturJual extends Faktur implements BolehPesanStok {

    @Enumerated
    StatusFakturJual status

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PengeluaranBarang pengeluaranBarang

    abstract BigDecimal nilaiPenjualan()

    @Override
    void tambah(ItemFaktur itemFaktur) {
        if (itemFaktur.produk.jumlah < itemFaktur.jumlah) {
            throw new StokTidakCukup(itemFaktur.produk.nama, itemFaktur.jumlah, itemFaktur.produk.jumlah)
        }
        super.tambah(itemFaktur)
    }

    abstract OperasiFakturJual getOperasiFakturJual()

    void proses(Map args = null) {
        getOperasiFakturJual().proses(this, args?: [:])
    }

    void prosesSampai(StatusFakturJual statusTujuan, Map args = null) {
        while (status != statusTujuan) {
            def key = status? status.toString(): 'Mulai'
            Map currentArgs = args? args[key]: [:]
            getOperasiFakturJual().proses(this, currentArgs?:[:])
        }
    }

    void hapus() {
        getOperasiFakturJual().hapus(this)
    }

    void tambahRetur(ReturFaktur returFaktur) {
        getOperasiFakturJual().tambahRetur(this, returFaktur)
    }

    void hapusRetur(String nomor) {
        getOperasiFakturJual().hapusRetur(this, nomor)
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


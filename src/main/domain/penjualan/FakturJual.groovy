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
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.exception.StokTidakCukup
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import griffon.util.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang')
abstract class FakturJual extends Faktur {

    @NotNull @Enumerated
    StatusFakturJual status = StatusFakturJual.DIBUAT

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PengeluaranBarang pengeluaranBarang

    protected void tambah(PengeluaranBarang pengeluaranBarang, boolean langsungDikirim = true) {
        if (!status.pengeluaranBolehDiubah || this.pengeluaranBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        this.pengeluaranBarang = pengeluaranBarang
        if (langsungDikirim) {
            kirim()
        }
    }

    protected void kirim() {
        if (!status.pengeluaranBolehDiubah && !pengeluaranBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        status = StatusFakturJual.DIANTAR
        ReferensiStok ref = new ReferensiStokBuilder(pengeluaranBarang, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, ref, false, true))
    }

    public void tambah(BuktiTerima buktiTerima) {
        if (!status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        if (pengeluaranBarang == null) {
            throw new DataTidakKonsisten(this)
        }
        pengeluaranBarang.diterima(buktiTerima)
        status = StatusFakturJual.DITERIMA
    }

    @Override
    void tambah(ItemFaktur itemFaktur) {
        if (itemFaktur.produk.jumlah < itemFaktur.jumlah) {
            throw new StokTidakCukup(itemFaktur.produk.nama, itemFaktur.jumlah, itemFaktur.produk.jumlah)
        }
        super.tambah(itemFaktur)
    }

    public void hapusPengeluaranBarang() {
        if (!status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }
        if (pengeluaranBarang == null) {
            throw new DataTidakKonsisten(this)
        }
        ReferensiStok ref = new ReferensiStokBuilder(pengeluaranBarang, this).buat()
        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, ref, true, true))
        pengeluaranBarang = null
        status = StatusFakturJual.DIBUAT
    }

    public void hapusBuktiTerima() {
        if (status == StatusFakturJual.LUNAS) {
            throw new DataTidakBolehDiubah(this)
        }
        if (pengeluaranBarang == null) {
            throw new DataTidakKonsisten(this)
        }
        pengeluaranBarang.batalDiterima()
        status = StatusFakturJual.DIANTAR
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


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
import domain.event.PerubahanStok
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.exception.DataTidakLengkap
import domain.exception.StokTidakCukup
import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.inventory.ItemBarang
import groovy.transform.*
import groovyx.net.http.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*
import griffon.util.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang') @EqualsAndHashCode(callSuper=true)
abstract class FakturJual extends Faktur {

    @NotNull @Enumerated
    StatusFakturJual status = StatusFakturJual.DIBUAT

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PengeluaranBarang pengeluaranBarang

    protected void tambah(PengeluaranBarang pengeluaranBarang) {
        if (!status.pengeluaranBolehDiubah) {
            throw new DataTidakBolehDiubah(this)
        }

        // Periksa apakah pengeluaran barang sama dengan isi faktur
        if (!pengeluaranBarang.isiSamaDengan(this)) {
            throw new DataTidakKonsisten(pengeluaranBarang)
        }

        this.pengeluaranBarang = pengeluaranBarang
        status = StatusFakturJual.DIANTAR

        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, this))
    }

    public void tambah(BuktiTerima buktiTerima) {
        if (!status.isPengeluaranBolehDiubah()) {
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
            throw new StokTidakCukup(itemFaktur.jumlah, itemFaktur.produk.jumlah)
        }
        super.tambah(itemFaktur)
    }

    public void hapusPengeluaranBarang() {
        if (!status.isPengeluaranBolehDiubah()) {
            throw new DataTidakBolehDiubah(this)
        }
        if (pengeluaranBarang == null) {
            throw new DataTidakKonsisten(this)
        }
        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, this, true))
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

}


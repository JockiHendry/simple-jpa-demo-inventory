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
package domain.pembelian

import domain.exception.DataTidakBolehDiubah
import domain.faktur.Faktur
import domain.inventory.DaftarBarang
import groovy.transform.*
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

@NamedEntityGraph(name='FakturBeli.Complete', attributeNodes = [
    @NamedAttributeNode(value='listItemFaktur'),
    @NamedAttributeNode(value='hutang', subgraph='sHutang')
], subgraphs=[
    @NamedSubgraph(name='hutang', type=Hutang, attributeNodes = [
        @NamedAttributeNode(value='listPembayaran')
    ])
])
@DomainClass @Entity @Canonical @ToString(excludes='hutang')
class FakturBeli extends Faktur {

    @NotNull @ManyToOne
    Supplier supplier

    @NotNull @Enumerated
    StatusFakturBeli status = StatusFakturBeli.DIBUAT

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    Hutang hutang

    Hutang buatHutang(LocalDate jatuhTempo) {
        if (status != StatusFakturBeli.BARANG_DITERIMA) {
            throw new DataTidakBolehDiubah(this)
        }
        if (!jatuhTempo) jatuhTempo = tanggal.plusDays(30)
        hutang = new Hutang(jatuhTempo, false, total())
    }

    void bayarHutang(PembayaranHutang pembayaranHutang) {
        hutang.bayar(pembayaranHutang)
        if (hutang.lunas) {
            status = StatusFakturBeli.LUNAS
        }
    }
}


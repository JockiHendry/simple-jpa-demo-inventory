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

package domain

import domain.container.Application
import groovy.transform.*
import simplejpa.DomainClass
import type.Periode

import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@NamedEntityGraph(name='Produk.Complete', attributeNodes = [
    @NamedAttributeNode(value='daftarStok', subgraph='stokProduk')
], subgraphs = [
    @NamedSubgraph(
        name = 'stokProduk',
        attributeNodes=[@NamedAttributeNode(value='daftarPeriodeItemStok', subgraph='periodeItemStok')]
    ),
    @NamedSubgraph(
        name = 'periodeItemStok',
        attributeNodes = [@NamedAttributeNode(value='listItemStok')]
    )
])
@DomainClass @Entity @Canonical(excludes='daftarStok')
class Produk {

    @NotBlank @Size(min=3, max=150)
    String nama

    @NotNull @Digits(integer=12, fraction=2)
    BigDecimal harga

    @Min(0l)
    Integer jumlah = 0

    @OneToMany(cascade=CascadeType.ALL) @JoinColumn(name='produk_id')
    Set<StokProduk> daftarStok = new HashSet<>()

    public StokProduk stok(Gudang gudang) {
        daftarStok.find { it.gudang == gudang }
    }

}


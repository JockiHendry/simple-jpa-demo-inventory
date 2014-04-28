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

package domain.faktur

import domain.inventory.Produk
import domain.validation.InputPurchaseOrder
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import javax.validation.groups.Default

@Embeddable @Canonical
class ItemFaktur {

    @NotNull(groups=[Default,InputPurchaseOrder]) @ManyToOne
    Produk produk

    @NotNull(groups=[Default,InputPurchaseOrder]) @Min(value=1l, groups=[Default,InputPurchaseOrder])
    Integer jumlah

    @NotNull(groups=[Default])
    BigDecimal harga

    @Size(min=2, max=200, groups=[Default,InputPurchaseOrder])
    String keterangan

    @Embedded
    Diskon diskon

    public BigDecimal total() {
        (diskon? diskon.hasil(harga): harga) * jumlah
    }

    public BigDecimal totalSebelumDiskon() {
        jumlah * harga
    }

    public BigDecimal jumlahDiskon() {
        (diskon? diskon.jumlah(harga): 0) * jumlah
    }

}


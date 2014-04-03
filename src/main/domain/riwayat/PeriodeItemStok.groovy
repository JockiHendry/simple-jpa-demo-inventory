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
package domain.riwayat

import domain.inventory.ItemStok
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*

@DomainClass @Entity @Canonical(excludes="listItem") @EqualsAndHashCode(callSuper=true)
class PeriodeItemStok extends PeriodeRiwayat<ItemStok> {

    @ElementCollection @OrderColumn
    List<ItemStok> listItem = []

}


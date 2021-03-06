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

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@DomainClass @Entity @Canonical
class Supplier implements Comparable {

    @NotEmpty @Size(min=2, max=100)
    String nama

    @Size(min=2, max=150)
    String alamat

    @Size(min=2, max=50)
    String nomorTelepon

    @Override
    String toString() {
        "$nama"
    }

    @Override
    int compareTo(Object o) {
        if (!(o instanceof Supplier)) return -1
        nama? nama.compareTo(o?.nama): -1
    }
}


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
package domain.general

import groovy.transform.*
import simplejpa.AuditableUser
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(includes='nama')
class User implements AuditableUser {

    @NotBlank @Size(min=3, max=50)
    String nama

    @NotNull
    byte[] password = []

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    LocalDateTime loginTerakhir

    @NotEmpty @ElementCollection(fetch=FetchType.EAGER) @Enumerated
    List<Menu> hakAkses = []

    public boolean bolehAkses(Menu menu) {
        hakAkses.contains(menu)
    }

    @Override
    String getUserName() {
        nama
    }

}


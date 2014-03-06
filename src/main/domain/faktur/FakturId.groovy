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

import domain.Application
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.joda.time.LocalDate

import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
class FakturId {

    @NotNull
    Long fakturId

    @NotBlank
    String nomor

    @NotBlank
    String nama

    public Faktur cariFaktur() {
        // TODO: Add implementation later!
    }
}

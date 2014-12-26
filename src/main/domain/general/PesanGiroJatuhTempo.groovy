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

import domain.faktur.BilyetGiro
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='bilyetGiro')
class PesanGiroJatuhTempo extends Pesan {

    @NotNull @ManyToOne(fetch=FetchType.LAZY)
    BilyetGiro bilyetGiro

    @SuppressWarnings("GroovyUnusedDeclaration")
    PesanGiroJatuhTempo() {}

    PesanGiroJatuhTempo(BilyetGiro bilyetGiro) {
        this.tanggal = LocalDateTime.now()
        this.bilyetGiro = bilyetGiro
        this.pesan = "Giro <span class='info'>${bilyetGiro.nomorSeri}</span> untuk bank <span class='info'>${bilyetGiro.namaBank?:'-'}</span> sudah jatuh tempo"
    }

    @Override
    boolean masihBerlaku() {
        !bilyetGiro.sudahDicairkan()
    }

    @Override
    String jenisPesan() {
        "Jatuh Tempo Giro"
    }

}


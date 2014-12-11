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
package project.user

import domain.general.Pesan
import simplejpa.transaction.Transaction
import griffon.core.*
import griffon.util.*

@Transaction
class PesanRepository {

    public static final String EVENT_UPDATE_PESAN = 'UpdatePesan'

    GriffonApplication app = ApplicationHolder.application

    public Pesan buat(Pesan pesan) {
        Pesan hasil = findPesanByPesan(pesan.pesan)
        if (!hasil) {
            persist(pesan)
            hasil = pesan
        }
        app.event(EVENT_UPDATE_PESAN, [true])
        hasil
    }

    public List<Pesan> refresh() {
        List<Pesan> listPesan = findAllPesan()
        for (Pesan pesan: listPesan.toArray()) {
            if (!pesan.masihBerlaku()) {
                listPesan.remove(pesan)
                remove(pesan)
            }
        }
        app.event(EVENT_UPDATE_PESAN, [!listPesan.empty])
        listPesan
    }

    public void hapus(Pesan pesan) {
        remove(pesan)
        app.event(EVENT_UPDATE_PESAN, [!findAllPesan().empty])
    }

}

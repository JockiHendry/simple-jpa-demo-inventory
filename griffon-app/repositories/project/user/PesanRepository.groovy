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

import domain.user.Pesan
import simplejpa.transaction.Transaction

@Transaction
class PesanRepository {

    public Pesan buat(Pesan pesan) {
        persist(pesan)
        pesan
    }

    public List<Pesan> refresh() {
        List<Pesan> listPesan = findAllPesan()
        for (Pesan pesan: listPesan.toArray()) {
            if (!pesan.masihBerlaku()) {
                listPesan.remove(pesan)
                remove(pesan)
            }
        }
        listPesan
    }

}

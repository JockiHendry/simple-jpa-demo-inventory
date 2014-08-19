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

import domain.Container
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.user.Menu
import domain.user.User
import org.joda.time.LocalDateTime
import simplejpa.transaction.Transaction

@Transaction
class UserRepository {

    private static final String DEFAULT_PASSWORD = '12345'

    List<User> cari(String namaSearch) {
        findAllUserByDsl {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
        }
    }

    User buat(User user, String password = null) {
        if (findUserByNama(user.nama)) {
            throw new DataDuplikat(user)
        }
        user.password = Container.app.passwordService.plainTextToEncrypted(password?:DEFAULT_PASSWORD)
        persist(user)
        user
    }

    User setPassword(User user, String passwordBaru) {
        user = findUserById(user.id)
        if (!user) {
            throw new DataTidakBolehDiubah(user)
        }
        user.password = Container.app.passwordService.plainTextToEncrypted(passwordBaru)
        user
    }

    User resetPassword(User user) {
        setPassword(user, DEFAULT_PASSWORD)
    }

    User login(String namaUser, String inputPassword) {
        User user = findUserByNama(namaUser)
        if (user==null && namaUser.equals('admin')) {
            user = new User(nama: 'admin')
            Menu.values().each { user.hakAkses << it }
            buat(user)
        }
        if (user && Container.app.passwordService.periksaPassword(user.password, inputPassword)) {
            user.loginTerakhir = LocalDateTime.now()
            return user
        }
        null
    }

    User update(User user, String passwordBaru = null) {
        User mergedUser = findUserById(user.id)
        if (!mergedUser) {
            throw DataTidakBolehDiubah(this)
        }
        mergedUser.with {
            nama = user.nama
            hakAkses.clear()
            hakAkses.addAll(user.hakAkses)
            if (passwordBaru && passwordBaru.length() > 0) {
                setPassword(mergedUser, passwordBaru)
            }
        }
        mergedUser
    }
}

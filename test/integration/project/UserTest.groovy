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
package project

import domain.Container
import domain.user.Menu
import domain.user.User
import domain.user.UserRepository
import simplejpa.testing.DbUnitTestCase

class UserTest extends DbUnitTestCase {

    protected void setUp() {
        super.setUp()
        Container.app.setupListener()
        setUpDatabase("gudang", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    public void testBuat() {
        UserRepository repo = Container.app.userRepository

        User userA = new User(nama: 'userA', hakAkses: [Menu.MAINTENANCE])
        userA = repo.buat(userA)
        assertEquals('userA', userA.nama)
        assertTrue(userA.password.length > 0)
        assertEquals(userA, repo.login('userA', repo.DEFAULT_PASSWORD))

        User userB = new User(nama: 'userB', hakAkses: [Menu.MAINTENANCE])
        userB = repo.buat(userB, 'mysolidpassword')
        assertEquals('userB', userB.nama)
        assertTrue(userB.password.length > 0)
        assertEquals(userB, repo.login('userB', 'mysolidpassword'))

        repo.remove(userA)
        repo.remove(userB)
    }

    public void testLogin() {
        UserRepository repo = Container.app.userRepository

        User user = new User(nama: 'theUser', hakAkses: [Menu.MAINTENANCE])
        user = repo.buat(user, 'mysolidpassword')

        assertEquals(user, repo.login('theUser', 'mysolidpassword'))
        assertNull(repo.login('theUser', 'passwordSembarangan'))

        repo.remove(user)
    }

    public void testSetPassword() {
        UserRepository repo = Container.app.userRepository

        User user = new User(nama: 'theUser', hakAkses: [Menu.MAINTENANCE])
        user = repo.buat(user)
        assertEquals(user, repo.login('theUser', repo.DEFAULT_PASSWORD))
        user = repo.setPassword(user, 'passwordBaru')
        assertEquals(user, repo.login('theUser', 'passwordBaru'))

        repo.remove(user)
    }

}

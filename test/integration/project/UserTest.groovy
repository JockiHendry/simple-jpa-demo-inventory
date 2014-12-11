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

import domain.general.Menu
import domain.general.User
import project.user.UserRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class UserTest extends DbUnitTestCase {

    UserRepository userRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_inventory.xlsx")
        userRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('User')
    }

    public void testBuat() {
        User userA = new User(nama: 'userA', hakAkses: [Menu.MAINTENANCE])
        userA = userRepository.buat(userA)
        assertEquals('userA', userA.nama)
        assertTrue(userA.password.length > 0)
        assertEquals(userA, userRepository.login('userA', UserRepository.DEFAULT_PASSWORD))

        User userB = new User(nama: 'userB', hakAkses: [Menu.MAINTENANCE])
        userB = userRepository.buat(userB, 'mysolidpassword')
        assertEquals('userB', userB.nama)
        assertTrue(userB.password.length > 0)
        assertEquals(userB, userRepository.login('userB', 'mysolidpassword'))

        userRepository.remove(userA)
        userRepository.remove(userB)
    }

    public void testLogin() {
        User user = new User(nama: 'theUser', hakAkses: [Menu.MAINTENANCE])
        user = userRepository.buat(user, 'mysolidpassword')

        assertEquals(user, userRepository.login('theUser', 'mysolidpassword'))
        assertNull(userRepository.login('theUser', 'passwordSembarangan'))

        userRepository.remove(user)
    }

    public void testSetPassword() {
        User user = new User(nama: 'theUser', hakAkses: [Menu.MAINTENANCE])
        user = userRepository.buat(user)
        assertEquals(user, userRepository.login('theUser', userRepository.DEFAULT_PASSWORD))
        user = userRepository.setPassword(user, 'passwordBaru')
        assertEquals(user, userRepository.login('theUser', 'passwordBaru'))

        userRepository.remove(user)
    }

}

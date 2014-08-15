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
package domain.user

import domain.Container
import org.jdesktop.swingx.auth.LoginService
import simplejpa.SimpleJpaUtil

import javax.swing.JOptionPane

class UserLoginService extends LoginService {

    @Override
    boolean authenticate(String nama, char[] password, String server) throws Exception {
        User user = (SimpleJpaUtil.container.userRepository as UserRepository).login(nama, new String(password))
        if (user) {
            Container.app.currentUser = user
            return true
        }
        false
    }

}

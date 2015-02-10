/*
 * Copyright 2015 Jocki Hendry.
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

import domain.exception.TanggalLampau
import org.jdesktop.swingx.auth.LoginService
import simplejpa.SimpleJpaUtil
import simplejpa.swing.DialogUtils
import javax.swing.JOptionPane

@SuppressWarnings("GroovyUnusedDeclaration")
class UserService extends LoginService {

    UserRepository userRepository

    @Override
    boolean authenticate(String nama, char[] password, String server) throws Exception {
        try {
            SimpleJpaUtil.instance.user = userRepository.login(nama, new String(password))
        } catch (TanggalLampau ex) {
            DialogUtils.message(null, ex.message, 'Kesalahan Tanggal', JOptionPane.ERROR_MESSAGE)
        }
        return (SimpleJpaUtil.instance.user != null)
    }

}

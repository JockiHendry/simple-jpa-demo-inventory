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

package domain

import domain.penjualan.BilyetGiroClearingService
import domain.user.User
import domain.user.UserLoginService
import domain.util.NomorService
import domain.util.PasswordService
import griffon.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import griffon.core.*

class Container {

    private final Logger log = LoggerFactory.getLogger(Container)

    public static Container app = new Container()

    public static final String SEMUA = "Semua"

    User currentUser

    PasswordService passwordService
    NomorService nomorService
    BilyetGiroClearingService bilyetGiroClearingService
    UserLoginService userLoginService

    private Container() {
        setup()
    }

    public void setup() {
        // Create services
        passwordService = new PasswordService()
        nomorService = new NomorService()
        bilyetGiroClearingService = new BilyetGiroClearingService()
        userLoginService = new UserLoginService()
    }

    public List searchEnum(Class enumeration) {
        List result = [SEMUA]
        result.addAll(EnumSet.allOf(enumeration))
        result
    }
}

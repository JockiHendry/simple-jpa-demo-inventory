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

import java.security.MessageDigest

class PasswordService {

    public byte[] plainTextToEncrypted(char[] plain) {
        MessageDigest digester = MessageDigest.getInstance('MD5')
        digester.update((byte[]) plain)
        digester.digest()
    }

    public byte[] plainTextToEncrypted(String plain) {
        plainTextToEncrypted((char[])plain.getBytes())
    }

    public boolean periksaPassword(byte[] diharapkan, String inputPassword) {
        Arrays.equals(diharapkan, plainTextToEncrypted(inputPassword))
    }

    public boolean periksaPassword(byte[] diharapkan, char[] inputPassword) {
        Arrays.equals(diharapkan, plainTextToEncrypted(inputPassword))
    }

}
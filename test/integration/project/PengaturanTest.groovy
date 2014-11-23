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

import domain.pengaturan.JenisNilai
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.Pengaturan
import project.pengaturan.PengaturanRepository
import project.user.PasswordService
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class PengaturanTest extends DbUnitTestCase {

    PengaturanRepository pengaturanRepository

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_inventory.xlsx")
        pengaturanRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan')
    }

    void testSerializing() {
        PasswordService p = new PasswordService()
        byte[] nilai

        String s = "My name is Jocki Hendry"
        nilai = pengaturanRepository.serialize(s, JenisNilai.STRING)
        assertEquals(s, pengaturanRepository.deserialize(nilai, JenisNilai.STRING))

        String pwd = "super-secret-password-12345"
        nilai = pengaturanRepository.serialize(pwd, JenisNilai.PASSWORD)
        assertTrue(Arrays.equals(p.plainTextToEncrypted(pwd), pengaturanRepository.deserialize(nilai, JenisNilai.PASSWORD)))

        Integer i = 12345678
        nilai = pengaturanRepository.serialize(i, JenisNilai.INTEGER)
        assertEquals(i, pengaturanRepository.deserialize(nilai, JenisNilai.INTEGER))
    }

    void testSave() {
        PasswordService p = new PasswordService()
        Pengaturan pengaturan = pengaturanRepository.save(KeyPengaturan.SUPERVISOR_PASSWORD, 'super_secret_password')
        assertEquals(KeyPengaturan.SUPERVISOR_PASSWORD, pengaturan.keyPengaturan)
        assertEquals(p.plainTextToEncrypted('super_secret_password'), pengaturanRepository.cache[KeyPengaturan.SUPERVISOR_PASSWORD])
    }
}

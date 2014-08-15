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
import domain.pengaturan.JenisNilai
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.Pengaturan
import domain.pengaturan.PengaturanRepository
import domain.util.PasswordService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.testing.DbUnitTestCase

import java.security.MessageDigest

class PengaturanTest extends DbUnitTestCase {

    private static final Logger log = LoggerFactory.getLogger(ProdukTest)

    protected void setUp() {
        super.setUp()
        setUpDatabase("produk", "/project/data_inventory.xls")
    }

    protected void tearDown() {
        super.tearDown()
        super.deleteAll()
    }

    void testSerializing() {
        PengaturanRepository repo = new PengaturanRepository()
        PasswordService p = new PasswordService()
        byte[] nilai

        String s = "My name is Jocki Hendry"
        nilai = repo.serialize(s, JenisNilai.STRING)
        assertEquals(s, repo.deserialize(nilai, JenisNilai.STRING))

        String pwd = "super-secret-password-12345"
        nilai = repo.serialize(pwd, JenisNilai.PASSWORD)
        assertTrue(Arrays.equals(p.plainTextToEncrypted(pwd), repo.deserialize(nilai, JenisNilai.PASSWORD)))

        Integer i = 12345678
        nilai = repo.serialize(i, JenisNilai.INTEGER)
        assertEquals(i, repo.deserialize(nilai, JenisNilai.INTEGER))
    }

    void testSave() {
        PengaturanRepository repo = new PengaturanRepository()
        PasswordService p = new PasswordService()
        Pengaturan pengaturan = repo.save(KeyPengaturan.SUPERVISOR_PASSWORD, 'super_secret_password')
        assertEquals(KeyPengaturan.SUPERVISOR_PASSWORD, pengaturan.keyPengaturan)
        assertEquals(p.plainTextToEncrypted('super_secret_password'), repo.cache[KeyPengaturan.SUPERVISOR_PASSWORD])
    }
}

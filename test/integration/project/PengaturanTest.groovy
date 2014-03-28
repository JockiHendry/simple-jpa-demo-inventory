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
import domain.pengaturan.PengaturanRepository
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
    }

    void testSerializing() {
        PengaturanRepository p = new PengaturanRepository()
        byte[] nilai

        String s = "My name is Jocki Hendry"
        nilai = p.serialize(s, JenisNilai.STRING)
        assertEquals(s, p.deserialize(nilai, JenisNilai.STRING))

        String pwd = "super-secret-password-12345"
        nilai = p.serialize(pwd, JenisNilai.PASSWORD)
        MessageDigest digester = MessageDigest.getInstance('MD5')
        digester.update(pwd.bytes)
        assertTrue(Arrays.equals(digester.digest(), p.deserialize(nilai, JenisNilai.PASSWORD)))

        Integer i = 12345678
        nilai = p.serialize(i, JenisNilai.INTEGER)
        assertEquals(i, p.deserialize(nilai, JenisNilai.INTEGER))
    }

    void testSave() {
        PengaturanRepository repo = new PengaturanRepository()
        Pengaturan pengaturan = repo.save(KeyPengaturan.SUPERVISOR_PASSWORD, 'super_secret_password')
        assertEquals(KeyPengaturan.SUPERVISOR_PASSWORD, pengaturan.keyPengaturan)
        MessageDigest digester = MessageDigest.getInstance('MD5')
        digester.update('super_secret_password'.bytes)
        assertEquals(digester.digest(), repo.cache[KeyPengaturan.SUPERVISOR_PASSWORD])
    }
}

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
package project.pengaturan

import domain.pengaturan.JenisNilai
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.Pengaturan
import project.user.PasswordService
import simplejpa.transaction.Transaction
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

@Transaction
class PengaturanRepository {

    public final Map cache = new ConcurrentHashMap()

    PasswordService passwordService

    @Transaction(Transaction.Policy.SKIP)
    public def getValue(KeyPengaturan keyPengaturan) {
        cache[keyPengaturan]?: keyPengaturan.defaultValue
    }

    @Transaction(Transaction.Policy.SKIP)
    public byte[] serialize(def value, JenisNilai jenisNilai) {
        switch (jenisNilai) {
            case JenisNilai.STRING:
                return ((String) value).bytes

            case JenisNilai.PASSWORD:
                return passwordService.plainTextToEncrypted((String)value)

            case JenisNilai.INTEGER:
                return ByteBuffer.allocate(4).putInt(value).array()
        }
    }

    @Transaction(Transaction.Policy.SKIP)
    public def deserialize(byte[] source, JenisNilai jenisNilai) {
        switch (jenisNilai) {
            case JenisNilai.STRING:
                return new String(source)

            case JenisNilai.PASSWORD:
                return source

            case JenisNilai.INTEGER:
                return ByteBuffer.wrap(source).getInt()
        }
    }

    public void refreshAll() {
        KeyPengaturan.values().each { KeyPengaturan keyPengaturan ->
            Pengaturan pengaturan = findPengaturanByKeyPengaturan(keyPengaturan)
            def value
            if (pengaturan?.nilai != null) {
                value = deserialize(pengaturan.nilai, keyPengaturan.jenisNilai)
            } else {
                value = keyPengaturan.defaultValue
            }
            cache[keyPengaturan] = value
        }
    }

    public Pengaturan save(KeyPengaturan keyPengaturan, Object nilai) {
        if (nilai == null) {
            throw new IllegalArgumentException('Nilai tidak boleh kosong!')
        }
        if (keyPengaturan.jenisNilai == JenisNilai.INTEGER) {
            try {
                nilai = Integer.parseInt((String) nilai)
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException('Nilai harus berupa angka!')
            }
        } else if (keyPengaturan.jenisNilai == JenisNilai.PASSWORD ||
                   keyPengaturan.jenisNilai == JenisNilai.STRING) {
            if (((String) nilai).isAllWhitespace()) {
                throw new IllegalArgumentException('Nilai bukan password yang valid!')
            }
        }

        Pengaturan pengaturan = findPengaturanByKeyPengaturan(keyPengaturan)
        if (pengaturan) {
            pengaturan.nilai = serialize(nilai, keyPengaturan.jenisNilai)
        } else {
            pengaturan = new Pengaturan(keyPengaturan: keyPengaturan, nilai: serialize(nilai, keyPengaturan.jenisNilai))
            persist(pengaturan)
        }

        if (keyPengaturan.jenisNilai == JenisNilai.PASSWORD) {
            cache[keyPengaturan] = pengaturan.nilai
        } else {
            cache[keyPengaturan] = nilai
        }
        pengaturan
    }

}

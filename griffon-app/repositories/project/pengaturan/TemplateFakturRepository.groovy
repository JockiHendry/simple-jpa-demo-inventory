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
package project.pengaturan

import domain.pengaturan.NamaTemplateFaktur
import domain.pengaturan.TemplateFaktur
import simplejpa.transaction.Transaction
import java.util.concurrent.ConcurrentHashMap

@Transaction
class TemplateFakturRepository {

    def app

    public final Map cache = new ConcurrentHashMap()

    @Transaction(Transaction.Policy.SKIP)
    public String getValue(NamaTemplateFaktur namaTemplateFaktur) {
        if (!cache.containsKey(namaTemplateFaktur)) {
            def resource = app.getResourceAsStream(namaTemplateFaktur.file)
            if (resource) {
                cache[namaTemplateFaktur] = resource.text
            } else {
                throw new RuntimeException("Tidak dapat membaca isi file ${namaTemplateFaktur.file} untuk template $namaTemplateFaktur")
            }
        }
        cache[namaTemplateFaktur]
    }

    void refreshAll() {
        NamaTemplateFaktur.values().each { NamaTemplateFaktur namaTemplateFaktur ->
            TemplateFaktur templateFaktur = findTemplateFakturByNama(namaTemplateFaktur)
            if (templateFaktur) {
                cache[templateFaktur.nama] = templateFaktur.isi
            }
        }
    }

    public TemplateFaktur save(NamaTemplateFaktur namaTemplateFaktur, String nilai) {
        TemplateFaktur templateFaktur = findTemplateFakturByNama(namaTemplateFaktur)
        if (!templateFaktur) {
            templateFaktur = new TemplateFaktur(nama: namaTemplateFaktur)
            persist(templateFaktur)
        }
        templateFaktur.isi = nilai
        cache[templateFaktur.nama] = nilai
        templateFaktur
    }

    public TemplateFaktur reset(NamaTemplateFaktur namaTemplateFaktur) {
        TemplateFaktur templateFaktur = findTemplateFakturByNama(namaTemplateFaktur)
        if (templateFaktur) {
            def resource = app.getResourceAsStream(namaTemplateFaktur.file)
            if (resource) {
                String defaultContent = resource.text
                templateFaktur.isi = defaultContent
                cache[templateFaktur.nama] = defaultContent
            } else {
                throw new RuntimeException("Tidak dapat membaca isi file ${namaTemplateFaktur.file} untuk template $namaTemplateFaktur")
            }
        }
        templateFaktur
    }

}

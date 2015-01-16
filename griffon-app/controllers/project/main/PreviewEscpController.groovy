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
package project.main

import domain.pengaturan.KeyPengaturan
import project.pengaturan.PengaturanRepository
import project.pengaturan.TemplateFakturRepository
import simple.escp.data.DataSources
import simple.escp.json.JsonTemplate
import simple.escp.swing.PrintPreviewPane
import simplejpa.SimpleJpaUtil

@SuppressWarnings("GroovyUnusedDeclaration")
class PreviewEscpController {

    def model
    def view
    PengaturanRepository pengaturanRepository
    TemplateFakturRepository templateFakturRepository

    void mvcGroupInit(Map args) {
        model.namaTemplateFaktur = args.namaTemplateFaktur
        model.dataSource = args.dataSource
        model.options = (args.containsKey('options')? args.options: [:])
        model.showParameter = (args.containsKey('showParameter')?: false)
        refresh()
    }

    def refresh = {
        execInsideUISync {
            JsonTemplate template = new JsonTemplate(templateFakturRepository.getValue(model.namaTemplateFaktur))
            model.options['createdBy'] = (model.dataSource.hasProperty('createdBy')? model.dataSource.createdBy: null)?: SimpleJpaUtil.instance.user.userName
            model.options['companyName'] = pengaturanRepository.getValue(KeyPengaturan.NAMA_PERUSAHAAN)
            model.options['cetakJatuhTempo'] = model.cetakJatuhTempo
            PrintPreviewPane printPreviewPane = view.printPreviewPane
            printPreviewPane.display(template, DataSources.from(model.dataSource, model.options))
        }
    }

}

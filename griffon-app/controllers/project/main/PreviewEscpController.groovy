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
package project.main

import domain.Container
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.PengaturanRepository
import simple.escp.data.DataSources
import simple.escp.json.JsonTemplate
import simple.escp.swing.PrintPreviewPane

class PreviewEscpController {

    def model
    def view
    PengaturanRepository pengaturanRepository

    void mvcGroupInit(Map args) {
        JsonTemplate template = new JsonTemplate(getResourceAsStream("escp/${args.'template'}"))
        def source = args.'dataSource'
        Map options = [:]
        options['createdBy'] = (source.hasProperty('createdBy')? source.createdBy: null) ?: Container.app.currentUser.nama
        options['companyName'] = pengaturanRepository.getValue(KeyPengaturan.NAMA_PERUSAHAAN)
        PrintPreviewPane printPreviewPane = view.printPreviewPane
        printPreviewPane.display(template, DataSources.from(source, options))
    }

}

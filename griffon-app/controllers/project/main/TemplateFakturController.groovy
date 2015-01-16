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

import domain.pengaturan.NamaTemplateFaktur
import domain.pengaturan.TemplateFaktur
import groovy.ui.text.TextEditor
import project.pengaturan.TemplateFakturRepository
import simplejpa.swing.DialogUtils
import util.SimpleEscpFilter
import javax.swing.text.DefaultStyledDocument

@SuppressWarnings("GroovyUnusedDeclaration")
class TemplateFakturController {

    TemplateFakturModel model
    def view
    TemplateFakturRepository templateFakturRepository

    def cari = {
        execInsideUISync {
            NamaTemplateFaktur namaTemplateFaktur = model.namaTemplateFaktur.selectedItem
            if (namaTemplateFaktur) {
                TextEditor textEditor = view.inputEditor.textEditor
                DefaultStyledDocument doc = new DefaultStyledDocument()
                doc.setDocumentFilter(new SimpleEscpFilter(doc))
                doc.insertString(0, templateFakturRepository.getValue(namaTemplateFaktur) ?: '', null)
                textEditor.setDocument(doc)
                textEditor.caretPosition = 0
            }
        }
    }

    def simpan = {
        NamaTemplateFaktur namaTemplateFaktur
        String nilai
        execInsideUISync {
            namaTemplateFaktur = model.namaTemplateFaktur.selectedItem
            TextEditor textEditor = view.inputEditor.textEditor
            nilai = textEditor.text
        }
        templateFakturRepository.save(namaTemplateFaktur, nilai)
    }

    def reset = {
        if (!DialogUtils.confirm(view.mainPanel, 'Apakah Anda yakin me-reset template faktur menjadi seperti semula?', 'Konfirmasi Reset')) {
            return
        }
        NamaTemplateFaktur namaTemplateFaktur = model.namaTemplateFaktur.selectedItem
        TemplateFaktur templateFaktur = templateFakturRepository.reset(namaTemplateFaktur)
        execInsideUISync {
            TextEditor textEditor = view.inputEditor.textEditor
            DefaultStyledDocument doc = new DefaultStyledDocument()
            doc.setDocumentFilter(new SimpleEscpFilter(doc))
            doc.insertString(0, templateFaktur.isi?: '', null)
            textEditor.setDocument(doc)
            textEditor.caretPosition = 0
        }
    }

}

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
package project

import domain.pengaturan.NamaTemplateFaktur
import project.pengaturan.TemplateFakturRepository
import simplejpa.SimpleJpaUtil
import simplejpa.testing.DbUnitTestCase

class TemplateFakturTest extends DbUnitTestCase {

    TemplateFakturRepository templateFakturRepository

    protected void setUp() {
        super.setUp()
        templateFakturRepository = SimpleJpaUtil.instance.repositoryManager.findRepository('TemplateFaktur')
    }

    void testAll() {
        String defaultTemplate = app.getResourceAsStream('escp/faktur_jual_sales.json').text
        assertEquals(defaultTemplate, templateFakturRepository.getValue(NamaTemplateFaktur.FAKTUR_JUAL_SALES))

        // update default template
        templateFakturRepository.save(NamaTemplateFaktur.FAKTUR_JUAL_SALES, 'NEW VALUE')
        assertEquals('NEW VALUE', templateFakturRepository.getValue(NamaTemplateFaktur.FAKTUR_JUAL_SALES))

        // reset default template
        templateFakturRepository.reset(NamaTemplateFaktur.FAKTUR_JUAL_SALES)
        assertEquals(defaultTemplate, templateFakturRepository.getValue(NamaTemplateFaktur.FAKTUR_JUAL_SALES))
    }

}

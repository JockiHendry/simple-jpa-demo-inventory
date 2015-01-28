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
package util

import groovy.ui.text.StructuredSyntaxDocumentFilter
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import java.awt.Color

class SimpleEscpFilter extends StructuredSyntaxDocumentFilter {

    public static final String VARIABLES = /(?ms:\$\{.*?\})/
    public static final String FUNCTIONS = /(?ms:%\{.*?\})/
    public static final String CODE = /(?ms:\{\{.*?\}\})/

    SimpleEscpFilter(DefaultStyledDocument document) {
        super(document)

        StyleContext styleContext = StyleContext.getDefaultStyleContext()
        Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE)

        Style variables = styleContext.addStyle(VARIABLES, defaultStyle)
        StyleConstants.setForeground(variables, Color.GREEN.darker().darker())
        getRootNode().putStyle(VARIABLES, variables)

        Style functions = styleContext.addStyle(FUNCTIONS, defaultStyle)
        StyleConstants.setForeground(functions, Color.BLUE.darker().darker())
        getRootNode().putStyle(FUNCTIONS, functions)

        Style code = styleContext.addStyle(CODE, defaultStyle)
        StyleConstants.setForeground(code, Color.MAGENTA.darker().darker())
        getRootNode().putStyle(CODE, code)
    }

}
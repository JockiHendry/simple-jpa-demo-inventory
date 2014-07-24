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
package ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.slf4j.LoggerFactory

@GroovyASTTransformation
class AuditableTransformation extends AbstractASTTransformation{

    private static final log = LoggerFactory.getLogger(AuditableTransformation)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        ClassNode classNode = astNodes[1]
        log.debug "Performing @Auditable transformation for ${classNode}..."
        classNode.addField("createdBy", ACC_PUBLIC, ClassHelper.make(String.class), null)
        classNode.addField("modifiedBy", ACC_PUBLIC, ClassHelper.make(String.class), null)
    }

}

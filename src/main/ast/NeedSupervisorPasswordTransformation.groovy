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

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.slf4j.LoggerFactory
import org.codehaus.groovy.ast.stmt.*

@GroovyASTTransformation
class NeedSupervisorPasswordTransformation extends AbstractASTTransformation{

    private static final log = LoggerFactory.getLogger(NeedSupervisorPasswordTransformation)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        AnnotationNode annotation = astNodes[0]
        AnnotatedNode node = astNodes[1]
        log.debug "Performing @NeedSupervisorPassword transformation for ${node}..."

        if (node instanceof FieldNode) {

            if (node.initialExpression instanceof ClosureExpression) {
                log.debug "Field name: ${node.name}"
                wrapStatements(node.initialExpression, node)
            }

        } else if (node instanceof MethodNode) {

            log.debug "Method name: ${node.name}"
            wrapStatements(node, node)
        }
    }

    private static void wrapStatements(def method, AnnotatedNode node) {
        Statement code = method.getCode()

        // begin wrap code

        BlockStatement wrappedCode = new BlockStatement()

        Statement line1 = new AstBuilder().buildFromCode(CompilePhase.CANONICALIZATION) {
            java.lang.Boolean passwordCorrect = simplejpa.swing.DialogUtils.showMVCGroup('supervisorPassword', [:],
                view, ['title': 'Password Supervisor', 'size': new java.awt.Dimension(500, 150)], null,
                { java.lang.Object m, java.lang.Object v, java.lang.Object c -> m .ok })
        }[0].getStatements()[0]
        IfStatement line2 = new IfStatement(new BooleanExpression(new VariableExpression('passwordCorrect')),
            code, EmptyStatement.INSTANCE)
        wrappedCode.addStatement(line1)
        wrappedCode.addStatement(line2)

        // end wrap code

        method.setCode(wrappedCode)
    }

}

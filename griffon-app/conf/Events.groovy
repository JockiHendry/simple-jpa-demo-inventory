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
import simplejpa.SimpleJpaUtil
import simplejpa.transaction.TransactionHolder
import util.BusyLayerUI
import griffon.util.*
import util.HttpUtil
import util.SplashScreen
import javax.validation.ConstraintViolationException

//noinspection GroovyUnusedAssignment
onUncaughtExceptionThrown = { Exception e ->
    BusyLayerUI.instance.hide()
    if (e instanceof org.codehaus.groovy.runtime.InvokerInvocationException) e = e.cause
    if (e.cause instanceof ConstraintViolationException) {
        ConstraintViolationException cv = e.cause
        StringBuilder pesan = new StringBuilder()
        cv.constraintViolations.each {
            if (it.propertyPath) {
                pesan.append(GriffonNameUtils.getNaturalName(it.propertyPath.toString()))
                pesan.append(' di ')
            }
            if (it.rootBeanClass) {
                pesan.append(GriffonNameUtils.getNaturalName(it.rootBeanClass.simpleName))
                pesan.append(' ')
            }
            pesan.append(it.message)
            pesan.append('\n')
        }
        javax.swing.JOptionPane.showMessageDialog(null, pesan.toString(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE)
    } else {
        javax.swing.JOptionPane.showMessageDialog(null, e.message, "Error", javax.swing.JOptionPane.ERROR_MESSAGE)
    }

    def stringWriter = new StringWriter()
    e.printStackTrace(new PrintWriter(stringWriter))
    HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, stringWriter.toString())
    if (SplashScreen.instance.window.visible) {
        System.exit(-1)
    }
}

//noinspection GroovyUnusedAssignment
onSimpleJpaNewTransaction = { TransactionHolder th ->
    BusyLayerUI.instance.show()
}

//noinspection GroovyUnusedAssignment
onSimpleJpaCommitTransaction = { TransactionHolder th ->
    BusyLayerUI.instance.hide()
}

//noinspection GroovyUnusedAssignment
onSimpleJpaRollbackTransaction = { TransactionHolder th ->
    BusyLayerUI.instance.hide()
}

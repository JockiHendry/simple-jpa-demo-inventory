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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.daemon.DaemonService
import simplejpa.SimpleJpaUtil
import griffon.util.*
import util.HttpUtil
import javax.validation.ConstraintViolationException

final Logger log = LoggerFactory.getLogger(DaemonService)

//noinspection GroovyUnusedAssignment
onUncaughtExceptionThrown = { Exception e ->
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
    }

    def stringWriter = new StringWriter()
    e.printStackTrace(new PrintWriter(stringWriter))
    log.error e.message
    HttpUtil.instance.sendNotification(SimpleJpaUtil.instance.user?.userName, stringWriter.toString())
}
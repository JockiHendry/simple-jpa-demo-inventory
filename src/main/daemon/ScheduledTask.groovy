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
package daemon

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.HttpUtil

class ScheduledTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTask)

    Closure action

    ScheduledTask action(Closure action) {
        this.action = action
        this
    }

    @Override
    void run() {
        try {
            action()
        } catch (Throwable e) {
            if (e instanceof org.codehaus.groovy.runtime.InvokerInvocationException) e = e.cause
            def stringWriter = new StringWriter()
            e.printStackTrace(new PrintWriter(stringWriter))
            log.error e.message
            HttpUtil.instance.sendNotification('daemon', stringWriter.toString())
        }
    }

}

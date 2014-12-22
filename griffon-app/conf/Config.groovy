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

//noinspection GroovyUnusedAssignment
log4j = {
    development {
        appenders {
            rollingFile name: 'daemonLog', file: "${System.getProperty('user.home')}/inventory/daemon.log", layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'),
                maxFileSize: 10485760, maxBackupIndex: 20
        }
        root {
            debug 'daemonLog'
            additivity = false
        }
        error  additivity: false, daemonLog: ['org.dbunit']
        debug additivity: false, daemonLog: ['org.hibernate.SQL', 'project']
        info  additivity: false, daemonLog: [
            'simplejpa',
            'project',
            'domain',
            'com.mchange'
            //'net.sf.jasperreports',
            //'org.jboss',
            //'org.codehaus',
            //'griffon.util',
            //'griffon.core',
            //'griffon.swing',
            //'griffon.app']
            ]
    }

    production {
        appenders {
            rollingFile name: 'daemonLog', file: "${System.getProperty('user.home')}/inventory/daemon.log", layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'),
                maxFileSize: 10485760, maxBackupIndex: 20
        }
        root {
            debug 'daemonLog'
        }
    }
}

i18n.basenames = ['messages','ValidationMessages']

griffon.simplejpa.finders.injectInto = ['service', 'repository']
griffon.simplejpa.validation.convertEmptyStringToNull = true
griffon.simplejpa.finders.alwaysExcludeSoftDeleted = true
griffon.simplejpa.scaffolding.generator = 'simplejpa.scaffolding.generator.ddd.DDDGenerator'

environments {
    production {
        // Read actual database configuration (server, user, and password) from properties file.
        griffon.config.locations = ['classpath:server.properties']
    }
}

griffon {
    simplejpa {
        entityManager {
            properties {
                environments {
                    development {
                        if (System.getProperty('simplejpa.cleandb') == 'true') {
                            javax.persistence.'schema-generation'.database.action = 'drop-and-create'
                            javax.persistence.'sql-load-script-source' = 'META-INF/data.sql'
                        }
                    }
                    test {
                        javax.persistence.'schema-generation'.database.action = 'drop-and-create'
                    }
                }
            }
        }
    }
}
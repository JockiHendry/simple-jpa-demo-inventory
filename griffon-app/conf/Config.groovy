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

log4j = {
    environments {

        development {
            appenders {
                console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
                rollingFile name: 'sqlLog', file: "${System.getProperty('user.home')}/inventory/sql.log",
                    layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'),
                    maxFileSize: 10485760, maxBackupIndex: 20
            }
            root {
                warn 'stdout'
                additivity = false
            }
            error  additivity: false, stdout: ['org.dbunit']
            debug additivity: false, stdout: ['org.hibernate.SQL', 'project']
            info  additivity: false, stdout: [
                    'simplejpa',
                    'project',
                    'domain',
                    //'net.sf.jasperreports',
                    //'org.jboss',
                    //'org.codehaus',
                    //'griffon.util',
                    //'griffon.core',
                    //'griffon.swing',
                    //'griffon.app']
                    ]
            debug  sqlLog: ['com.mysql.jdbc.log']
        }

        test {
            appenders {
                console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
            }
            root {
                warn 'stdout'
                additivity = false
            }
            error  additivity: false, stdout: ['org.dbunit']
            debug additivity: false, stdout: [
                'org.hibernate.SQL',
                'project',
                'domain',
            ]
            info additivity: false, stdout: [
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
                rollingFile name: 'errorLog', file: "${System.getProperty('user.home')}/inventory/error.log", layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'),
                    maxFileSize: 10485760, maxBackupIndex: 20
            }
            root {
                error 'errorLog'
            }
        }
    }
}
i18n.basenames = ['messages','ValidationMessages']

griffon.simplejpa.finders.injectInto = ['service', 'repository']
griffon.simplejpa.validation.convertEmptyStringToNull = true
griffon.simplejpa.finders.alwaysExcludeSoftDeleted = true
griffon.simplejpa.entityManager.propertiesFile = "${System.getProperty('user.home')}/inventory/simplejpa.properties"
griffon.simplejpa.scaffolding.generator = 'simplejpa.scaffolding.generator.ddd.DDDGenerator'

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
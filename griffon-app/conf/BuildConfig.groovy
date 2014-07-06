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

// key signing information
environments {
    development {
        signingkey {
            params {
                // sigfile = 'GRIFFON'
                // keystore = "${basedir}/griffon-app/conf/keys/devKeystore"
                // alias = 'development'
                storepass = 'BadStorePassword'
                keypass   = 'BadKeyPassword'
                lazy      = true // only sign when unsigned
            }
        }
    }
    test {
        griffon {
            jars {
                sign = false
                pack = false
            }
        }
    }
    production {
        signingkey {
            params {
                // NOTE: for production keys it is more secure to rely on key prompting
                // no value means we will prompt //storepass = 'BadStorePassword'
                // no value means we will prompt //keypass   = 'BadKeyPassword'
                storepass = 'inventory:)'
                keypass = 'inventory:)'
                lazy = true // sign, regardless of existing signatures
            }
        }

        griffon {
            jars {
                sign = true
                pack = true
                destDir = "${basedir}/staging"
                manifest = [
                    'Permissions': 'all-permissions',
                    'Codebase': '*'
                ]
            }
            webstart {
                codebase = 'CHANGE ME'
            }
        }
    }
}

griffon {
    memory {
        //max = '64m'
        //min = '2m'
        //minPermSize = '2m'
        //maxPermSize = '64m'
    }
    jars {
        sign = false
        pack = false
        destDir = "${basedir}/staging"
        jarName = "${appName}-${appVersion}.jar"
    }
    extensions {
        jarUrls = []
        jnlpUrls = []
        /*
        props {
            someProperty = 'someValue'
        }
        resources {
            linux { // windows, macosx, solaris
                jars = []
                nativelibs = []
                props {
                    someProperty = 'someValue'
                }
            }
        }
        */
    }
    webstart {
        codebase = "${new File(griffon.jars.destDir).toURI().toASCIIString()}"
        jnlp = 'application.jnlp'
    }
    applet {
        jnlp = 'applet.jnlp'
        html = 'applet.html'
    }
}

// required for custom environments
signingkey {
    params {
        def env = griffon.util.Environment.current.name
        sigfile = 'GRIFFON-' + env
        keystore = "${basedir}/griffon-app/conf/keys/${env}Keystore"
        alias = env
        // storepass = 'BadStorePassword'
        // keypass   = 'BadKeyPassword'
        lazy      = true // only sign when unsigned
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon-framework.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (1.5.0)"
    }
}

deploy {
    application {
        title = "${appName} ${appVersion}"
        vendor = 'Jocki Hendry'
        homepage = "https://github.com/JockiHendry/simple-jpa-demo-inventory"
        description {
            complete = "${appName} ${appVersion}"
            oneline  = "${appName} ${appVersion}"
            minimal  = "${appName} ${appVersion}"
            tooltip  = "${appName} ${appVersion}"
        }
        icon {
            'default' {
                name   = 'icon.png'
                width  = '64'
                height = '64'
            }
            splash {
                name   = 'splashscreen.png'
                width  = '400'
                height = '300'
            }
            selected {
                name   = 'icon.png'
                width  = '64'
                height = '64'
            }
            disabled {
                name   = 'icon.png'
                width  = '64'
                height = '64'
            }
            rollover {
                name   = 'icon.png'
                width  = '64'
                height = '64'
            }
            shortcut {
                name   = 'icon.ico'
                width  = '48'
                height = '48'
            }
        }
    }
}

griffon.project.dependency.resolution = {
    // inherit Griffon' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        griffonHome()
        mavenLocal()
        mavenCentral()
        mavenRepo "http://jasperreports.sourceforge.net/maven2"
        mavenRepo "http://dl.bintray.com/jockihendry/maven"
    }
    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.20'
        runtime 'org.hibernate:hibernate-entitymanager:4.3.4.Final'
        compile 'org.hibernate:hibernate-validator:4.3.0.Final'
        compile('net.sf.jasperreports:jasperreports:5.5.1') {
            exclude 'commons-collections'
        }
        compile 'jockihendry:simple-escp:0.3'
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon',
          'simplejpa'
}


app.fileType = '.groovy'
app.defaultPackageName = 'simple.jpa.demo.inventory'

vbox {

    vboxManage = 'C:\\Program Files\\Oracle\\VirtualBox\\VBoxManage.exe'

    images {
        'Windows 7 Test' {
            os = 'windows'
            username = 'Tester'
            testAliveCmd = 'C:\\Windows\\System32\\ipconfig.exe'
            targetDir = 'C:\\Users\\Tester\\Desktop\\'
            sevenZip = 'C:\\Program Files\\7-Zip\\7z.exe'
            griffonExec = 'C:\\Programs\\Griffon-1.5.0\\bin\\griffon.bat'
            griffonHome = 'C:\\Programs\\Griffon-1.5.0'
            javaHome = 'C:\\Progra~1\\Java\\jdk1.7.0_21'
        }
        'Linux Mint Test' {
            os = 'linux'
            username = 'tester'
            password = 'toor'
            testAliveCmd = '/bin/ls'
            targetDir = '/home/tester/Desktop'
            sevenZip = '/usr/bin/7z'
            griffonExec = '/home/tester/griffon-1.5.0/bin/griffon'
            griffonHome = '/home/tester/griffon-1.5.0'
            javaHome = '/usr/lib/jvm/java-7-openjdk-i386'
        }
    }

}
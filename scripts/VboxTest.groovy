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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

def hasil = [:]

def tambahGagal = { String vBoxImage, def jumlahGagal ->
    if (!hasil.containsKey(vBoxImage)) {
        hasil[vBoxImage] = jumlahGagal
    } else {
        hasil[vBoxImage] = hasil[vBoxImage] + jumlahGagal
    }
}

def exec = { String command, String vBoxImage, boolean silentFail, Object... args ->
    List commands = [buildConfig.vbox.vboxManage, command, vBoxImage]
    args?.each { arg ->
        if (arg instanceof Map) {
            arg.each { k, v ->
                commands << ("--" + k)
                commands << v
            }
        } else {
            commands << arg as String
        }
    }
    ProcessBuilder builder = new ProcessBuilder(commands)
    builder.redirectErrorStream(true)
    if (!silentFail) {
        println "Mengerjakan ${commands}"
    }
    Process p = builder.start()
    if (silentFail) {
        p.waitForOrKill(10000000)
    } else {
        p.waitForProcessOutput(System.out, System.out)
        if (p.exitValue() != 0) {
            throw new RuntimeException("Gagal mengerjakan perintah. Exit code: ${p.exitValue()}")
        }
    }
    p.exitValue()
}

def execute = { String vBoxImage, String image, def config, def args = [] ->
    def p = [image: image, username: config.username]
    if (config.containsKey('password')) p.password = config.password
    exec('guestcontrol', vBoxImage, false, 'execute', p, '--wait-exit', '--wait-stdout', '--wait-stderr', *args)
}

def executeSilent = { String vBoxImage, String image, def config, def args = [] ->
    def p = [image: image, username: config.username]
    if (config.containsKey('password')) p.password = config.password
    exec('guestcontrol', vBoxImage, true, 'execute', p, '--wait-exit', '--wait-stdout', '--wait-stderr', *args)
}

def copyTo = { String vBoxImage, def source, def dest, def config ->
    def p = ['username': config.username]
    if (config.containsKey('password')) p.password = config.password
    String strSource = source instanceof Path? source.toAbsolutePath().toString(): source
    String strDest = dest instanceof Path? dest.toAbsolutePath().toString(): dest
    exec('guestcontrol', vBoxImage, false, 'copyto', strSource, strDest, p)
}

def copyFrom = { String vBoxImage, def source, def dest, def config ->
    def p = ['username': config.username]
    if (config.containsKey('password')) p.password = config.password
    String strSource = source instanceof Path? source.toAbsolutePath().toString(): source
    String strDest = dest instanceof Path? dest.toAbsolutePath().toString(): dest
    exec('guestcontrol', vBoxImage, false, 'copyFrom', strSource, strDest, p)
}

def snapshotRestore = { String vBoxImage, def config ->
    exec('controlvm', vBoxImage, false, 'poweroff')
    exec('snapshot', vBoxImage, false, 'restorecurrent')
}

def concat = { String path1, String path2, String separator ->
    if (path1.endsWith(separator)) {
        return path1 + path2
    } else {
        return path1 + separator + path2
    }
}

def proses = {String vBoxImage, def config  ->
    def separator = (config.os == 'windows')? '\\': '/'

    exec('startvm', vBoxImage, false, [type: 'headless'])

    // menunggu sistem operasi guest siap
    print "Menunggu virtual machine siap  "
    int exitCode = -1
    while (exitCode != 0) {
        exitCode = executeSilent(vBoxImage, config.testAliveCmd, config)
        print "."
        sleep(5000)
    }
    println ""
    println "Virtual machine sudah siap"

    // membuat file ZIP untuk di-copy ke host
    println "Membuat file ZIP untuk dipindahkan ke guest"
    def baseDir = griffonSettings.baseDir.path
    Path tempZip = Files.createTempDirectory("griffonvboxtemp").resolve('project.zip')
    ant.zip(destFile: tempZip.toAbsolutePath().toString(), basedir: baseDir)
    tempZip.toFile().deleteOnExit()

    // mencopy file project ke host
    println "Mencopy file ZIP ke guest"
    if (config.os == 'windows') {
        copyTo(vBoxImage, tempZip, config.targetDir, config)
    } else {
        copyTo(vBoxImage, tempZip, config.targetDir + separator + 'project.zip', config)
    }

    // men-extract file ZIP di host
    println "Men-extract file ZIP di guest"
    def targetZip = concat(config.targetDir, 'project.zip', separator)
    def targetExtractZip = concat(config.targetDir, 'project', separator)
    execute(vBoxImage, config.sevenZip, config, ['--', 'x', targetZip, '-y', '-o' + targetExtractZip])

    // mengerjakan test-app
    println "Mengerjakan test-app"
    try {
        def envs = "\"GRIFFON_HOME=${config.griffonHome} JAVA_HOME=${config.javaHome} DISPLAY=:0.0\"".toString()
        execute(vBoxImage, config.griffonExec, config, ['--environment', envs, '--',
                                                        '-Dbase.dir=' + targetExtractZip, 'test-app'])
    } catch (RuntimeException ex) {
        println "PENTING: Pengujian untuk proyek ini gagal!"
        tambahGagal(vBoxImage, 1)
    }

    // mengambil hasil pengujian di guest di folder target\test-reports\TESTS-TestSuites.xml
    println "Men-copy file hasil pengujian dari guest"
    def lokasiTestReports = Paths.get(baseDir, 'target', 'vbox-test-reports')
    if (!lokasiTestReports.toFile().exists()) lokasiTestReports.toFile().mkdirs()
    def dest = lokasiTestReports.resolve("TESTS-$vBoxImage-TestSuites-${new Date().format('yyyyMMddhhmm')}.xml")
    def source = concat(targetExtractZip, 'target' + separator + 'test-reports' + separator + 'TESTS-TestSuites.xml', separator)
    copyFrom(vBoxImage, source, dest, config)
    println "File $dest berhasil dibuat"

    // membaca hasil pengujian
    def testsuites = new XmlSlurper().parse(dest.toFile())
    testsuites.testsuite.each { node ->
        if (node.@errors.toInteger() > 0) {
            tambahGagal(vBoxImage, node.@errors.toInteger())
        }
        if (node.@failures.toInteger() > 0) {
            tambahGagal(vBoxImage, node.@failures.toInteger())
        }
    }

}

target(name: 'vboxtest', description: "Run test in VirtualBox", prehook: null, posthook: null) {

    buildConfig.vbox.images.each { k, v ->
        proses(k,v)

        // rollback ke state terakhir
        println "Mengembalikan snapshot seperti semula"
        snapshotRestore(k, v)
    }

    println "\nHasil pengujian:"
    println '-'*40
    printf "%-30s %5s\n", 'VM Image', 'Gagal'
    println '-'*40
    buildConfig.vbox.images.each { k, v ->
        printf "%-30s %5d\n", k, hasil[k]?:0
    }
    println '-'*40
    println "\nSelesai.\n"

}

setDefaultTarget('vboxtest')

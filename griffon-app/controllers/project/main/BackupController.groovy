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
package project.main

import project.inventory.ProdukRepository
import org.joda.time.DateTime
import simplejpa.SimpleJpaUtil
import javax.swing.JTextArea
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@SuppressWarnings("GroovyUnusedDeclaration")
class BackupController {

    BackupModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        init()
    }

    def init = {
        model.basedir = produkRepository.executeNativeQuery("SHOW VARIABLES LIKE 'basedir'")[0][1]
        model.lokasiTujuan = File.listRoots().last()
        model.arguments = "--complete-insert --skip-extended-insert --compact --no-create-info"
    }

    def mulai = {
        JTextArea output = view.output
        String userName = SimpleJpaUtil.instance.dbUsername
        String dbName = SimpleJpaUtil.instance.dbName
        String password = "12345"
        if (view.databasePassword?.password?.length > 0) password = view.databasePassword.password.toString()

        String namafile = DateTime.now().toString('yyyyMMdd-hhmm')
        String lokasiTujuan = model.lokasiTujuan.absolutePath
        Path fileBackup = Paths.get(lokasiTujuan, "${namafile}-${dbName}-backup.sql")
        Path fileError = Paths.get(lokasiTujuan, "${namafile}-${dbName}-error.log")

        Files.newOutputStream(fileBackup, StandardOpenOption.CREATE_NEW).withStream { outputFile ->
            Files.newOutputStream(fileError, StandardOpenOption.CREATE_NEW).withStream { errorFile ->

                outputFile.write("SET FOREIGN_KEY_CHECKS = 0;${System.lineSeparator()}".getBytes())

                String mySqlDump = Paths.get(model.basedir, "bin", "mysqldump").toString()
                List execs = [mySqlDump, "-u${userName}", "-p${password}", "${dbName}"]
                if (!model.arguments.isEmpty()) {
                    execs.addAll(model.arguments.split(/\s+/))
                }
                execInsideUISync { output.append("Mulai mengerjakan $mySqlDump ...\n") }
                Process process = execs.execute()
                execInsideUISync { output.append("Backup sedang diproses. Harap sabar menunggu...\n")}

                process.consumeProcessOutput(outputFile, errorFile)
                process.waitFor()

                execInsideUISync {
                    output.append("Proses backup selesai!\n")
                    output.append("Hasil backup dapat ditemukan di ${fileBackup}\n")
                    output.append("Pesan kesalahan selama backup dapat ditemukan di ${fileError}\n")
                }

            }
        }

        execInsideUISync { output.append("Selesai.\n\n") }
    }

}

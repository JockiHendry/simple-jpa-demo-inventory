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

import org.dbunit.database.DatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.dataset.excel.XlsDataSet
import org.dbunit.operation.DatabaseOperation
import org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor
import project.main.RestoreModel
import simplejpa.SimpleJpaUtil

import javax.swing.JTextArea
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class RestoreController {

    RestoreModel model
    def view

    void mvcGroupInit(Map args) {
        model.fileRestore = Paths.get(System.getProperty('user.home')).toFile()
        //view.fileChooser.currentDirectory = model.fileRestore
    }

    def mulai =  {
        JTextArea output = view.output
        String password = "12345"
        if (view.databasePassword?.password?.length > 0) password = view.databasePassword.password.toString()
        Connection connection
        SimpleJpaUtil.instance.with {
            execInsideUISync { output.append("Melakukan koneksi ke database...\n") }
            connection = DriverManager.getConnection(getDbUrl(), getDbUsername(), password)
            execInsideUISync { output.append("Koneksi ke database berhasil diperoleh!\n") }
        }

        try {
            if (model.fileRestore.name.endsWith('.xls')) {

                execInsideUISync { output.append("Menyiapkan dataset Microsoft Excel...\n") }
                IDataSet dataSet = new XlsDataSet(model.fileRestore)
                execInsideUISync { output.append("Memulai proses eksekusi ke database...\n") }
                DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(connection), dataSet)

            } else if (model.fileRestore.name.endsWith('.csv')) {

                execInsideUISync { output.append("Menyiapkan dataset CSV...\n") }
                CsvDataSet dataSet = new CsvDataSet(model.fileRestore)
                execInsideUISync { output.append("Memulai proses eksekusi ke database...\n") }
                DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(connection), dataSet)

            } else if (model.fileRestore.name.endsWith('.sql')) {

                Statement statement = connection.createStatement()
                MultipleLinesSqlCommandExtractor extractor = new MultipleLinesSqlCommandExtractor()
                extractor.extractCommands(model.fileRestore.newReader()).each { String sql ->
                    if (!sql.isAllWhitespace()) {
                        execInsideUISync { output.append("${sql}\n\n") }
                        statement.execute(sql)
                    }
                }

            }
        } finally {
            if (!connection?.isClosed()) connection.close()
        }
        execInsideUISync { output.append("Selesai.\n\n") }
    }


}

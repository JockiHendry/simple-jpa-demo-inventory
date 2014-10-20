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

import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.penjualan.FakturJual
import domain.penjualan.StatusFakturJual
import domain.retur.ReturJual
import org.dbunit.database.DatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.dataset.excel.XlsDataSet
import org.dbunit.operation.DatabaseOperation
import org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor
import project.inventory.ProdukRepository
import simplejpa.SimpleJpaUtil
import javax.swing.JTextArea
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class RestoreController {

    ProdukRepository produkRepository
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

    def refreshStok = {
        JTextArea output = view.output
        execInsideUISync { output.append("Mulai...\n\n") }
        produkRepository.withTransaction {
            findAllProduk().each { Produk p ->
                def total = 0
                p.daftarStok.each { Gudang g, StokProduk s ->
                    def jumlah = 0
                    s.listPeriodeRiwayat.each { PeriodeItemStok pr ->
                        def totalPeriode = pr.listItem.sum {it.jumlah}?: 0
                        if (pr.jumlah != totalPeriode) {
                            execInsideUISync { output.append("${p.nama} pada ${pr.tanggalMulai.toString('dd-MM-YYYY')} sampai ${pr.tanggalSelesai.toString('dd-MM-YYYY')} harus berjumlah ${totalPeriode} tetapi ${pr.jumlah}\n") }
                            pr.jumlah = totalPeriode
                        }
                        jumlah += totalPeriode
                    }
                    total += jumlah
                    if (s.jumlah != jumlah) {
                        execInsideUISync { output.append("${p.nama} pada ${g.nama} harus berjumlah ${jumlah} tetapi ${s.jumlah}\n") }
                        s.jumlah = (jumlah < 0)? 0: jumlah
                    }
                }
                if (p.jumlah != total) {
                    execInsideUISync { output.append("${p.nama} harus berjumlah ${total} tetapi ${p.jumlah}\n") }
                    p.jumlah = (total < 0)? 0: total
                }
            }
        }
        execInsideUISync { output.append("\nSelesai!\n\n") }
    }

    def refreshJumlahAkanDikirim = {
        JTextArea output = view.output
        execInsideUISync { output.append("Mulai...\n\n") }
        def pengiriman = [:]
        produkRepository.withTransaction {
            findAllFakturJualByStatus(StatusFakturJual.DIBUAT).each { FakturJual f ->
                if (f.isBolehPesanStok()) {
                    f.listItemFaktur.each { ItemFaktur i ->
                        if (pengiriman.containsKey(i.produk)) {
                            pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                        } else {
                            pengiriman[i.produk] = i.jumlah
                        }
                    }
                }
            }
            findAllReturJualBySudahDiproses(false).each { ReturJual r ->
                r.yangHarusDitukar().items.each { ItemBarang i ->
                    if (pengiriman.containsKey(i.produk)) {
                        pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                    } else {
                        pengiriman[i.produk] = i.jumlah
                    }
                }
            }
            findAllProduk().each { Produk p ->
                int nilaiSeharusnya = pengiriman[p]?: 0
                if (p.jumlahAkanDikirim != nilaiSeharusnya) {
                    execInsideUISync { output.append("${p.nama} seharusnya memiliki jumlah akan dikirim $nilaiSeharusnya tetapi ${p.jumlahAkanDikirim}\n")}
                    p.jumlahAkanDikirim = nilaiSeharusnya
                }
            }
        }
        execInsideUISync { output.append("\nSelesai!\n\n") }
    }

}

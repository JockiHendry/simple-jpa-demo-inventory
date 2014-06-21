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
package project.laporan

import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.swing.JRViewer
import simplejpa.swing.DialogUtils
import util.BusyLayerUI

import javax.swing.JOptionPane
import java.awt.BorderLayout

class LaporanController {

    LaporanModel model
    def view

    def search = {
        JenisLaporan jenisLaporan = model.jenisLaporanSearch.selectedItem
        if (!jenisLaporan) {
            JOptionPane.showMessageDialog(view.mainPanel, "Anda harus memilih jenis laporan yang akan ditampilkan!",
                    "Pesan Kesalahan", JOptionPane.ERROR_MESSAGE)
            return
        }
        def result, batal, params
        execInsideUISync {
            DialogUtils.showMVCGroup(jenisLaporan.namaMVC, [:], app, view, [title: 'Pilih Kriteria'], { m, v, c ->
                result = m.result
                params = m.params
                batal = m.batal
            }, {v -> v})
            BusyLayerUI.instance.hide()
        }

        if (!batal) {
            JRDataSource dataSource = new JRBeanCollectionDataSource(result)

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    getResourceAsStream("report/${jenisLaporan.namaLaporan}.jasper"), params, dataSource)

            execInsideUISync {
                view.content.clear()
                view.content.add(new JRViewer(jasperPrint), BorderLayout.CENTER)
            }
        }

    }


}

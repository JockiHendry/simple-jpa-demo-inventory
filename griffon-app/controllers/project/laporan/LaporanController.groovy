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

import domain.pengaturan.KeyPengaturan
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.swing.JRViewer
import project.pengaturan.PengaturanRepository
import simplejpa.swing.DialogUtils
import util.BusyLayerUI
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import java.awt.BorderLayout

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanController {

    LaporanModel model
    def view
    PengaturanRepository pengaturanRepository

    def search = {
        JenisLaporan jenisLaporan = model.jenisLaporanSearch.selectedItem
        if (!jenisLaporan) {
            DialogUtils.message(view.mainPanel, "Anda harus memilih jenis laporan yang akan ditampilkan!", "Pesan Kesalahan", JOptionPane.ERROR_MESSAGE)
            return
        }
        def result, batal, params
        execInsideUISync {
            DialogUtils.showAndReuseMVCGroup(jenisLaporan.namaMVC, [:], view, [title: 'Pilih Kriteria'], null, { m, v, c ->
                result = m.result
                params = m.params
                batal = m.batal
            })
        }

        if (!batal) {
            execInsideUISync { BusyLayerUI.instance.show() }
            JRDataSource dataSource = new JRBeanCollectionDataSource(result)
            params.companyName = pengaturanRepository.getValue(KeyPengaturan.NAMA_PERUSAHAAN)
            InputStream logoInputStream = getResourceAsStream('report/logo.png')
            if (logoInputStream) params.logo = ImageIO.read(logoInputStream)

            def fileLaporan = getResourceAsStream(params.fileLaporan?: "report/${jenisLaporan.namaLaporan}.jasper")
            JasperPrint jasperPrint = JasperFillManager.fillReport(fileLaporan, params, dataSource)

            execInsideUISync {
                view.content.clear()
                view.content.add(new JRViewer(jasperPrint), BorderLayout.CENTER)
                BusyLayerUI.instance.hide()
            }

        }

    }

}

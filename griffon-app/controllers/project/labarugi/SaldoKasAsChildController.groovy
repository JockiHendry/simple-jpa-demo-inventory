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
package project.labarugi

import javax.swing.*

@SuppressWarnings("GroovyUnusedDeclaration")
class SaldoKasAsChildController {

	SaldoKasAsChildModel model
	def view
    KategoriKasRepository kategoriKasRepository
    KategoriKasService kategoriKasService

	void mvcGroupInit(Map args) {
        model.saldoKasList.addAll(args.'parentList'?:[])
	}

    def refreshSaldo = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Memperbaharui nilai saldo akan membutuhkan waktu yang lama.  Apakah Anda yakin ingin melanjutkan?', 'Konfirmasi', JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return
        }
        kategoriKasService.refreshSaldoKas(model.kategoriKas)
        model.kategoriKas = kategoriKasRepository.findKategoriKasById(model.kategoriKas.id)
        JOptionPane.showMessageDialog(view.mainPanel, 'Saldo berhasil diperbaharui!')
        execInsideUISync {
            model.saldoKasList.clear()
            model.saldoKasList.addAll(model.kategoriKas.listSaldoKas)
        }
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
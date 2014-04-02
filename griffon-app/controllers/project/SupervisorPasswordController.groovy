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
package project

import domain.Container
import domain.pengaturan.KeyPengaturan

import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class SupervisorPasswordController {

    def model
    def view

    def proses = {

        char[] password
        model.ok = false

        try {
            password = view.password.getPassword()
            if (password.length == 0) {
                throw new IllegalArgumentException('Password tidak boleh kosong!')
            }
        } catch (Exception ex) {
            model.errors['password'] = ex.message
            return
        }

        if (!Arrays.equals(
                Container.app.passwordService.plainTextToEncrypted(view.password.getPassword()),
                Container.app.pengaturanRepository.getValue(KeyPengaturan.SUPERVISOR_PASSWORD))) {
            model.errors['password'] = 'Password Anda salah!'
            return
        }

        if (JOptionPane.showConfirmDialog(view.mainPanel, 'Anda yakin untuk menyetujui proses ini?',
                'Konfirmasi Persetujuan', JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
            return
        }

        model.ok = true

        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false

    }

}

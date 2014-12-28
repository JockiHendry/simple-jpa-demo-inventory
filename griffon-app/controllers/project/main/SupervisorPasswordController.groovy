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

import domain.pengaturan.KeyPengaturan
import project.pengaturan.PengaturanRepository
import project.user.PasswordService
import simplejpa.swing.DialogUtils
import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class SupervisorPasswordController {

    SupervisorPasswordModel model
    def view
    PengaturanRepository pengaturanRepository
    PasswordService passwordService

    void mvcGroupInit(Map args) {
        if (args.containsKey('pesan')) {
            model.pesan = args.pesan
        }
    }

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

        if (!passwordService.periksaPassword(
                pengaturanRepository.getValue(KeyPengaturan.SUPERVISOR_PASSWORD),
                view.password.getPassword())) {
            model.errors['password'] = 'Password Anda salah!'
            return
        }

        if (!DialogUtils.confirm(view.mainPanel, 'Anda yakin untuk menyetujui proses ini?', 'Konfirmasi Persetujuan')) {
            return
        }

        model.ok = true

        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false

    }

}

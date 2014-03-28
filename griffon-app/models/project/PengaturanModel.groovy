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

import ca.odell.glazedlists.BasicEventList
import domain.pengaturan.KeyPengaturan
import domain.pengaturan.Pengaturan
import groovy.beans.Bindable
import groovy.transform.Canonical

class PengaturanModel {

    @Bindable Long id
    @Bindable KeyPengaturan keyPengaturan
    @Bindable Object nilai

    BasicEventList<PengaturanDTO> pengaturanList = new BasicEventList<>()

    @Bindable boolean genericValue = false
    @Bindable boolean passwordValue = false

}

@Canonical
public class PengaturanDTO {
    KeyPengaturan keyPengaturan
    Object nilai
}
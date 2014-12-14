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

import domain.labarugi.*
import ca.odell.glazedlists.*
import util.*
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class KategoriKasModel {

	@Bindable Long id

	@Bindable String nama
	EnumComboBoxModel<JENIS_KATEGORI_KAS> jenis = new EnumComboBoxModel<JENIS_KATEGORI_KAS>(JENIS_KATEGORI_KAS)
	@Bindable Boolean dipakaiDiLaporan
	BasicEventList<KategoriKas> kategoriKasList = new BasicEventList<>()

	@Bindable String namaSearch
	ListComboBoxModel jenisSearch = new ListComboBoxModel(SwingHelper.searchEnum(JENIS_KATEGORI_KAS))

	@Bindable String created
	@Bindable String modified
	@Bindable String createdBy
	@Bindable String modifiedBy

}
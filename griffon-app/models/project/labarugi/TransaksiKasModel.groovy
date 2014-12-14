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
import ca.odell.glazedlists.swing.*
import org.joda.time.*

class TransaksiKasModel {

    Kas kas

    @Bindable LocalDate tanggal
    @Bindable String pihakTerkait
    BasicEventList<KategoriKas> kategoriKasList = new BasicEventList<>()
    DefaultEventComboBoxModel<KategoriKas> kategoriKas = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(kategoriKasList)
    @Bindable BigDecimal jumlah
    BasicEventList<JenisTransaksiKas> jenisTransaksiKasList = new BasicEventList<>()
    DefaultEventComboBoxModel<JenisTransaksiKas> jenisTransaksiKas = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(jenisTransaksiKasList)
    @Bindable String keterangan
    BasicEventList<TransaksiKas> transaksiKasList = new BasicEventList<>()

    @Bindable String pihakTerkaitSearch
    @Bindable String kategoriKasSearch

    BasicEventList<PeriodeKas> periodeKasList = new BasicEventList<>()
    DefaultEventComboBoxModel<PeriodeKas> periodeKas = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(periodeKasList)

}
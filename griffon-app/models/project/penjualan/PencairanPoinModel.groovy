/*
 * Copyright 2015 Jocki Hendry.
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
package project.penjualan

import ca.odell.glazedlists.BasicEventList
import domain.inventory.ItemBarang
import domain.pengaturan.NamaTemplateFaktur
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.Konsumen
import org.jdesktop.swingx.combobox.EnumComboBoxModel
import org.joda.time.LocalDate

class PencairanPoinModel {

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable Integer jumlahPoin
    @Bindable String keterangan
    @Bindable BigDecimal rate
    @Bindable Konsumen konsumen
    List<ItemBarang> items = []
    @Bindable FakturJualOlehSales fakturPotongPiutang
    EnumComboBoxModel<JenisPencairanPoin> jenisPencairanPoin = new EnumComboBoxModel<JenisPencairanPoin>(JenisPencairanPoin)

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

    BasicEventList pencairanPoinList = new BasicEventList()

    @Bindable boolean daftarBarangVisible
    @Bindable boolean potongPiutangVisible

    @Bindable String nomorSearch
    @Bindable String konsumenSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch

}

enum JenisPencairanPoin {

    TUKAR_UANG('Tukar Uang', NamaTemplateFaktur.BUKTI_PENCAIRAN_POIN_TUKAR_UANG),
    TUKAR_BARANG('Tukar Barang', NamaTemplateFaktur.BUKTI_PENCAIRAN_POIN_TUKAR_BARANG),
    POTONG_PIUTANG('Potong Piutang', NamaTemplateFaktur.BUKTI_PENCAIRAN_POIN_POTONG_PIUTANG)

    String desc
    String namaTemplateFaktur

    JenisPencairanPoin(String desc, NamaTemplateFaktur namaTemplateFaktur) {
        this.desc = desc
        this.namaTemplateFaktur = namaTemplateFaktur
    }

    @Override
    String toString() {
        desc
    }

}
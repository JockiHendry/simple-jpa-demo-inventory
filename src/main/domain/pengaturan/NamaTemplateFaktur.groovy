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
package domain.pengaturan

enum NamaTemplateFaktur {

    BUKTI_PENCAIRAN_POIN_POTONG_PIUTANG('escp/bukti_pencairan_poin_potong_piutang.json'),
    BUKTI_PENCAIRAN_POIN_TUKAR_BARANG('escp/bukti_pencairan_poin_tukar_barang.json'),
    BUKTI_PENCAIRAN_POIN_TUKAR_UANG('escp/bukti_pencairan_poin_tukar_uang.json'),
    DAFTAR_BARANG_KIRIM('escp/daftar_barang_kirim.json'),
    FAKTUR_JUAL_ECERAN('escp/faktur_jual_eceran.json'),
    FAKTUR_JUAL_SALES('escp/faktur_jual_sales.json'),
    FORMULIR_SERVIS('escp/formulir_servis.json'),
    KEMASAN_RETUR('escp/kemasan_retur.json'),
    PENERIMAAN_SERVIS('escp/penerimaan_servis.json'),
    PURCHASE_ORDER('escp/purchase_order.json'),
    RETUR_FAKTUR('escp/retur_faktur.json'),
    RETUR_JUAL_ECERAN('escp/retur_jual_eceran.json'),
    RETUR_JUAL_SALES('escp/retur_jual_sales.json'),
    SURAT_JALAN('escp/surat_jalan.json')

    String file

    NamaTemplateFaktur(String file) {
        this.file = file
    }

}
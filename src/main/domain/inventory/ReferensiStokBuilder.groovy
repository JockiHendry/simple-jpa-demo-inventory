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
package domain.inventory

import domain.pembelian.PenerimaanBarang
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.PencairanPoinTukarBarang
import domain.penjualan.PengeluaranBarang
import domain.penjualan.ReturFaktur
import domain.retur.ReturBeli
import domain.retur.ReturJualEceran
import domain.retur.ReturJualOlehSales

class ReferensiStokBuilder {

    public static final String PIHAK_INTERNAL = '[Internal]'

    private ReferensiStok hasil

    public ReferensiStokBuilder() {
        hasil = new ReferensiStok()
    }

    public ReferensiStokBuilder(def ...objects) {
        this()
        objects.each {
            refer(it)
        }
    }

    ReferensiStokBuilder informasiAudit(def object) {
        hasil.dibuatOleh = "${object.createdBy} (${object.createdDate})"
        if (object.modifiedBy) {
            hasil.diubahOleh = "${object.modifiedBy} (${object.modifiedDate})"
        }
        this
    }

    ReferensiStokBuilder refer(PenerimaanBarang penerimaanBarang) {
        hasil.classGudang = PenerimaanBarang.simpleName
        hasil.nomorGudang = penerimaanBarang.nomor
        informasiAudit(penerimaanBarang)
        this
    }

    ReferensiStokBuilder refer(ReturFaktur returFaktur) {
        hasil.classGudang = ReturFaktur.simpleName
        hasil.nomorGudang = returFaktur.nomor
        informasiAudit(returFaktur)
        this
    }

    ReferensiStokBuilder refer(PengeluaranBarang pengeluaranBarang) {
        hasil.classGudang = PengeluaranBarang.simpleName
        hasil.nomorGudang = pengeluaranBarang.nomor
        informasiAudit(pengeluaranBarang)
        this
    }

    ReferensiStokBuilder refer(PenyesuaianStok penyesuaianStok) {
        hasil.classGudang = PenyesuaianStok.simpleName
        hasil.nomorGudang = penyesuaianStok.nomor
        hasil.pihakTerkait = PIHAK_INTERNAL
        informasiAudit(penyesuaianStok)
        this
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    ReferensiStokBuilder refer(DaftarBarangSementara daftarBarangSementara) {
        this
    }

    ReferensiStokBuilder refer(PurchaseOrder purchaseOrder) {
        hasil.classFinance = PurchaseOrder.simpleName
        hasil.nomorFinance = purchaseOrder.nomor
        hasil.pihakTerkait = purchaseOrder.supplier?.nama
        informasiAudit(purchaseOrder)
        this
    }

    ReferensiStokBuilder refer(FakturJualOlehSales fakturJualOlehSales) {
        hasil.classFinance = FakturJualOlehSales.simpleName
        hasil.nomorFinance = fakturJualOlehSales.nomor
        hasil.pihakTerkait = fakturJualOlehSales.konsumen?.nama
        informasiAudit(fakturJualOlehSales)
        this
    }

    ReferensiStokBuilder refer(FakturJualEceran fakturJualEceran) {
        hasil.classFinance = FakturJualEceran.simpleName
        hasil.nomorFinance = fakturJualEceran.nomor
        hasil.pihakTerkait = fakturJualEceran.namaPembeli
        informasiAudit(fakturJualEceran)
        this
    }

    ReferensiStokBuilder refer(PencairanPoinTukarBarang pencairanPoinTukarBarang) {
        hasil.classFinance = PencairanPoinTukarBarang.simpleName
        hasil.nomorFinance = pencairanPoinTukarBarang.nomor
        hasil.pihakTerkait = pencairanPoinTukarBarang.konsumen?.nama
        informasiAudit(pencairanPoinTukarBarang)
        this
    }

    ReferensiStokBuilder refer(ReturBeli returBeli) {
        hasil.classFinance = ReturBeli.simpleName
        hasil.nomorFinance = returBeli.nomor
        hasil.pihakTerkait = returBeli.supplier?.nama
        informasiAudit(returBeli)
        this
    }

    ReferensiStokBuilder refer(ReturJualOlehSales returJualOlehSales) {
        hasil.classFinance = ReturJualOlehSales.simpleName
        hasil.nomorFinance = returJualOlehSales.nomor
        hasil.pihakTerkait = returJualOlehSales.konsumen?.nama
        informasiAudit(returJualOlehSales)
        this
    }

    ReferensiStokBuilder refer(ReturJualEceran returJualEceran) {
        hasil.classFinance = ReturJualEceran.simpleName
        hasil.nomorFinance = returJualEceran.nomor
        hasil.pihakTerkait = returJualEceran.namaKonsumen
        informasiAudit(returJualEceran)
        this
    }

    ReferensiStokBuilder refer(Transfer transfer) {
        hasil.classGudang = Transfer.simpleName
        hasil.nomorGudang = transfer.nomor
        hasil.pihakTerkait = PIHAK_INTERNAL
        informasiAudit(transfer)
        this
    }

    ReferensiStok buat() {
        if (!hasil.pihakTerkait) {
            hasil.pihakTerkait = PIHAK_INTERNAL
        }
        hasil
    }
}

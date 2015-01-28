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
package domain.penjualan.state

import domain.event.PerubahanStok
import domain.event.PesanStok
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.PengeluaranBarang
import domain.penjualan.ReturFaktur
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import project.user.NomorService
import griffon.util.*

class FakturJualOlehSalesDibuat implements OperasiFakturJual {

    @Override
    void proses(FakturJual fakturJual, Map args) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        if (!fakturJual.pengeluaranBarang) {
            // Surat jalan belum pernah dibuat sebelumnya
            LocalDate tanggal = args.containsKey('tanggal')? args.tanggal: LocalDate.now()
            String keterangan = args.containsKey('keterangan')? args.keterangan: null
            String alamatTujuan = args.containsKey('alamatTujuan')? args.alamatTujuan: ''
            PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
                nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
                tanggal: tanggal, gudang: fakturJual.kirimDari(), keterangan: keterangan, alamatTujuan: alamatTujuan
            )
            pengeluaranBarang.items = fakturJual.barangYangHarusDikirim().items
            fakturJual.pengeluaranBarang = pengeluaranBarang
        }
        kirim(fakturJual)
    }

    @Override
    void hapus(FakturJual fakturJual) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        ApplicationHolder.application.event(new PesanStok(fakturJual, true))
        fakturJual.konsumen.hapusFakturBelumLunas(fakturJual)
        fakturJual.deleted = 'Y'
    }

    @Override
    void tambahRetur(FakturJual fakturJual, ReturFaktur returFaktur) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        fakturJual.prosesTambahRetur(returFaktur)
    }

    @Override
    void hapusRetur(FakturJual fakturJual, String nomor) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        fakturJual.prosesHapusRetur(nomor)
    }

    void buatSuratJalan(FakturJualOlehSales fakturJual, String alamatTujuan, LocalDate tanggal = LocalDate.now(), String keterangan = null) {
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
                nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
                tanggal: tanggal, gudang: fakturJual.kirimDari(), keterangan: keterangan, alamatTujuan: alamatTujuan
        )
        pengeluaranBarang.items = fakturJual.barangYangHarusDikirim().items
        fakturJual.pengeluaranBarang = pengeluaranBarang
    }

    protected void kirim(FakturJualOlehSales fakturJualOlehSales) {
        fakturJualOlehSales.status = StatusFakturJual.DIANTAR
        ReferensiStok ref = new ReferensiStokBuilder(fakturJualOlehSales.pengeluaranBarang, fakturJualOlehSales).buat()
        ApplicationHolder.application?.event(new PerubahanStok(fakturJualOlehSales.pengeluaranBarang, ref,
                false, fakturJualOlehSales.isBolehPesanStok()))
    }

}

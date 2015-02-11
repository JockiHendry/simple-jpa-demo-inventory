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

import domain.event.BayarPiutang
import domain.exception.DataTidakBolehDiubah
import domain.faktur.Pembayaran
import domain.faktur.Referensi
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.ReturFaktur
import domain.penjualan.StatusFakturJual
import org.joda.time.LocalDate
import griffon.util.*

class FakturJualOlehSalesDiterima implements OperasiFakturJual {

    @Override
    void proses(FakturJual fakturJual, Map args) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        if (!args.containsKey('operasi')) {
            throw new IllegalArgumentException('Argument operasi dibutuhkan oleh proses()!')
        }
        if (args.operasi == 'bayar') {
            bayar(fakturJual, args.pembayaran)
        } else if (args.operasi == 'hapusPembayaran') {
            if (args.containsKey('pembayaran')) {
                hapusPembayaran(fakturJual, args.pembayaran)
            } else {
                hapusPembayaran(fakturJual, args.nomorReferensi, args.jenisReferensi)
            }
        } else {
            throw new UnsupportedOperationException("Operasi ${args.operasi} tidak dikenal!")
        }
    }

    @Override
    void hapus(FakturJual fakturJual) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        if (fakturJual.piutang.jumlahDibayar() > 0) {
            throw new DataTidakBolehDiubah("Penerimaan untuk ${fakturJual.nomor} tidak dapat dihapus karena pembayaran sebesar ${fakturJual.piutang.jumlahDibayar()} belum dihapus!", fakturJual)
        }
        fakturJual.pengeluaranBarang.batalDiterima()
        fakturJual.piutang = null
        if (fakturJual.poinBerlaku) {
            fakturJual.konsumen.hapusPoin(fakturJual.pengeluaranBarang.toPoin())
        }
        fakturJual.status = StatusFakturJual.DIANTAR
    }

    @Override
    void tambahRetur(FakturJual fakturJual, ReturFaktur returFaktur) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        BigDecimal harga = fakturJual.prosesTambahRetur(returFaktur)
        fakturJual.bayar(new Pembayaran(LocalDate.now(), harga, true, null, new Referensi(FakturJualOlehSales.RETUR_FAKTUR, returFaktur.nomor)))
        if (fakturJual.poinBerlaku) {
            fakturJual.konsumen.hapusPoin(returFaktur)
        }
    }

    @Override
    void hapusRetur(FakturJual fakturJual, String nomor) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        ReturFaktur returFaktur = fakturJual.prosesHapusRetur(nomor)
        hapusPembayaran(fakturJual, nomor, FakturJualOlehSales.RETUR_FAKTUR)
        if (fakturJual.poinBerlaku) {
            fakturJual.konsumen.tambahPoin(returFaktur)
        }
    }

    void bayar(FakturJualOlehSales fakturJual, Pembayaran pembayaran) {
        fakturJual.piutang.bayar(pembayaran)
        if (fakturJual.piutang.lunas) {
            fakturJual.status = StatusFakturJual.LUNAS
            fakturJual.konsumen.hapusFakturBelumLunas(fakturJual)
        }
        ApplicationHolder.application?.event(new BayarPiutang(fakturJual, pembayaran))
    }

    void hapusPembayaran(FakturJualOlehSales fakturJual, Pembayaran pembayaran) {
        fakturJual.piutang.hapus(pembayaran)
        ApplicationHolder.application?.event(new BayarPiutang(fakturJual, pembayaran, true))
    }

    void hapusPembayaran(FakturJualOlehSales fakturJual, String nomorReferensi, String jenisReferensi = null) {
        fakturJual.piutang.listPembayaran.findAll {
            if (jenisReferensi) {
                return (it.referensi?.namaClass == jenisReferensi) && (it.referensi?.nomor == nomorReferensi)
            } else {
                return (it.referensi?.nomor == nomorReferensi)
            }
        }.each { Pembayaran pembayaran ->
            hapusPembayaran(fakturJual, pembayaran)
        }
    }

}

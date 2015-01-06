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
import domain.faktur.Pembayaran
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.ReturFaktur
import domain.penjualan.StatusFakturJual
import griffon.util.*

class FakturJualOlehSalesLunas implements OperasiFakturJual {

    @Override
    void proses(FakturJual fakturJual, Map args) {
        if (!(fakturJual instanceof FakturJualOlehSales)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualOlehSales!')
        }
        if (!args.containsKey('operasi')) {
            throw new IllegalArgumentException('Argument operasi dibutuhkan oleh proses()!')
        }
        if (args.operasi == 'hapusPembayaran') {
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
        throw new UnsupportedOperationException('Untuk menghapus state lunas, gunakan hapusPembayaran!')
    }

    @Override
    void tambahRetur(FakturJual fakturJual, ReturFaktur returFaktur) {
        throw new UnsupportedOperationException('Operasi tambahRetur tidak didukung untuk faktur yang sudah lunas!')
    }

    @Override
    void hapusRetur(FakturJual fakturJual, String nomor) {
        throw new UnsupportedOperationException('Operasi hapusRetur tidak didukung untuk faktur yang sudah lunas!')
    }

    void hapusPembayaran(FakturJualOlehSales fakturJual, Pembayaran pembayaran) {
        fakturJual.piutang.hapus(pembayaran)
        fakturJual.status = StatusFakturJual.DITERIMA
        fakturJual.konsumen.tambahFakturBelumLunas(fakturJual)
        ApplicationHolder.application?.event(new BayarPiutang(fakturJual, pembayaran, true))
    }

    void hapusPembayaran(FakturJualOlehSales fakturJual, String nomorReferensi, String jenisReferensi = null) {
        fakturJual.piutang.listPembayaran.find {
            if (jenisReferensi) {
                return (it.referensi.namaClass == jenisReferensi) && (it.referensi.nomor == nomorReferensi)
            } else {
                return (it.referensi.nomor == nomorReferensi)
            }
        }.each { Pembayaran pembayaran ->
            hapusPembayaran(fakturJual, pembayaran)
        }
    }

}

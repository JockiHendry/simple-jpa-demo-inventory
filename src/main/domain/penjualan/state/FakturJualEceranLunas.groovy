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

import domain.penjualan.FakturJual
import domain.penjualan.FakturJualEceran
import domain.penjualan.ReturFaktur
import domain.penjualan.StatusFakturJual

class FakturJualEceranLunas implements OperasiFakturJual {

    @Override
    void proses(FakturJual fakturJual, Map args) {
        throw new UnsupportedOperationException('Tidak ada operasi proses untuk state FakturJualEceranLunas!')
    }

    @Override
    void hapus(FakturJual fakturJual) {
        if (!(fakturJual instanceof FakturJualEceran)) {
            throw new IllegalArgumentException('Argumen fakturJual harus berupa FakturJualEceran!')
        }
        fakturJual.status = StatusFakturJual.DIANTAR
    }

    @Override
    void tambahRetur(FakturJual fakturJual, ReturFaktur returFaktur) {
        throw new UnsupportedOperationException('Tidak ada operasi tambahRetur untuk state FakturJualEceranLunas!')
    }

    @Override
    void hapusRetur(FakturJual fakturJual, String nomor) {
        throw new UnsupportedOperationException('Tidak ada operasi hapusRetur untuk state FakturJualEceranLunas!')
    }

}

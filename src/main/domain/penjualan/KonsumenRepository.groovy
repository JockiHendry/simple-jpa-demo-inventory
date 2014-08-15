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
package domain.penjualan

import domain.Container
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pengaturan.KeyPengaturan
import org.joda.time.LocalDate
import simplejpa.SimpleJpaUtil
import simplejpa.transaction.Transaction

@Transaction
class KonsumenRepository {

    public List<Konsumen> cari(String namaSearch) {
        findAllKonsumenByDsl([excludeDeleted: false]) {
            if (namaSearch) {
                nama like("%${namaSearch}%")
            }
        }
    }

    public Konsumen buat(Konsumen konsumen) {
        if (findKonsumenByNama(konsumen.nama)) {
            throw new DataDuplikat(konsumen)
        }
        konsumen.creditLimit = SimpleJpaUtil.container.pengaturanRepository.getValue(KeyPengaturan.CREDIT_LIMIT_DEFAULT)
        persist(konsumen)
        konsumen
    }

    public Konsumen update(Konsumen konsumen) {
        Konsumen mergedKonsumen = findKonsumenById(konsumen.id)
        if (!mergedKonsumen) {
            throw new DataTidakBolehDiubah(konsumen)
        }
        mergedKonsumen.with {
            nama = konsumen.nama
            nomorTelepon = konsumen.nomorTelepon
            alamat = konsumen.alamat
            region = konsumen.region
            sales = konsumen.sales
        }
        mergedKonsumen
    }

    public Konsumen aturCreditLimit(Konsumen konsumen, BigDecimal creditLimit) {
        konsumen = findKonsumenById(konsumen.id)
        konsumen.creditLimit = creditLimit
        konsumen
    }

    public BigDecimal hargaTerakhir(Konsumen konsumen, Produk produk) {
        konsumen = findKonsumenById(konsumen.id)
        konsumen.hargaTerakhir(produk)
    }

}

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
package domain.pembelian

import domain.Container
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class FakturBeliRepository {

    public List<FakturBeli> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch, def statusSearch) {
        findAllFakturBeliByDslFetchItems([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
        }
    }

    public List<FakturBeli> cariHutang(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch,
            String supplierSearch, LocalDate tanggalJatuhTempo = null, StatusHutangSearch statusHutangSearch = StatusHutangSearch.SEMUA) {

        findAllFakturBeliByDslFetchHutang([orderBy: 'tanggal,nomor']) {
            hutang isNotNull()
            if (statusHutangSearch != StatusHutangSearch.BELUM_LUNAS) {
                and()
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
            if (tanggalJatuhTempo) {
                and()
                hutang__jatuhTempo eq(tanggalJatuhTempo)
            }
            if (statusHutangSearch == StatusHutangSearch.BELUM_LUNAS) {
                and()
                hutang__lunas eq(false)
            } else if (statusHutangSearch == StatusHutangSearch.LUNAS) {
                and()
                hutang__lunas eq(true)
            }
        }

    }

    public FakturBeli buat(FakturBeli fakturBeli) {
        if (findFakturBeliByNomor(fakturBeli.nomor)) {
            throw new DataDuplikat(fakturBeli)
        }
        fakturBeli.status = StatusFakturBeli.DIBUAT
        persist(fakturBeli)
        fakturBeli
    }

    public FakturBeli update(FakturBeli fakturBeli) {
        FakturBeli f = findFakturBeliById(fakturBeli.id)
        if (f.status.setelah(StatusFakturBeli.DIBUAT) || f.deleted == 'Y') {
            throw new DataTidakBolehDiubah(fakturBeli)
        }
        f.nomor = fakturBeli.nomor
        f.tanggal = fakturBeli.tanggal
        f.keterangan = fakturBeli.keterangan
        f.supplier = fakturBeli.supplier
        f.diskon = fakturBeli.diskon
        f
    }

    public FakturBeli hapus(FakturBeli fakturBeli) {
        FakturBeli f = findFakturBeliById(fakturBeli.id)
        if (f.status.setelah(StatusFakturBeli.DIBUAT)) {
            throw new DataTidakBolehDiubah(fakturBeli)
        }
        if (f.deleted != 'Y') f.deleted = 'Y'
        f
    }

    public enum StatusHutangSearch {
        SEMUA('Semua'), BELUM_LUNAS('Belum Lunas'), LUNAS('Lunas')

        String description

        public StatusHutangSearch(String description) {
            this.description = description
        }

        @Override
        String toString() {
            description
        }
    }

}

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
package project.inventory

import domain.event.PerubahanStok
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.exception.StokTidakCukup
import domain.inventory.PenyesuaianStok
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import project.user.NomorService
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class PenyesuaianStokRepository {

    def app

    NomorService nomorService

    List cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String gudangSearch) {
        findAllPenyesuaianStokByDsl([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorSearch}%")
            }
            if (gudangSearch) {
                and()
                gudang__nama like("%${gudangSearch}%")
            }
        }
    }

    PenyesuaianStok buat(PenyesuaianStok penyesuaianStok) {
        penyesuaianStok.nomor = nomorService.buatNomor(NomorService.TIPE.PENYESUAIAN_STOK)
        if (findPenyesuaianStokByNomor(penyesuaianStok.nomor)) {
            throw new DataDuplikat(penyesuaianStok)
        }
        penyesuaianStok.items.each {
            it.produk = findProdukById(it.produk.id)
            if (!penyesuaianStok.bertambah) {
                int jumlahTersedia = it.produk.stok(penyesuaianStok.gudang).jumlah
                if (jumlahTersedia < it.jumlah) {
                    throw new StokTidakCukup(it.produk.nama, it.jumlah, jumlahTersedia, penyesuaianStok.gudang)
                }
            }
        }
        persist(penyesuaianStok)
        ReferensiStok ref = new ReferensiStokBuilder(penyesuaianStok).buat()
        app?.event(new PerubahanStok(penyesuaianStok.toDaftarBarang(), ref))
        penyesuaianStok
    }

    PenyesuaianStok update(PenyesuaianStok penyesuaianStok) {
        PenyesuaianStok mergedPenyesuaianStok = findPenyesuaianStokById(penyesuaianStok.id)
        if (!mergedPenyesuaianStok) {
            throw new DataTidakBolehDiubah(mergedPenyesuaianStok)
        }
        mergedPenyesuaianStok.with {
            nomor = penyesuaianStok.nomor
            tanggal = penyesuaianStok.tanggal
            keterangan = penyesuaianStok.keterangan
        }
        mergedPenyesuaianStok.ubahHarga(penyesuaianStok.items)
        mergedPenyesuaianStok
    }

    PenyesuaianStok hapus(PenyesuaianStok penyesuaianStok) {
        penyesuaianStok = findPenyesuaianStokById(penyesuaianStok.id)
        if (!penyesuaianStok) {
            throw new DataTidakBolehDiubah(penyesuaianStok)
        }
        penyesuaianStok.deleted = 'Y'
        ReferensiStok ref = new ReferensiStokBuilder(penyesuaianStok).buat()
        app?.event(new PerubahanStok(penyesuaianStok.toDaftarBarang(), ref, true))
        penyesuaianStok
    }

}

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
import domain.event.DaftarBarangDihapus
import domain.event.PerubahanStok
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.inventory.Gudang
import domain.inventory.Periode
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction
import griffon.util.*

@Transaction
class PenerimaanBarangRepository {

    public List<PenerimaanBarang> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch) {
        findAllPenerimaanBarangByDslFetchComplete([orderBy: 'tanggal']) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
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

    public List<PenerimaanBarang> cariReceivedNotInvoiced(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch) {
        findAllPenerimaanBarangByDslFetchComplete([orderBy: 'tanggal']) {
            faktur isNull()
            and()
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
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

    public PenerimaanBarang buat(PenerimaanBarang penerimaanBarang) {
        if (findPenerimaanBarangByNomor(penerimaanBarang.nomor)) {
            throw new DataDuplikat(penerimaanBarang)
        }

        //
        // Penerimaan barang (untuk non-mutasi) selalu ke gudang utama
        //
        Gudang gudangUtama = Container.app.gudangRepository.cariGudangUtama()
        penerimaanBarang.gudang = gudangUtama
        persist(penerimaanBarang)

        ApplicationHolder.application.event(new PerubahanStok(penerimaanBarang))
        penerimaanBarang
    }

    public PenerimaanBarang update(PenerimaanBarang penerimaanBarang) {
        PenerimaanBarang p = findPenerimaanBarangByIdFetchComplete(penerimaanBarang.id)
        if (p.faktur || p.deleted == 'Y') {
            throw new DataTidakBolehDiubah(penerimaanBarang)
        }
        p.nomor = penerimaanBarang.nomor
        p.tanggal = penerimaanBarang.tanggal
        p.keterangan = penerimaanBarang.keterangan
        p.supplier = penerimaanBarang.supplier
        p
    }

    public PenerimaanBarang hapus(PenerimaanBarang penerimaanBarang) {
        PenerimaanBarang p = findPenerimaanBarangByIdFetchComplete(penerimaanBarang.id)
        if (p.faktur) {
            throw new DataTidakBolehDiubah(penerimaanBarang)
        }
        if (p.deleted != 'Y') {
            p.deleted = 'Y'
            ApplicationHolder.application.event(new DaftarBarangDihapus(p))
        }
        p
    }

}

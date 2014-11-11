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
package project.servis

import domain.event.PerubahanStokTukar
import domain.exception.DataTidakBolehDiubah
import domain.inventory.ItemBarang
import domain.servis.*
import org.joda.time.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.exception.EntityNotFoundException
import simplejpa.transaction.Transaction

@Transaction
class PenerimaanServisRepository {

    public List<PenerimaanServis> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch) {
        findAllPenerimaanServisByDsl([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
        }
    }

    public PenerimaanServis buat(PenerimaanServis penerimaanServis) {
        if (findPenerimaanServisByNomor(penerimaanServis.nomor)) {
            throw new DuplicateEntityException(penerimaanServis)
        }
        // Periksa apakah qty barang tidak melebihi jumlah yang di-retur
        penerimaanServis.items.each { ItemBarang i ->
            i.produk = findProdukById(i.produk.id)
            if (i.jumlah > i.produk.jumlahRetur) {
                throw new IllegalArgumentException("Produk ${i.produk.nama} yang di-retur hanya [${i.produk.jumlahRetur}], tetapi yang akan di-servis sebanyak [${i.jumlah}]")
            }
        }

        persist(penerimaanServis)
        ApplicationHolder.application?.event(new PerubahanStokTukar(penerimaanServis))
        penerimaanServis
    }

    public PenerimaanServis update(PenerimaanServis penerimaanServis) {
        PenerimaanServis mergedPenerimaanServis = findPenerimaanServisById(penerimaanServis.id)
        if (!mergedPenerimaanServis) {
            throw new EntityNotFoundException(penerimaanServis)
        }
        mergedPenerimaanServis.with {
            nomor = penerimaanServis.nomor
            tanggal = penerimaanServis.tanggal
            keterangan = penerimaanServis.keterangan
        }
        mergedPenerimaanServis
    }

    public PenerimaanServis hapus(PenerimaanServis penerimaanServis) {
        penerimaanServis = findPenerimaanServisById(penerimaanServis.id)
        if (!penerimaanServis) {
            throw new DataTidakBolehDiubah(penerimaanServis)
        }
        penerimaanServis.deleted = 'Y'
        ApplicationHolder.application?.event(new PerubahanStokTukar(penerimaanServis, true))
        penerimaanServis
    }

}
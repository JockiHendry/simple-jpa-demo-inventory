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
package project.retur

import domain.event.PerubahanRetur
import domain.event.PerubahanStok
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.pembelian.PenerimaanBarang
import domain.retur.ReturBeli
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class ReturBeliRepository {

    List<ReturBeli> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch) {
        findAllReturBeliByDsl([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier_nama like("%${supplierSearch}%")
            }
        }
    }

    public ReturBeli buat(ReturBeli returBeli) {
        if (findReturBeliByNomor(returBeli.nomor)) {
            throw new DataDuplikat(returBeli)
        }
        returBeli.items.each { it.produk = merge(it.produk) }
        persist(returBeli)
        ApplicationHolder.application?.event(new PerubahanRetur(returBeli))
        returBeli
    }

    public ReturBeli update(ReturBeli returBeli) {
        ReturBeli mergedRetur = findReturBeliById(returBeli.id)
        if (!mergedRetur) {
            throw new DataTidakBolehDiubah(returBeli)
        }
        mergedRetur.with {
            nomor = returBeli.nomor
            tanggal = returBeli.tanggal
            supplier = merge(returBeli.supplier)
            keterangan = returBeli.keterangan
        }
        mergedRetur
    }


    public ReturBeli hapus(ReturBeli returBeli) {
        returBeli = findReturBeliById(returBeli.id)
        if (!returBeli || returBeli.penerimaanBarang) {
            throw new DataTidakBolehDiubah(returBeli)
        }
        ApplicationHolder.application?.event(new PerubahanRetur(returBeli, true))
        returBeli.deleted = 'Y'
        returBeli
    }

    public ReturBeli tukarBaru(ReturBeli returBeli) {
        returBeli = findReturBeliById(returBeli.id)
        PenerimaanBarang penerimaanBarang = returBeli.tukar()
        persist(penerimaanBarang)
        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, null))
        returBeli
    }

}

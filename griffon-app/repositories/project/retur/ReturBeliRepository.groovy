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
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import domain.retur.Kemasan
import domain.retur.ReturBeli
import org.joda.time.LocalDate
import project.user.NomorService
import simplejpa.transaction.Transaction

@Transaction
class ReturBeliRepository {

    List<ReturBeli> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String supplierSearch, Boolean sudahDiterimaSearch, boolean excludeDeleted = false) {
        findAllReturBeliByDsl([orderBy: 'tanggal,nomor', excludeDeleted: excludeDeleted]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (supplierSearch) {
                and()
                supplier__nama like("%${supplierSearch}%")
            }
            if (sudahDiterimaSearch != null) {
                and()
                sudahDiterima eq(sudahDiterimaSearch)
            }
        }
    }

    List<ReturBeli> cariForSupplier(Supplier supplierSearch, LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, Boolean sudahDiterimaSearch, boolean excludeDeleted = false) {
        findAllReturBeliByDsl([orderBy: 'tanggal,nomor', excludeDeleted: excludeDeleted]) {
            supplier eq(supplierSearch)
            and()
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (sudahDiterimaSearch != null) {
                and()
                sudahDiterima eq(sudahDiterimaSearch)
            }
        }
    }

    public ReturBeli buat(ReturBeli returBeli) {
        if (findReturBeliByNomor(returBeli.nomor)) {
            throw new DataDuplikat(returBeli)
        }
        returBeli.nomor = ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.RETUR_BELI)
        returBeli.items.each { Kemasan k -> k.items.each { it.produk = findProdukById(it.produk.id) } }
        persist(returBeli)
        ApplicationHolder.application?.event(new PerubahanRetur(returBeli))
        returBeli
    }

    public ReturBeli update(ReturBeli returBeli) {
        ReturBeli mergedRetur = findReturBeliById(returBeli.id)
        if (!mergedRetur) {
            throw new DataTidakBolehDiubah(returBeli)
        }
        if (mergedRetur.sudahDiterima) {
            throw new DataTidakBolehDiubah('Tidak boleh mengubah retur beli karena sudah diproses dan diterima!', returBeli)
        }
        returBeli.items.each { Kemasan k -> k.items.each {it.produk = findProdukById(it.produk.id)} }
        ApplicationHolder.application?.event(new PerubahanRetur(returBeli, mergedRetur))
        mergedRetur.with {
            nomor = returBeli.nomor
            tanggal = returBeli.tanggal
            supplier = findSupplierById(returBeli.supplier.id)
            keterangan = returBeli.keterangan
            items.clear()
            returBeli.items.each {
                tambah(merge(it))
            }
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

    public ReturBeli terima(ReturBeli returBeli) {
        returBeli = findReturBeliById(returBeli.id)
        PenerimaanBarang penerimaanBarang = returBeli.terima()
        persist(penerimaanBarang)
        ReferensiStok ref = new ReferensiStokBuilder(penerimaanBarang, returBeli).buat()
        ApplicationHolder.application?.event(new PerubahanStok(penerimaanBarang, ref))
        returBeli
    }

}

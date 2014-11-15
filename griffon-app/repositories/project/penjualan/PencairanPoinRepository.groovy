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
package project.penjualan

import domain.exception.DataTidakBolehDiubah
import domain.exception.PencairanPoinTidakValid
import domain.pengaturan.KeyPengaturan
import domain.penjualan.PencairanPoin
import domain.penjualan.PencairanPoinTukarBarang
import project.user.NomorService
import org.joda.time.LocalDate
import simplejpa.SimpleJpaUtil
import simplejpa.transaction.Transaction

@Transaction
class PencairanPoinRepository {

    NomorService nomorService

    public List<PencairanPoin> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String konsumenSearch) {
        findAllPencairanPoinByDsl([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (!nomorSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
        }
    }

    public PencairanPoin buat(PencairanPoin pencairanPoin) {
        pencairanPoin.konsumen = findKonsumenById(pencairanPoin.konsumen.id)
        // Apakah poin cukup?
        if (pencairanPoin.konsumen.poinTerkumpul < pencairanPoin.jumlahPoin) {
            throw new IllegalArgumentException("Jumlah poin konsumen [${pencairanPoin.konsumen.poinTerkumpul}] tidak mencukupi!")
        }
        if (pencairanPoin instanceof PencairanPoinTukarBarang) {
            pencairanPoin.listItemBarang.each { it.produk = findProdukById(it.produk.id) }
        }
        pencairanPoin.nomor = nomorService.buatNomor(NomorService.TIPE.PENCAIRAN_POIN)
        BigDecimal rate = SimpleJpaUtil.instance.repositoryManager.findRepository('Pengaturan').getValue(KeyPengaturan.BONUS_POINT_RATE)
        pencairanPoin.rate = rate
        if (!pencairanPoin.valid()) {
            throw new PencairanPoinTidakValid(pencairanPoin)
        }
        pencairanPoin.proses()
        pencairanPoin.konsumen.hapusPoin(pencairanPoin.jumlahPoin, "${pencairanPoin.class.simpleName} ${pencairanPoin.nomor}")
        persist(pencairanPoin)
        pencairanPoin
    }

    public PencairanPoin hapus(PencairanPoin pencairanPoin) {
        pencairanPoin = findPencairanPoinById(pencairanPoin.id)
        if (!pencairanPoin) {
            throw new DataTidakBolehDiubah(pencairanPoin)
        }
        if (pencairanPoin instanceof PencairanPoinTukarBarang) {
            pencairanPoin.listItemBarang.each { it.produk = merge(it.produk) }
        }
        pencairanPoin.hapus()
        pencairanPoin.deleted = 'Y'
        pencairanPoin.konsumen.tambahPoin(pencairanPoin.jumlahPoin)
        pencairanPoin
    }

}

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
package project.labarugi

import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.labarugi.*
import org.joda.time.*
import project.user.NomorService
import simplejpa.transaction.Transaction
import util.SwingHelper

@Transaction
class TransaksiKasRepository {

    NomorService nomorService

    public List<TransaksiKas> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String pihakTerkaitSearch, String kategoriKasSearch, def jenisSearch) {
        findAllTransaksiKasByDsl([excludeDeleted: false, orderBy: 'tanggal,nomor']) {
            if (!nomorSearch) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            } else {
                nomor like("%${nomorSearch}%")
            }
            if (pihakTerkaitSearch) {
                and()
                pihakTerkait like("%${pihakTerkaitSearch}%")
            }
            if (kategoriKasSearch) {
                and()
                kategoriKas__nama like("%${kategoriKasSearch}%")
            }
            if (jenisSearch && (jenisSearch != SwingHelper.SEMUA)) {
                and()
                jenis eq(jenisSearch)
            }
        }
    }

    public TransaksiKas buat(TransaksiKas transaksiKas) {
        if (findTransaksiKasByNomor(transaksiKas.nomor)) {
            throw new DataDuplikat(transaksiKas)
        }
        transaksiKas.nomor = nomorService.buatNomor(NomorService.TIPE.TRANSAKSI_KAS)
        transaksiKas.kategoriKas = findKategoriKasById(transaksiKas.kategoriKas.id)
        persist(transaksiKas)
        transaksiKas.tambahKas()
        transaksiKas
    }

    public TransaksiKas update(TransaksiKas transaksiKas) {
        TransaksiKas mergedTransaksiKas = findTransaksiKasById(transaksiKas.id)
        if (!mergedTransaksiKas) {
            throw new DataTidakBolehDiubah(transaksiKas)
        }
        mergedTransaksiKas.with {
            tanggal = transaksiKas.tanggal
            pihakTerkait = transaksiKas.pihakTerkait
            keterangan = transaksiKas.keterangan
        }
        mergedTransaksiKas
    }

    public TransaksiKas hapus(TransaksiKas transaksiKas) {
        transaksiKas = findTransaksiKasById(transaksiKas.id)
        if (!transaksiKas) {
            throw new DataTidakBolehDiubah(transaksiKas)
        }
        transaksiKas.deleted = 'Y'
        transaksiKas.kategoriKas = findKategoriKasById(transaksiKas.kategoriKas.id)
        transaksiKas.tambahKas(true)
        transaksiKas
    }

}
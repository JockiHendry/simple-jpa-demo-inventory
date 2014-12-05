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

import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.KATEGORI_SISTEM
import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import org.joda.time.LocalDate
import static project.labarugi.KategoriKasRepository.*

@SuppressWarnings("GroovyUnusedDeclaration")
class KategoriKasService {

    KategoriKasRepository kategoriKasRepository

    void serviceInit() {
        // Buat kategori pendapatan tukar barang bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }

        // Buat kategori lain-lain bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }
    }

    void refreshSaldoKas(KategoriKas kategoriKas) {
        kategoriKasRepository.withTransaction {
            kategoriKas = findKategoriKasById(kategoriKas.id)
            kategoriKas.listSaldoKas.clear()
            List<TransaksiKas> daftarTransaksi = findAllTransaksiKasByKategoriKas(kategoriKas)
            for (TransaksiKas transaksiKas : daftarTransaksi) {
                transaksiKas.tambahKas()
            }
        }
    }

    BigDecimal totalPendapatan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        kategoriKasRepository.executeQuery('''
            SELECT
                SUM(t.jumlah)
            FROM
                TransaksiKas t
            WHERE
                (t.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai)
            AND
                t.kategoriKas.dipakaiDiLaporan = TRUE
            AND
                t.kategoriKas.jenis = domain.labarugi.JENIS_KATEGORI_KAS.PENDAPATAN
            AND
                t.deleted <> 'Y'
        ''', [:], [tanggalMulai: tanggalMulai, tanggalSelesai: tanggalSelesai])[0]?:0 as BigDecimal
    }

    BigDecimal totalPengeluaran(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        kategoriKasRepository.executeQuery('''
            SELECT
                SUM(t.jumlah)
            FROM
                TransaksiKas t
            WHERE
                (t.tanggal BETWEEN :tanggalMulai AND :tanggalSelesai)
            AND
                t.kategoriKas.dipakaiDiLaporan = TRUE
            AND
                t.kategoriKas.jenis = domain.labarugi.JENIS_KATEGORI_KAS.PENGELUARAN
            AND
                t.deleted <> 'Y'
        ''', [:], [tanggalMulai: tanggalMulai, tanggalSelesai: tanggalSelesai])[0]?:0 as BigDecimal
    }

}

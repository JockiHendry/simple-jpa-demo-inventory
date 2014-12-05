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

import domain.exception.DataTidakBolehDiubah
import domain.labarugi.*
import simplejpa.transaction.Transaction
import util.SwingHelper

@Transaction
class KategoriKasRepository {

	public static final String KATEGORI_TUKAR_BARANG = 'Tukar Barang'
	public static final String KATEGORI_LAIN = 'Lain-Lain'

	public List<KategoriKas> cari(String namaSearch = null, def jenisSearch = null) {
		findAllKategoriKasByDsl([excludeDeleted: false, orderBy: 'nama']) {
			if (namaSearch) {
				nama like("%${namaSearch}%")
			}
			if (jenisSearch && (jenisSearch != SwingHelper.SEMUA)) {
				and()
				jenis eq(jenisSearch)
			}
		}
	}

	public KategoriKas buat(KategoriKas kategoriKas) {
		persist(kategoriKas)
		kategoriKas
	}

	public KategoriKas getKategoriSistem(KATEGORI_SISTEM kategoriSistem) {
		if (kategoriSistem == KATEGORI_SISTEM.PENDAPATAN_TUKAR_BARANG) {
			return findKategoriKasByNamaAndJenisAndSistem(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENDAPATAN, true)
		} else if (kategoriSistem == KATEGORI_SISTEM.PENGELUARAN_TUKAR_BARANG) {
			return findKategoriKasByNamaAndJenisAndSistem(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENGELUARAN, true)
		} else if (kategoriSistem == KATEGORI_SISTEM.PENDAPATAN_LAIN) {
			return findKategoriKasByNamaAndJenisAndSistem(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENDAPATAN, true)
		} else if (kategoriSistem == KATEGORI_SISTEM.PENGELUARAN_LAIN) {
			return findKategoriKasByNamaAndJenisAndSistem(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENGELUARAN, true)
		}
		throw new IllegalArgumentException('Kategori sistem tidak dikenal!')
	}

	public KategoriKas update(KategoriKas kategoriKas) {
		KategoriKas mergedKategoriKas = findKategoriKasById(kategoriKas.id)
		if (!mergedKategoriKas) {
			throw new DataTidakBolehDiubah(kategoriKas)
		}
		if (kategoriKas.sistem) {
			throw new DataTidakBolehDiubah('Kategori bawaan sistem tidak boleh diubah!', kategoriKas)
		}
		mergedKategoriKas.with {
			nama = kategoriKas.nama
			jenis = kategoriKas.jenis
			dipakaiDiLaporan = kategoriKas.dipakaiDiLaporan
		}
		mergedKategoriKas
	}

    public KategoriKas hapus(KategoriKas kategoriKas) {
        kategoriKas = findKategoriKasById(kategoriKas.id)
        if (!kategoriKas) {
            throw new DataTidakBolehDiubah(kategoriKas)
        }
        kategoriKas.deleted = 'Y'
        kategoriKas
    }

}
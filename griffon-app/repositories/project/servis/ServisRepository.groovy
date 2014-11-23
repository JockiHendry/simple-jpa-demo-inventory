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

import domain.exception.DataTidakBolehDiubah
import domain.servis.*
import simplejpa.exception.EntityNotFoundException
import simplejpa.transaction.Transaction

@Transaction
class ServisRepository {

	public List<Servis> cari(String namaKonsumenSearch) {
		findAllServisByDsl([excludeDeleted: false]) {
			if (namaKonsumenSearch) {
				namaKonsumen like("%${namaKonsumenSearch}%")
			}
		}
	}

	public Servis buat(Servis servis) {
		persist(servis)
		servis
	}

	public Servis update(Servis servis) {
		Servis mergedServis = findServisById(servis.id)
		if (!mergedServis) {
			throw new EntityNotFoundException(servis)
		}
		mergedServis.with {
			namaKonsumen = servis.namaKonsumen
			alamat = servis.alamat
			tipe = servis.tipe
			keluhan = servis.keluhan
			keterangan = servis.keterangan
			tanggalMasuk = servis.tanggalMasuk
			tanggalSelesai = servis.tanggalSelesai
			tanggalDiambil = servis.tanggalDiambil
		}
		mergedServis
	}

    public void hapus(Servis servis) {
        servis = findServisById(servis.id)
        if (!servis) {
            throw new DataTidakBolehDiubah(servis)
        }
        remove(servis)
    }

}
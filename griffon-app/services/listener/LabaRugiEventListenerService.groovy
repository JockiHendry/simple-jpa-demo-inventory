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
package listener

import domain.event.TransaksiSistem
import domain.labarugi.KategoriKas
import domain.labarugi.TransaksiKas
import org.joda.time.LocalDate
import project.labarugi.JenisTransaksiKasRepository
import project.labarugi.KategoriKasRepository
import project.labarugi.TransaksiKasRepository

@SuppressWarnings("GroovyUnusedDeclaration")
class LabaRugiEventListenerService {

    TransaksiKasRepository transaksiKasRepository
    KategoriKasRepository kategoriKasRepository
    JenisTransaksiKasRepository jenisTransaksiKasRepository

    void onTransaksiSistem(TransaksiSistem transaksiSistem) {
        KategoriKas kategori = kategoriKasRepository.getKategoriSistem(transaksiSistem.kategori)
        TransaksiKas transaksiKas = new TransaksiKas(null, LocalDate.now(), transaksiSistem.nomorReferensi, kategori,
            transaksiSistem.jumlah, jenisTransaksiKasRepository.cariUntukSistem())
        if (transaksiSistem.invers) {
            transaksiKas.jumlah = -1 * transaksiKas.jumlah
        }
        transaksiKasRepository.buat(transaksiKas)
    }

}

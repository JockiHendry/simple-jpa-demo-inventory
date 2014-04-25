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
package domain.penjualan

import domain.Container
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.BilyetGiro
import domain.faktur.Pembayaran
import domain.inventory.Gudang
import domain.inventory.Produk
import domain.pengaturan.KeyPengaturan
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction

@Transaction
class FakturJualRepository {

    List<FakturJual> cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String konsumenSearch, def statusSearch) {
        findAllFakturJualByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
        }
    }

    List<FakturJualEceran> cariFakturJualEceran(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String namaPembeliSearch, def statusSearch) {
        findAllFakturJualEceranByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (namaPembeliSearch) {
                and()
                namaPembeli like("%${namaPembeliSearch}%")
            }
        }
    }

    FakturJual buatFakturJualOlehSales(FakturJualOlehSales fakturJual, boolean tanpaLimit = false) {
        Konsumen konsumen = merge(fakturJual.konsumen)

        // Periksa limit bila perlu
        if (!tanpaLimit) {
            if (!konsumen.bolehKredit(fakturJual.total())) {
                throw new MelebihiBatasKredit(konsumen)
            }
        }

        // Hitung tanggal jatuh tempo
        fakturJual.jatuhTempo = fakturJual.tanggal.plusDays(Container.app.pengaturanRepository.getValue(KeyPengaturan.MASA_JATUH_TEMPO))

        persist(fakturJual)

        // Perlakuan khusus untuk faktur jual luar kota
        if (!fakturJual.sales.dalamKota()) {
            fakturJual.listItemFaktur.each {
                it.produk = merge(it.produk)
            }
            fakturJual.kirim('Luar Kota', "Luar Kota (${fakturJual.sales.nama})")
            fakturJual.tambah(new BuktiTerima(fakturJual.tanggal, 'Luar Kota'))
        }

        // Tambahkan faktur jual yang baru dibuat pada konsumen yang bersangkutan
        konsumen.listFakturBelumLunas << fakturJual
        fakturJual
    }

    FakturJualEceran buatFakturJualEceran(FakturJualEceran fakturJualEceran) {

        // Periksa apakah barang dalam gudang utama mencukupi
        Gudang gudangUtama = Container.app.gudangRepository.cariGudangUtama()
        fakturJualEceran.listItemFaktur.each {
            Produk produk = merge(it.produk)
            int jumlahTersedia = produk.stok(gudangUtama).jumlah
            if (jumlahTersedia < it.jumlah) {
                throw new StokTidakCukup(produk.nama, it.jumlah, jumlahTersedia)
            }
        }

        persist(fakturJualEceran)
        fakturJualEceran
    }

    FakturJual buat(FakturJual fakturJual, boolean tanpaLimit = false) {
        fakturJual.nomor = Container.app.nomorService.buatNomor(NomorService.TIPE.FAKTUR_JUAL)
        if (findFakturJualByNomor(fakturJual.nomor)) {
            throw new DataDuplikat(fakturJual)
        }

        if (fakturJual instanceof FakturJualOlehSales) {
            fakturJual = buatFakturJualOlehSales(fakturJual, tanpaLimit)
        } else {
            fakturJual = buatFakturJualEceran(fakturJual)
        }

        fakturJual
    }

    FakturJualOlehSales bayar(FakturJualOlehSales fakturJualOlehSales, Pembayaran pembayaran, BilyetGiro bilyetGiro = null) {
        fakturJualOlehSales = merge(fakturJualOlehSales)
        if(bilyetGiro) {
            if (bilyetGiro.id == null) {
                persist(bilyetGiro)
            } else {
                bilyetGiro = merge(bilyetGiro)
            }
            pembayaran.bilyetGiro = bilyetGiro
        }
        fakturJualOlehSales.bayar(pembayaran)
        fakturJualOlehSales
    }

    FakturJual update(FakturJual fakturJual) {
        FakturJual mergedFakturJual = findFakturJualById(fakturJual.id)
        if (!mergedFakturJual || !mergedFakturJual.status.bolehDiubah) {
            throw new DataTidakBolehDiubah(fakturJual)
        }
        mergedFakturJual.with {
            tanggal = fakturJual.tanggal
            diskon = fakturJual.diskon
            keterangan = fakturJual.keterangan
        }
        if (fakturJual instanceof FakturJualOlehSales) {
            ((FakturJualOlehSales)mergedFakturJual).with {
                sales = ((FakturJualOlehSales)fakturJual).sales
                konsumen = ((FakturJualOlehSales)fakturJual).konsumen
                jatuhTempo = ((FakturJualOlehSales)fakturJual).jatuhTempo
            }
        } else if (fakturJual instanceof FakturJualEceran) {
            ((FakturJualEceran)mergedFakturJual).with {
                namaPembeli = ((FakturJualEceran)fakturJual).namaPembeli
            }
        }
        mergedFakturJual
    }

    FakturJual hapus(FakturJual fakturJual) {
        fakturJual = findFakturJualById(fakturJual.id)
        if (!fakturJual || !fakturJual.status.bolehDiubah) {
            throw new DataTidakBolehDiubah(fakturJual)
        }
        fakturJual.deleted = 'Y'
        fakturJual
    }

    FakturJualEceran antar(FakturJualEceran fakturJualEceran) {
        fakturJualEceran = findFakturJualEceranById(fakturJualEceran.id)
        fakturJualEceran.antar()
        fakturJualEceran
    }

    FakturJualEceran bayar(FakturJualEceran fakturJualEceran) {
        fakturJualEceran = findFakturJualEceranById(fakturJualEceran.id)
        fakturJualEceran.bayar()
        fakturJualEceran
    }
}

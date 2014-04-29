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
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.pengaturan.KeyPengaturan
import domain.util.NomorService
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

    List<FakturJualOlehSales> cariFakturJualOlehSales(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String salesSearch, String konsumenSearch, def statusSearch) {
        findAllFakturJualOlehSalesByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (salesSearch) {
                and()
                sales__nama like("%${salesSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
        }
    }

    List<FakturJualOlehSales> cariFakturJualUntukPengiriman(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String salesSearch, String konsumenSearch, def statusSearch) {
        findAllFakturJualOlehSalesByDslFetchPengeluaranBarang([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (salesSearch) {
                and()
                sales__nama like("%${salesSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
        }
    }

    List<FakturJualOlehSales> cariFakturJualUntukBuktiTerima(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorFakturSearch, String nomorSuratJalanSearch, String konsumenSearch, def statusSearch) {
        findAllFakturJualOlehSalesByDslFetchPengeluaranBarang([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (statusSearch != Container.SEMUA) {
                and()
                status eq(statusSearch)
            }
            if (nomorFakturSearch) {
                and()
                nomor like("%${nomorFakturSearch}%")
            }
            if (nomorSuratJalanSearch) {
                and()
                pengeluaranBarang__nomor like("%${nomorSuratJalanSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
        }
    }

    public List<FakturJualOlehSales> cariPiutang(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch,
                                        String konsumenSearch, LocalDate tanggalJatuhTempo = null,
                                        StatusPiutangSearch statusPiutangSearch = StatusPiutangSearch.SEMUA) {
        findAllFakturJualOlehSalesByDslFetchComplete([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            if (statusPiutangSearch != StatusPiutangSearch.BELUM_LUNAS) {
                tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            }
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
            if (tanggalJatuhTempo) {
                and()
                jatuhTempo eq(tanggalJatuhTempo)
            }
            List statusSearch = []
            if (statusPiutangSearch == StatusPiutangSearch.BELUM_LUNAS || statusPiutangSearch == StatusPiutangSearch.SEMUA) {
                statusSearch << StatusFakturJual.DITERIMA
            } else if (statusPiutangSearch == StatusPiutangSearch.LUNAS || statusPiutangSearch == StatusPiutangSearch.SEMUA) {
                statusSearch << StatusFakturJual.LUNAS
            }
            and()
            status isIn(statusSearch)
        }
    }

    FakturJual buatFakturJualOlehSales(FakturJualOlehSales fakturJual, boolean tanpaLimit = false, List<ItemBarang> bonus) {
        fakturJual.listItemFaktur.each { it.produk = merge(it.produk) }
        bonus.each { it.produk = merge(it.produk) }
        fakturJual.tambahBonus(bonus)
        buatFakturJualOlehSales(fakturJual, tanpaLimit)
    }

    FakturJual buatFakturJualOlehSales(FakturJualOlehSales fakturJual, boolean tanpaLimit = false) {
        Konsumen konsumen = merge(fakturJual.konsumen)

        // Periksa apakah jumlah barang yang tersedia cukup
        DaftarBarangSementara stokYangDibutuhkan = fakturJual.barangYangHarusDikirim()
        stokYangDibutuhkan.items.each { ItemBarang itemBarang ->
            Produk produk = findProdukById(itemBarang.produk.id)
            int jumlahTersedia = produk.stok(fakturJual.sales.gudang).jumlah
            if (jumlahTersedia < itemBarang.jumlah) {
                throw new StokTidakCukup(produk.nama, itemBarang.jumlah, jumlahTersedia)
            }
        }

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
        konsumen.tambahFakturBelumLunas(fakturJual)
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

    FakturJual buat(FakturJual fakturJual, boolean tanpaLimit = false, List<ItemBarang> bonus = []) {
        fakturJual.nomor = Container.app.nomorService.buatNomor(NomorService.TIPE.FAKTUR_JUAL)
        if (findFakturJualByNomor(fakturJual.nomor)) {
            throw new DataDuplikat(fakturJual)
        }

        if (fakturJual instanceof FakturJualOlehSales) {
            if (bonus && !bonus.empty) {
                fakturJual = buatFakturJualOlehSales(fakturJual, tanpaLimit, bonus)
            } else {
                fakturJual = buatFakturJualOlehSales(fakturJual, tanpaLimit)
            }
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
            FakturJualOlehSales nilaiFakturBaru = (FakturJualOlehSales) fakturJual
            ((FakturJualOlehSales)mergedFakturJual).with {
                // TODO:  Apakah boleh mengubah sales?  Ini akan mempengaruhi status faktur karena proses
                // TODO:  pengiriman berbeda antara sales dalam kota dan sales luar kota.
                //sales = nilaiFakturBaru.sales

                // Periksa limit bila mengubah konsumen
                if (konsumen != nilaiFakturBaru.konsumen) {
                    konsumen = findKonsumenById(nilaiFakturBaru.konsumen.id)
                    if (!konsumen.bolehKredit(mergedFakturJual.total())) {
                        throw new MelebihiBatasKredit(konsumen)
                    }
                }
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

    FakturJualEceran batalAntar(FakturJualEceran fakturJualEceran) {
        fakturJualEceran = findFakturJualEceranById(fakturJualEceran.id)
        fakturJualEceran.batalAntar()
        fakturJualEceran
    }

    FakturJualEceran bayar(FakturJualEceran fakturJualEceran) {
        fakturJualEceran = findFakturJualEceranById(fakturJualEceran.id)
        fakturJualEceran.bayar()
        fakturJualEceran
    }

    FakturJualOlehSales kirim(FakturJualOlehSales faktur, String alamatTujuan, String namaSupir, LocalDate tanggalKirim = LocalDate.now()) {
        faktur = findFakturJualOlehSalesById(faktur.id)
        faktur.kirim(alamatTujuan, namaSupir, tanggalKirim)
        faktur
    }

    FakturJualOlehSales batalKirim(FakturJualOlehSales faktur) {
        faktur = findFakturJualOlehSalesById(faktur.id)
        faktur.hapusPengeluaranBarang()
        faktur
    }

    FakturJualOlehSales terima(FakturJualOlehSales faktur, BuktiTerima buktiTerima) {
        faktur = findFakturJualOlehSalesById(faktur.id)
        faktur.tambah(buktiTerima)
        faktur
    }

    FakturJualOlehSales hapusBuktiTerima(FakturJualOlehSales faktur) {
        faktur = findFakturJualOlehSalesById(faktur.id)
        faktur.hapusBuktiTerima()
        faktur
    }

    public enum StatusPiutangSearch {
        SEMUA('Semua'), BELUM_LUNAS('Belum Lunas'), LUNAS('Lunas')

        String description

        public StatusPiutangSearch(String description) {
            this.description = description
        }

        @Override
        String toString() {
            description
        }
    }

}

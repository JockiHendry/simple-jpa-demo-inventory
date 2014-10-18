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
import domain.event.PesanStok
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.exception.StokTidakCukup
import domain.faktur.Referensi
import domain.inventory.Produk
import domain.inventory.ReferensiStok
import domain.inventory.ReferensiStokBuilder
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.PengeluaranBarang
import domain.retur.*
import org.joda.time.LocalDate
import project.penjualan.FakturJualRepository
import project.user.NomorService
import simplejpa.transaction.Transaction

@Transaction
class ReturJualRepository {

    NomorService nomorService
    FakturJualRepository fakturJualRepository
    
    List<ReturJual> cariReturOlehSales(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String konsumenSearch, Boolean sudahDiprosesSearch, boolean excludeDeleted = false) {
        findAllReturJualOlehSalesByDsl([orderBy: 'tanggal,nomor', excludeDeleted: excludeDeleted]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (konsumenSearch) {
                and()
                konsumen__nama like("%${konsumenSearch}%")
            }
            if (sudahDiprosesSearch != null) {
                and()
                sudahDiproses eq(sudahDiprosesSearch)
            }
        }
    }

    List<ReturJual> cariReturEceran(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String konsumenSearch, Boolean sudahDiprosesSearch, boolean excludeDeleted = false) {
        findAllReturJualEceranByDsl([orderBy: 'tanggal,nomor', excludeDeleted: excludeDeleted]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (konsumenSearch) {
                and()
                namaKonsumen like("%${konsumenSearch}%")
            }
            if (sudahDiprosesSearch != null) {
                and()
                sudahDiproses eq(sudahDiprosesSearch)
            }
        }
    }

	public ReturJual buat(ReturJual returJual) {
		if (findReturJualByNomor(returJual.nomor)) {
			throw new DataDuplikat(returJual)
		}
        if (returJual instanceof ReturJualOlehSales) {
            returJual = buatReturOlehSales(returJual)
        } else if (returJual instanceof ReturJualEceran) {
            returJual = buatReturEceran(returJual)
        }

        // Khusus untuk retur jual yang bukan kirim dari gudang utama, barang yang ditukar dianggap langsung dikirim.
        if (returJual instanceof ReturJualOlehSales && !returJual.gudang.utama && !returJual.getKlaimsTukar().empty) {
            tukar(returJual)
        } else {
            ApplicationHolder.application?.event(new PerubahanRetur(returJual))
        }

        ApplicationHolder.application?.event(new PesanStok(returJual))
        returJual
	}

    private ReturJualOlehSales buatReturOlehSales(ReturJualOlehSales returJualOlehSales) {
        // Periksa apakah barang yang di-klaim tersedia
        returJualOlehSales.getKlaimsTukar().each { KlaimTukar k ->
            Produk produk = findProdukById(k.produk.id)
            if (returJualOlehSales.gudang.utama) {
                if (!produk.tersediaUntuk(k.jumlah)) {
                    throw new StokTidakCukup(produk.nama, k.jumlah, produk.jumlahReadyGudangUtama(), returJualOlehSales.gudang)
                }
            } else {
                int jumlahStokGudang = produk.stok(returJualOlehSales.gudang).jumlah
                if (k.jumlah > jumlahStokGudang) {
                    throw new StokTidakCukup(produk.nama, k.jumlah, jumlahStokGudang, returJualOlehSales.gudang)
                }
            }
        }
        returJualOlehSales.nomor = nomorService.buatNomor(NomorService.TIPE.RETUR_JUAL_SALES)
        returJualOlehSales.konsumen = findKonsumenById(returJualOlehSales.konsumen.id)
        returJualOlehSales.items.each { it.produk = findProdukById(it.produk.id) }
        persist(returJualOlehSales)
        returJualOlehSales.potongPiutang()
        returJualOlehSales
    }

    private ReturJualEceran buatReturEceran(ReturJualEceran returJualEceran) {
        // Periksa apakah ada retur jual eceran yang di-klaim selain tukar
        if (!returJualEceran.items.every { ItemRetur i -> i.klaims.every { it instanceof KlaimTukar }}) {
            throw new DataTidakKonsisten('Tidak ada klaim selain tukar di retur jual eceran!', returJualEceran)
        }
        // Periksa apakah barang yang di-klaim tersedia
        returJualEceran.getKlaimsTukar().each { KlaimTukar k ->
            Produk produk = findProdukById(k.produk.id)
            if (!produk.tersediaUntuk(k.jumlah)) {
                throw new StokTidakCukup(produk.nama, k.jumlah, produk.jumlahReadyGudangUtama(), null)
            }
        }
        returJualEceran.nomor = nomorService.buatNomor(NomorService.TIPE.RETUR_JUAL_ECERAN)
        returJualEceran.items.each { it.produk = findProdukById(it.produk.id) }
        persist(returJualEceran)
        returJualEceran
    }

	public ReturJual update(ReturJual returJual) {
		ReturJual mergedRetur = findReturJualById(returJual.id)
		if (!mergedRetur) {
			throw new DataTidakBolehDiubah(returJual)
		}
		mergedRetur.with {
			nomor = returJual.nomor
			tanggal = returJual.tanggal
			keterangan = returJual.keterangan
		}
		mergedRetur
	}

    public ReturJual hapus(ReturJual returJual) {
        returJual = findReturJualById(returJual.id)
        if (!returJual) {
            throw new DataTidakBolehDiubah(returJual)
        }
        if (returJual.pengeluaranBarang != null) {
            throw new DataTidakBolehDiubah(returJual)
        }
        ApplicationHolder.application?.event(new PerubahanRetur(returJual, true))
        if (returJual instanceof ReturJualOlehSales) {
            // Hapus piutang khusus untuk retur jual oleh sales
            returJual.konsumen = findKonsumenById(returJual.konsumen.id)
            returJual.fakturPotongPiutang.each { Referensi r ->
                FakturJualOlehSales f = findFakturJualOlehSalesByNomor(r.nomor)
                f.hapusPembayaran(returJual.nomor)
            }
        }
        ApplicationHolder.application?.event(new PesanStok(returJual, true))
        returJual.deleted = 'Y'
        returJual
    }

    public ReturJual hapusPengeluaranBarang(ReturJual returJual) {
        returJual = findReturJualById(returJual.id)
        PengeluaranBarang pengeluaranBarang = returJual.pengeluaranBarang
        ReferensiStok ref = new ReferensiStokBuilder(pengeluaranBarang, returJual).buat()
        ApplicationHolder.application?.event(new PerubahanStok(pengeluaranBarang, ref, true, true))
        returJual.hapusPenukaran()
        returJual
    }

    public ReturJual tukar(ReturJual returJual) {
        returJual = findReturJualById(returJual.id)
        returJual.getKlaimsTukar(true).each { it.produk = findProdukById(it.produk.id) }
        PengeluaranBarang pengeluaranBarang = returJual.tukar()
        persist(pengeluaranBarang)
        returJual
    }

}


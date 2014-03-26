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
package domain.pembelian

import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.exception.FakturTidakDitemukan
import domain.inventory.ItemBarang
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simplejpa.transaction.Transaction

@Transaction
class PenerimaanBarangService {

    private static final Logger log = LoggerFactory.getLogger(PenerimaanBarangService)

    public PenerimaanBarang assign(PenerimaanBarang penerimaanBarang, String nomorFakturBeli) {
        FakturBeli fakturBeli = findFakturBeliByNomor(nomorFakturBeli)
        if (!fakturBeli) {
            throw new FakturTidakDitemukan(nomorFakturBeli)
        }
        assign(penerimaanBarang, fakturBeli)
    }

    public PenerimaanBarang assign(PenerimaanBarang penerimaanBarang, FakturBeli fakturBeli) {
        log.debug "Assign $penerimaanBarang ke $fakturBeli..."
        if (penerimaanBarang.deleted == 'Y') {
            throw new DataTidakBolehDiubah(penerimaanBarang)
        }
        if (fakturBeli.deleted == 'Y') {
            throw new DataTidakBolehDiubah(fakturBeli)
        }
        if (penerimaanBarang.faktur) {
            throw new DataTidakBolehDiubah('Penerimaan barang ini sudah di-assing sebelumnya!', penerimaanBarang)
        }
        if (fakturBeli.status.setelah(StatusFakturBeli.DIBUAT)) {
            throw new DataTidakBolehDiubah('Barang untuk faktur ini sudah diterima secara lengkap!', fakturBeli)
        }
        if (!fakturBeli.supplier.equals(penerimaanBarang.supplier)) {
            throw new DataTidakKonsisten('Supplier tidak sama!', fakturBeli)
        }
        penerimaanBarang = merge(penerimaanBarang)
        fakturBeli = merge(fakturBeli)
        penerimaanBarang.faktur = fakturBeli

        // Periksa apakah barang sudah diterima seluruhnya
        PenerimaanBarang p = new PenerimaanBarang()
        findAllPenerimaanBarangByFaktur(fakturBeli).each { p += it }
        if (p.isiSamaDengan(fakturBeli)) {
            log.debug "Faktur $fakturBeli sudah diterima seluruh barangnya."
            fakturBeli.status = StatusFakturBeli.BARANG_DITERIMA
            fakturBeli.buatHutang()
        }

        log.debug "Proses assign selesai."
        penerimaanBarang
    }

    public List<ItemBarang> sisaBelumDiterima(FakturBeli fakturBeli) {
        List daftarBarang = findFakturBeliByIdFetchComplete(fakturBeli.id).normalisasi()
        log.debug "Barang yang tertera di faktur: $daftarBarang"

        PenerimaanBarang p
        findAllPenerimaanBarangByFaktur(fakturBeli).each {
            p = (!p? it: (p + it))
        }
        if (!p) return daftarBarang
        List diterima = p.normalisasi()
        log.debug "Barang yang telah diterima: $diterima"

        diterima.each { d ->
            def i = daftarBarang.findIndexOf { it.produk == d.produk }
            if (i>=0) {
                if (daftarBarang[i].jumlah == d.jumlah) {
                    daftarBarang.remove(i)
                } else {
                    daftarBarang[i].jumlah -= d.jumlah
                }
            }
        }

        daftarBarang
    }

    public PenerimaanBarang hapusAssignment(PenerimaanBarang penerimaanBarang) {
        if (penerimaanBarang.deleted == 'Y') {
            throw new DataTidakBolehDiubah(penerimaanBarang)
        }
        penerimaanBarang = merge(penerimaanBarang)
        FakturBeli fakturBeli = (FakturBeli) penerimaanBarang.faktur
        if (fakturBeli.status.setelah(StatusFakturBeli.BARANG_DITERIMA)) {
            throw new DataTidakBolehDiubah('Faktur beli sudah lunas dan tidak boleh dimodifikasi!', fakturBeli)
        }
        if (fakturBeli.status == StatusFakturBeli.BARANG_DITERIMA) {
            if (fakturBeli.hutang?.jumlahDibayar() > 0) {
                throw new DataTidakBolehDiubah('Faktur beli memiliki hutang yang sudah dibayar!', fakturBeli)
            }
        }
        penerimaanBarang.faktur = null
        fakturBeli.status = StatusFakturBeli.DIBUAT
        fakturBeli.hutang = null

        penerimaanBarang
    }

}

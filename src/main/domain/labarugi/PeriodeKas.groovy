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
package domain.labarugi

import domain.general.ItemPeriodik
import domain.general.NilaiPeriodik
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*

@DomainClass @Entity @Canonical(excludes='listTransaksiKas,saldoPerKategori') @TupleConstructor(includeSuperProperties=true)
class PeriodeKas extends NilaiPeriodik {

    @ElementCollection @OrderColumn
    List<TransaksiKas> listTransaksiKas = []

    @ElementCollection(fetch=FetchType.EAGER)
    List<JumlahPeriodeKas> jumlahPeriodik = []

    @Override
    List getListItemPeriodik() {
        listTransaksiKas
    }

    JumlahPeriodeKas cariJumlahPeriodeKas(KategoriKas kategoriKas, JenisTransaksiKas jenisTransaksiKas) {
        JumlahPeriodeKas hasil = jumlahPeriodik.find { (it.kategoriKas == kategoriKas) && (it.jenisTransaksiKas == jenisTransaksiKas) }
        if (!hasil) {
            hasil = new JumlahPeriodeKas(kategoriKas, jenisTransaksiKas, 0)
            jumlahPeriodik << hasil
        }
        hasil
    }

    long jumlah(KategoriKas kategoriKas, JenisTransaksiKas jenisTransaksiKas = null) {
        jumlahPeriodik.sum { JumlahPeriodeKas s ->
            if (jenisTransaksiKas) {
                return ((s.kategoriKas == kategoriKas) && (s.jenisTransaksiKas == jenisTransaksiKas))? s.saldo : 0
            } else {
                return (s.kategoriKas == kategoriKas)? s.saldo: 0
            }
        }?: 0
    }

    long jumlah(JENIS_KATEGORI_KAS jenisKategoriKas, boolean dipakaiDiLaporan = false) {
        jumlahPeriodik.sum { JumlahPeriodeKas s ->
            if (dipakaiDiLaporan && !s.kategoriKas.dipakaiDiLaporan)  return 0
            (s.kategoriKas.jenis == jenisKategoriKas)? s.saldo: 0
        }?: 0
    }

    @Override
    void tambah(ItemPeriodik item) {
        if (!item instanceof TransaksiKas) {
            throw new IllegalArgumentException("$item bukan sebuah transaksi kas!")
        }
        TransaksiKas transaksiKas = (TransaksiKas) item
        super.tambah(transaksiKas)
        cariJumlahPeriodeKas(transaksiKas.kategoriKas, transaksiKas.jenis).saldo += item.jumlah
    }

    @Override
    void hapus(ItemPeriodik item) {
        if (!item instanceof TransaksiKas) {
            throw new IllegalArgumentException("$item bukan sebuah transaksi kas!")
        }
        TransaksiKas transaksiKas = (TransaksiKas) item
        super.hapus(transaksiKas)
        cariJumlahPeriodeKas(transaksiKas.kategoriKas, transaksiKas.jenis).saldo -= item.jumlah
    }

}


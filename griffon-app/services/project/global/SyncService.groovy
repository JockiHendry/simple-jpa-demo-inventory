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
package project.global

import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.ItemStok
import domain.inventory.PeriodeItemStok
import domain.inventory.Produk
import domain.inventory.StokProduk
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.Kas
import domain.labarugi.PeriodeKas
import domain.labarugi.TransaksiKas
import domain.penjualan.FakturJual
import domain.penjualan.StatusFakturJual
import domain.retur.ReturJual
import project.inventory.ProdukRepository

@griffon.transform.EventPublisher
class SyncService {

    public static final String EVENT_PESAN_SYNC = "PesanSync"

    ProdukRepository produkRepository

    void refreshStok() {
        publishEvent(EVENT_PESAN_SYNC, ["Mulai mengerjakan refreshStok()..."])
        produkRepository.withTransaction {
            findAllProduk().each { Produk p ->
                def total = 0
                p.daftarStok.each { Gudang g, StokProduk s ->
                    def jumlah = 0
                    s.listPeriodeRiwayat.sort { it.tanggalMulai }.each { PeriodeItemStok pr ->
                        def totalPeriode = pr.listItem.sum {it.jumlah}?: 0
                        if (pr.jumlah != totalPeriode) {
                            publishEvent(EVENT_PESAN_SYNC, ["${p.nama} pada ${pr.tanggalMulai.toString('dd-MM-YYYY')} sampai ${pr.tanggalSelesai.toString('dd-MM-YYYY')} harus berjumlah ${totalPeriode} tetapi ${pr.jumlah}"])
                            pr.jumlah = totalPeriode
                        }
                        jumlah += totalPeriode
                    }
                    total += jumlah
                    if (s.jumlah != jumlah) {
                        publishEvent(EVENT_PESAN_SYNC, ["${p.nama} pada ${g.nama} harus berjumlah ${jumlah} tetapi ${s.jumlah}"])
                        s.jumlah = (jumlah < 0)? 0: jumlah
                    }
                }
                if (p.jumlah != total) {
                    publishEvent(EVENT_PESAN_SYNC, ["${p.nama} harus berjumlah ${total} tetapi ${p.jumlah}"])
                    p.jumlah = (total < 0)? 0: total
                }
            }
        }
        publishEvent(EVENT_PESAN_SYNC, ["Selesai mengerjakan refreshStok()."])
    }

    void refreshJumlahAkanDikirim() {
        publishEvent(EVENT_PESAN_SYNC, ["Mulai mengerjakan refreshJumlahAkanDikirim()..."])
        def pengiriman = [:]
        produkRepository.withTransaction {
            findAllFakturJualByStatus(StatusFakturJual.DIBUAT).each { FakturJual f ->
                if (f.isBolehPesanStok()) {
                    f.listItemFaktur.each { ItemFaktur i ->
                        if (pengiriman.containsKey(i.produk)) {
                            pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                        } else {
                            pengiriman[i.produk] = i.jumlah
                        }
                    }
                }
            }
            findAllReturJualBySudahDiproses(false).each { ReturJual r ->
                r.yangHarusDitukar().items.each { ItemBarang i ->
                    if (pengiriman.containsKey(i.produk)) {
                        pengiriman[i.produk] = pengiriman[i.produk] + i.jumlah
                    } else {
                        pengiriman[i.produk] = i.jumlah
                    }
                }
            }
            findAllProduk().each { Produk p ->
                int nilaiSeharusnya = pengiriman[p]?: 0
                if (p.jumlahAkanDikirim != nilaiSeharusnya) {
                    publishEvent(EVENT_PESAN_SYNC, ["${p.nama} seharusnya memiliki jumlah akan dikirim $nilaiSeharusnya tetapi ${p.jumlahAkanDikirim}"])
                }
            }
        }
        publishEvent(EVENT_PESAN_SYNC, ["Selesai mengerjakan refreshJumlahAkanDikirim()."])
    }

    void refreshSaldoStok() {
        publishEvent(EVENT_PESAN_SYNC, ["Mulai mengerjakan refreshSaldoStok()..."])
        long errorItemStok = 0, errorPeriode = 0
        produkRepository.withTransaction {
            findAllProduk().each { Produk p ->
                p.daftarStok.each { Gudang g, StokProduk s ->
                    long saldo = 0
                    s.listPeriodeRiwayat.sort { it.tanggalMulai }.each { PeriodeItemStok pr ->
                        pr.listItem.each { ItemStok i ->
                            saldo += i.jumlah
                            if (i.saldo != saldo) {
                                errorItemStok++
                                i.saldo = saldo
                            }
                        }
                        if (pr.saldo != saldo) {
                            errorPeriode++
                            pr.saldo = saldo
                        }
                    }
                }
            }
        }
        publishEvent(EVENT_PESAN_SYNC, ["Jumlah saldo per item yang tidak sesuai: $errorItemStok."])
        publishEvent(EVENT_PESAN_SYNC, ["Jumlah saldo periode yang tidak sesuai: $errorPeriode."])
        publishEvent(EVENT_PESAN_SYNC, ["Selesai mengerjakan refreshSaldoStok()."])
    }

    void refreshSaldoKas() {
        publishEvent(EVENT_PESAN_SYNC, ["Mulai mengerjakan refreshSaldoKas()..."])
        long errorItemTransaksi = 0, errorPeriode = 0
        produkRepository.withTransaction {
            findAllKas().each { Kas kas ->
                long saldo = 0
                kas.listPeriodeRiwayat.sort { it.tanggalMulai }.each { PeriodeKas p ->
                    long jumlahPeriode = 0
                    p.jumlahPeriodik.clear()
                    p.listItemPeriodik.each { TransaksiKas tr ->
                        if (tr.kategoriKas.jenis == JENIS_KATEGORI_KAS.PENDAPATAN) {
                            saldo += tr.jumlah
                            jumlahPeriode += tr.jumlah
                        } else if (tr.kategoriKas.jenis == JENIS_KATEGORI_KAS.PENGELUARAN) {
                            saldo -= tr.jumlah
                            jumlahPeriode -= tr.jumlah
                        } else {
                            throw new IllegalStateException('Jenis kategori kas tidak dikenali!')
                        }
                        p.cariJumlahPeriodeKas(tr.kategoriKas, tr.jenis).saldo += tr.jumlah
                        if (tr.saldo != saldo) {
                            errorItemTransaksi++
                            tr.saldo = saldo
                        }
                    }
                    if (p.saldo != saldo) {
                        errorPeriode++
                        p.saldo = saldo
                    }
                    if (p.jumlah != jumlahPeriode) {
                        p.jumlah = jumlahPeriode
                    }
                }
                if (kas.jumlah != saldo) {
                    publishEvent(EVENT_PESAN_SYNC, ["Menyesuaikan saldo kas ${kas.nama} dari ${kas.jumlah} menjadi ${saldo}."])
                    kas.jumlah = saldo
                }
            }
        }
        publishEvent(EVENT_PESAN_SYNC, ["Jumlah saldo per item yang tidak sesuai: $errorItemTransaksi."])
        publishEvent(EVENT_PESAN_SYNC, ["Jumlah saldo periode yang tidak sesuai: $errorPeriode."])
        publishEvent(EVENT_PESAN_SYNC, ["Selesai mengerjakan refreshSaldoKas()."])
    }

}

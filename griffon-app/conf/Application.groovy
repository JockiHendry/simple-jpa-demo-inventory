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



application {
    title = 'Demo Sistem Inventory'
    startupGroups = ['mainGroup']
    autoShutdown = true
    locale = 'id_ID'
}
mvcGroups {
    // MVC Group for "laporanReturBeliPerSupplier"
    'laporanReturBeliPerSupplier' {
        model      = 'project.laporan.LaporanReturBeliPerSupplierModel'
        view       = 'project.laporan.LaporanReturBeliPerSupplierView'
        controller = 'project.laporan.LaporanReturBeliPerSupplierController'
    }

    // MVC Group for "kartuStok"
    'kartuStok' {
        model      = 'project.laporan.KartuStokModel'
        view       = 'project.laporan.KartuStokView'
        controller = 'project.laporan.KartuStokController'
    }

    // MVC Group for "returJualEceran"
    'returJualEceran' {
        model      = 'project.retur.ReturJualEceranModel'
        view       = 'project.retur.ReturJualEceranView'
        controller = 'project.retur.ReturJualEceranController'
    }

    // MVC Group for "klaimAsChild"
    'klaimAsChild' {
        model      = 'project.retur.KlaimAsChildModel'
        view       = 'project.retur.KlaimAsChildView'
        controller = 'project.retur.KlaimAsChildController'
    }

    // MVC Group for "itemReturAsChild"
    'itemReturAsChild' {
        model      = 'project.retur.ItemReturAsChildModel'
        view       = 'project.retur.ItemReturAsChildView'
        controller = 'project.retur.ItemReturAsChildController'
    }

    // MVC Group for "laporanPenjualanProduk"
    'laporanPenjualanProduk' {
        model      = 'project.laporan.LaporanPenjualanProdukModel'
        view       = 'project.laporan.LaporanPenjualanProdukView'
        controller = 'project.laporan.LaporanPenjualanProdukController'
    }

    // MVC Group for "laporanSuratJalan"
    'laporanSuratJalan' {
        model      = 'project.laporan.LaporanSuratJalanModel'
        view       = 'project.laporan.LaporanSuratJalanView'
        controller = 'project.laporan.LaporanSuratJalanController'
    }

    // MVC Group for "previewFaktur"
    'previewFaktur' {
        model      = 'project.laporan.PreviewFakturModel'
        view       = 'project.laporan.PreviewFakturView'
        controller = 'project.laporan.PreviewFakturController'
    }

    // MVC Group for "kemasanRetur"
    'kemasanRetur' {
        model      = 'project.retur.KemasanReturModel'
        view       = 'project.retur.KemasanReturView'
        controller = 'project.retur.KemasanReturController'
    }

    // MVC Group for "retur"
    'retur' {
        model      = 'project.penjualan.ReturModel'
        view       = 'project.penjualan.ReturView'
        controller = 'project.penjualan.ReturController'
    }

    // MVC Group for "pembayaranPiutangAsChild"
    'pembayaranPiutangAsChild' {
        model      = 'project.penjualan.PembayaranPiutangAsChildModel'
        view       = 'project.penjualan.PembayaranPiutangAsChildView'
        controller = 'project.penjualan.PembayaranPiutangAsChildController'
    }

    // MVC Group for "riwayatPoin"
    'riwayatPoin' {
        model      = 'project.penjualan.RiwayatPoinModel'
        view       = 'project.penjualan.RiwayatPoinView'
        controller = 'project.penjualan.RiwayatPoinController'
    }

    // MVC Group for "laporanStok"
    'laporanStok' {
        model      = 'project.laporan.LaporanStokModel'
        view       = 'project.laporan.LaporanStokView'
        controller = 'project.laporan.LaporanStokController'
    }

    // MVC Group for "returBeli"
    'returBeli' {
        model      = 'project.retur.ReturBeliModel'
        view       = 'project.retur.ReturBeliView'
        controller = 'project.retur.ReturBeliController'
    }

    // MVC Group for "returJualOlehSales"
    'returJualOlehSales' {
        model      = 'project.retur.ReturJualOlehSalesModel'
        view       = 'project.retur.ReturJualOlehSalesView'
        controller = 'project.retur.ReturJualOlehSalesController'
    }

    // MVC Group for "previewEscp"
    'previewEscp' {
        model      = 'project.main.PreviewEscpModel'
        view       = 'project.main.PreviewEscpView'
        controller = 'project.main.PreviewEscpController'
    }

    // MVC Group for "pesan"
    'pesan' {
        model      = 'project.main.PesanModel'
        view       = 'project.main.PesanView'
        controller = 'project.main.PesanController'
    }

    // MVC Group for "penukaranPoinAsChild"
    'pencairanPoin' {
        model      = 'project.penjualan.PencairanPoinModel'
        view       = 'project.penjualan.PencairanPoinView'
        controller = 'project.penjualan.PencairanPoinController'
    }

    // MVC Group for "penyesuaianStok"
    'penyesuaianStok' {
        model      = 'project.inventory.PenyesuaianStokModel'
        view       = 'project.inventory.PenyesuaianStokView'
        controller = 'project.inventory.PenyesuaianStokController'
    }

    // MVC Group for "user"
    'user' {
        model      = 'project.main.UserModel'
        view       = 'project.main.UserView'
        controller = 'project.main.UserController'
    }

    // MVC Group for "transfer"
    'transfer' {
        model      = 'project.inventory.TransferModel'
        view       = 'project.inventory.TransferView'
        controller = 'project.inventory.TransferController'
    }

    // MVC Group for "laporanSisaPiutang"
    'laporanSisaPiutang' {
        model      = 'project.laporan.LaporanSisaPiutangModel'
        view       = 'project.laporan.LaporanSisaPiutangView'
        controller = 'project.laporan.LaporanSisaPiutangController'
    }

    // MVC Group for "laporanPenjualanPerRegion"
    'laporanPenjualanPerRegion' {
        model      = 'project.laporan.LaporanPenjualanPerRegionModel'
        view       = 'project.laporan.LaporanPenjualanPerRegionView'
        controller = 'project.laporan.LaporanPenjualanPerRegionController'
    }

    // MVC Group for "laporanPenjualanPerKonsumen"
    'laporanPenjualanPerKonsumen' {
        model      = 'project.laporan.LaporanPenjualanPerKonsumenModel'
        view       = 'project.laporan.LaporanPenjualanPerKonsumenView'
        controller = 'project.laporan.LaporanPenjualanPerKonsumenController'
    }

    // MVC Group for "laporanPenjualanPerSales"
    'laporanPenjualanPerSales' {
        model      = 'project.laporan.LaporanPenjualanPerSalesModel'
        view       = 'project.laporan.LaporanPenjualanPerSalesView'
        controller = 'project.laporan.LaporanPenjualanPerSalesController'
    }

    // MVC Group for "laporanStok"
    'laporanStokGudang' {
        model      = 'project.laporan.LaporanStokGudangModel'
        view       = 'project.laporan.LaporanStokGudangView'
        controller = 'project.laporan.LaporanStokGudangController'
    }

    // MVC Group for "laporan"
    'laporan' {
        model      = 'project.laporan.LaporanModel'
        view       = 'project.laporan.LaporanView'
        controller = 'project.laporan.LaporanController'
    }

    // MVC Group for "giro"
    'bilyetGiro' {
        model      = 'project.faktur.BilyetGiroModel'
        view       = 'project.faktur.BilyetGiroView'
        controller = 'project.faktur.BilyetGiroController'
    }

    // MVC Group for "piutang"
    'piutang' {
        model      = 'project.penjualan.PiutangModel'
        view       = 'project.penjualan.PiutangView'
        controller = 'project.penjualan.PiutangController'
    }

    // MVC Group for "buktiTerima"
    'buktiTerima' {
        model      = 'project.penjualan.BuktiTerimaModel'
        view       = 'project.penjualan.BuktiTerimaView'
        controller = 'project.penjualan.BuktiTerimaController'
    }

    // MVC Group for "pengiriman"
    'pengiriman' {
        model      = 'project.penjualan.PengirimanModel'
        view       = 'project.penjualan.PengirimanView'
        controller = 'project.penjualan.PengirimanController'
    }

    // MVC Group for "fakturJualOlehSalesAsChild"
    'fakturJualOlehSalesAsChild' {
        model      = 'project.penjualan.FakturJualOlehSalesAsChildModel'
        view       = 'project.penjualan.FakturJualOlehSalesAsChildView'
        controller = 'project.penjualan.FakturJualOlehSalesAsChildController'
    }

    // MVC Group for "fakturJualOlehSales"
    'fakturJualOlehSales' {
        model      = 'project.penjualan.FakturJualOlehSalesModel'
        view       = 'project.penjualan.FakturJualOlehSalesView'
        controller = 'project.penjualan.FakturJualOlehSalesController'
    }

    // MVC Group for "konsumen"
    'konsumen' {
        model      = 'project.penjualan.KonsumenModel'
        view       = 'project.penjualan.KonsumenView'
        controller = 'project.penjualan.KonsumenController'
    }

    // MVC Group for "fakturJualEceran"
    'fakturJualEceran' {
        model      = 'project.penjualan.FakturJualEceranModel'
        view       = 'project.penjualan.FakturJualEceranView'
        controller = 'project.penjualan.FakturJualEceranController'
    }

    // MVC Group for "satuan"
    'satuan' {
        model      = 'project.inventory.SatuanModel'
        view       = 'project.inventory.SatuanView'
        controller = 'project.inventory.SatuanController'
    }

    // MVC Group for "region"
    'region' {
        model      = 'project.penjualan.RegionModel'
        view       = 'project.penjualan.RegionView'
        controller = 'project.penjualan.RegionController'
    }

    // MVC Group for "sales"
    'sales' {
        model      = 'project.penjualan.SalesModel'
        view       = 'project.penjualan.SalesView'
        controller = 'project.penjualan.SalesController'
    }

    // MVC Group for "supervisorPassword"
    'supervisorPassword' {
        model      = 'project.main.SupervisorPasswordModel'
        view       = 'project.main.SupervisorPasswordView'
        controller = 'project.main.SupervisorPasswordController'
    }

    // MVC Group for "pengaturan"
    'pengaturan' {
        model      = 'project.main.PengaturanModel'
        view       = 'project.main.PengaturanView'
        controller = 'project.main.PengaturanController'
    }

    // MVC Group for "pembayaranHutangAsChild"
    'pembayaranHutangAsChild' {
        model      = 'project.pembelian.PembayaranHutangAsChildModel'
        view       = 'project.pembelian.PembayaranHutangAsChildView'
        controller = 'project.pembelian.PembayaranHutangAsChildController'
    }

    // MVC Group for "hutang"
    'hutang' {
        model      = 'project.pembelian.HutangModel'
        view       = 'project.pembelian.HutangView'
        controller = 'project.pembelian.HutangController'
    }

    // MVC Group for "receivedNotInvoiced"
    'purchaseOrder' {
        model      = 'project.pembelian.PurchaseOrderModel'
        view       = 'project.pembelian.PurchaseOrderView'
        controller = 'project.pembelian.PurchaseOrderController'
    }

    // MVC Group for "itemFakturAsChild"
    'itemFakturAsChild' {
        model      = 'project.faktur.ItemFakturAsChildModel'
        view       = 'project.faktur.ItemFakturAsChildView'
        controller = 'project.faktur.ItemFakturAsChildController'
    }

    // MVC Group for "fakturBeli"
    'fakturBeli' {
        model      = 'project.pembelian.FakturBeliModel'
        view       = 'project.pembelian.FakturBeliView'
        controller = 'project.pembelian.FakturBeliController'
    }

    // MVC Group for "itemStok"
    'itemStok' {
        model      = 'project.inventory.ItemStokModel'
        view       = 'project.inventory.ItemStokView'
        controller = 'project.inventory.ItemStokController'
    }

    // MVC Group for "stokProduk"
    'stokProduk' {
        model      = 'project.inventory.StokProdukModel'
        view       = 'project.inventory.StokProdukView'
        controller = 'project.inventory.StokProdukController'
    }

    // MVC Group for "restore"
    'restore' {
        model      = 'project.main.RestoreModel'
        view       = 'project.main.RestoreView'
        controller = 'project.main.RestoreController'
    }

    // MVC Group for "backup"
    'backup' {
        model      = 'project.main.BackupModel'
        view       = 'project.main.BackupView'
        controller = 'project.main.BackupController'
    }

    // MVC Group for "itemBarangAsChild"
    'itemBarangAsChild' {
        model      = 'project.inventory.ItemBarangAsChildModel'
        view       = 'project.inventory.ItemBarangAsChildView'
        controller = 'project.inventory.ItemBarangAsChildController'
    }

    // MVC Group for "penerimaanBarang"
    'penerimaanBarang' {
        model      = 'project.pembelian.PenerimaanBarangModel'
        view       = 'project.pembelian.PenerimaanBarangView'
        controller = 'project.pembelian.PenerimaanBarangController'
    }

    // MVC Group for "supplier"
    'supplier' {
        model      = 'project.pembelian.SupplierModel'
        view       = 'project.pembelian.SupplierView'
        controller = 'project.pembelian.SupplierController'
    }

    // MVC Group for "mainGroup"
    'mainGroup' {
        model      = 'project.main.MainGroupModel'
        view       = 'project.main.MainGroupView'
        controller = 'project.main.MainGroupController'
    }

    // MVC Group for "gudang"
    'gudang' {
        model      = 'project.inventory.GudangModel'
        view       = 'project.inventory.GudangView'
        controller = 'project.inventory.GudangController'
    }

    // MVC Group for "produk"
    'produk' {
        model      = 'project.inventory.ProdukModel'
        view       = 'project.inventory.ProdukView'
        controller = 'project.inventory.ProdukController'
    }

}

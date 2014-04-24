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

    // MVC Group for "previewFaktur"
    'previewFaktur' {
        model      = 'project.main.PreviewFakturModel'
        view       = 'project.main.PreviewFakturView'
        controller = 'project.main.PreviewFakturController'
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
        model      = 'project.pembelian.ItemFakturAsChildModel'
        view       = 'project.pembelian.ItemFakturAsChildView'
        controller = 'project.pembelian.ItemFakturAsChildController'
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
        model      = 'project.pembelian.ItemBarangAsChildModel'
        view       = 'project.pembelian.ItemBarangAsChildView'
        controller = 'project.pembelian.ItemBarangAsChildController'
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

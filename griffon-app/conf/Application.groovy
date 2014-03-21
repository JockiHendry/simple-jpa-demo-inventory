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
    // MVC Group for "itemStok"
    'itemStok' {
        model      = 'project.ItemStokModel'
        view       = 'project.ItemStokView'
        controller = 'project.ItemStokController'
    }

    // MVC Group for "stokProduk"
    'stokProduk' {
        model      = 'project.StokProdukModel'
        view       = 'project.StokProdukView'
        controller = 'project.StokProdukController'
    }

    // MVC Group for "restore"
    'restore' {
        model      = 'project.RestoreModel'
        view       = 'project.RestoreView'
        controller = 'project.RestoreController'
    }

    // MVC Group for "backup"
    'backup' {
        model      = 'project.BackupModel'
        view       = 'project.BackupView'
        controller = 'project.BackupController'
    }

    // MVC Group for "itemBarangAsChild"
    'itemBarangAsChild' {
        model      = 'project.ItemBarangAsChildModel'
        view       = 'project.ItemBarangAsChildView'
        controller = 'project.ItemBarangAsChildController'
    }

    // MVC Group for "penerimaanBarang"
    'penerimaanBarang' {
        model      = 'project.PenerimaanBarangModel'
        view       = 'project.PenerimaanBarangView'
        controller = 'project.PenerimaanBarangController'
    }

    // MVC Group for "supplier"
    'supplier' {
        model      = 'project.SupplierModel'
        view       = 'project.SupplierView'
        controller = 'project.SupplierController'
    }

    // MVC Group for "mainGroup"
    'mainGroup' {
        model      = 'project.MainGroupModel'
        view       = 'project.MainGroupView'
        controller = 'project.MainGroupController'
    }

    // MVC Group for "gudang"
    'gudang' {
        model      = 'project.GudangModel'
        view       = 'project.GudangView'
        controller = 'project.GudangController'
    }

    // MVC Group for "produk"
    'produk' {
        model      = 'project.ProdukModel'
        view       = 'project.ProdukView'
        controller = 'project.ProdukController'
    }

}

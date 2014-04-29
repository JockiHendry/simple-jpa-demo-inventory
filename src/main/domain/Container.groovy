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



package domain

import domain.event.InventoryEventConsumer
import domain.faktur.BilyetGiro
import domain.inventory.GudangRepository
import domain.inventory.ProdukRepository
import domain.inventory.SatuanRepository
import domain.pembelian.PurchaseOrderRepository
import domain.pembelian.SupplierRepository
import domain.pengaturan.PengaturanRepository
import domain.penjualan.BilyetGiroRepository
import domain.penjualan.FakturJualRepository
import domain.penjualan.KonsumenRepository
import domain.penjualan.NomorService
import domain.penjualan.RegionRepository
import domain.penjualan.SalesRepository
import domain.util.PasswordService
import griffon.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Container {

    private final Logger log = LoggerFactory.getLogger(Container)

    public static Container app = new Container()

    public static final String SEMUA = "Semua"

    GudangRepository gudangRepository
    ProdukRepository produkRepository
    SupplierRepository supplierRepository
    PengaturanRepository pengaturanRepository
    SalesRepository salesRepository
    RegionRepository regionRepository
    PurchaseOrderRepository purchaseOrderRepository
    SatuanRepository satuanRepository
    FakturJualRepository fakturJualRepository
    KonsumenRepository konsumenRepository
    BilyetGiroRepository bilyetGiroRepository

    InventoryEventConsumer inventoryEventConsumer

    PasswordService passwordService
    NomorService nomorService

    private Container() {
        setup()
    }

    private void setup() {
        // Create repositories
        gudangRepository = new GudangRepository()
        produkRepository = new ProdukRepository()
        supplierRepository = new SupplierRepository()
        pengaturanRepository = new PengaturanRepository()
        salesRepository = new SalesRepository()
        regionRepository = new RegionRepository()
        purchaseOrderRepository = new PurchaseOrderRepository()
        satuanRepository = new SatuanRepository()
        fakturJualRepository = new FakturJualRepository()
        konsumenRepository = new KonsumenRepository()
        bilyetGiroRepository = new BilyetGiroRepository()

        // Create services
        passwordService = new PasswordService()
        nomorService = new NomorService()

        // Create event consumers
        inventoryEventConsumer = new InventoryEventConsumer()
        if (ApplicationHolder.application) {
            ApplicationHolder.application.addApplicationEventListener(inventoryEventConsumer)
        } else {
            log.warn 'Can not find Griffon application. Is this launched by Griffon? Event listener will not working!'
        }
    }

    public List searchEnum(Class enumeration) {
        List result = [SEMUA]
        result.addAll(EnumSet.allOf(enumeration))
        result
    }
}

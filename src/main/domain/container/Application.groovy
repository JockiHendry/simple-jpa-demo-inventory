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



package domain.container

import domain.repository.GudangRepository
import domain.repository.ProdukRepository
import domain.repository.SupplierRepository

class Application {

    public static Application instance = new Application()

    private GudangRepository gudangRepository
    private ProdukRepository produkRepository
    private SupplierRepository supplierRepository

    private Application() {
        gudangRepository = new GudangRepository()
        produkRepository = new ProdukRepository()
        supplierRepository = new SupplierRepository()
    }

    public GudangRepository getGudangRepository() {
        gudangRepository
    }

    public ProdukRepository getProdukRepository() {
        produkRepository
    }

    public SupplierRepository getSupplierRepository() {
        supplierRepository
    }

}

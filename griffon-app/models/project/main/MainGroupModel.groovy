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

package project.main

import groovy.beans.Bindable

class MainGroupModel {

    @Bindable String status

    @Bindable boolean penerimaanBarangVisible = true
    @Bindable boolean pengeluaranBarangVisible = true
    @Bindable boolean buktiTerimaVisible = true
    @Bindable boolean purchaseOrderVisible = true
    @Bindable boolean fakturBeliVisible = true
    @Bindable boolean fakturJualVisible = true
    @Bindable boolean hutangVisible = true
    @Bindable boolean piutangVisible = true
    @Bindable boolean giroVisible = true
    @Bindable boolean pencairanPoinVisible = true
    @Bindable boolean produkVisible = true
    @Bindable boolean transferVisible = true
    @Bindable boolean penyesuaianStokVisible = true
    @Bindable boolean returJualVisible = true
    @Bindable boolean laporanVisible = true
    @Bindable boolean pesanVisible = true
    @Bindable boolean maintenanceVisible = true

}
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
package project.laporan

import domain.pembelian.PurchaseOrder
import domain.pembelian.Supplier
import laporan.SisaHutangSupplier
import org.joda.time.LocalDate
import project.pembelian.PurchaseOrderRepository

import javax.swing.SwingUtilities

@SuppressWarnings("GroovyUnusedDeclaration")
class LaporanSisaHutangController {

    LaporanSisaHutangModel model
    def view
    PurchaseOrderRepository purchaseOrderRepository

    void mvcGroupInit(Map args) {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        List<Supplier> daftarSupplier = purchaseOrderRepository.findAllSupplier([orderBy: 'nama'])
        execInsideUISync {
            model.supplierSearch.values = daftarSupplier
        }
    }

    def tampilkanLaporan = {
        String jpql = '''
    SELECT DISTINCT p FROM PurchaseOrder p JOIN FETCH p.fakturBeli f JOIN FETCH f.hutang
    WHERE p.status IN (domain.pembelian.StatusPurchaseOrder.FAKTUR_DITERIMA, domain.pembelian.StatusPurchaseOrder.OK)
        AND f.tanggal BETWEEN :tanggalMulaiSearch AND :tanggalSelesaiSearch
        AND p.deleted != 'Y'
'''
        if (model.supplierSearch.selectedValues?.size() > 0) {
            String ids = model.supplierSearch.selectedValues.collect { it.id }.join(',')
            jpql += " AND p.supplier.id IN ($ids)"
        }

        Map params = [tanggalMulaiSearch: model.tanggalMulaiCari, tanggalSelesaiSearch: model.tanggalSelesaiCari]
        Map result = [:]
        purchaseOrderRepository.executeQuery(jpql, [:], params).each { PurchaseOrder p ->
            SisaHutangSupplier shs = result[p.supplier]
            if (!shs) {
                shs = new SisaHutangSupplier(p.supplier, [])
                result[p.supplier] = shs
            }
            shs.fakturBelumLunas << p.fakturBeli
        }

        model.result = result.values().sort { SisaHutangSupplier shs -> shs.supplier.nama }
        model.params.tanggalMulaiCari = model.tanggalMulaiCari
        model.params.tanggalSelesaiCari = model.tanggalSelesaiCari

        close()
    }

    def reset = {
        model.tanggalMulaiCari = LocalDate.now().withDayOfMonth(1)
        model.tanggalSelesaiCari = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1)
        model.supplierSearch.clearSelectedValues()
    }

    def batal = {
        model.batal = true
        close()
    }

    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel).visible = false
    }

}

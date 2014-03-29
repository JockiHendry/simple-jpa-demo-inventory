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
package project

import ast.NeedSupervisorPassword
import domain.Container
import domain.pembelian.FakturBeli
import domain.pembelian.FakturBeliRepository
import domain.pembelian.PembayaranHutang

import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class PembayaranHutangAsChildController {

    PembayaranHutangAsChildModel model
    def view

    FakturBeliRepository fakturBeliRepository

    void mvcGroupInit(Map args) {
        fakturBeliRepository = Container.app.fakturBeliRepository
        model.parent = args.'parent'
        model.editable = true
        execInsideUISync {
            model.pembayaranHutangList.clear()
            model.pembayaranHutangList.addAll(args.'listPembayaranHutang')
        }
    }

    def save = {
        PembayaranHutang pembayaranHutang = new PembayaranHutang(tanggal: model.tanggal, jumlah: model.jumlah)
        if (!fakturBeliRepository.validate(pembayaranHutang, Default, model)) return

        try {
            fakturBeliRepository.withTransaction {
                model.parent = fakturBeliRepository.merge(model.parent)
                model.parent.bayarHutang(pembayaranHutang)
            }
            execInsideUISync {
                model.pembayaranHutangList << pembayaranHutang
                clear()
            }
        } catch (IllegalArgumentException ex) {
            model.errors['jumlah'] = ex.message
            return
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            PembayaranHutang pembayaranHutang = view.table.selectionModel.selected[0]
            fakturBeliRepository.withTransaction {
                model.parent = fakturBeliRepository.merge(model.parent)
                model.parent.hapus(pembayaranHutang)
            }
            execInsideUISync {
                model.pembayaranHutangList.remove(pembayaranHutang)
                clear()
            }
        } catch (IllegalArgumentException ex) {
            model.errors['jumlah'] = ex.message
            return
        }
    }

    def clear = {
        execInsideUISync {
            model.tanggal = null
            model.jumlah = null
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                PembayaranHutang selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.tanggal = selected.tanggal
                model.jumlah = selected.jumlah
            }
        }
    }

}

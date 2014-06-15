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
package project.penjualan

import ast.NeedSupervisorPassword
import domain.penjualan.*
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import domain.exception.DataDuplikat
import domain.Container

import java.awt.Dimension

class KonsumenController {

    KonsumenModel model
    def view

    KonsumenRepository konsumenRepository

    void mvcGroupInit(Map args) {
        konsumenRepository = Container.app.konsumenRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.regionList.clear()
            model.salesList.clear()
        }
        List region = konsumenRepository.findAllRegion()
        List sales  = konsumenRepository.findAllSales()
        execInsideUISync {
            model.regionList.addAll(region)
            model.salesList.addAll(sales)
        }
    }

    def search = {
        List result = konsumenRepository.cari(model.namaSearch)
        execInsideUISync {
            model.konsumenList.clear()
            model.konsumenList.addAll(result)
            model.namaSearch = null
        }
    }

    def save = {
        Konsumen konsumen = new Konsumen(id: model.id, nama: model.nama, nomorTelepon: model.nomorTelepon,
            alamat: model.alamat, region: model.region.selectedItem, sales: model.sales.selectedItem, creditLimit: 0)

        if (!konsumenRepository.validate(konsumen, Default, model)) return

        try {
            if (konsumen.id == null) {
                konsumenRepository.buat(konsumen)
                execInsideUISync {
                    model.konsumenList << konsumen
                    view.table.changeSelection(model.konsumenList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                konsumen = konsumenRepository.update(konsumen)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = konsumen
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nama'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    @NeedSupervisorPassword
    def creditLimit = {
        String input = JOptionPane.showInputDialog(view.mainPanel, 'Masukkan nilai credit limit baru:', 'Input Credit Limit', JOptionPane.QUESTION_MESSAGE)
        try {
            BigDecimal creditLimit = new BigDecimal(input)
            Konsumen konsumen = view.table.selectionModel.selected[0]
            konsumen = konsumenRepository.aturCreditLimit(konsumen, creditLimit)
            execInsideUISync {
                view.table.selectionModel.selected[0] = konsumen
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Nilai credit limit bukan nilai yang valid!', 'Kesalahan Input', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showFakturBelumLunas = {
        execInsideUISync {
            def args = [konsumen: view.table.selectionModel.selected[0]]
            def dialogProps = [title: 'Faktur Belum Lunas', size: new Dimension(900,420)]
            DialogUtils.showMVCGroup('fakturJualOlehSalesAsChild', args, app, view, dialogProps)
        }
    }

    def showPencairanPoin = {
        execInsideUISync {
            def args = [konsumen: view.table.selectionModel.selected[0]]
            def dialogProps = [title: 'Pencairan Poin', size: new Dimension(900,420)]
            DialogUtils.showMVCGroup('pencairanPoinAsChild', args, app, view, dialogProps) { m, v, c ->
                Konsumen konsumen = view.table.selectionModel.selected[0]
                konsumen = konsumenRepository.findKonsumenById(konsumen.id)
                view.table.selectionModel.selected[0] = konsumen
            }
        }
    }

    def delete = {
        Konsumen konsumen = view.table.selectionModel.selected[0]
        konsumenRepository.remove(konsumen)
        execInsideUISync {
            model.konsumenList.remove(konsumen)
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nama = null
            model.nomorTelepon = null
            model.region.selectedItem = null
            model.alamat = null
            model.creditLimit = null
            model.sales.selectedItem = null

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                Konsumen selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nama = selected.nama
                model.nomorTelepon = selected.nomorTelepon
                model.alamat = selected.alamat
                model.region.selectedItem = selected.region
                model.sales.selectedItem = selected.sales
                model.creditLimit = selected.creditLimit
            }
        }
    }

}
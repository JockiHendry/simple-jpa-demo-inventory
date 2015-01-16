/*
 * Copyright 2015 Jocki Hendry.
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
package project.servis

import ast.NeedSupervisorPassword
import domain.pengaturan.NamaTemplateFaktur
import domain.servis.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import java.awt.Dimension

@SuppressWarnings("GroovyUnusedDeclaration")
class ServisController {

	ServisModel model
	def view
    ServisRepository servisRepository

	void mvcGroupInit(Map args) {
	}

	void mvcGroupDestroy() {
	}

	def listAll = {
		execInsideUISync {
			model.servisList.clear()
		}

		List servisResult = servisRepository.findAllServis()

		execInsideUISync {
			model.servisList.addAll(servisResult)
			model.namaKonsumenSearch = null
		}
	}

	def search = {
        List result = servisRepository.cari(model.namaKonsumenSearch)
        execInsideUISync {
            model.servisList.clear()
            model.servisList.addAll(result)
        }
	}

	def save = {
        if (model.id!=null) {
			if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
				return
			}
        }
		Servis servis = new Servis(id: model.id, namaKonsumen: model.namaKonsumen, alamat: model.alamat, tipe: model.tipe, keluhan: model.keluhan, keterangan: model.keterangan, tanggalMasuk: model.tanggalMasuk, tanggalSelesai: model.tanggalSelesai, tanggalDiambil: model.tanggalDiambil)

		if (!servisRepository.validate(servis, Default, model)) return

        try {
		    if (model.id == null) {
			    // Insert operation
                servis = servisRepository.buat(servis)
                execInsideUISync {
                    model.servisList << servis
                    view.table.changeSelection(model.servisList.size()-1, 0, false, false)
                    clear()
                    cetak(servis)
                }
            } else {
                // Update operation
                servis = servisRepository.update(servis)
			    execInsideUISync {
                    view.table.selectionModel.selected[0] = servis
                    clear()
                }
		    }
        } catch (DuplicateEntityException ex) {
            model.errors['namaKonsumen'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
	}

    @NeedSupervisorPassword
    def delete = {
		if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
			return
		}
        Servis servis = view.table.selectionModel.selected[0]
        servisRepository.hapus(servis)
        execInsideUISync {
            model.servisList.remove(servis)
            clear()
        }
    }

    def cetak = { e ->
        execInsideUISync {
            def args = [dataSource: view.table.selectionModel.selected[0], namaTemplateFaktur: NamaTemplateFaktur.FORMULIR_SERVIS]
            if (e instanceof Servis) args.dataSource = e
            def dialogProps = [title: 'Preview Formulir Servis', preferredSize: new Dimension(970, 700)]
            DialogUtils.showMVCGroup('previewEscp', args, view, dialogProps)
        }
    }

	def clear = {
		execInsideUISync {
			model.id = null
			model.namaKonsumen = null
			model.alamat = null
			model.tipe = null
			model.keluhan = null
			model.keterangan = null
			model.tanggalMasuk = null
			model.tanggalSelesai = null
			model.tanggalDiambil = null
			model.created = null
			model.createdBy = null
			model.modified = null
			model.modifiedBy = null
            model.errors.clear()
			view.table.selectionModel.clearSelection()
		}
	}

	def tableSelectionChanged = { ListSelectionEvent event ->
		execInsideUISync {
			if (view.table.selectionModel.isSelectionEmpty()) {
				clear()
			} else {
				Servis selected = view.table.selectionModel.selected[0]
				model.errors.clear()
				model.id = selected.id
				model.namaKonsumen = selected.namaKonsumen
				model.alamat = selected.alamat
				model.tipe = selected.tipe
				model.keluhan = selected.keluhan
				model.keterangan = selected.keterangan
				model.tanggalMasuk = selected.tanggalMasuk
				model.tanggalSelesai = selected.tanggalSelesai
				model.tanggalDiambil = selected.tanggalDiambil
				model.created = selected.createdDate
				model.createdBy = selected.createdBy?'('+selected.createdBy+')':null
				model.modified = selected.modifiedDate
				model.modifiedBy = selected.modifiedBy?'('+selected.modifiedBy+')':null
			}
		}
	}

}
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
package project.inventory

import domain.Container
import domain.inventory.ProdukRepository
import simplejpa.swing.DialogUtils

import java.awt.Dimension

class StokProdukController {

    StokProdukModel model
    def view

    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        produkRepository = Container.app.produkRepository
        model.parent = args.'parent'
        model.stokProdukList.clear()
        model.stokProdukList.addAll(model.parent.stokSemuaGudang())
    }

    def showItemStok = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0]]
            def dialogProps = [title: 'Item Stok', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemStok', args, app, view, dialogProps)
        }
    }

}

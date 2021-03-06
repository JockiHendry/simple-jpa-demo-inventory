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
package project.inventory

@SuppressWarnings("GroovyUnusedDeclaration")
class StokProdukController {

    StokProdukModel model
    def view
    ProdukRepository produkRepository

    void mvcGroupInit(Map args) {
        model.parent = args.'parent'
        model.stokProdukList.clear()
        model.stokProdukList.addAll(model.parent.stokSemuaGudang())
    }

    def showItemStok = {
        def mainView = app.getMvcGroupManager()['mainGroup'].view
        def selected = view.table.selectionModel.selected[0]
        if (selected) {
            execInsideUISync {
                mainView.mainTab.addMVCTab('itemStok', [parent: selected], "Stok ${model.parent.nama} - ${selected.gudang.nama}")
            }
        }
    }

}

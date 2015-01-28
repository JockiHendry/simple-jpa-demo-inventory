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
package ${g.targetPackageName}

${g.imports()}
import simplejpa.exception.DuplicateEntityException
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default

class ${g.domainClassName}Controller {

	${g.domainClassName}Model model
	def view
    ${g.repositoryType} ${g.repositoryVar}

	void mvcGroupInit(Map args) {
		listAll()
	}

	void mvcGroupDestroy() {
	}

	def listAll = {
		execInsideUISync {
${g.listAll_clear(3)}
		}

${g.listAll_find(2)}

		execInsideUISync {
${g.listAll_set(3)}
		}
	}

	def search = {
        List result = ${g.repositoryVar}.cari(model.${g.firstAttrSearch})
        execInsideUISync {
            model.${g.domainClassGlazedListVariable}.clear()
            model.${g.domainClassGlazedListVariable}.addAll(result)
        }
	}

	def save = {
        if (model.id!=null) {
            if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                return
            }
        }
		${g.domainClassName} ${g.domainClassNameAsProperty} = ${g.domainClassConstructor()}

		if (!${g.repositoryVar}.validate(${g.domainClassNameAsProperty}, Default, model)) return

        try {
		    if (model.id == null) {
			    // Insert operation
<%
    out << g.saveOneToManyInverse(g.domainClass,4)
    out << g.saveManyToManyInverse(g.domainClass,4)
%>                ${g.repositoryVar}.buat(${g.domainClassNameAsProperty})
                execInsideUISync {
                    model.${g.domainClassGlazedListVariable} << ${g.domainClassNameAsProperty}
                    view.table.changeSelection(model.${g.domainClassGlazedListVariable}.size()-1, 0, false, false)
                }
            } else {
                // Update operation
                ${g.domainClassNameAsProperty} = ${g.repositoryVar}.update(${g.domainClassNameAsProperty})
			    execInsideUISync { view.table.selectionModel.selected[0] = ${g.domainClassNameAsProperty} }
		    }
        } catch (DuplicateEntityException ex) {
            model.errors['${g.firstAttr}'] = app.getMessage('simplejpa.error.alreadyExist.message')
        }
		execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
	}

    @NeedSupervisorPassword
    def delete = {
        if (!DialogUtils.confirm(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
            return
        }
        ${g.domainClassName} ${g.domainClassNameAsProperty} = view.table.selectionModel.selected[0]
        ${g.domainClassNameAsProperty} = ${g.repositoryVar}.hapus(${g.domainClassNameAsProperty})
        execInsideUISync {
            view.table.selectionModel.selected[0] = ${g.domainClassNameAsProperty}
            clear()
        }
    }
${g.popups(1)}
	def clear = {
		execInsideUISync {
			model.id = null
${g.clear(3)}
            model.errors.clear()
			view.table.selectionModel.clearSelection()
		}
	}

	def tableSelectionChanged = { ListSelectionEvent event ->
		execInsideUISync {
			if (view.table.selectionModel.isSelectionEmpty()) {
				clear()
			} else {
				${g.domainClassName} selected = view.table.selectionModel.selected[0]
				model.errors.clear()
				model.id = selected.id
${g.selected(4)}
			}
		}
	}

}
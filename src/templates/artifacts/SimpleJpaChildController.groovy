package ${g.targetPackageName}

${g.imports()}
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.swing.event.ListSelectionEvent

@Transaction
class ${g.customClassName}Controller {

	${g.customClassName}Model model
	def view

	void mvcGroupInit(Map args) {
        model.${g.domainClassGlazedListVariable}.addAll(args.'parentList'?:[])
		listAll()
	}

	void mvcGroupDestroy() {
		destroyEntityManager()
	}

	def listAll = {
<%
    out << g.sub_listAll_clear(2)
    out << g.sub_listAll_find(2)
    out << g.sub_listAll_set(2)
%>    }

	def save = {
        if (!view.table.selectionModel.selectionEmpty) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

		${g.domainClassName} ${g.domainClassNameAsProperty} = ${g.domainClassConstructor()}
<%
out << g.saveOneToManyInverse(g.domainClass,2)
out << g.saveManyToManyInverse(g.domainClass,2)
%>
		if (!validate(${g.domainClassNameAsProperty})) return

		if (view.table.selectionModel.selectionEmpty) {
			// Insert operation
			execInsideUISync {
				model.${g.domainClassGlazedListVariable} << ${g.domainClassNameAsProperty}
				view.table.changeSelection(model.${g.domainClassGlazedListVariable}.size()-1, 0, false, false)
			}
		} else {
			// Update operation
			${g.domainClassName} selected${g.domainClassName} = view.table.selectionModel.selected[0]
${g.update(3)}
		}
		execInsideUISync {
            clear()
            view.form.getFocusTraversalPolicy().getFirstComponent(view.form).requestFocusInWindow()
        }
	}

	def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
		${g.domainClassName} ${g.domainClassNameAsProperty} = view.table.selectionModel.selected[0]
		execInsideUISync {
			model.${g.domainClassGlazedListVariable}.remove(${g.domainClassNameAsProperty})
			clear()
		}
	}
${g.popups(1)}
	@Transaction(Transaction.Policy.SKIP)
	def clear = {
		execInsideUISync {
<%
    if (g.domainClass.entity) {
        out << "\t\t\t\tmodel.id = null\n"
    }
%>			${g.clear(3)}
			model.errors.clear()
			view.table.selectionModel.clearSelection()
		}
	}

	@Transaction(Transaction.Policy.SKIP)
	def tableSelectionChanged = { ListSelectionEvent event ->
		execInsideUISync {
			if (view.table.selectionModel.isSelectionEmpty()) {
				clear()
			} else {
				${g.domainClassName} selected = view.table.selectionModel.selected[0]
				model.errors.clear()
<%
    if (g.domainClass.entity) {
        out << "\t\t\t\tmodel.id = selected.id\n"
    }
%>				${g.selected(4)}
			}
		}
	}

    @Transaction(Transaction.Policy.SKIP)
    def close = {
        SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose()
    }

}
package ${g.targetPackageName}

${g.imports()}
import simplejpa.swing.DialogUtils
import simplejpa.transaction.Transaction
import javax.swing.*
import javax.validation.groups.Default

class ${g.customClassName}Controller {

	${g.customClassName}Model model
	def view
    ${g.repositoryType} ${g.repositoryVar}

	void mvcGroupInit(Map args) {
		model.${g.domainClassNameAsProperty} = args.'pair'
		init()
		listAll()
	}

	void mvcGroupDestroy() {
	}

	def init = {
		model.${g.domainClassNameAsProperty} = model.${g.domainClassNameAsProperty}
		if (model.${g.domainClassNameAsProperty} != null) {
${g.pair_init(3)}
		}
	}

	def listAll = {
<%
	out << g.sub_listAll_clear(2)
	out << g.sub_listAll_find(2)
	out << g.sub_listAll_set(2)
%>    }

	def save = {
        if (model.${g.domainClassNameAsProperty}!=null) {
            if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return
            }
        }

		${g.domainClassName} ${g.domainClassNameAsProperty} = ${g.domainClassConstructor()}

		if (!${g.repositoryVar}.validate(${g.domainClassNameAsProperty}, Default, model)) return

		if (model.${g.domainClassNameAsProperty}==null) {
			// Insert operation
<%
out << g.saveOneToManyInverse(g.domainClass, 3)
out << g.saveManyToManyInverse(g.domainClass, 3)
%>            model.${g.domainClassNameAsProperty} = ${g.domainClassNameAsProperty}
		} else {
			// Update operation
${g.update(3, 'model.' + g.domainClassNameAsProperty)}
<%
    out << g.saveOneToManyInverse(g.domainClass, 3, "model.${g.domainClassNameAsProperty}")
    out << g.saveManyToManyInverse(g.domainClass, 3, "model.${g.domainClassNameAsProperty}")
%>
		}
		close()
	}

	def delete = {
        if (JOptionPane.showConfirmDialog(view.mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return
        }
		model.${g.domainClassNameAsProperty} = null
		close()
	}
${g.popups(1)}
    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose() }
    }

}
package ${g.targetPackageName}

${g.imports()}
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.joda.time.*
import javax.swing.event.*
import simplejpa.swing.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class ${g.customClassName}Model {

	@Bindable Long id

${g.modelAttrs(1)}

	@Bindable ${g.domainClassName} ${g.domainClassNameAsProperty}

}
package ${g.targetPackageName}

${g.imports()}
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import org.joda.time.*
import javax.swing.event.*
import simplejpa.swing.*
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class ${g.domainClassName}Model {

	@Bindable Long id

${g.modelAttrs(1)}
	BasicEventList<${g.domainClassName}> ${g.domainClassGlazedListVariable} = new BasicEventList<>()

	@Bindable String ${g.firstAttrSearch}
	@Bindable String created
	@Bindable String modified

}
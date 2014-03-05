package $packageName

import ${domainPackage}.*
import ${domainPackage}.container.*
import ${domainPackage}.exception.*
import ${domainPackage}.repository.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import com.google.common.base.Strings
import domain.exception.DataDuplikat

class $className {

    ${domainClass}Model model
    def view

    ${domainClass}Repository ${domainClassAsProp}Repository

    void mvcGroupInit(Map args) {
        ${domainClassAsProp}Repository = Application.instance.${domainClassAsProp}Repository
        search()
    }

    def search = {
        List result
        if (Strings.isNullOrEmpty(model.${firstField}Search)) {
            result = ${domainClassAsProp}Repository.findAll${domainClass}()
        } else {
            result = ${domainClassAsProp}Repository.findAll${domainClass}By${cls(firstField)}Like("%\${model.${firstField}Search}%")
        }
        execInsideUISync {
            model.${domainClassAsProp}List.clear()
            model.${domainClassAsProp}List.addAll(result)
            model.${firstField}Search = null
        }
    }

    def save = {
        ${domainClass} ${domainClassAsProp} = new ${domainClass}(id: model.id, <%
    out << fields.findAll{ !(isOneToOne(it) && isMappedBy(it)) }.collect { field ->
        if (isManyToOne(field) || isEnumerated(field)) {
            return "'${field.name}': model.${field.name}.selectedItem"
        } else if (isOneToMany(field)) {
            return "'${field.name}': new ArrayList(model.${field.name})"
        } else if (isManyToMany(field)) {
            return "'${field.name}': model.${field.name}.selectedValues"
        } else {
            return "'${field.name}': model.${field.name}"
        }
    }.join(", ")
%>)
<%
    def printTab(int n) {
        n.times { out << "\t" }
    }

    def processOneToManyInSave(List fields, String currentClass, String currentAttribute = null, int numOfTab, String currentField=null) {
        if (!currentAttribute) currentAttribute = currentClass
        fields.findAll{ isOneToMany(it) && isBidirectional(it) }.each { field ->
            if (currentField && field.name.toString()!=currentField) return
            printTab(numOfTab)
            out << "${currentClass}.${field.name}.each { ${field.info} ${prop(field.info)} ->\n"
            printTab(numOfTab+1)
            out << "${prop(field.info)}.${currentAttribute} = ${currentClass}\n"
            processOneToManyInSave(getField(field.info), prop(field.info), numOfTab+1)
            printTab(numOfTab)
            out << "}\n"
        }
    }

    def processManyToManyInSave(List fields, String currentClass, String currentAttribute = null, int numOfTab, String currentField=null) {
        if (!currentAttribute) currentAttribute = currentClass
        fields.findAll{ isManyToMany(it) && isBidirectional(it) && !isMappedBy(it) }.each { field ->
            if (currentField && field.name.toString()!=currentField) return
            printTab(numOfTab)
            out << "${currentClass}.${field.name}.each { ${field.info} ${prop(field.info)} ->\n"
            printTab(numOfTab+1)
            out << "if (!${prop(field.info)}.${linkedAttribute(field).name}.contains(${currentClass})) {\n"
            printTab(numOfTab+2)
            out << "${prop(field.info)}.${linkedAttribute(field).name}.add(${currentClass})\n"
            processManyToManyInSave(getField(field.info), prop(field.info), numOfTab+2)
            printTab(numOfTab+1)
            out << "}\n"
            printTab(numOfTab)
            out << "}\n"
        }
    }

    processOneToManyInSave(fields, domainClassAsProp, 2)
    processManyToManyInSave(fields, domainClassAsProp, 2)
%>
        if (!${domainClassAsProp}Repository.validate(${domainClassAsProp}, Default, model)) return

        try {
            if (${domainClassAsProp}.id == null) {
                ${domainClassAsProp}Repository.buat(${domainClassAsProp})
                execInsideUISync {
                    model.${domainClassAsProp}List << ${domainClassAsProp}
                    view.table.changeSelection(model.${domainClassAsProp}List.size()-1, 0, false, false)
                    clear()
                }
            } else {
                ${domainClassAsProp} = ${domainClassAsProp}Repository.merge(${domainClassAsProp})
                execInsideUISync {
                    view.table.selectionModel.selected[0] = ${domainClassAsProp}
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['${firstField}'] = app.getMessage("simplejpa.error.alreadyExist.message")
        }
    }

    def delete = {
        ${domainClass} ${domainClassAsProp} = view.table.selectionModel.selected[0]
<% if (softDelete) {
        out << "\t\t${domainClassAsProp}Repository.softDelete(${domainClassAsProp})\n"
   } else {
        out << "\t\t${domainClassAsProp}Repository.remove(${domainClassAsProp})"  } %>
        execInsideUISync {
            model.${domainClassAsProp}List.remove(${domainClassAsProp})
            clear()
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
<% fields.collect { field ->
        if (isOneToOne(field) && isMappedBy(field)) return

        if (["BASIC_TYPE", "DATE"].contains(field.info) ||
            (field.info=="DOMAIN_CLASS" && field.annotations?.containsAnnotation('OneToOne'))) {
                if (["Boolean", "boolean"].contains(field.type as String)) {
                    out << "\t\t\tmodel.${field.name} = false\n"
                } else {
                    out << "\t\t\tmodel.${field.name} = null\n"
                }
        } else if (isOneToOne(field) || isManyToOne(field) || isEnumerated(field)) {
            out << "\t\t\tmodel.${field.name}.selectedItem = null\n"
        } else if (isOneToMany(field)) {
            out << "\t\t\tmodel.${field.name}.clear()\n"
        } else if (isManyToMany(field)) {
            out << "\t\t\tmodel.${field.name}.clearSelectedValues()\n"
        } else if (field.info=="UNKNOWN") {
            out << "\t\t\t// ${field.name} is not supported by generator.  You will need to code it manually.\n"
            out << "\t\t\tmodel.${field.name} = null\n"
        }
   }
%>
            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                ${domainClass} selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
<%
    fields.each { field ->
        if (isOneToOne(field) && isMappedBy(field)) return

        if (["BASIC_TYPE", "DATE"].contains(field.info) ||
            (field.info=="DOMAIN_CLASS" && field.annotations?.containsAnnotation('OneToOne'))) {
            out << "\t\t\t\tmodel.${field.name} = selected.${field.name}\n"
        } else if (isOneToOne(field) || isManyToOne(field) || isEnumerated(field)) {
            out << "\t\t\t\tmodel.${field.name}.selectedItem = selected.${field.name}\n"
        } else if (isOneToMany(field)) {
            out << "\t\t\t\tmodel.${field.name}.clear()\n"
            out << "\t\t\t\tmodel.${field.name}.addAll(selected.${field.name})\n"
        } else if (isManyToMany(field)) {
            out << "\t\t\t\tmodel.${field.name}.replaceSelectedValues(selected.${field.name})\n"
        } else if (field.info=="UNKNOWN") {
            out << "\t\t\t\t// ${field.name} is not supported by generator.  You will need to code it manually.\n"
            out << "\t\t\t\tmodel.${field.name} = selected.${field.name}\n"
        }
    }
%>            }
        }
    }

}
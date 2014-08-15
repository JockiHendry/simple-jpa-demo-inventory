package ${g.domainPackageName}

import groovy.transform.*
import org.apache.poi.hssf.record.formula.functions.Search
import org.joda.time.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.exception.EntityNotFoundException
import simplejpa.transaction.Transaction

@Transaction
class ${g.repositoryType} {

	public List<${g.domainClassName}> search(String ${g.firstAttrSearch}) {
		findAll${g.domainClassName}ByDsl([excludeDeleted: false]) {
			if (${g.firstAttrSearch}) {
				${g.firstAttr} like("%\${${g.firstAttrSearch}}%")
			}
		}
	}

	public ${g.domainClassName} create(${g.domainClassName} ${g.domainClassNameAsProperty}) {
		if (find${g.domainClassName}By${g.firstAttrAsCapitalized}(${g.domainClassNameAsProperty}.${g.firstAttr})) {
			throw new DuplicateEntityException(${g.domainClassNameAsProperty})
		}
		persist(${g.domainClassNameAsProperty})
		${g.domainClassNameAsProperty}
	}

	public ${g.domainClassName} update(${g.domainClassName} ${g.domainClassNameAsProperty}) {
		${g.domainClassName} merged${g.domainClassName} = find${g.domainClassName}ById(${g.domainClassNameAsProperty}.id)
		if (!merged${g.domainClassName}) {
			throw new EntityNotFoundException(${g.domainClassNameAsProperty})
		}
		merged${g.domainClassName}.with {
${g.updates(3)}
		}
		merged${g.domainClassName}
	}

}


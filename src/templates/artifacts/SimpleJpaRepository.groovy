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
import groovy.transform.*
import org.joda.time.*
import simplejpa.exception.DuplicateEntityException
import simplejpa.exception.EntityNotFoundException
import simplejpa.transaction.Transaction

@Transaction
class ${g.repositoryType} {

	public List<${g.domainClassName}> cari(String ${g.firstAttrSearch}) {
		findAll${g.domainClassName}ByDsl([excludeDeleted: false]) {
			if (${g.firstAttrSearch}) {
				${g.firstAttr} like("%\${${g.firstAttrSearch}}%")
			}
		}
	}

	public ${g.domainClassName} buat(${g.domainClassName} ${g.domainClassNameAsProperty}) {
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

    public ${g.domainClassName} hapus(${g.domainClassName} ${g.domainClassNameAsProperty}) {
        ${g.domainClassNameAsProperty} = find${g.domainClassName}ById(${g.domainClassNameAsProperty}.id)
        if (!${g.domainClassNameAsProperty}) {
            throw new DataTidakBolehDiubah(${g.domainClassNameAsProperty})
        }
        ${g.domainClassNameAsProperty}.deleted = 'Y'
        ${g.domainClassNameAsProperty}
    }

}
package ${packageName}

import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical
class ${className} {

	//
	// Example of attribute declarations:
	//
	// @NotEmpty @Size(min=2, max=50)
	// String name
	//
	// @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	// LocalDate date
	//
	// @Embedded
	// Address address
	//
	// @NotNull @Min(value=1l)
	// Integer score
	//
	// @NotNull @ManyToOne
	// ClassRoom classRoom
	//
	// @NotEmpty @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
	// List<Others> others = []
	//
	// @ElementCollection @OrderColumn @NotEmpty
	// List<Items> items = []
	//

}


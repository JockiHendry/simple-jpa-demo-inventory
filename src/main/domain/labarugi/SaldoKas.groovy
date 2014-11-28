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
package domain.labarugi

import domain.inventory.Periode
import groovy.transform.*
import org.hibernate.validator.constraints.Range
import javax.persistence.*
import javax.validation.constraints.*

@Embeddable @Canonical
class SaldoKas {

	@NotNull @Range(min=1l, max=12l)
	Integer bulan

	@NotNull @Min(1970l)
	Integer tahun

	@NotNull @Min(0l)
	BigDecimal saldo

	@NotNull @ManyToOne
	JenisTransaksiKas jenis

	Periode getPeriode() {
		Periode.bulan(bulan, tahun)
	}

}

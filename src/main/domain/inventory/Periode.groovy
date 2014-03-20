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

package domain.inventory

import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class Periode {

    public static final DateTimeFormatter format = DateTimeFormat.forPattern("dd-MM-YYYY")

    LocalDate tanggalMulai
    LocalDate tanggalSelesai

    public Periode(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        this.tanggalMulai = tanggalMulai
        this.tanggalSelesai = tanggalSelesai
        if (tanggalMulai.isAfter(tanggalSelesai)) {
            throw new RuntimeException("Tanggal mulai [$tanggalMulai] tidak boleh setelah tanggal selesai [$tanggalSelesai]")
        }
    }

    public overlaps(Periode periode) {
        Interval i1 = new Interval(tanggalMulai.toDateMidnight(), tanggalSelesai.plusDays(1).toDateMidnight())
        Interval i2 = new Interval(periode.tanggalMulai.toDateMidnight(), periode.tanggalSelesai.plusDays(1).toDateMidnight())
        i1.overlaps(i2)
    }

    public static Periode sekarang() {
        new Periode(LocalDate.now().minusDays(30), LocalDate.now())
    }

    public static Periode dari(String tanggalMulai, String tanggalSelesai) {
        new Periode(format.parseLocalDate(tanggalMulai), format.parseLocalDate(tanggalSelesai))
    }

}

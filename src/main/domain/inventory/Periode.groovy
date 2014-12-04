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

import groovy.transform.Canonical
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

@Canonical @SuppressWarnings("GroovyUnusedDeclaration")
class Periode {

    public static final DateTimeFormatter format = DateTimeFormat.forPattern("dd-MM-YYYY")

    private Interval interval

    LocalDate tanggalMulai

    LocalDate tanggalSelesai

    public Periode(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        this.tanggalMulai = tanggalMulai
        this.tanggalSelesai = tanggalSelesai
        if (tanggalMulai.isAfter(tanggalSelesai)) {
            throw new IllegalArgumentException("Tanggal mulai [$tanggalMulai] tidak boleh setelah tanggal selesai [$tanggalSelesai]")
        }
        interval = new Interval(tanggalMulai.toDateMidnight(), tanggalSelesai.plusDays(1).toDateMidnight())
    }

    public boolean overlaps(Periode periode) {
        Interval i = new Interval(periode.tanggalMulai.toDateMidnight(), periode.tanggalSelesai.plusDays(1).toDateMidnight())
        interval.overlaps(i)
    }

    public boolean termasuk(LocalDate tanggal) {
        interval.contains(tanggal.toDateMidnight())
    }

    public static Periode sekarang() {
        new Periode(LocalDate.now().minusDays(30), LocalDate.now())
    }

    public static Periode dari(String tanggalMulai, String tanggalSelesai) {
        new Periode(format.parseLocalDate(tanggalMulai), format.parseLocalDate(tanggalSelesai))
    }

    public static Periode bulan(LocalDate tanggal) {
        new Periode(tanggal.dayOfMonth().withMinimumValue(), tanggal.dayOfMonth().withMaximumValue())
    }

    public static Periode bulan(int bulan, int tahun) {
        LocalDate tanggal = new LocalDate(tahun, bulan, 1)
        new Periode(tanggal, tanggal.dayOfMonth().withMaximumValue())
    }

    @Override
    boolean equals(Object obj) {
        if (obj && (obj instanceof Periode)) {
            return (obj.tanggalMulai == tanggalMulai) && (obj.tanggalSelesai == tanggalSelesai)
        }
        false
    }

    @Override
    String toString() {
        "${tanggalMulai.toString('dd-MM-YYYY')} s/d ${tanggalSelesai.toString('dd-MM-YYYY')}"
    }

}

package pe.gob.municipalidad.sigm.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {

    @Test
    @DisplayName("DateUtils debe usar zona horaria America/Lima (-05:00)")
    void timeZoneShouldBeAmericaLima() {
        assertEquals("America/Lima", DateUtils.LIMA_ZONE_ID.getId());
        OffsetDateTime now = DateUtils.nowOffset();
        assertEquals("-05:00", now.getOffset().getId());
    }

    @Test
    @DisplayName("DateUtils debe permitir Clock fijo inyectado para pruebas")
    void dateUtilsWithFixedClock() {
        Instant fixedInstant = Instant.parse("2026-10-14T15:30:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        OffsetDateTime dateTime = DateUtils.nowOffset(fixedClock);
        assertNotNull(dateTime);
        assertEquals("-05:00", dateTime.getOffset().getId());
        assertEquals(10, dateTime.getHour()); // 15:30 UTC = 10:30 America/Lima (-5)
    }

    @Test
    @DisplayName("Formateo de fechas civiles a formato peruano dd/MM/yyyy")
    void dateFormatting() {
        LocalDate date = LocalDate.of(2026, 10, 14);
        String formatted = DateUtils.format(date);
        assertEquals("14/10/2026", formatted);

        LocalDate parsed = DateUtils.parseLocalDate("14/10/2026");
        assertEquals(date, parsed);
    }

    @Test
    @DisplayName("Formateo ISO-8601 con zona horaria peruana")
    void isoFormatting() {
        OffsetDateTime now = DateUtils.nowOffset();
        String iso = DateUtils.formatIso(now);
        assertTrue(iso.endsWith("-05:00"));
    }
}

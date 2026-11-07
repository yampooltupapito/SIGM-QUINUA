package pe.gob.municipalidad.sigm.common.util;

import pe.gob.municipalidad.sigm.common.constants.AppConstants;
import pe.gob.municipalidad.sigm.config.TimeConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para manejo seguro de fechas, instantes y zonas horarias en SIGM-QUINUA.
 * Garantiza que la representación horaria respete UTC-5 (America/Lima) y formatos ISO-8601.
 */
public final class DateUtils {

    public static final ZoneId LIMA_ZONE_ID = TimeConfig.ZONE_ID_PERU;
    public static final ZoneOffset PERU_OFFSET = ZoneOffset.of("-05:00");
    public static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT);

    private DateUtils() {
        // Utility class
    }

    public static OffsetDateTime nowOffset() {
        return OffsetDateTime.now(TimeConfig.ZONE_ID_PERU);
    }

    public static OffsetDateTime nowOffset(Clock clock) {
        if (clock != null) {
            return OffsetDateTime.now(clock.withZone(TimeConfig.ZONE_ID_PERU));
        }
        return OffsetDateTime.now(TimeConfig.ZONE_ID_PERU);
    }

    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(TimeConfig.ZONE_ID_PERU);
    }

    public static LocalDate today() {
        return LocalDate.now(TimeConfig.ZONE_ID_PERU);
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    public static OffsetDateTime toOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(TimeConfig.ZONE_ID_PERU).toOffsetDateTime();
    }

    public static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(TimeConfig.ZONE_ID_PERU).toOffsetDateTime();
    }

    public static String formatLocalDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String format(LocalDate date) {
        return formatLocalDate(date);
    }

    public static LocalDate parseLocalDate(String text) {
        return text != null && !text.trim().isEmpty() ? LocalDate.parse(text.trim(), DATE_FORMATTER) : null;
    }

    public static String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    public static String formatIsoOffset(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.format(ISO_OFFSET_FORMATTER) : null;
    }

    public static String formatIso(OffsetDateTime offsetDateTime) {
        return formatIsoOffset(offsetDateTime);
    }
}

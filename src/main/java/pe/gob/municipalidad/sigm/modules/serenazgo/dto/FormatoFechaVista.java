package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad de formateo de fechas para las vistas de Serenazgo (zona horaria de Perú).
 * Usada por los DTOs que exponen propiedades calculadas "*Formateada" a las plantillas.
 */
final class FormatoFechaVista {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatoFechaVista() {
    }

    static String fechaHora(OffsetDateTime valor) {
        return valor == null ? null : valor.atZoneSameInstant(ZONA_PERU).format(FECHA_HORA);
    }
}

package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Abstracción de servicio para el cálculo de plazos administrativos según TUO Ley 27444.
 * Diferencia entre días hábiles y días calendario.
 * Excluye fines de semana y feriados nacionales oficiales del Perú.
 */
@Service
public class PlazoAdministrativoService {

    private static final Logger log = LoggerFactory.getLogger(PlazoAdministrativoService.class);

    // Feriados fijos nacionales peruanos reconocidos por ley (Decreto Legislativo 713)
    private static final Set<MonthDay> FERIADOS_NACIONALES_FIJOS = Set.of(
            MonthDay.of(1, 1),   // Año Nuevo
            MonthDay.of(5, 1),   // Día del Trabajo
            MonthDay.of(6, 7),   // Batalla de Arica y Día de la Bandera
            MonthDay.of(6, 29),  // San Pedro y San Pablo
            MonthDay.of(7, 23),  // Día de la Fuerza Aérea del Perú
            MonthDay.of(7, 28),  // Fiestas Patrias
            MonthDay.of(7, 29),  // Fiestas Patrias
            MonthDay.of(8, 6),   // Batalla de Junín
            MonthDay.of(8, 30),  // Santa Rosa de Lima
            MonthDay.of(10, 8),  // Combate de Angamos
            MonthDay.of(11, 1),  // Día de Todos los Santos
            MonthDay.of(12, 8),  // Inmaculada Concepción
            MonthDay.of(12, 9),  // Batalla de Ayacucho (Feriado nacional de especial relevancia en Quinua)
            MonthDay.of(12, 25)  // Navidad
    );

    private final pe.gob.municipalidad.sigm.modules.mesapartes.repository.CalendarioAdministrativoRepository calendarioRepository;

    public PlazoAdministrativoService() {
        this.calendarioRepository = null;
    }

    public PlazoAdministrativoService(pe.gob.municipalidad.sigm.modules.mesapartes.repository.CalendarioAdministrativoRepository calendarioRepository) {
        this.calendarioRepository = calendarioRepository;
    }

    /**
     * Calcula la fecha límite administrativa a partir de una fecha base, cantidad de días y unidad de cómputo.
     *
     * @param fechaInicio Fecha inicial de cómputo (recepción o notificación)
     * @param dias Cantidad de días de plazo
     * @param unidadPlazo Días hábiles o días calendario
     * @return Fecha y hora límite calculada, o null si los parámetros son nulos/inválidos
     */
    public OffsetDateTime calcularFechaLimite(OffsetDateTime fechaInicio, Integer dias, UnidadPlazo unidadPlazo) {
        if (fechaInicio == null || dias == null || dias <= 0) {
            return null;
        }

        if (unidadPlazo == null || unidadPlazo == UnidadPlazo.DIAS_CALENDARIO) {
            return fechaInicio.plusDays(dias);
        }

        // Cómputo por días hábiles (el plazo se inicia el día hábil siguiente a la notificación/recepción según LPAG)
        LocalDate actual = fechaInicio.toLocalDate().plusDays(1);
        int diasContados = 0;

        while (diasContados < dias) {
            if (esDiaHabil(actual)) {
                diasContados++;
            }
            if (diasContados < dias) {
                actual = actual.plusDays(1);
            }
        }

        // Se fija la fecha límite al final de la jornada de atención o a la misma hora del día de vencimiento
        return fechaInicio.withYear(actual.getYear())
                .withMonth(actual.getMonthValue())
                .withDayOfMonth(actual.getDayOfMonth());
    }

    /**
     * Determina si una fecha es día hábil administrativo (no es sábado, domingo ni feriado nacional).
     */
    public boolean esDiaHabil(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        DayOfWeek dow = fecha.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }

        if (calendarioRepository != null) {
            var calOpt = calendarioRepository.findByFecha(fecha);
            if (calOpt.isPresent()) {
                var cal = calOpt.get();
                if (Boolean.TRUE.equals(cal.getActivo())) {
                    return Boolean.TRUE.equals(cal.getEsLaborable());
                }
            }
        }

        MonthDay md = MonthDay.from(fecha);
        return !FERIADOS_NACIONALES_FIJOS.contains(md);
    }
}

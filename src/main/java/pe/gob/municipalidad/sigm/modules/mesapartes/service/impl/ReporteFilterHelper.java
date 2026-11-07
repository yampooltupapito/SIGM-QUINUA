package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.FiltroReporteRequest;

import java.time.*;
import java.util.List;

/**
 * Utilidades para cálculo de rangos cronológicos, resolución de permisos de oficina y sanitización.
 */
public final class ReporteFilterHelper {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private ReporteFilterHelper() {
    }

    /**
     * Resuelve las fechas 'desde' y 'hasta' en OffsetDateTime según el filtro o período predeterminado.
     */
    public static OffsetDateTime[] resolverRangoFechas(FiltroReporteRequest filtro) {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        LocalDate desde;
        LocalDate hasta;

        String periodo = filtro != null && filtro.getPeriodo() != null ? filtro.getPeriodo().toUpperCase() : "";

        switch (periodo) {
            case "MES_ACTUAL":
                desde = hoy.withDayOfMonth(1);
                hasta = hoy.withDayOfMonth(hoy.lengthOfMonth());
                break;
            case "MES_ANTERIOR":
                LocalDate mesAnt = hoy.minusMonths(1);
                desde = mesAnt.withDayOfMonth(1);
                hasta = mesAnt.withDayOfMonth(mesAnt.lengthOfMonth());
                break;
            case "TRIMESTRE_ACTUAL":
                int primerMesTrimestre = ((hoy.getMonthValue() - 1) / 3) * 3 + 1;
                desde = LocalDate.of(hoy.getYear(), primerMesTrimestre, 1);
                LocalDate finTrim = desde.plusMonths(2);
                hasta = finTrim.withDayOfMonth(finTrim.lengthOfMonth());
                break;
            case "ANIO_ACTUAL":
                desde = LocalDate.of(hoy.getYear(), 1, 1);
                hasta = LocalDate.of(hoy.getYear(), 12, 31);
                break;
            default:
                if (filtro != null && filtro.getFechaInicio() != null) {
                    desde = filtro.getFechaInicio();
                } else if (filtro != null && filtro.getAnio() != null) {
                    desde = LocalDate.of(filtro.getAnio(), 1, 1);
                } else {
                    desde = LocalDate.of(hoy.getYear(), 1, 1);
                }

                if (filtro != null && filtro.getFechaFin() != null) {
                    hasta = filtro.getFechaFin();
                } else if (filtro != null && filtro.getAnio() != null) {
                    hasta = LocalDate.of(filtro.getAnio(), 12, 31);
                } else {
                    hasta = LocalDate.of(hoy.getYear(), 12, 31);
                }
                break;
        }

        if (desde.isAfter(hasta)) {
            LocalDate temp = desde;
            desde = hasta;
            hasta = temp;
        }

        OffsetDateTime dtDesde = desde.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime dtHasta = hasta.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime();

        return new OffsetDateTime[]{dtDesde, dtHasta};
    }

    /**
     * Resuelve el ID de oficina permitido para el usuario autenticado.
     * Si el usuario no tiene permisos globales, fuerza el filtro a su oficina activa.
     */
    public static Long resolverOficinaPermitida(Usuario usuario, Long oficinaSolicitada, PersonaOficinaRepository personaOficinaRepository) {
        if (usuario == null) {
            throw new AccessDeniedException("No existe usuario autenticado.");
        }

        // 1. Administrador General o Superadmin tienen acceso global
        boolean esGlobal = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodigo()) || "ROLE_SUPERADMIN".equals(r.getCodigo()));

        if (esGlobal) {
            return oficinaSolicitada; // Puede ser null (todas) o una oficina específica
        }

        // 2. Servidores públicos / jefes con alcance de oficina
        if (usuario.getPersona() == null) {
            throw new AccessDeniedException("El usuario no tiene una persona natural vinculada.");
        }

        List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO");
        if (asignaciones.isEmpty()) {
            throw new AccessDeniedException("El servidor público no cuenta con una oficina activa asignada.");
        }

        List<Long> misOficinasIds = asignaciones.stream()
                .filter(a -> a.getOficina() != null)
                .map(a -> a.getOficina().getId())
                .toList();

        if (oficinaSolicitada == null) {
            // Se predetermina a su oficina principal o primera asignación activa
            return misOficinasIds.get(0);
        }

        // Si solicita una oficina distinta a las asignadas, se restringe estrictamente (Anti-IDOR)
        if (!misOficinasIds.contains(oficinaSolicitada)) {
            throw new AccessDeniedException("Acceso restringido: No cuenta con autorización para consultar estadísticas de otra oficina.");
        }

        return oficinaSolicitada;
    }

    /**
     * Sanitiza texto para evitar inyecciones de fórmulas en Microsoft Excel (CSV/XLSX Formula Injection).
     * Si la cadena inicia con =, +, -, @, tabulación o retorno de carro, se antepone una comilla simple.
     */
    public static String sanitizarParaExcel(String texto) {
        if (texto == null) {
            return "";
        }
        String recortado = texto.strip();
        if (recortado.isEmpty()) {
            return "";
        }
        char primerCaracter = recortado.charAt(0);
        if (primerCaracter == '=' || primerCaracter == '+' || primerCaracter == '-' || primerCaracter == '@'
                || primerCaracter == '\t' || primerCaracter == '\r') {
            return "'" + texto;
        }
        return texto;
    }
}

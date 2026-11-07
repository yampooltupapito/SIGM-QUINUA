package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ResumenDashboardSeguridadResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DashboardSeguridadService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSeguridadServiceImpl implements DashboardSeguridadService {

    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final PatrullajeSerenazgoRepository patrullajeRepository;
    private final IntervencionSerenazgoRepository intervencionRepository;

    @Override
    public ResumenDashboardSeguridadResponse obtenerResumenOperativo() {
        ZoneId zone = ZoneId.of("America/Lima");
        OffsetDateTime inicioHoy = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime finHoy = LocalDate.now(zone).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        long personalActivo = personalRepository.countByEstado(EstadoPersonalSerenazgo.ACTIVO);
        long unidadesOperativas = unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.OPERATIVA);
        long sectoresActivos = sectorRepository.countByEstado(EstadoSectorPatrullaje.ACTIVO);
        long zonasActivas = zonaRepository.count();

        long incidenciasAbiertas = incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.REGISTRADA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_EVALUACION)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.ASIGNADA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_ATENCION);

        long incidenciasHoy = incidenciaRepository.countByFechaReporteBetween(inicioHoy, finHoy);
        long alertasPendientes = alertaRepository.countByEstado(EstadoAlertaCiudadana.PENDIENTE);
        long patrullajesEnCurso = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.EN_CURSO);
        long intervencionesEnCurso = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.INICIADA)
                + intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.EN_PROCESO);

        return ResumenDashboardSeguridadResponse.builder()
                .totalPersonalActivo(personalActivo)
                .totalUnidadesOperativas(unidadesOperativas)
                .totalSectoresActivos(sectoresActivos)
                .totalZonasActivas(zonasActivas)
                .incidenciasAbiertas(incidenciasAbiertas)
                .incidenciasHoy(incidenciasHoy)
                .alertasPendientes(alertasPendientes)
                .patrullajesEnCurso(patrullajesEnCurso)
                .intervencionesEnCurso(intervencionesEnCurso)
                .build();
    }
}

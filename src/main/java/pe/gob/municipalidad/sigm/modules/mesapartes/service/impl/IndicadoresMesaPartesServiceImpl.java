package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.IndicadoresMesaPartesService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PlazoAdministrativoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class IndicadoresMesaPartesServiceImpl implements IndicadoresMesaPartesService {

    private final ExpedienteRepository expedienteRepository;
    private final DerivacionExpedienteRepository derivacionRepository;
    private final ReporteMesaPartesService reporteService;
    private final PlazoAdministrativoService plazoService;

    public IndicadoresMesaPartesServiceImpl(ExpedienteRepository expedienteRepository,
                                           DerivacionExpedienteRepository derivacionRepository,
                                           ReporteMesaPartesService reporteService,
                                           PlazoAdministrativoService plazoService) {
        this.expedienteRepository = expedienteRepository;
        this.derivacionRepository = derivacionRepository;
        this.reporteService = reporteService;
        this.plazoService = plazoService;
    }

    @Override
    public IndicadoresEficienciaDto calcularIndicadoresEficiencia(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Expediente> expedientes = expedienteRepository.findParaIndicadores(oficinaId, fechas[0], fechas[1]);
        if (expedientes.isEmpty()) {
            return IndicadoresEficienciaDto.builder()
                    .datosSuficientes(false)
                    .porcentajeResueltos(0.0)
                    .porcentajeObservados(0.0)
                    .porcentajePendientes(0.0)
                    .totalAtendidosPeriodo(0)
                    .build();
        }

        long total = expedientes.size();
        long resueltos = 0;
        long observados = 0;
        long pendientes = 0;

        List<Double> duracionesAtencionDias = new ArrayList<>();
        List<Double> duracionesRecepcionHoras = new ArrayList<>();

        for (Expediente exp : expedientes) {
            EstadoExpediente est = exp.getEstadoActual();
            if (est == null) continue;

            if (est == EstadoExpediente.RESUELTO || est == EstadoExpediente.NOTIFICADO) {
                resueltos++;
                OffsetDateTime fInicio = exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt();
                OffsetDateTime fFin = exp.getFechaCierre() != null ? exp.getFechaCierre() : exp.getUpdatedAt();
                if (fInicio != null && fFin != null && !fFin.isBefore(fInicio)) {
                    double dias = Duration.between(fInicio, fFin).toMinutes() / (60.0 * 24.0);
                    duracionesAtencionDias.add(Math.max(0.1, dias));
                }
            } else if (est == EstadoExpediente.OBSERVADO) {
                observados++;
                pendientes++;
            } else if (est == EstadoExpediente.ARCHIVADO) {
                // Estado final no pendiente
            } else {
                pendientes++;
            }

            // Tiempo hasta recepción
            if (exp.getFechaRecepcion() != null) {
                OffsetDateTime fPres = exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt();
                if (fPres != null && !exp.getFechaRecepcion().isBefore(fPres)) {
                    double horas = Duration.between(fPres, exp.getFechaRecepcion()).toMinutes() / 60.0;
                    duracionesRecepcionHoras.add(Math.max(0.0, horas));
                }
            }
        }

        // Promedio y mediana de atención
        Double promedioAtencion = null;
        Double medianaAtencion = null;
        if (!duracionesAtencionDias.isEmpty()) {
            double suma = duracionesAtencionDias.stream().mapToDouble(Double::doubleValue).sum();
            promedioAtencion = Math.round((suma / duracionesAtencionDias.size()) * 100.0) / 100.0;

            Collections.sort(duracionesAtencionDias);
            int n = duracionesAtencionDias.size();
            if (n % 2 == 1) {
                medianaAtencion = Math.round(duracionesAtencionDias.get(n / 2) * 100.0) / 100.0;
            } else {
                medianaAtencion = Math.round(((duracionesAtencionDias.get(n / 2 - 1) + duracionesAtencionDias.get(n / 2)) / 2.0) * 100.0) / 100.0;
            }
        }

        // Promedio de recepción
        Double promedioRecepcion = null;
        if (!duracionesRecepcionHoras.isEmpty()) {
            double sumaRec = duracionesRecepcionHoras.stream().mapToDouble(Double::doubleValue).sum();
            promedioRecepcion = Math.round((sumaRec / duracionesRecepcionHoras.size()) * 10.0) / 10.0;
        }

        // Promedio de derivación
        List<DerivacionExpediente> derivaciones = derivacionRepository.findRecibidasConTiempos(oficinaId, fechas[0], fechas[1]);
        Double promedioDerivacion = null;
        if (!derivaciones.isEmpty()) {
            double sumaDeriv = 0;
            int countDeriv = 0;
            for (DerivacionExpediente d : derivaciones) {
                if (d.getFechaDerivacion() != null && d.getFechaRecepcion() != null && !d.getFechaRecepcion().isBefore(d.getFechaDerivacion())) {
                    sumaDeriv += Duration.between(d.getFechaDerivacion(), d.getFechaRecepcion()).toMinutes() / 60.0;
                    countDeriv++;
                }
            }
            if (countDeriv > 0) {
                promedioDerivacion = Math.round((sumaDeriv / countDeriv) * 10.0) / 10.0;
            }
        }

        double pctResueltos = total > 0 ? Math.round(((double) resueltos / total) * 1000.0) / 10.0 : 0.0;
        double pctObservados = total > 0 ? Math.round(((double) observados / total) * 1000.0) / 10.0 : 0.0;
        double pctPendientes = total > 0 ? Math.round(((double) pendientes / total) * 1000.0) / 10.0 : 0.0;

        return IndicadoresEficienciaDto.builder()
                .tiempoPromedioAtencionDias(promedioAtencion)
                .tiempoMedianoAtencionDias(medianaAtencion)
                .tiempoPromedioRecepcionHoras(promedioRecepcion)
                .tiempoPromedioDerivacionHoras(promedioDerivacion)
                .porcentajeResueltos(pctResueltos)
                .porcentajeObservados(pctObservados)
                .porcentajePendientes(pctPendientes)
                .totalAtendidosPeriodo(resueltos)
                .datosSuficientes(true)
                .build();
    }

    @Override
    public IndicadoresPlazosDto calcularIndicadoresPlazos(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Expediente> expedientes = expedienteRepository.findParaIndicadores(oficinaId, fechas[0], fechas[1]);

        long enPlazo = 0;
        long proximosVencer = 0;
        long vencidos = 0;
        long suspendidos = 0;
        long sinPlazoConfigurado = 0;

        OffsetDateTime ahora = OffsetDateTime.now();

        for (Expediente exp : expedientes) {
            EstadoExpediente est = exp.getEstadoActual();
            // Solo se calculan plazos en expedientes que se encuentran en trámite
            if (est == EstadoExpediente.RESUELTO || est == EstadoExpediente.NOTIFICADO || est == EstadoExpediente.ARCHIVADO || est == EstadoExpediente.ANULADO) {
                continue;
            }

            if (est == EstadoExpediente.OBSERVADO) {
                suspendidos++;
                continue;
            }

            if (exp.getTipoTramite() == null || exp.getTipoTramite().getPlazoDias() == null || exp.getTipoTramite().getPlazoDias() <= 0) {
                sinPlazoConfigurado++;
                continue;
            }

            OffsetDateTime fBase = exp.getFechaRecepcion() != null ? exp.getFechaRecepcion()
                    : (exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt());

            OffsetDateTime fechaLimite = exp.getFechaLimite();
            if (fechaLimite == null && fBase != null) {
                fechaLimite = plazoService.calcularFechaLimite(
                        fBase,
                        exp.getTipoTramite().getPlazoDias(),
                        exp.getTipoTramite().getUnidadPlazo()
                );
            }

            if (fechaLimite == null) {
                sinPlazoConfigurado++;
                continue;
            }

            if (ahora.isAfter(fechaLimite)) {
                vencidos++;
            } else {
                long diasRestantes = Duration.between(ahora, fechaLimite).toDays();
                if (diasRestantes <= 3) {
                    proximosVencer++;
                } else {
                    enPlazo++;
                }
            }
        }

        long totalConPlazo = enPlazo + proximosVencer + vencidos + suspendidos;

        return IndicadoresPlazosDto.builder()
                .enPlazo(enPlazo)
                .proximosVencer(proximosVencer)
                .vencidos(vencidos)
                .suspendidos(suspendidos)
                .sinPlazoConfigurado(sinPlazoConfigurado)
                .totalConPlazo(totalConPlazo)
                .build();
    }
}

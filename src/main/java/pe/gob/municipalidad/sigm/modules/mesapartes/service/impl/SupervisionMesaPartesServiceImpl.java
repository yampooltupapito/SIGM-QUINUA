package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SupervisionMesaPartesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SupervisionMesaPartesServiceImpl implements SupervisionMesaPartesService {

    private final DerivacionExpedienteRepository derivacionRepository;
    private final ObservacionExpedienteRepository observacionRepository;
    private final SubsanacionExpedienteRepository subsanacionRepository;
    private final ActuacionExpedienteRepository actuacionRepository;
    private final AsignacionExpedienteRepository asignacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final OficinaRepository oficinaRepository;
    private final ReporteMesaPartesService reporteService;

    public SupervisionMesaPartesServiceImpl(DerivacionExpedienteRepository derivacionRepository,
                                           ObservacionExpedienteRepository observacionRepository,
                                           SubsanacionExpedienteRepository subsanacionRepository,
                                           ActuacionExpedienteRepository actuacionRepository,
                                           AsignacionExpedienteRepository asignacionRepository,
                                           ExpedienteRepository expedienteRepository,
                                           OficinaRepository oficinaRepository,
                                           ReporteMesaPartesService reporteService) {
        this.derivacionRepository = derivacionRepository;
        this.observacionRepository = observacionRepository;
        this.subsanacionRepository = subsanacionRepository;
        this.actuacionRepository = actuacionRepository;
        this.asignacionRepository = asignacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.oficinaRepository = oficinaRepository;
        this.reporteService = reporteService;
    }

    @Override
    public AnalisisDerivacionesDto analizarDerivaciones(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        long enviadas = derivacionRepository.countEnviadas(oficinaId, fechas[0], fechas[1]);
        long recibidas = derivacionRepository.countRecibidas(oficinaId, fechas[0], fechas[1]);
        long expedientesUnicos = derivacionRepository.countExpedientesUnicosDerivados(oficinaId, fechas[0], fechas[1]);

        List<Object[]> estados = derivacionRepository.countGroupByEstado(oficinaId, fechas[0], fechas[1]);
        long pendientes = 0;
        long rechazadas = 0;
        long devueltas = 0;

        for (Object[] row : estados) {
            EstadoDerivacion est = (EstadoDerivacion) row[0];
            long count = ((Number) row[1]).longValue();
            if (est == null) continue;

            switch (est) {
                case PENDIENTE:
                    pendientes += count;
                    break;
                case RECHAZADA:
                    rechazadas += count;
                    break;
                case DEVUELTA:
                    devueltas += count;
                    break;
                default:
                    break;
            }
        }

        List<DerivacionExpediente> recibidasConTiempos = derivacionRepository.findRecibidasConTiempos(oficinaId, fechas[0], fechas[1]);
        Double promedioHoras = null;
        if (!recibidasConTiempos.isEmpty()) {
            double totalHoras = 0;
            int validos = 0;
            for (DerivacionExpediente d : recibidasConTiempos) {
                if (d.getFechaDerivacion() != null && d.getFechaRecepcion() != null && !d.getFechaRecepcion().isBefore(d.getFechaDerivacion())) {
                    totalHoras += Duration.between(d.getFechaDerivacion(), d.getFechaRecepcion()).toMinutes() / 60.0;
                    validos++;
                }
            }
            if (validos > 0) {
                promedioHoras = Math.round((totalHoras / validos) * 10.0) / 10.0;
            }
        }

        return AnalisisDerivacionesDto.builder()
                .enviadas(enviadas)
                .recibidas(recibidas)
                .pendientes(pendientes)
                .rechazadas(rechazadas)
                .devueltas(devueltas)
                .expedientesUnicos(expedientesUnicos)
                .tiempoPromedioRecepcionHoras(promedioHoras)
                .build();
    }

    @Override
    public AnalisisObservacionesDto analizarObservaciones(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        long expsObservados = observacionRepository.countExpedientesObservados(oficinaId, fechas[0], fechas[1]);
        long totalObs = observacionRepository.countTotalObservaciones(oficinaId, fechas[0], fechas[1]);
        long subsPresentadas = subsanacionRepository.countSubsanaciones(oficinaId, fechas[0], fechas[1]);
        long subsPendientes = Math.max(0, totalObs - subsPresentadas);

        return AnalisisObservacionesDto.builder()
                .expedientesObservados(expsObservados)
                .observacionesTotales(totalObs)
                .solicitudesSubsanacion(totalObs)
                .subsanacionesPresentadas(subsPresentadas)
                .subsanacionesPendientes(subsPendientes)
                .build();
    }

    @Override
    public ProductividadOperativaDto obtenerProductividadOperativa(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Expediente> expedientes = expedienteRepository.findParaIndicadores(oficinaId, fechas[0], fechas[1]);

        long asignados = 0;
        long enAtencion = 0;
        long finalizados = 0;

        for (Expediente e : expedientes) {
            if (e.getResponsableActual() != null) {
                asignados++;
            }
            EstadoExpediente est = e.getEstadoActual();
            if (est == EstadoExpediente.EN_ATENCION || est == EstadoExpediente.EN_REVISION || est == EstadoExpediente.DERIVADO) {
                enAtencion++;
            } else if (est == EstadoExpediente.RESUELTO || est == EstadoExpediente.NOTIFICADO || est == EstadoExpediente.ARCHIVADO) {
                finalizados++;
            }
        }

        long actuaciones = actuacionRepository.countActuaciones(oficinaId, fechas[0], fechas[1]);

        // Desglose de pendientes por oficina (agregado institucional, no individual)
        Map<Long, Long> pendientesPorOficinaMap = expedientes.stream()
                .filter(e -> e.getOficinaResponsable() != null)
                .filter(e -> {
                    EstadoExpediente est = e.getEstadoActual();
                    return est != EstadoExpediente.RESUELTO && est != EstadoExpediente.NOTIFICADO && est != EstadoExpediente.ARCHIVADO && est != EstadoExpediente.ANULADO;
                })
                .collect(Collectors.groupingBy(e -> e.getOficinaResponsable().getId(), Collectors.counting()));

        List<OficinaPendienteDto> listaOficinas = new ArrayList<>();
        if (oficinaId != null) {
            Oficina ofi = oficinaRepository.findById(oficinaId).orElse(null);
            String nombre = ofi != null ? ofi.getNombre() : "Oficina " + oficinaId;
            listaOficinas.add(OficinaPendienteDto.builder()
                    .oficinaId(oficinaId)
                    .nombreOficina(nombre)
                    .pendientes(pendientesPorOficinaMap.getOrDefault(oficinaId, 0L))
                    .build());
        } else {
            List<Oficina> todas = oficinaRepository.findByEstado("ACTIVO");
            for (Oficina ofi : todas) {
                listaOficinas.add(OficinaPendienteDto.builder()
                        .oficinaId(ofi.getId())
                        .nombreOficina(ofi.getNombre())
                        .pendientes(pendientesPorOficinaMap.getOrDefault(ofi.getId(), 0L))
                        .build());
            }
        }

        listaOficinas.sort((a, b) -> Long.compare(b.getPendientes(), a.getPendientes()));

        return ProductividadOperativaDto.builder()
                .expedientesAsignados(asignados)
                .expedientesEnAtencion(enAtencion)
                .actuacionesRegistradas(actuaciones)
                .expedientesFinalizados(finalizados)
                .oficinasPendientes(listaOficinas)
                .build();
    }
}

package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EstadisticasOficinaService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EstadisticasOficinaServiceImpl implements EstadisticasOficinaService {

    private final ExpedienteRepository expedienteRepository;
    private final OficinaRepository oficinaRepository;
    private final ReporteMesaPartesService reporteService;

    public EstadisticasOficinaServiceImpl(ExpedienteRepository expedienteRepository,
                                          OficinaRepository oficinaRepository,
                                          ReporteMesaPartesService reporteService) {
        this.expedienteRepository = expedienteRepository;
        this.oficinaRepository = oficinaRepository;
        this.reporteService = reporteService;
    }

    @Override
    public List<EstadisticaOficinaDto> obtenerEstadisticasPorOficina(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Expediente> expedientes = expedienteRepository.findParaIndicadores(oficinaId, fechas[0], fechas[1]);

        // Agrupar por Oficina
        Map<Long, List<Expediente>> porOficina = expedientes.stream()
                .filter(e -> e.getOficinaResponsable() != null)
                .collect(Collectors.groupingBy(e -> e.getOficinaResponsable().getId()));

        List<EstadisticaOficinaDto> lista = new ArrayList<>();

        if (oficinaId != null) {
            // Usuario restringido o filtro específico por oficina
            Oficina ofi = oficinaRepository.findById(oficinaId).orElse(null);
            String nombreOfi = ofi != null ? ofi.getNombre() : "Oficina " + oficinaId;
            List<Expediente> expsOfi = porOficina.getOrDefault(oficinaId, Collections.emptyList());
            lista.add(calcularMetricasOficina(oficinaId, nombreOfi, expsOfi));
        } else {
            // Consulta transversal global
            List<Oficina> todasOficinas = oficinaRepository.findByEstado("ACTIVO");
            for (Oficina ofi : todasOficinas) {
                List<Expediente> expsOfi = porOficina.getOrDefault(ofi.getId(), Collections.emptyList());
                lista.add(calcularMetricasOficina(ofi.getId(), ofi.getNombre(), expsOfi));
            }
        }

        // Ordenar descendentemente por cantidad total de expedientes
        lista.sort((a, b) -> Long.compare(b.getTotal(), a.getTotal()));
        return lista;
    }

    private EstadisticaOficinaDto calcularMetricasOficina(Long ofiId, String nombre, List<Expediente> exps) {
        long recibidos = 0;
        long pendientes = 0;
        long enAtencion = 0;
        long resueltos = 0;

        for (Expediente e : exps) {
            EstadoExpediente est = e.getEstadoActual();
            if (est == null) continue;

            if (est == EstadoExpediente.RECIBIDO) {
                recibidos++;
                pendientes++;
            } else if (est == EstadoExpediente.PRESENTADO || est == EstadoExpediente.BORRADOR || est == EstadoExpediente.EN_REVISION || est == EstadoExpediente.OBSERVADO || est == EstadoExpediente.EN_SUBSANACION) {
                pendientes++;
            } else if (est == EstadoExpediente.EN_ATENCION || est == EstadoExpediente.DERIVADO) {
                enAtencion++;
                pendientes++;
            } else if (est == EstadoExpediente.RESUELTO || est == EstadoExpediente.NOTIFICADO) {
                resueltos++;
            }
        }

        return EstadisticaOficinaDto.builder()
                .oficinaId(ofiId)
                .nombreOficina(nombre)
                .recibidos(recibidos)
                .pendientes(pendientes)
                .enAtencion(enAtencion)
                .resueltos(resueltos)
                .total(exps.size())
                .build();
    }
}

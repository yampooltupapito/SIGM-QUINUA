package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IncidenciaReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.InspeccionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RenovacionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ReporteOperativoService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReporteOperativoServiceImpl implements ReporteOperativoService {

    private final InspeccionMercadoRepository inspeccionRepository;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final SolicitudGestionOcupacionRepository solicitudRepository;

    public ReporteOperativoServiceImpl(
            InspeccionMercadoRepository inspeccionRepository,
            IncidenciaMercadoRepository incidenciaRepository,
            SolicitudGestionOcupacionRepository solicitudRepository) {
        this.inspeccionRepository = inspeccionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InspeccionReporteItem> listarInspecciones(ReporteMercadoFilter filtro, Pageable pageable) {
        List<InspeccionReporteItem> todos = listarTodasInspecciones(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<InspeccionReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InspeccionReporteItem> listarTodasInspecciones(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate[] rango = filtro != null ? filtro.resolverRangoEfectivo() : new LocalDate[]{LocalDate.now().minusMonths(1), LocalDate.now()};
        LocalDate fechaDesde = rango[0];
        LocalDate fechaHasta = rango[1];

        List<InspeccionMercado> inspecciones = inspeccionRepository.findAll();

        return inspecciones.stream()
                .filter(i -> {
                    if (mercadoId != null && !i.getMercado().getId().equals(mercadoId)) {
                        return false;
                    }
                    if (i.getFechaProgramada() != null &&
                        (i.getFechaProgramada().isBefore(fechaDesde) || i.getFechaProgramada().isAfter(fechaHasta))) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = i.getCodigoInspeccion().toLowerCase();
                        String puesto = i.getPuesto() != null ? i.getPuesto().getCodigo().toLowerCase() : "";
                        String inspector = i.getInspectorResponsable() != null ? i.getInspectorResponsable().getNombreCompleto().toLowerCase() : "";
                        if (!cod.contains(termino) && !puesto.contains(termino) && !inspector.contains(termino)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(InspeccionMercado::getFechaProgramada, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(i -> {
                    int cantHallazgos = i.getHallazgos() != null ? i.getHallazgos().size() : 0;
                    int cantMedidas = 0;
                    if (i.getHallazgos() != null) {
                        for (HallazgoInspeccionMercado h : i.getHallazgos()) {
                            if (h.getMedidasCorrectivas() != null) {
                                cantMedidas += h.getMedidasCorrectivas().size();
                            }
                        }
                    }

                    String titular = i.getComerciante() != null ? i.getComerciante().getNombreOTitular() : "-";

                    return InspeccionReporteItem.builder()
                            .id(i.getId())
                            .codigoInspeccion(i.getCodigoInspeccion())
                            .mercadoNombre(i.getMercado().getNombre())
                            .puestoCodigo(i.getPuesto() != null ? i.getPuesto().getCodigo() : "-")
                            .titularNombre(titular)
                            .tipoInspeccion(i.getTipoInspeccion().name())
                            .fechaProgramada(i.getFechaProgramada())
                            .inspectorNombre(i.getInspectorResponsable() != null ? i.getInspectorResponsable().getNombreCompleto() : "No asignado")
                            .estado(i.getEstado().name())
                            .calificacionFinal(i.getResultadoGeneral() != null ? i.getResultadoGeneral() : "PENDIENTE")
                            .cantidadHallazgos(cantHallazgos)
                            .cantidadMedidasPendientes(cantMedidas)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenciaReporteItem> listarIncidencias(ReporteMercadoFilter filtro, Pageable pageable) {
        List<IncidenciaReporteItem> todos = listarTodasIncidencias(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<IncidenciaReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaReporteItem> listarTodasIncidencias(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate[] rango = filtro != null ? filtro.resolverRangoEfectivo() : new LocalDate[]{LocalDate.now().minusMonths(1), LocalDate.now()};
        LocalDate fechaDesde = rango[0];
        LocalDate fechaHasta = rango[1];

        List<IncidenciaMercado> incidencias = incidenciaRepository.findAll();

        return incidencias.stream()
                .filter(inc -> {
                    if (mercadoId != null && !inc.getMercado().getId().equals(mercadoId)) {
                        return false;
                    }
                    LocalDate fRep = inc.getFechaReporte() != null ? inc.getFechaReporte().toLocalDate() : null;
                    if (fRep != null && (fRep.isBefore(fechaDesde) || fRep.isAfter(fechaHasta))) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = inc.getCodigoIncidencia().toLowerCase();
                        String desc = inc.getDescripcion() != null ? inc.getDescripcion().toLowerCase() : "";
                        String puesto = inc.getPuesto() != null ? inc.getPuesto().getCodigo().toLowerCase() : "";
                        if (!cod.contains(termino) && !desc.contains(termino) && !puesto.contains(termino)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(IncidenciaMercado::getFechaReporte, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(inc -> {
                    Double horasAtencion = null;
                    // Criterio 39: Calcular duración entre eventos reales, no presentar tiempos inexistentes como cero
                    if (inc.getEstado() == EstadoIncidencia.RESUELTA || inc.getEstado() == EstadoIncidencia.CERRADA) {
                        if (inc.getFechaCierre() != null && inc.getFechaReporte() != null) {
                            Duration d = Duration.between(inc.getFechaReporte(), inc.getFechaCierre());
                            horasAtencion = Math.max(0.1, d.toMinutes() / 60.0);
                        }
                    }

                    return IncidenciaReporteItem.builder()
                            .id(inc.getId())
                            .codigoIncidencia(inc.getCodigoIncidencia())
                            .mercadoNombre(inc.getMercado().getNombre())
                            .puestoCodigo(inc.getPuesto() != null ? inc.getPuesto().getCodigo() : "Área Común")
                            .tipoIncidencia(inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getNombre() : "General")
                            .origen(inc.getOrigen() != null ? inc.getOrigen().name() : "INTERNO")
                            .prioridad(inc.getPrioridad() != null ? inc.getPrioridad().name() : "MEDIA")
                            .estado(inc.getEstado() != null ? inc.getEstado().name() : "REGISTRADA")
                            .fechaReporte(inc.getFechaReporte())
                            .responsableNombre(inc.getResponsable() != null ? inc.getResponsable().getNombreCompleto() : "Sin Asignar")
                            .tiempoAtencionHoras(horasAtencion)
                            .descripcion(inc.getDescripcion())
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RenovacionReporteItem> listarRenovaciones(ReporteMercadoFilter filtro, Pageable pageable) {
        List<RenovacionReporteItem> todos = listarTodasRenovaciones(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<RenovacionReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenovacionReporteItem> listarTodasRenovaciones(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate[] rango = filtro != null ? filtro.resolverRangoEfectivo() : new LocalDate[]{LocalDate.now().minusMonths(1), LocalDate.now()};
        LocalDate fechaDesde = rango[0];
        LocalDate fechaHasta = rango[1];

        List<SolicitudGestionOcupacion> solicitudes = solicitudRepository.findAll();

        return solicitudes.stream()
                .filter(s -> {
                    if (mercadoId != null && (s.getPuesto() == null || !s.getPuesto().getMercado().getId().equals(mercadoId))) {
                        return false;
                    }
                    if (s.getFechaPresentacion() != null &&
                        (s.getFechaPresentacion().isBefore(fechaDesde) || s.getFechaPresentacion().isAfter(fechaHasta))) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = s.getCodigoSolicitud().toLowerCase();
                        String com1 = s.getComercianteActual() != null ? s.getComercianteActual().getNombreOTitular().toLowerCase() : "";
                        String com2 = s.getComerciantePropuesto() != null ? s.getComerciantePropuesto().getNombreOTitular().toLowerCase() : "";
                        if (!cod.contains(termino) && !com1.contains(termino) && !com2.contains(termino)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(SolicitudGestionOcupacion::getFechaPresentacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(s -> RenovacionReporteItem.builder()
                        .id(s.getId())
                        .codigoSolicitud(s.getCodigoSolicitud())
                        .tipoSolicitud(s.getTipoSolicitud().name())
                        .mercadoNombre(s.getPuesto() != null && s.getPuesto().getMercado() != null ? s.getPuesto().getMercado().getNombre() : "-")
                        .puestoCodigo(s.getPuesto() != null ? s.getPuesto().getCodigo() : "-")
                        .comercianteActual(s.getComercianteActual() != null ? s.getComercianteActual().getNombreOTitular() : "-")
                        .comerciantePropuesto(s.getComerciantePropuesto() != null ? s.getComerciantePropuesto().getNombreOTitular() : "-")
                        .fechaPresentacion(s.getFechaPresentacion())
                        .estado(s.getEstado().name())
                        .resultado(s.getEstado().name())
                        .motivo(s.getMotivoSolicitud())
                        .build())
                .collect(Collectors.toList());
    }
}

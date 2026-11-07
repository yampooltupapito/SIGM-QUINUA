package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SupervisionSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupervisionSerenazgoServiceImpl implements SupervisionSerenazgoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SeguimientoSupervisionSerenazgoRepository seguimientoRepository;
    private final ObservacionSupervisionSerenazgoRepository observacionRepository;
    private final HistorialSupervisionSerenazgoRepository historialRepository;

    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final IntervencionSerenazgoRepository intervencionRepository;
    private final PatrullajeSerenazgoRepository patrullajeRepository;
    private final DocumentoSeguridadCiudadanaRepository documentoRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final ProgramacionServicioSerenazgoRepository programacionServicioRepository;

    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;

    @Override
    @Transactional(readOnly = true)
    public IndicadoresSupervisionResponse obtenerIndicadores(FiltroSupervisionRequest filtro, CustomUserDetails user) {
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        OffsetDateTime[] rango = resolverRangoFechas(filtro, ahora);
        OffsetDateTime desde = rango[0];
        OffsetDateTime hasta = rango[1];

        // 1. Incidencias
        long incidenciasPeriodo = incidenciaRepository.countByFechaReporteBetween(desde, hasta);
        long incidenciasPendientes = incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.REGISTRADA);
        long incidenciasAtencion = incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.ASIGNADA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_ATENCION);
        long incidenciasCerradas = incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.CERRADA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.RESUELTA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.DESCARTADA);

        // 2. Alertas
        long alertasPendientes = alertaRepository.countByEstado(EstadoAlertaCiudadana.RECIBIDA);
        long alertasEnEval = alertaRepository.countByEstado(EstadoAlertaCiudadana.EN_REVISION)
                + alertaRepository.countByEstado(EstadoAlertaCiudadana.PENDIENTE);

        // 3. Atenciones
        long atencionesEnCurso = atencionRepository.countByEstado(EstadoAtencionIncidencia.EN_ATENCION)
                + atencionRepository.countByEstado(EstadoAtencionIncidencia.ACEPTADA);
        long atencionesEnEspera = atencionRepository.countByEstado(EstadoAtencionIncidencia.EN_ESPERA);
        long atencionesDespachadas = atencionRepository.countByEstado(EstadoAtencionIncidencia.DESPACHADA);

        // 4. Intervenciones
        long intervencionesPendientes = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.REGISTRADA);
        long intervencionesObservadas = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.OBSERVADA);

        // 5. Patrullajes
        long patrullajesProgramados = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.PROGRAMADO);
        long patrullajesEnCurso = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.EN_CURSO);
        long patrullajesPausados = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.EN_PAUSA);
        long patrullajesFinalizados = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.FINALIZADO);

        // 6. Documentos
        long documentosRevision = documentoRepository.countByEstado(EstadoDocumentoSeguridad.EN_REVISION);
        long documentosObservados = documentoRepository.countByEstado(EstadoDocumentoSeguridad.OBSERVADO);
        long documentosNoCoincidentes = documentoRepository.countByUltimoResultadoVerificacion(ResultadoVerificacionIntegridad.NO_COINCIDENTE);

        // 7. Seguimientos de Supervisión
        long segAbiertos = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.ABIERTO);
        long segRevision = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.EN_REVISION);
        long segObservados = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.OBSERVADO);
        long segSubsanacion = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.EN_SUBSANACION);
        long segResueltos = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.RESUELTO);
        long segCerrados = seguimientoRepository.countByEstado(EstadoSeguimientoSupervision.CERRADO);
        long segVencidos = seguimientoRepository.contarVencidos(ahora);

        // 8. Recursos
        LocalDate hoy = ahora.toLocalDate();
        long personalProgramado = programacionServicioRepository.findByFechaServicio(hoy).size();
        long unidadesOp = unidadMovilRepository.countByEstadoOperativo(EstadoUnidadMovil.OPERATIVA);
        long unidadesMant = unidadMovilRepository.countByEstadoOperativo(EstadoUnidadMovil.EN_MANTENIMIENTO);

        return IndicadoresSupervisionResponse.builder()
                .incidenciasRegistradasPeriodo(incidenciasPeriodo)
                .incidenciasPendientesRevision(incidenciasPendientes)
                .incidenciasEnAtencion(incidenciasAtencion)
                .incidenciasCerradas(incidenciasCerradas)
                .alertasPendientes(alertasPendientes)
                .alertasEnEvaluacion(alertasEnEval)
                .atencionesEnCurso(atencionesEnCurso)
                .atencionesEnEspera(atencionesEnEspera)
                .atencionesDespachadasSinConfirmar(atencionesDespachadas)
                .intervencionesPendientesValidacion(intervencionesPendientes)
                .intervencionesObservadas(intervencionesObservadas)
                .patrullajesProgramados(patrullajesProgramados)
                .patrullajesEnCurso(patrullajesEnCurso)
                .patrullajesPausados(patrullajesPausados)
                .patrullajesFinalizados(patrullajesFinalizados)
                .documentosEnRevision(documentosRevision)
                .documentosObservados(documentosObservados)
                .documentosIntegridadNoCoincidente(documentosNoCoincidentes)
                .seguimientosAbiertos(segAbiertos)
                .seguimientosEnRevision(segRevision)
                .seguimientosObservados(segObservados)
                .seguimientosEnSubsanacion(segSubsanacion)
                .seguimientosResueltos(segResueltos)
                .seguimientosCerrados(segCerrados)
                .seguimientosVencidos(segVencidos)
                .personalProgramadoHoy(personalProgramado)
                .unidadesOperativas(unidadesOp)
                .unidadesEnMantenimiento(unidadesMant)
                .fechaHoraActualizacion(ahora)
                .zonaHoraria("America/Lima")
                .periodoDescripcion(describirPeriodo(filtro, desde, hasta))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BandejaPendienteItemDto> obtenerBandejaPendientes(FiltroSupervisionRequest filtro, CustomUserDetails user) {
        List<BandejaPendienteItemDto> pendientes = new ArrayList<>();

        // 1. Incidencias registradas sin atención asignada
        List<IncidenciaSeguridadCiudadana> incPend = incidenciaRepository.findByEstado(EstadoIncidenciaSeguridad.REGISTRADA);
        for (IncidenciaSeguridadCiudadana inc : incPend) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(inc.getId())
                    .tipoRegistro(TipoRegistroSupervision.INCIDENCIA)
                    .codigo(inc.getCodigo())
                    .titulo(inc.getTitulo())
                    .fecha(inc.getFechaReporte())
                    .prioridad(inc.getPrioridad() != null ? inc.getPrioridad().getDescripcion() : "MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad(inc.getPrioridad() != null ? inc.getPrioridad().name() : "MEDIA"))
                    .estadoActual(inc.getEstado().name())
                    .estadoBadge("bg-amber-500/10 text-amber-400 border-amber-500/20")
                    .sector(inc.getSector() != null ? inc.getSector().getNombre() : "No asignado")
                    .motivoPendiente("Incidencia registrada sin despacho de atención")
                    .responsable("Pendiente de despacho")
                    .urlDetalle("/dashboard/serenazgo/incidencias/" + inc.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=INCIDENCIA&id=" + inc.getId())
                    .build());
        }

        // 2. Alertas ciudadanas pendientes de evaluación
        List<AlertaCiudadanaSeguridad> alertas = alertaRepository.findByEstado(EstadoAlertaCiudadana.RECIBIDA);
        for (AlertaCiudadanaSeguridad al : alertas) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(al.getId())
                    .tipoRegistro(TipoRegistroSupervision.ALERTA)
                    .codigo(al.getCodigo())
                    .titulo(al.getTitulo() != null ? al.getTitulo() : "Alerta de vecino")
                    .fecha(al.getFechaRecepcion())
                    .prioridad(al.getPrioridad() != null ? al.getPrioridad().name() : "MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad(al.getPrioridad() != null ? al.getPrioridad().name() : "MEDIA"))
                    .estadoActual(al.getEstado().name())
                    .estadoBadge("bg-blue-500/10 text-blue-400 border-blue-500/20")
                    .sector("Sector no georreferenciado")
                    .motivoPendiente("Alerta ciudadana recibida sin evaluación")
                    .responsable("Operador de turno")
                    .urlDetalle("/dashboard/serenazgo/alertas/" + al.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=ALERTA&id=" + al.getId())
                    .build());
        }

        // 3. Atenciones en espera o en curso prolongadas
        List<AtencionIncidenciaSerenazgo> atenciones = atencionRepository.findAll().stream()
                .filter(a -> a.getEstado() == EstadoAtencionIncidencia.EN_ESPERA || a.getEstado() == EstadoAtencionIncidencia.DESPACHADA)
                .limit(20)
                .toList();
        for (AtencionIncidenciaSerenazgo at : atenciones) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(at.getId())
                    .tipoRegistro(TipoRegistroSupervision.ATENCION)
                    .codigo(at.getCodigo())
                    .titulo("Atención a incidencia: " + (at.getIncidencia() != null ? at.getIncidencia().getCodigo() : "S/N"))
                    .fecha(at.getFechaHoraAsignacion())
                    .prioridad("ALTA")
                    .prioridadBadge(obtenerBadgePrioridad("ALTA"))
                    .estadoActual(at.getEstado().name())
                    .estadoBadge(at.getEstado() == EstadoAtencionIncidencia.EN_ESPERA ? "bg-amber-500/10 text-amber-400 border-amber-500/20" : "bg-purple-500/10 text-purple-400 border-purple-500/20")
                    .sector(at.getIncidencia() != null && at.getIncidencia().getSector() != null ? at.getIncidencia().getSector().getNombre() : "General")
                    .motivoPendiente(at.getEstado() == EstadoAtencionIncidencia.EN_ESPERA ? "Atención en espera por verificación de recursos" : "Despacho emitido pendiente de confirmación en campo")
                    .responsable(at.getUnidadMovil() != null ? at.getUnidadMovil().getCodigoInterno() : "Personal motorizado")
                    .urlDetalle("/dashboard/serenazgo/atenciones/" + at.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=ATENCION&id=" + at.getId())
                    .build());
        }

        // 4. Intervenciones pendientes de validación o con observaciones
        List<IntervencionSerenazgo> intervenciones = intervencionRepository.findAll().stream()
                .filter(i -> i.getEstado() == EstadoIntervencionSerenazgo.REGISTRADA || i.getEstado() == EstadoIntervencionSerenazgo.OBSERVADA)
                .limit(20)
                .toList();
        for (IntervencionSerenazgo inter : intervenciones) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(inter.getId())
                    .tipoRegistro(TipoRegistroSupervision.INTERVENCION)
                    .codigo(inter.getCodigo())
                    .titulo("Intervención: " + (inter.getTipoIntervencion() != null ? inter.getTipoIntervencion().getDescripcion() : "Operativa"))
                    .fecha(inter.getFechaInicio())
                    .prioridad(inter.getIncidencia() != null && inter.getIncidencia().getPrioridad() != null ? inter.getIncidencia().getPrioridad().name() : "MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad(inter.getIncidencia() != null && inter.getIncidencia().getPrioridad() != null ? inter.getIncidencia().getPrioridad().name() : "MEDIA"))
                    .estadoActual(inter.getEstado().name())
                    .estadoBadge(inter.getEstado() == EstadoIntervencionSerenazgo.OBSERVADA ? "bg-rose-500/10 text-rose-400 border-rose-500/20" : "bg-blue-500/10 text-blue-400 border-blue-500/20")
                    .sector(inter.getSector() != null ? inter.getSector().getNombre() : "Quinua")
                    .motivoPendiente(inter.getEstado() == EstadoIntervencionSerenazgo.OBSERVADA ? "Intervención con observaciones formales pendientes" : "Intervención registrada pendiente de validación técnica")
                    .responsable(inter.getResponsable() != null ? inter.getResponsable().getCodigoInterno() : "Sereno a cargo")
                    .urlDetalle("/dashboard/serenazgo/intervenciones/" + inter.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=INTERVENCION&id=" + inter.getId())
                    .build());
        }

        // 5. Patrullajes pausados
        List<PatrullajeSerenazgo> patrullajesPausados = patrullajeRepository.findByEstado(EstadoPatrullajeSerenazgo.EN_PAUSA);
        for (PatrullajeSerenazgo p : patrullajesPausados) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(p.getId())
                    .tipoRegistro(TipoRegistroSupervision.PATRULLAJE)
                    .codigo(p.getCodigo())
                    .titulo("Patrullaje en pausa: " + (p.getRuta() != null ? p.getRuta().getNombre() : "Ruta sectorial"))
                    .fecha(p.getFechaInicio())
                    .prioridad("MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad("MEDIA"))
                    .estadoActual(p.getEstado().name())
                    .estadoBadge("bg-amber-500/10 text-amber-400 border-amber-500/20")
                    .sector(p.getSector() != null ? p.getSector().getNombre() : "Sector Serenazgo")
                    .motivoPendiente("Patrullaje pausado en campo. Verificar motivo reportado")
                    .responsable(p.getResponsable() != null ? p.getResponsable().getCodigoInterno() : "Patrulla")
                    .urlDetalle("/dashboard/serenazgo/patrullajes/" + p.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=PATRULLAJE&id=" + p.getId())
                    .build());
        }

        // 6. Documentos observados o con hash no coincidente
        List<DocumentoSeguridadCiudadana> docObs = documentoRepository.findAll().stream()
                .filter(d -> d.getEstado() == EstadoDocumentoSeguridad.OBSERVADO || d.getUltimoResultadoVerificacion() == ResultadoVerificacionIntegridad.NO_COINCIDENTE)
                .limit(20)
                .toList();
        for (DocumentoSeguridadCiudadana doc : docObs) {
            boolean falloHash = doc.getUltimoResultadoVerificacion() == ResultadoVerificacionIntegridad.NO_COINCIDENTE;
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(doc.getId())
                    .tipoRegistro(TipoRegistroSupervision.DOCUMENTO)
                    .codigo(doc.getCodigo())
                    .titulo(doc.getNombreOriginal() != null ? doc.getNombreOriginal() : "Archivo digital")
                    .fecha(doc.getFechaHoraCarga())
                    .prioridad(falloHash ? "ALTA" : "MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad(falloHash ? "ALTA" : "MEDIA"))
                    .estadoActual(doc.getEstado().name())
                    .estadoBadge(falloHash ? "bg-rose-500/10 text-rose-400 border-rose-500/20" : "bg-amber-500/10 text-amber-400 border-amber-500/20")
                    .sector("Acervo digital")
                    .motivoPendiente(falloHash ? "ALERTA TÉCNICA: Verificación de hash SHA-256 no coincidente" : "Documento observado por omisión de requisitos")
                    .responsable(doc.getCreatedBy() != null ? doc.getCreatedBy() : "Usuario carga")
                    .urlDetalle("/dashboard/serenazgo/documentos/" + doc.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/nuevo?tipo=DOCUMENTO&id=" + doc.getId())
                    .build());
        }

        // 7. Seguimientos de supervisión abiertos u observados
        List<SeguimientoSupervisionSerenazgo> seguimientos = seguimientoRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoSeguimientoSupervision.ABIERTO || s.getEstado() == EstadoSeguimientoSupervision.OBSERVADO)
                .limit(20)
                .toList();
        for (SeguimientoSupervisionSerenazgo s : seguimientos) {
            pendientes.add(BandejaPendienteItemDto.builder()
                    .registroId(s.getId())
                    .tipoRegistro(s.getTipoRegistroOrigen())
                    .codigo(s.getCodigo())
                    .titulo(s.getDescripcion())
                    .fecha(s.getFechaCreacion())
                    .prioridad(s.getPrioridadAdministrativa() != null ? s.getPrioridadAdministrativa().name() : "MEDIA")
                    .prioridadBadge(obtenerBadgePrioridad(s.getPrioridadAdministrativa() != null ? s.getPrioridadAdministrativa().name() : "MEDIA"))
                    .estadoActual(s.getEstado().name())
                    .estadoBadge(s.getEstado().getBadgeClass())
                    .sector(s.getOficinaResponsableNombre() != null ? s.getOficinaResponsableNombre() : "Supervisión Operativa")
                    .motivoPendiente("Seguimiento de supervisión: " + s.getTipoSeguimiento().getDescripcion())
                    .responsable(s.getResponsableAsignadoNombre() != null ? s.getResponsableAsignadoNombre() : "Sin asignar")
                    .urlDetalle("/dashboard/serenazgo/supervision/seguimientos/" + s.getId())
                    .urlAccionSupervision("/dashboard/serenazgo/supervision/seguimientos/" + s.getId())
                    .build());
        }

        // Ordenar por fecha descendente
        pendientes.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return b.getFecha().compareTo(a.getFecha());
        });

        return pendientes;
    }

    @Override
    @Transactional(readOnly = true)
    public GraficosSupervisionDto obtenerGraficos(FiltroSupervisionRequest filtro, CustomUserDetails user) {
        Map<String, Long> incPorEstado = new LinkedHashMap<>();
        for (EstadoIncidenciaSeguridad e : EstadoIncidenciaSeguridad.values()) {
            incPorEstado.put(e.name(), incidenciaRepository.countByEstado(e));
        }

        Map<String, Long> patPorEstado = new LinkedHashMap<>();
        for (EstadoPatrullajeSerenazgo e : EstadoPatrullajeSerenazgo.values()) {
            patPorEstado.put(e.name(), patrullajeRepository.countByEstado(e));
        }

        Map<String, Long> interPorTipo = new LinkedHashMap<>();
        for (TipoIntervencionSerenazgo t : TipoIntervencionSerenazgo.values()) {
            long c = intervencionRepository.findAll().stream()
                    .filter(i -> i.getTipoIntervencion() == t)
                    .count();
            if (c > 0) {
                interPorTipo.put(t.getDescripcion(), c);
            }
        }
        if (interPorTipo.isEmpty()) {
            interPorTipo.put("Sin intervenciones registradas", 0L);
        }

        Map<String, Long> segPorEstado = new LinkedHashMap<>();
        for (EstadoSeguimientoSupervision e : EstadoSeguimientoSupervision.values()) {
            segPorEstado.put(e.getDescripcion(), seguimientoRepository.countByEstado(e));
        }

        Map<String, Long> segPorPrioridad = new LinkedHashMap<>();
        for (PrioridadAdministrativaSupervision p : PrioridadAdministrativaSupervision.values()) {
            segPorPrioridad.put(p.getDescripcion(), seguimientoRepository.countByPrioridadAdministrativa(p));
        }

        Map<String, Long> atPorEstado = new LinkedHashMap<>();
        for (EstadoAtencionIncidencia e : EstadoAtencionIncidencia.values()) {
            atPorEstado.put(e.name(), atencionRepository.countByEstado(e));
        }

        return GraficosSupervisionDto.builder()
                .incidenciasPorEstado(incPorEstado)
                .patrullajesPorEstado(patPorEstado)
                .intervencionesPorTipo(interPorTipo)
                .seguimientosPorEstado(segPorEstado)
                .seguimientosPorPrioridad(segPorPrioridad)
                .atencionesPorEstado(atPorEstado)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeguimientoResponse> listarSeguimientos(FiltroSupervisionRequest filtro, Pageable pageable, CustomUserDetails user) {
        Specification<SeguimientoSupervisionSerenazgo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null) {
                if (filtro.getTipoRegistro() != null) {
                    predicates.add(cb.equal(root.get("tipoRegistroOrigen"), filtro.getTipoRegistro()));
                }
                if (filtro.getEstado() != null && !filtro.getEstado().isBlank()) {
                    predicates.add(cb.equal(root.get("estado"), EstadoSeguimientoSupervision.valueOf(filtro.getEstado())));
                }
                if (filtro.getPrioridad() != null) {
                    predicates.add(cb.equal(root.get("prioridadAdministrativa"), filtro.getPrioridad()));
                }
                if (filtro.getResponsableId() != null) {
                    predicates.add(cb.equal(root.get("responsableAsignado").get("id"), filtro.getResponsableId()));
                }
                if (filtro.getFechaInicio() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaInicio().atStartOfDay(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getFechaFin() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaFin().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime()));
                }
                if (filtro.getTexto() != null && !filtro.getTexto().isBlank()) {
                    String pattern = "%" + filtro.getTexto().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("codigo")), pattern),
                            cb.like(cb.lower(root.get("descripcion")), pattern),
                            cb.like(cb.lower(root.get("supervisorNombre")), pattern),
                            cb.like(cb.lower(root.get("responsableAsignadoNombre")), pattern),
                            cb.like(cb.lower(root.get("registroOrigenCodigo")), pattern)
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        return seguimientoRepository.findAll(spec, pageable).map(s -> mapearSeguimientoResponse(s, ahora));
    }

    @Override
    @Transactional(readOnly = true)
    public SeguimientoDetalleResponse obtenerDetalleSeguimiento(UUID id, CustomUserDetails user) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento solicitado no existe"));

        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        boolean vencido = s.getFechaCompromiso() != null && s.getFechaCompromiso().isBefore(ahora)
                && s.getEstado() != EstadoSeguimientoSupervision.RESUELTO
                && s.getEstado() != EstadoSeguimientoSupervision.CERRADO
                && s.getEstado() != EstadoSeguimientoSupervision.CANCELADO;

        // Cargar observaciones
        List<SeguimientoDetalleResponse.ObservacionItemDto> obsList = observacionRepository.findBySeguimientoIdOrderByFechaDesc(s.getId()).stream()
                .map(o -> SeguimientoDetalleResponse.ObservacionItemDto.builder()
                        .id(o.getId())
                        .tipo(o.getTipo())
                        .usuarioId(o.getUsuario() != null ? o.getUsuario().getId() : null)
                        .usuarioNombre(o.getUsuarioNombre())
                        .usuarioRol(o.getUsuarioRol())
                        .motivo(o.getMotivo())
                        .detalle(o.getDetalle())
                        .estadoResultante(o.getEstadoResultante())
                        .fecha(o.getFecha())
                        .build())
                .collect(Collectors.toList());

        // Cargar historial
        List<SeguimientoDetalleResponse.HistorialItemDto> histList = historialRepository.findBySeguimientoIdOrderByFechaDesc(s.getId()).stream()
                .map(h -> SeguimientoDetalleResponse.HistorialItemDto.builder()
                        .id(h.getId())
                        .usuarioId(h.getUsuario() != null ? h.getUsuario().getId() : null)
                        .usuarioNombre(h.getUsuarioNombre())
                        .accion(h.getAccion() != null ? h.getAccion().getDescripcion() : "Evento")
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .detalle(h.getDetalle())
                        .ipOrigen(h.getIpOrigen())
                        .fecha(h.getFecha())
                        .build())
                .collect(Collectors.toList());

        String urlRegistro = resolverUrlRegistroOrigen(s.getTipoRegistroOrigen(), s.getRegistroOrigenId());
        String resumenRegistro = resolverResumenRegistroOrigen(s.getTipoRegistroOrigen(), s.getRegistroOrigenId());

        return SeguimientoDetalleResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .tipoRegistroOrigen(s.getTipoRegistroOrigen())
                .registroOrigenId(s.getRegistroOrigenId())
                .registroOrigenCodigo(s.getRegistroOrigenCodigo())
                .oficinaResponsableId(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(s.getOficinaResponsableNombre())
                .supervisorId(s.getSupervisor() != null ? s.getSupervisor().getId() : null)
                .supervisorNombre(s.getSupervisorNombre())
                .responsableAsignadoId(s.getResponsableAsignado() != null ? s.getResponsableAsignado().getId() : null)
                .responsableAsignadoNombre(s.getResponsableAsignadoNombre())
                .tipoSeguimiento(s.getTipoSeguimiento())
                .descripcion(s.getDescripcion())
                .estado(s.getEstado())
                .prioridadAdministrativa(s.getPrioridadAdministrativa())
                .fechaCreacion(s.getFechaCreacion())
                .fechaCompromiso(s.getFechaCompromiso())
                .fechaCierre(s.getFechaCierre())
                .resultado(s.getResultado())
                .motivoCancelacion(s.getMotivoCancelacion())
                .oficinaEscalamiento(s.getOficinaEscalamiento())
                .version(s.getVersion())
                .vencido(vencido)
                .urlRegistroOrigen(urlRegistro)
                .resumenRegistroOrigen(resumenRegistro)
                .observaciones(obsList)
                .historial(histList)
                .build();
    }

    @Override
    @Transactional
    public SeguimientoResponse crearSeguimiento(CrearSeguimientoRequest req, CustomUserDetails user, String ip) {
        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado en el sistema"));

        String supervisorNombre = resolverNombreUsuario(supervisor);

        // Generar código único correlativo
        Long correlativo = seguimientoRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now(ZONA_LIMA).getYear();
        String codigo = String.format("SEG-SUP-%d-%05d", anio, correlativo);

        // Resolver contexto del registro origen
        String codigoOrigen = req.getRegistroOrigenCodigo();
        if (codigoOrigen == null || codigoOrigen.isBlank()) {
            codigoOrigen = resolverCodigoRegistroOrigen(req.getTipoRegistroOrigen(), req.getRegistroOrigenId());
        }

        // Oficina
        Oficina oficina = null;
        String oficinaNombre = "Gerencia de Seguridad Ciudadana y Serenazgo";
        if (req.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(req.getOficinaResponsableId()).orElse(null);
            if (oficina != null) {
                oficinaNombre = oficina.getNombre();
            }
        }

        // Responsable asignado
        Usuario responsable = null;
        String responsableNombre = null;
        if (req.getResponsableAsignadoId() != null) {
            responsable = usuarioRepository.findById(req.getResponsableAsignadoId()).orElse(null);
            if (responsable != null) {
                responsableNombre = resolverNombreUsuario(responsable);
            }
        }

        OffsetDateTime fechaComp = req.getFechaCompromiso() != null ?
                req.getFechaCompromiso().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime() : null;

        SeguimientoSupervisionSerenazgo seg = SeguimientoSupervisionSerenazgo.builder()
                .codigo(codigo)
                .tipoRegistroOrigen(req.getTipoRegistroOrigen())
                .registroOrigenId(req.getRegistroOrigenId())
                .registroOrigenCodigo(codigoOrigen)
                .oficinaResponsable(oficina)
                .oficinaResponsableNombre(oficinaNombre)
                .supervisor(supervisor)
                .supervisorNombre(supervisorNombre)
                .responsableAsignado(responsable)
                .responsableAsignadoNombre(responsableNombre)
                .tipoSeguimiento(req.getTipoSeguimiento())
                .descripcion(req.getDescripcion())
                .estado(responsable != null ? EstadoSeguimientoSupervision.EN_REVISION : EstadoSeguimientoSupervision.ABIERTO)
                .prioridadAdministrativa(req.getPrioridadAdministrativa() != null ? req.getPrioridadAdministrativa() : PrioridadAdministrativaSupervision.MEDIA)
                .fechaCreacion(OffsetDateTime.now(ZONA_LIMA))
                .fechaCompromiso(fechaComp)
                .createdBy(user.getUsername())
                .build();

        SeguimientoSupervisionSerenazgo guardado = seguimientoRepository.save(seg);

        // Historial
        registrarHistorial(guardado, supervisor, supervisorNombre, AccionHistorialSupervision.CREACION,
                null, guardado.getEstado().name(), "Apertura de expediente de supervisión sobre " + req.getTipoRegistroOrigen().getDescripcion() + " [" + codigoOrigen + "]", ip);

        log.info("Expediente de supervisión {} creado por supervisor {}", guardado.getCodigo(), supervisor.getUsername());
        return mapearSeguimientoResponse(guardado, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse asignarResponsable(UUID id, AsignarResponsableRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("No se puede asignar responsable a un seguimiento que ya ha sido cerrado o cancelado");
        }

        Usuario resp = usuarioRepository.findById(req.getResponsableId())
                .orElseThrow(() -> new BusinessException("El usuario responsable especificado no existe"));
        String respNombre = resolverNombreUsuario(resp);

        Usuario actualizador = usuarioRepository.findById(user.getId()).orElse(resp);
        String anteriorEstado = s.getEstado().name();

        s.setResponsableAsignado(resp);
        s.setResponsableAsignadoNombre(respNombre);

        if (req.getOficinaId() != null) {
            oficinaRepository.findById(req.getOficinaId()).ifPresent(of -> {
                s.setOficinaResponsable(of);
                s.setOficinaResponsableNombre(of.getNombre());
            });
        }

        if (s.getEstado() == EstadoSeguimientoSupervision.ABIERTO) {
            s.setEstado(EstadoSeguimientoSupervision.EN_REVISION);
        }
        s.setUpdatedBy(user.getUsername());

        seguimientoRepository.save(s);

        // Observación interna
        registrarObservacionInterna(s, actualizador, resolverNombreUsuario(actualizador), TipoObservacionSupervision.REVISION,
                "Asignación de responsable operativo", req.getMotivo() != null && !req.getMotivo().isBlank() ? req.getMotivo() : "Asignado a: " + respNombre, s.getEstado().name());

        registrarHistorial(s, actualizador, resolverNombreUsuario(actualizador), AccionHistorialSupervision.ASIGNACION,
                anteriorEstado, s.getEstado().name(), "Responsable asignado: " + respNombre + ". Motivo: " + req.getMotivo(), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse registrarObservacion(UUID id, RegistrarObservacionRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("No se pueden ingresar observaciones a un expediente concluido");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        String estadoAnt = s.getEstado().name();
        s.setEstado(EstadoSeguimientoSupervision.OBSERVADO);
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.OBSERVACION,
                req.getMotivo(), req.getDetalle(), EstadoSeguimientoSupervision.OBSERVADO.name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.OBSERVACION,
                estadoAnt, EstadoSeguimientoSupervision.OBSERVADO.name(), "Observación registrada: " + req.getMotivo(), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse registrarSubsanacion(UUID id, RegistrarSubsanacionRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() != EstadoSeguimientoSupervision.OBSERVADO && s.getEstado() != EstadoSeguimientoSupervision.EN_REVISION) {
            throw new BusinessException("Solo se pueden ingresar subsanaciones en seguimientos con estado OBSERVADO o EN REVISIÓN");
        }

        Usuario resp = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario no autenticado"));
        String respNombre = resolverNombreUsuario(resp);

        String estadoAnt = s.getEstado().name();
        s.setEstado(EstadoSeguimientoSupervision.EN_SUBSANACION);
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, resp, respNombre, TipoObservacionSupervision.SUBSANACION,
                req.getMotivo(), req.getDetalle(), EstadoSeguimientoSupervision.EN_SUBSANACION.name());

        registrarHistorial(s, resp, respNombre, AccionHistorialSupervision.SUBSANACION,
                estadoAnt, EstadoSeguimientoSupervision.EN_SUBSANACION.name(), "Subsanación ingresada: " + req.getMotivo(), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse revisarRespuesta(UUID id, RevisarRespuestaRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() != EstadoSeguimientoSupervision.EN_SUBSANACION && s.getEstado() != EstadoSeguimientoSupervision.EN_REVISION) {
            throw new BusinessException("Solo se pueden revisar respuestas de seguimientos en estado EN SUBSANACIÓN o EN REVISIÓN");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        String estadoAnt = s.getEstado().name();
        EstadoSeguimientoSupervision nuevoEstado = Boolean.TRUE.equals(req.getConforme()) ?
                EstadoSeguimientoSupervision.RESUELTO : EstadoSeguimientoSupervision.OBSERVADO;

        s.setEstado(nuevoEstado);
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.REVISION,
                Boolean.TRUE.equals(req.getConforme()) ? "Subsanación Conforme" : "Subsanación No Conforme",
                req.getObservaciones(), nuevoEstado.name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.REVISION,
                estadoAnt, nuevoEstado.name(), "Revisión técnica de subsanación: " + (Boolean.TRUE.equals(req.getConforme()) ? "Conforme" : "Observada"), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse escalarSeguimiento(UUID id, EscalarSeguimientoRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("No se puede escalar un expediente que ya se encuentra concluido");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        s.setOficinaEscalamiento(req.getOficinaDestinataria());
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.ESCALAMIENTO,
                "Escalamiento a: " + req.getOficinaDestinataria() + " - " + req.getMotivo(),
                req.getObservaciones() != null ? req.getObservaciones() : "Elevación de actuación para conocimiento y competencia funcional",
                s.getEstado().name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.ESCALAMIENTO,
                s.getEstado().name(), s.getEstado().name(), "Escalado a " + req.getOficinaDestinataria() + ". Motivo: " + req.getMotivo(), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse resolverSeguimiento(UUID id, ResolverSeguimientoRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("El expediente ya se encuentra concluido");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        String estadoAnt = s.getEstado().name();
        s.setEstado(EstadoSeguimientoSupervision.RESUELTO);
        s.setResultado(req.getResolucion() + (req.getDisposiciones() != null ? "\nDisposiciones: " + req.getDisposiciones() : ""));
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.DECISION,
                "Resolución Administrativa de Supervisión", req.getResolucion(), EstadoSeguimientoSupervision.RESUELTO.name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.RESOLUCION,
                estadoAnt, EstadoSeguimientoSupervision.RESUELTO.name(), "Resolución administrativa emitida", ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse cerrarSeguimiento(UUID id, CerrarSeguimientoRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("El expediente ya se encuentra concluido");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        String estadoAnt = s.getEstado().name();
        s.setEstado(EstadoSeguimientoSupervision.CERRADO);
        s.setFechaCierre(OffsetDateTime.now(ZONA_LIMA));
        s.setResultado(req.getResultado());
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.DECISION,
                "Cierre Definitivo de Expediente", req.getResultado() + (req.getObservacionesFinales() != null ? "\nObs: " + req.getObservacionesFinales() : ""), EstadoSeguimientoSupervision.CERRADO.name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.CIERRE,
                estadoAnt, EstadoSeguimientoSupervision.CERRADO.name(), "Expediente cerrado y archivado en supervisión", ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional
    public SeguimientoResponse cancelarSeguimiento(UUID id, CancelarSeguimientoRequest req, CustomUserDetails user, String ip) {
        SeguimientoSupervisionSerenazgo s = seguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El expediente de seguimiento no existe"));

        if (s.getEstado() == EstadoSeguimientoSupervision.CERRADO || s.getEstado() == EstadoSeguimientoSupervision.CANCELADO) {
            throw new BusinessException("El expediente ya se encuentra concluido");
        }

        Usuario supervisor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario supervisor no autenticado"));
        String supervisorNombre = resolverNombreUsuario(supervisor);

        String estadoAnt = s.getEstado().name();
        s.setEstado(EstadoSeguimientoSupervision.CANCELADO);
        s.setFechaCierre(OffsetDateTime.now(ZONA_LIMA));
        s.setMotivoCancelacion(req.getMotivo());
        s.setUpdatedBy(user.getUsername());
        seguimientoRepository.save(s);

        registrarObservacionInterna(s, supervisor, supervisorNombre, TipoObservacionSupervision.DECISION,
                "Cancelación de Expediente", "Motivo: " + req.getMotivo(), EstadoSeguimientoSupervision.CANCELADO.name());

        registrarHistorial(s, supervisor, supervisorNombre, AccionHistorialSupervision.CANCELACION,
                estadoAnt, EstadoSeguimientoSupervision.CANCELADO.name(), "Expediente cancelado. Motivo: " + req.getMotivo(), ip);

        return mapearSeguimientoResponse(s, OffsetDateTime.now(ZONA_LIMA));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportarExcel(FiltroSupervisionRequest filtro, CustomUserDetails user) {
        IndicadoresSupervisionResponse kpis = obtenerIndicadores(filtro, user);
        List<BandejaPendienteItemDto> pendientes = obtenerBandejaPendientes(filtro, user);
        List<SeguimientoSupervisionSerenazgo> seguimientos = seguimientoRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setColor(IndexedColors.WHITE.getIndex());
            fontH.setBold(true);
            headerStyle.setFont(fontH);

            // Hoja 1: Indicadores
            Sheet sheet1 = workbook.createSheet("Indicadores de Supervisión");
            Row r0 = sheet1.createRow(0);
            r0.createCell(0).setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - CENTRO DE SUPERVISIÓN Y MONITOREO");
            Row r1 = sheet1.createRow(1);
            r1.createCell(0).setCellValue("Generado por: " + user.getUsername() + " | Fecha: " + OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA));
            Row r2 = sheet1.createRow(2);
            r2.createCell(0).setCellValue("Ley N° 31297 / Ley N° 27933: Datos administrativos de supervisión municipal. Sin valor probatorio penal ni pericial automático.");

            String[][] datosKpi = {
                    {"Incidencias Registradas en Período", String.valueOf(kpis.getIncidenciasRegistradasPeriodo())},
                    {"Incidencias Pendientes de Revisión", String.valueOf(kpis.getIncidenciasPendientesRevision())},
                    {"Alertas Ciudadanas Pendientes", String.valueOf(kpis.getAlertasPendientes())},
                    {"Atenciones en Curso", String.valueOf(kpis.getAtencionesEnCurso())},
                    {"Atenciones en Espera", String.valueOf(kpis.getAtencionesEnEspera())},
                    {"Intervenciones Pendientes de Validación", String.valueOf(kpis.getIntervencionesPendientesValidacion())},
                    {"Patrullajes Programados", String.valueOf(kpis.getPatrullajesProgramados())},
                    {"Patrullajes en Curso", String.valueOf(kpis.getPatrullajesEnCurso())},
                    {"Patrullajes Pausados", String.valueOf(kpis.getPatrullajesPausados())},
                    {"Documentos en Revisión", String.valueOf(kpis.getDocumentosEnRevision())},
                    {"Documentos Observados", String.valueOf(kpis.getDocumentosObservados())},
                    {"Documentos con Integridad No Coincidente", String.valueOf(kpis.getDocumentosIntegridadNoCoincidente())},
                    {"Seguimientos Abiertos", String.valueOf(kpis.getSeguimientosAbiertos())},
                    {"Seguimientos Observados", String.valueOf(kpis.getSeguimientosObservados())},
                    {"Seguimientos Resueltos", String.valueOf(kpis.getSeguimientosResueltos())},
                    {"Seguimientos Cerrados", String.valueOf(kpis.getSeguimientosCerrados())},
                    {"Personal Programado Hoy", String.valueOf(kpis.getPersonalProgramadoHoy())},
                    {"Unidades Móviles Operativas", String.valueOf(kpis.getUnidadesOperativas())}
            };

            Row rHead = sheet1.createRow(4);
            Cell c1 = rHead.createCell(0); c1.setCellValue("Indicador Operativo"); c1.setCellStyle(headerStyle);
            Cell c2 = rHead.createCell(1); c2.setCellValue("Valor Registrado"); c2.setCellStyle(headerStyle);

            for (int i = 0; i < datosKpi.length; i++) {
                Row r = sheet1.createRow(5 + i);
                r.createCell(0).setCellValue(datosKpi[i][0]);
                r.createCell(1).setCellValue(datosKpi[i][1]);
            }
            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);

            // Hoja 2: Bandeja de Pendientes
            Sheet sheet2 = workbook.createSheet("Bandeja de Pendientes");
            Row rHead2 = sheet2.createRow(0);
            String[] colsP = {"Tipo", "Código", "Título / Motivo", "Fecha", "Prioridad", "Estado", "Sector", "Responsable"};
            for (int i = 0; i < colsP.length; i++) {
                Cell c = rHead2.createCell(i);
                c.setCellValue(colsP[i]);
                c.setCellStyle(headerStyle);
            }
            int rowIdx2 = 1;
            for (BandejaPendienteItemDto item : pendientes) {
                Row r = sheet2.createRow(rowIdx2++);
                r.createCell(0).setCellValue(item.getTipoRegistro() != null ? item.getTipoRegistro().getDescripcion() : "");
                r.createCell(1).setCellValue(item.getCodigo());
                r.createCell(2).setCellValue(item.getTitulo());
                r.createCell(3).setCellValue(item.getFecha() != null ? item.getFecha().format(FORMATO_FECHA_HORA) : "");
                r.createCell(4).setCellValue(item.getPrioridad());
                r.createCell(5).setCellValue(item.getEstadoActual());
                r.createCell(6).setCellValue(item.getSector());
                r.createCell(7).setCellValue(item.getResponsable());
            }
            for (int i = 0; i < colsP.length; i++) sheet2.autoSizeColumn(i);

            // Hoja 3: Seguimientos
            Sheet sheet3 = workbook.createSheet("Seguimientos");
            Row rHead3 = sheet3.createRow(0);
            String[] colsS = {"Código", "Tipo Origen", "Registro Origen", "Supervisor", "Responsable", "Tipo Seguimiento", "Estado", "Prioridad", "Fecha Creación", "Fecha Compromiso"};
            for (int i = 0; i < colsS.length; i++) {
                Cell c = rHead3.createCell(i);
                c.setCellValue(colsS[i]);
                c.setCellStyle(headerStyle);
            }
            int rowIdx3 = 1;
            for (SeguimientoSupervisionSerenazgo s : seguimientos) {
                Row r = sheet3.createRow(rowIdx3++);
                r.createCell(0).setCellValue(s.getCodigo());
                r.createCell(1).setCellValue(s.getTipoRegistroOrigen() != null ? s.getTipoRegistroOrigen().getDescripcion() : "");
                r.createCell(2).setCellValue(s.getRegistroOrigenCodigo());
                r.createCell(3).setCellValue(s.getSupervisorNombre());
                r.createCell(4).setCellValue(s.getResponsableAsignadoNombre() != null ? s.getResponsableAsignadoNombre() : "Sin asignar");
                r.createCell(5).setCellValue(s.getTipoSeguimiento() != null ? s.getTipoSeguimiento().getDescripcion() : "");
                r.createCell(6).setCellValue(s.getEstado() != null ? s.getEstado().getDescripcion() : "");
                r.createCell(7).setCellValue(s.getPrioridadAdministrativa() != null ? s.getPrioridadAdministrativa().getDescripcion() : "");
                r.createCell(8).setCellValue(s.getFechaCreacion().format(FORMATO_FECHA_HORA));
                r.createCell(9).setCellValue(s.getFechaCompromiso() != null ? s.getFechaCompromiso().format(FORMATO_FECHA) : "Sin plazo");
            }
            for (int i = 0; i < colsS.length; i++) sheet3.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return new ByteArrayResource(baos.toByteArray());
        } catch (IOException e) {
            log.error("Error al generar reporte Excel de supervisión: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el archivo Excel de supervisión");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportarPdf(FiltroSupervisionRequest filtro, CustomUserDetails user) {
        IndicadoresSupervisionResponse kpis = obtenerIndicadores(filtro, user);
        List<BandejaPendienteItemDto> pendientes = obtenerBandejaPendientes(filtro, user);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(29, 78, 216));
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font legalFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);

            // Cabecera institucional
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSub = new Paragraph("CENTRO DE SUPERVISIÓN, MONITOREO Y CONTROL OPERATIVO - SERENAZGO", subFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            document.add(pSub);

            Paragraph pMeta = new Paragraph("Generado por: " + user.getUsername() + " | Fecha/Hora: " + OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA), bodyFont);
            pMeta.setAlignment(Element.ALIGN_CENTER);
            pMeta.setSpacingAfter(10);
            document.add(pMeta);

            // Disclaimer Legal
            PdfPTable tLegal = new PdfPTable(1);
            tLegal.setWidthPercentage(100);
            PdfPCell cLegal = new PdfPCell(new Phrase("AVISO LEGAL ADMINISTRATIVO: La presente información corresponde a registros administrativos de supervisión municipal (Ley N° 31297 / Ley N° 27933). El Serenazgo de Quinua no tiene funciones policiales de investigación criminal ni peritaje forense. Este reporte no constituye cadena de custodia legal.", legalFont));
            cLegal.setBackgroundColor(new Color(241, 245, 249));
            cLegal.setPadding(6);
            tLegal.addCell(cLegal);
            tLegal.setSpacingAfter(10);
            document.add(tLegal);

            // Resumen de Indicadores Clave
            document.add(new Paragraph("1. INDICADORES EN TIEMPO DE CONSULTA", subFont));
            PdfPTable tKpis = new PdfPTable(4);
            tKpis.setWidthPercentage(100);
            tKpis.setSpacingBefore(5);
            tKpis.setSpacingAfter(15);

            agregarKpiPdf(tKpis, "Incidencias Período", String.valueOf(kpis.getIncidenciasRegistradasPeriodo()), bodyFont);
            agregarKpiPdf(tKpis, "Incidencias Pendientes", String.valueOf(kpis.getIncidenciasPendientesRevision()), bodyFont);
            agregarKpiPdf(tKpis, "Alertas Ciudadanas", String.valueOf(kpis.getAlertasPendientes()), bodyFont);
            agregarKpiPdf(tKpis, "Atenciones Activas", String.valueOf(kpis.getAtencionesEnCurso()), bodyFont);

            agregarKpiPdf(tKpis, "Patrullajes en Curso", String.valueOf(kpis.getPatrullajesEnCurso()), bodyFont);
            agregarKpiPdf(tKpis, "Patrullajes Pausados", String.valueOf(kpis.getPatrullajesPausados()), bodyFont);
            agregarKpiPdf(tKpis, "Docs. Observados", String.valueOf(kpis.getDocumentosObservados()), bodyFont);
            agregarKpiPdf(tKpis, "Docs. Hash No Coincidente", String.valueOf(kpis.getDocumentosIntegridadNoCoincidente()), bodyFont);

            agregarKpiPdf(tKpis, "Seguimientos Abiertos", String.valueOf(kpis.getSeguimientosAbiertos()), bodyFont);
            agregarKpiPdf(tKpis, "Seguimientos Vencidos", String.valueOf(kpis.getSeguimientosVencidos()), bodyFont);
            agregarKpiPdf(tKpis, "Personal Programado", String.valueOf(kpis.getPersonalProgramadoHoy()), bodyFont);
            agregarKpiPdf(tKpis, "Unidades Operativas", String.valueOf(kpis.getUnidadesOperativas()), bodyFont);
            document.add(tKpis);

            // Resumen de Pendientes Operativos
            document.add(new Paragraph("2. PRINCIPALES ASUNTOS PENDIENTES DE ATENCIÓN OPERATIVA", subFont));
            PdfPTable tPend = new PdfPTable(new float[]{15, 18, 32, 18, 17});
            tPend.setWidthPercentage(100);
            tPend.setSpacingBefore(5);

            String[] hPend = {"Tipo", "Código", "Motivo Pendiente", "Fecha", "Prioridad"};
            for (String h : hPend) {
                PdfPCell ch = new PdfPCell(new Phrase(h, headerFont));
                ch.setBackgroundColor(new Color(29, 78, 216));
                ch.setPadding(4);
                tPend.addCell(ch);
            }

            int count = 0;
            for (BandejaPendienteItemDto item : pendientes) {
                if (++count > 25) break;
                tPend.addCell(new Phrase(item.getTipoRegistro() != null ? item.getTipoRegistro().getDescripcion() : "", bodyFont));
                tPend.addCell(new Phrase(item.getCodigo(), bodyFont));
                tPend.addCell(new Phrase(item.getMotivoPendiente(), bodyFont));
                tPend.addCell(new Phrase(item.getFecha() != null ? item.getFecha().format(FORMATO_FECHA) : "", bodyFont));
                tPend.addCell(new Phrase(item.getPrioridad(), bodyFont));
            }
            document.add(tPend);

            document.close();
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error al exportar reporte PDF de supervisión: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el informe PDF de supervisión");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource generarFichaSeguimientoPdf(UUID id, CustomUserDetails user) {
        SeguimientoDetalleResponse det = obtenerDetalleSeguimiento(id, user);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216));
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font legalFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA\nEXPEDIENTE DE SUPERVISIÓN OPERATIVA DE SERENAZGO", titleFont);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            pHeader.setSpacingAfter(8);
            document.add(pHeader);

            PdfPTable tLegal = new PdfPTable(1);
            tLegal.setWidthPercentage(100);
            PdfPCell cLegal = new PdfPCell(new Phrase("Aviso: Documento de supervisión administrativa municipal bajo el marco de la Ley N° 31297. No constituye atestado policial ni acta de prueba penal.", legalFont));
            cLegal.setBackgroundColor(new Color(241, 245, 249));
            cLegal.setPadding(4);
            tLegal.addCell(cLegal);
            tLegal.setSpacingAfter(8);
            document.add(tLegal);

            // Datos Generales
            PdfPTable tGen = new PdfPTable(4);
            tGen.setWidthPercentage(100);
            tGen.setSpacingAfter(10);

            agregarCeldaClaveValor(tGen, "Código Expediente:", det.getCodigo(), bodyFont);
            agregarCeldaClaveValor(tGen, "Estado Actual:", det.getEstado().getDescripcion(), bodyFont);
            agregarCeldaClaveValor(tGen, "Registro Origen:", det.getTipoRegistroOrigen().getDescripcion() + " [" + det.getRegistroOrigenCodigo() + "]", bodyFont);
            agregarCeldaClaveValor(tGen, "Prioridad:", det.getPrioridadAdministrativa().getDescripcion(), bodyFont);
            agregarCeldaClaveValor(tGen, "Supervisor Responsable:", det.getSupervisorNombre(), bodyFont);
            agregarCeldaClaveValor(tGen, "Responsable Asignado:", det.getResponsableAsignadoNombre() != null ? det.getResponsableAsignadoNombre() : "Sin asignar", bodyFont);
            agregarCeldaClaveValor(tGen, "Fecha Apertura:", det.getFechaCreacion().format(FORMATO_FECHA_HORA), bodyFont);
            agregarCeldaClaveValor(tGen, "Fecha Compromiso:", det.getFechaCompromiso() != null ? det.getFechaCompromiso().format(FORMATO_FECHA) : "Sin plazo", bodyFont);
            document.add(tGen);

            // Descripción
            document.add(new Paragraph("MOTIVO Y DESCRIPCIÓN DEL SEGUIMIENTO:", subFont));
            Paragraph pDesc = new Paragraph(det.getDescripcion(), bodyFont);
            pDesc.setSpacingAfter(10);
            document.add(pDesc);

            // Observaciones
            if (det.getObservaciones() != null && !det.getObservaciones().isEmpty()) {
                document.add(new Paragraph("OBSERVACIONES Y SUBSANACIONES:", subFont));
                PdfPTable tObs = new PdfPTable(new float[]{15, 25, 45, 15});
                tObs.setWidthPercentage(100);
                tObs.setSpacingBefore(4);
                tObs.setSpacingAfter(10);

                String[] hObs = {"Tipo", "Usuario", "Motivo y Detalle", "Fecha"};
                for (String h : hObs) {
                    PdfPCell ch = new PdfPCell(new Phrase(h, headerFont));
                    ch.setBackgroundColor(new Color(29, 78, 216));
                    ch.setPadding(3);
                    tObs.addCell(ch);
                }
                for (SeguimientoDetalleResponse.ObservacionItemDto o : det.getObservaciones()) {
                    tObs.addCell(new Phrase(o.getTipo().getDescripcion(), bodyFont));
                    tObs.addCell(new Phrase(o.getUsuarioNombre(), bodyFont));
                    tObs.addCell(new Phrase(o.getMotivo() + "\n" + o.getDetalle(), bodyFont));
                    tObs.addCell(new Phrase(o.getFecha().format(FORMATO_FECHA), bodyFont));
                }
                document.add(tObs);
            }

            // Historial
            if (det.getHistorial() != null && !det.getHistorial().isEmpty()) {
                document.add(new Paragraph("HISTORIAL DE TRAZABILIDAD:", subFont));
                PdfPTable tHist = new PdfPTable(new float[]{20, 25, 40, 15});
                tHist.setWidthPercentage(100);
                tHist.setSpacingBefore(4);

                String[] hHist = {"Acción", "Usuario", "Detalle", "Fecha"};
                for (String h : hHist) {
                    PdfPCell ch = new PdfPCell(new Phrase(h, headerFont));
                    ch.setBackgroundColor(new Color(51, 65, 85));
                    ch.setPadding(3);
                    tHist.addCell(ch);
                }
                for (SeguimientoDetalleResponse.HistorialItemDto h : det.getHistorial()) {
                    tHist.addCell(new Phrase(h.getAccion(), bodyFont));
                    tHist.addCell(new Phrase(h.getUsuarioNombre(), bodyFont));
                    tHist.addCell(new Phrase(h.getDetalle() != null ? h.getDetalle() : "-", bodyFont));
                    tHist.addCell(new Phrase(h.getFecha().format(FORMATO_FECHA), bodyFont));
                }
                document.add(tHist);
            }

            document.close();
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar ficha PDF de seguimiento: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar la ficha PDF de seguimiento");
        }
    }

    // Métodos auxiliares de mapeo y resolución
    private void agregarKpiPdf(PdfPTable table, String titulo, String valor, Font font) {
        PdfPCell c = new PdfPCell();
        c.setPadding(5);
        c.addElement(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY)));
        c.addElement(new Paragraph(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(29, 78, 216))));
        table.addCell(c);
    }

    private void agregarCeldaClaveValor(PdfPTable table, String clave, String valor, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(clave, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY)));
        c1.setBackgroundColor(new Color(248, 250, 252));
        c1.setPadding(3);
        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", font));
        c2.setPadding(3);
        table.addCell(c1);
        table.addCell(c2);
    }

    private OffsetDateTime[] resolverRangoFechas(FiltroSupervisionRequest filtro, OffsetDateTime ahora) {
        String periodo = (filtro != null && filtro.getPeriodo() != null) ? filtro.getPeriodo().toUpperCase() : "HOY";
        OffsetDateTime desde;
        OffsetDateTime hasta = ahora;

        switch (periodo) {
            case "ULTIMOS_7_DIAS" -> desde = ahora.minusDays(7).withHour(0).withMinute(0).withSecond(0);
            case "MES_ACTUAL" -> desde = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case "PERSONALIZADO" -> {
                desde = filtro.getFechaInicio() != null ? filtro.getFechaInicio().atStartOfDay(ZONA_LIMA).toOffsetDateTime() : ahora.minusDays(30);
                hasta = filtro.getFechaFin() != null ? filtro.getFechaFin().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime() : ahora;
            }
            default -> desde = ahora.withHour(0).withMinute(0).withSecond(0);
        }
        return new OffsetDateTime[]{desde, hasta};
    }

    private String describirPeriodo(FiltroSupervisionRequest filtro, OffsetDateTime desde, OffsetDateTime hasta) {
        String periodo = (filtro != null && filtro.getPeriodo() != null) ? filtro.getPeriodo().toUpperCase() : "HOY";
        return switch (periodo) {
            case "ULTIMOS_7_DIAS" -> "Últimos 7 días (" + desde.format(FORMATO_FECHA) + " al " + hasta.format(FORMATO_FECHA) + ")";
            case "MES_ACTUAL" -> "Mes actual (" + desde.format(FORMATO_FECHA) + " al " + hasta.format(FORMATO_FECHA) + ")";
            case "PERSONALIZADO" -> "Intervalo personalizado (" + desde.format(FORMATO_FECHA) + " al " + hasta.format(FORMATO_FECHA) + ")";
            default -> "Hoy (" + ahoraFechaFormateada() + ")";
        };
    }

    private String ahoraFechaFormateada() {
        return LocalDate.now(ZONA_LIMA).format(FORMATO_FECHA);
    }

    private String obtenerBadgePrioridad(String prioridad) {
        return switch (prioridad.toUpperCase()) {
            case "ALTA" -> "bg-rose-500/10 text-rose-400 border-rose-500/20";
            case "BAJA" -> "bg-emerald-500/10 text-emerald-400 border-emerald-500/20";
            default -> "bg-amber-500/10 text-amber-400 border-amber-500/20";
        };
    }

    private SeguimientoResponse mapearSeguimientoResponse(SeguimientoSupervisionSerenazgo s, OffsetDateTime ahora) {
        boolean vencido = s.getFechaCompromiso() != null && s.getFechaCompromiso().isBefore(ahora)
                && s.getEstado() != EstadoSeguimientoSupervision.RESUELTO
                && s.getEstado() != EstadoSeguimientoSupervision.CERRADO
                && s.getEstado() != EstadoSeguimientoSupervision.CANCELADO;

        return SeguimientoResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .tipoRegistroOrigen(s.getTipoRegistroOrigen())
                .registroOrigenId(s.getRegistroOrigenId())
                .registroOrigenCodigo(s.getRegistroOrigenCodigo())
                .oficinaResponsableId(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(s.getOficinaResponsableNombre())
                .supervisorId(s.getSupervisor() != null ? s.getSupervisor().getId() : null)
                .supervisorNombre(s.getSupervisorNombre())
                .responsableAsignadoId(s.getResponsableAsignado() != null ? s.getResponsableAsignado().getId() : null)
                .responsableAsignadoNombre(s.getResponsableAsignadoNombre())
                .tipoSeguimiento(s.getTipoSeguimiento())
                .descripcion(s.getDescripcion())
                .estado(s.getEstado())
                .prioridadAdministrativa(s.getPrioridadAdministrativa())
                .fechaCreacion(s.getFechaCreacion())
                .fechaCompromiso(s.getFechaCompromiso())
                .fechaCierre(s.getFechaCierre())
                .resultado(s.getResultado())
                .oficinaEscalamiento(s.getOficinaEscalamiento())
                .version(s.getVersion())
                .totalObservaciones(s.getObservaciones() != null ? s.getObservaciones().size() : 0)
                .vencido(vencido)
                .build();
    }

    private String resolverNombreUsuario(Usuario u) {
        if (u == null) return "Usuario Municipal";
        if (u.getPersona() != null) {
            String nom = (u.getPersona().getNombres() != null ? u.getPersona().getNombres() : "") + " " +
                    (u.getPersona().getApellidoPaterno() != null ? u.getPersona().getApellidoPaterno() : "");
            if (!nom.isBlank()) return nom.trim();
        }
        return u.getUsername();
    }

    private String resolverCodigoRegistroOrigen(TipoRegistroSupervision tipo, UUID id) {
        if (tipo == null || id == null) return "S/N";
        try {
            return switch (tipo) {
                case INCIDENCIA -> incidenciaRepository.findById(id).map(IncidenciaSeguridadCiudadana::getCodigo).orElse("INC-" + id.toString().substring(0, 8));
                case ALERTA -> alertaRepository.findById(id).map(AlertaCiudadanaSeguridad::getCodigo).orElse("ALE-" + id.toString().substring(0, 8));
                case ATENCION -> atencionRepository.findById(id).map(AtencionIncidenciaSerenazgo::getCodigo).orElse("ATN-" + id.toString().substring(0, 8));
                case INTERVENCION -> intervencionRepository.findById(id).map(IntervencionSerenazgo::getCodigo).orElse("INT-" + id.toString().substring(0, 8));
                case PATRULLAJE -> patrullajeRepository.findById(id).map(PatrullajeSerenazgo::getCodigo).orElse("PAT-" + id.toString().substring(0, 8));
                case DOCUMENTO -> documentoRepository.findById(id).map(DocumentoSeguridadCiudadana::getCodigo).orElse("DOC-" + id.toString().substring(0, 8));
                default -> "REG-" + id.toString().substring(0, 8);
            };
        } catch (Exception e) {
            return "REG-" + id.toString().substring(0, 8);
        }
    }

    private String resolverUrlRegistroOrigen(TipoRegistroSupervision tipo, UUID id) {
        if (tipo == null || id == null) return "#";
        return switch (tipo) {
            case INCIDENCIA -> "/dashboard/serenazgo/incidencias/" + id;
            case ALERTA -> "/dashboard/serenazgo/alertas/" + id;
            case ATENCION -> "/dashboard/serenazgo/atenciones/" + id;
            case INTERVENCION -> "/dashboard/serenazgo/intervenciones/" + id;
            case PATRULLAJE -> "/dashboard/serenazgo/patrullajes/" + id;
            case DOCUMENTO -> "/dashboard/serenazgo/documentos/" + id;
            default -> "#";
        };
    }

    private String resolverResumenRegistroOrigen(TipoRegistroSupervision tipo, UUID id) {
        if (tipo == null || id == null) return "Sin datos de referencia";
        try {
            return switch (tipo) {
                case INCIDENCIA -> incidenciaRepository.findById(id)
                        .map(i -> "Incidencia: " + i.getTitulo() + " | Estado: " + i.getEstado().name() + " | Sector: " + (i.getSector() != null ? i.getSector().getNombre() : "N/A"))
                        .orElse("Incidencia no encontrada");
                case ALERTA -> alertaRepository.findById(id)
                        .map(a -> "Alerta: " + a.getTitulo() + " | Estado: " + a.getEstado().name())
                        .orElse("Alerta no encontrada");
                case ATENCION -> atencionRepository.findById(id)
                        .map(a -> "Atención de despacho: " + a.getCodigo() + " | Estado: " + a.getEstado().name())
                        .orElse("Atención no encontrada");
                case INTERVENCION -> intervencionRepository.findById(id)
                        .map(i -> "Intervención de serenazgo: " + i.getCodigo() + " | Estado: " + i.getEstado().name())
                        .orElse("Intervención no encontrada");
                case PATRULLAJE -> patrullajeRepository.findById(id)
                        .map(p -> "Patrullaje: " + p.getCodigo() + " | Estado: " + p.getEstado().name())
                        .orElse("Patrullaje no encontrado");
                case DOCUMENTO -> documentoRepository.findById(id)
                        .map(d -> "Evidencia/Documento: " + d.getNombreOriginal() + " | Hash: " + d.getHashSha256().substring(0, 16) + "...")
                        .orElse("Documento no encontrado");
                default -> "Registro operativo complementario";
            };
        } catch (Exception e) {
            return "Detalle de actuación no disponible";
        }
    }

    private void registrarObservacionInterna(SeguimientoSupervisionSerenazgo s, Usuario u, String uNombre,
                                             TipoObservacionSupervision tipo, String motivo, String detalle, String estadoResultante) {
        ObservacionSupervisionSerenazgo obs = ObservacionSupervisionSerenazgo.builder()
                .seguimiento(s)
                .usuario(u)
                .usuarioNombre(uNombre)
                .usuarioRol("SUPERVISOR")
                .tipo(tipo)
                .motivo(motivo)
                .detalle(detalle)
                .estadoResultante(estadoResultante)
                .fecha(OffsetDateTime.now(ZONA_LIMA))
                .build();
        observacionRepository.save(obs);
    }

    private void registrarHistorial(SeguimientoSupervisionSerenazgo s, Usuario u, String uNombre,
                                    AccionHistorialSupervision accion, String estAnt, String estNuevo, String detalle, String ip) {
        HistorialSupervisionSerenazgo h = HistorialSupervisionSerenazgo.builder()
                .seguimiento(s)
                .usuario(u)
                .usuarioNombre(uNombre)
                .accion(accion)
                .estadoAnterior(estAnt)
                .estadoNuevo(estNuevo)
                .detalle(detalle)
                .ipOrigen(ip)
                .fecha(OffsetDateTime.now(ZONA_LIMA))
                .build();
        historialRepository.save(h);
    }
}

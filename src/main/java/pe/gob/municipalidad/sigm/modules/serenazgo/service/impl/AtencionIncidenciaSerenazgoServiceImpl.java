package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AtencionIncidenciaSerenazgoService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtencionIncidenciaSerenazgoServiceImpl implements AtencionIncidenciaSerenazgoService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<EstadoAtencionIncidencia> ESTADOS_FINALIZADOS = List.of(
            EstadoAtencionIncidencia.FINALIZADA,
            EstadoAtencionIncidencia.CANCELADA
    );

    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final AtencionPersonalSerenazgoRepository atencionPersonalRepository;
    private final AtencionEquipamientoSerenazgoRepository atencionEquipamientoRepository;
    private final AtencionEventoSerenazgoRepository atencionEventoRepository;
    private final AtencionEscalamientoSerenazgoRepository atencionEscalamientoRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardAtencionesResponse obtenerMetricas() {
        long pendientesInc = incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.REGISTRADA)
                + incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_EVALUACION);

        long asignadas = atencionRepository.countByEstado(EstadoAtencionIncidencia.ASIGNADA);
        long despachadas = atencionRepository.countByEstado(EstadoAtencionIncidencia.DESPACHADA);
        long enCurso = atencionRepository.countByEstado(EstadoAtencionIncidencia.EN_ATENCION)
                + atencionRepository.countByEstado(EstadoAtencionIncidencia.ACEPTADA);
        long enEspera = atencionRepository.countByEstado(EstadoAtencionIncidencia.EN_ESPERA);
        long finalizadas = atencionRepository.countByEstado(EstadoAtencionIncidencia.FINALIZADA);
        long canceladas = atencionRepository.countByEstado(EstadoAtencionIncidencia.CANCELADA);
        long total = atencionRepository.count();

        return DashboardAtencionesResponse.builder()
                .incidenciasPendientesAsignacion(pendientesInc)
                .atencionesAsignadas(asignadas)
                .atencionesDespachadas(despachadas)
                .atencionesEnCurso(enCurso)
                .atencionesEnEspera(enEspera)
                .atencionesFinalizadas(finalizadas)
                .atencionesCanceladas(canceladas)
                .totalAtenciones(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AtencionResponse> listarAtenciones(AtencionFilter filtro, Pageable pageable) {
        log.debug("Listando atenciones con filtros operativos...");
        Specification<AtencionIncidenciaSerenazgo> spec = crearSpecification(filtro);
        return atencionRepository.findAll(spec, pageable).map(this::mapearResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenciaSeguridadCiudadana> listarIncidenciasPendientes(Pageable pageable) {
        return incidenciaRepository.findByEstado(EstadoIncidenciaSeguridad.REGISTRADA, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AtencionDetailResponse obtenerDetallePorId(UUID id) {
        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Atención operativa no encontrada con ID: " + id));
        return mapearDetalle(atn);
    }

    @Override
    @Transactional(readOnly = true)
    public AtencionDetailResponse obtenerDetallePorCodigo(String codigo) {
        AtencionIncidenciaSerenazgo atn = atencionRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("Atención operativa no encontrada con código: " + codigo));
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse crearAtencion(AtencionCrearRequest request, String username) {
        log.info("Creando orden de atención para incidencia ID: {} por: {}", request.getIncidenciaId(), username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(request.getIncidenciaId())
                .orElseThrow(() -> new IllegalArgumentException("La incidencia especificada no existe."));

        if (inc.getEstado() == EstadoIncidenciaSeguridad.CERRADA || inc.getEstado() == EstadoIncidenciaSeguridad.DESCARTADA) {
            throw new IllegalStateException("No se puede generar atención para una incidencia cerrada o descartada.");
        }

        // Validar si ya tiene una atención activa
        if (atencionRepository.existsByIncidenciaIdAndEstadoNotIn(inc.getId(), ESTADOS_FINALIZADOS)) {
            throw new IllegalStateException("La incidencia " + inc.getCodigo() + " ya cuenta con una atención activa en curso.");
        }

        Usuario usuarioSupervisor = usuarioRepository.findByUsername(username).orElse(null);

        // Generar código único ATN-YYYY-00000
        Long sec = atencionRepository.getNextCorrelativo();
        String codigo = String.format("ATN-%d-%05d", OffsetDateTime.now(ZONA_PERU).getYear(), sec);

        SectorPatrullaje sector = inc.getSector();
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(sector);
        }
        ZonaPatrullaje zona = inc.getZona();
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId()).orElse(zona);
        }

        PrioridadIncidenciaSeguridad prioridad = request.getPrioridadOperativa() != null
                ? request.getPrioridadOperativa() : inc.getPrioridad();

        CanalComunicacionDespacho canal = request.getCanalComunicacion() != null
                ? request.getCanalComunicacion() : CanalComunicacionDespacho.RADIO_TETRA_VHF;

        AtencionIncidenciaSerenazgo atn = AtencionIncidenciaSerenazgo.builder()
                .codigo(codigo)
                .incidencia(inc)
                .oficinaResponsable(inc.getOficinaResponsable())
                .supervisorAsignador(usuarioSupervisor)
                .sector(sector)
                .zona(zona)
                .prioridadOperativa(prioridad)
                .canalComunicacion(canal)
                .instruccionesDespacho(request.getInstruccionesDespacho() != null ? request.getInstruccionesDespacho().trim() : null)
                .estado(EstadoAtencionIncidencia.PENDIENTE_ASIGNACION)
                .fechaHoraAsignacion(OffsetDateTime.now(ZONA_PERU))
                .createdBy(username)
                .updatedBy(username)
                .build();

        // 1. Unidad Móvil opcional
        if (request.getUnidadMovilId() != null) {
            UnidadMovilSerenazgo um = validarYObtenerUnidad(request.getUnidadMovilId());
            atn.setUnidadMovil(um);
        }

        // 2. Responsable Operativo opcional
        if (request.getResponsableOperativoId() != null) {
            PersonalSerenazgo resp = validarYObtenerPersonal(request.getResponsableOperativoId());
            atn.setResponsableOperativo(resp);
        }

        atn = atencionRepository.save(atn);

        // 3. Asignar Personal
        Set<UUID> todosPersonalIds = new LinkedHashSet<>();
        if (atn.getResponsableOperativo() != null) {
            todosPersonalIds.add(atn.getResponsableOperativo().getId());
        }
        if (request.getPersonalIds() != null) {
            todosPersonalIds.addAll(request.getPersonalIds());
        }

        if (!todosPersonalIds.isEmpty()) {
            for (UUID perId : todosPersonalIds) {
                PersonalSerenazgo p = validarYObtenerPersonal(perId);
                boolean esResp = atn.getResponsableOperativo() != null && atn.getResponsableOperativo().getId().equals(p.getId());

                AtencionPersonalSerenazgo ap = AtencionPersonalSerenazgo.builder()
                        .atencion(atn)
                        .personal(p)
                        .funcionOperativa(esResp ? FuncionOperativaPersonal.JEFE_GRUPO : FuncionOperativaPersonal.PATRULLERO)
                        .esResponsable(esResp)
                        .activo(true)
                        .fechaAsignacion(OffsetDateTime.now(ZONA_PERU))
                        .build();
                atencionPersonalRepository.save(ap);
            }
            atn.setEstado(EstadoAtencionIncidencia.ASIGNADA);
        }

        // 4. Asignar Equipamiento
        if (request.getEquipamientoIds() != null && !request.getEquipamientoIds().isEmpty()) {
            for (UUID eqId : request.getEquipamientoIds()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(eqId)
                        .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
                AtencionEquipamientoSerenazgo ae = AtencionEquipamientoSerenazgo.builder()
                        .atencion(atn)
                        .equipamiento(eq)
                        .cantidad(1)
                        .devuelto(false)
                        .fechaAsignacion(OffsetDateTime.now(ZONA_PERU))
                        .build();
                atencionEquipamientoRepository.save(ae);
            }
        }

        // 5. Bitácora de Creación
        registrarEvento(atn, TipoEventoAtencion.CREACION_ATENCION,
                "Orden de atención registrada para la incidencia " + inc.getCodigo(),
                null, atn.getEstado().name(), null, usuarioSupervisor, false);

        // 6. Actualizar Incidencia a ASIGNADA si estaba REGISTRADA o EN_EVALUACION
        if (inc.getEstado() == EstadoIncidenciaSeguridad.REGISTRADA || inc.getEstado() == EstadoIncidenciaSeguridad.EN_EVALUACION) {
            inc.setEstado(EstadoIncidenciaSeguridad.ASIGNADA);
            inc.setUpdatedBy(username);
            incidenciaRepository.save(inc);
        }

        // 7. Despacho Inmediato si se solicitó
        if (Boolean.TRUE.equals(request.getDespacharInmediatamente()) && atn.getEstado() == EstadoAtencionIncidencia.ASIGNADA) {
            atn.setEstado(EstadoAtencionIncidencia.DESPACHADA);
            atn.setFechaHoraDespacho(OffsetDateTime.now(ZONA_PERU));
            registrarEvento(atn, TipoEventoAtencion.DESPACHO_OPERATIVO,
                    "Despacho operativo inmediato emitido por " + username,
                    EstadoAtencionIncidencia.ASIGNADA.name(), EstadoAtencionIncidencia.DESPACHADA.name(),
                    atn.getResponsableOperativo(), usuarioSupervisor, true);
        }

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse asignarRecursos(UUID atencionId, AtencionAsignarRecursosRequest request, String username) {
        log.info("Asignando recursos a atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() == EstadoAtencionIncidencia.FINALIZADA || atn.getEstado() == EstadoAtencionIncidencia.CANCELADA) {
            throw new IllegalStateException("No se pueden asignar recursos a una atención finalizada o cancelada.");
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // 1. Unidad móvil
        if (request.getUnidadMovilId() != null) {
            UnidadMovilSerenazgo um = validarYObtenerUnidad(request.getUnidadMovilId());
            atn.setUnidadMovil(um);
        }

        // 2. Responsable Operativo
        if (request.getResponsableOperativoId() != null) {
            PersonalSerenazgo resp = validarYObtenerPersonal(request.getResponsableOperativoId());
            atn.setResponsableOperativo(resp);
        }

        // 3. Personal
        if (request.getPersonalIds() != null && !request.getPersonalIds().isEmpty()) {
            for (int i = 0; i < request.getPersonalIds().size(); i++) {
                UUID perId = request.getPersonalIds().get(i);
                PersonalSerenazgo p = validarYObtenerPersonal(perId);

                // Comprobar si ya estaba asignado
                Optional<AtencionPersonalSerenazgo> existente = atencionPersonalRepository
                        .findByAtencionIdAndPersonalIdAndActivoTrue(atn.getId(), perId);

                if (existente.isEmpty()) {
                    FuncionOperativaPersonal funcion = (request.getFunciones() != null && request.getFunciones().size() > i)
                            ? request.getFunciones().get(i) : FuncionOperativaPersonal.PATRULLERO;
                    boolean esResp = atn.getResponsableOperativo() != null && atn.getResponsableOperativo().getId().equals(p.getId());

                    AtencionPersonalSerenazgo ap = AtencionPersonalSerenazgo.builder()
                            .atencion(atn)
                            .personal(p)
                            .funcionOperativa(funcion)
                            .esResponsable(esResp)
                            .activo(true)
                            .fechaAsignacion(OffsetDateTime.now(ZONA_PERU))
                            .build();
                    atencionPersonalRepository.save(ap);
                }
            }
        }

        // 4. Equipamiento
        if (request.getEquipamientoIds() != null && !request.getEquipamientoIds().isEmpty()) {
            for (UUID eqId : request.getEquipamientoIds()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(eqId)
                        .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
                AtencionEquipamientoSerenazgo ae = AtencionEquipamientoSerenazgo.builder()
                        .atencion(atn)
                        .equipamiento(eq)
                        .cantidad(1)
                        .devuelto(false)
                        .fechaAsignacion(OffsetDateTime.now(ZONA_PERU))
                        .build();
                atencionEquipamientoRepository.save(ae);
            }
        }

        if (StringUtils.hasText(request.getInstruccionesDespacho())) {
            atn.setInstruccionesDespacho(request.getInstruccionesDespacho().trim());
        }
        if (request.getCanalComunicacion() != null) {
            atn.setCanalComunicacion(request.getCanalComunicacion());
        }

        String estadoAnterior = atn.getEstado().name();
        if (atn.getEstado() == EstadoAtencionIncidencia.PENDIENTE_ASIGNACION) {
            atn.setEstado(EstadoAtencionIncidencia.ASIGNADA);
        }

        registrarEvento(atn, TipoEventoAtencion.ASIGNACION_RECURSOS,
                "Recursos operativos asignados a la atención por " + username,
                estadoAnterior, atn.getEstado().name(), atn.getResponsableOperativo(), usuario, false);

        if (Boolean.TRUE.equals(request.getDespachar())) {
            atn.setEstado(EstadoAtencionIncidencia.DESPACHADA);
            atn.setFechaHoraDespacho(OffsetDateTime.now(ZONA_PERU));
            registrarEvento(atn, TipoEventoAtencion.DESPACHO_OPERATIVO,
                    "Despacho operativo emitido tras asignación por " + username,
                    EstadoAtencionIncidencia.ASIGNADA.name(), EstadoAtencionIncidencia.DESPACHADA.name(),
                    atn.getResponsableOperativo(), usuario, true);
        }

        atn.setUpdatedBy(username);
        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse despachar(UUID atencionId, AtencionDespacharRequest request, String username) {
        log.info("Despachando atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() != EstadoAtencionIncidencia.PENDIENTE_ASIGNACION && atn.getEstado() != EstadoAtencionIncidencia.ASIGNADA) {
            throw new IllegalStateException("Solo se pueden despachar atenciones en estado PENDIENTE_ASIGNACION o ASIGNADA. Estado actual: " + atn.getEstado());
        }

        List<AtencionPersonalSerenazgo> efectivos = atencionPersonalRepository.findByAtencionIdAndActivoTrue(atn.getId());
        if (efectivos.isEmpty()) {
            throw new IllegalStateException("No se puede despachar la atención sin haber asignado al menos a un efectivo de Serenazgo.");
        }

        if (request != null) {
            if (request.getCanalComunicacion() != null) {
                atn.setCanalComunicacion(request.getCanalComunicacion());
            }
            if (StringUtils.hasText(request.getInstruccionesDespacho())) {
                atn.setInstruccionesDespacho(request.getInstruccionesDespacho().trim());
            }
        }

        String estadoAnt = atn.getEstado().name();
        atn.setEstado(EstadoAtencionIncidencia.DESPACHADA);
        atn.setFechaHoraDespacho(OffsetDateTime.now(ZONA_PERU));
        atn.setUpdatedBy(username);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.DESPACHO_OPERATIVO,
                "Activación operativa y despacho emitido por la Central de Serenazgo. Canal: " + atn.getCanalComunicacion().getDescripcion(),
                estadoAnt, EstadoAtencionIncidencia.DESPACHADA.name(), atn.getResponsableOperativo(), u, true);

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse responderDespacho(UUID atencionId, AtencionAceptarRechazarRequest request, String username) {
        log.info("Respuesta a despacho para atención ID: {} (aceptar={}) por: {}", atencionId, request.getAceptar(), username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() != EstadoAtencionIncidencia.DESPACHADA) {
            throw new IllegalStateException("Solo se puede aceptar o rechazar un despacho en estado DESPACHADA. Estado actual: " + atn.getEstado());
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        if (Boolean.TRUE.equals(request.getAceptar())) {
            atn.setEstado(EstadoAtencionIncidencia.ACEPTADA);
            atn.setFechaHoraAceptacion(OffsetDateTime.now(ZONA_PERU));
            atn.setUpdatedBy(username);

            registrarEvento(atn, TipoEventoAtencion.ACEPTACION_UNIDAD,
                    "La unidad operativa confirmó la recepción y aceptación de la asignación de servicio.",
                    EstadoAtencionIncidencia.DESPACHADA.name(), EstadoAtencionIncidencia.ACEPTADA.name(),
                    atn.getResponsableOperativo(), u, true);
        } else {
            if (!StringUtils.hasText(request.getMotivoRechazo())) {
                throw new IllegalArgumentException("Debe consignar un motivo fundamentado para rechazar el despacho.");
            }
            atn.setEstado(EstadoAtencionIncidencia.PENDIENTE_ASIGNACION);
            atn.setFechaHoraRechazo(OffsetDateTime.now(ZONA_PERU));
            atn.setMotivoRechazo(request.getMotivoRechazo().trim());
            atn.setUpdatedBy(username);

            registrarEvento(atn, TipoEventoAtencion.RECHAZO_UNIDAD,
                    "Despacho rechazado por la unidad operativa. Motivo: " + request.getMotivoRechazo().trim(),
                    EstadoAtencionIncidencia.DESPACHADA.name(), EstadoAtencionIncidencia.PENDIENTE_ASIGNACION.name(),
                    atn.getResponsableOperativo(), u, false);
        }

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse iniciarAtencion(UUID atencionId, AtencionIniciarRequest request, String username) {
        log.info("Iniciando atención en sitio para ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() != EstadoAtencionIncidencia.ACEPTADA && atn.getEstado() != EstadoAtencionIncidencia.DESPACHADA) {
            throw new IllegalStateException("Para iniciar atención la orden debe estar ACEPTADA o DESPACHADA. Estado actual: " + atn.getEstado());
        }

        String estadoAnt = atn.getEstado().name();
        atn.setEstado(EstadoAtencionIncidencia.EN_ATENCION);
        atn.setFechaHoraInicio(OffsetDateTime.now(ZONA_PERU));
        if (request != null && StringUtils.hasText(request.getObservaciones())) {
            atn.setObservacionesOperativas(request.getObservaciones().trim());
        }
        atn.setUpdatedBy(username);

        // Sincronizar estado de Incidencia a EN_ATENCION
        IncidenciaSeguridadCiudadana inc = atn.getIncidencia();
        if (inc.getEstado() != EstadoIncidenciaSeguridad.EN_ATENCION) {
            inc.setEstado(EstadoIncidenciaSeguridad.EN_ATENCION);
            inc.setUpdatedBy(username);
            incidenciaRepository.save(inc);
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.INICIO_ATENCION,
                "Llegada de la unidad al punto e inicio de actuaciones en sitio.",
                estadoAnt, EstadoAtencionIncidencia.EN_ATENCION.name(),
                atn.getResponsableOperativo(), u, true);

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse registrarNovedad(UUID atencionId, AtencionNovedadRequest request, String username) {
        log.info("Registrando novedad para atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        PersonalSerenazgo informante = null;
        if (request.getPersonalId() != null) {
            informante = personalRepository.findById(request.getPersonalId()).orElse(null);
        } else {
            informante = atn.getResponsableOperativo();
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, request.getTipoEvento(),
                request.getDescripcion().trim(),
                atn.getEstado().name(), atn.getEstado().name(),
                informante, u, Boolean.TRUE.equals(request.getVisibleCiudadano()));

        atn.setUpdatedBy(username);
        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse pausarEnEspera(UUID atencionId, AtencionPausarReanudarRequest request, String username) {
        log.info("Poniendo en espera atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() != EstadoAtencionIncidencia.EN_ATENCION) {
            throw new IllegalStateException("Solo se puede pausar una atención que esté EN_ATENCION. Estado actual: " + atn.getEstado());
        }

        if (request == null || !StringUtils.hasText(request.getMotivo())) {
            throw new IllegalArgumentException("Debe indicar el motivo por el cual la atención queda en espera.");
        }

        atn.setEstado(EstadoAtencionIncidencia.EN_ESPERA);
        atn.setMotivoEspera(request.getMotivo().trim());
        atn.setUpdatedBy(username);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.PAUSA_EN_ESPERA,
                "Atención puesta en espera. Motivo: " + request.getMotivo().trim(),
                EstadoAtencionIncidencia.EN_ATENCION.name(), EstadoAtencionIncidencia.EN_ESPERA.name(),
                atn.getResponsableOperativo(), u, false);

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse reanudarAtencion(UUID atencionId, AtencionPausarReanudarRequest request, String username) {
        log.info("Reanudando atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() != EstadoAtencionIncidencia.EN_ESPERA) {
            throw new IllegalStateException("Solo se puede reanudar una atención que esté EN_ESPERA. Estado actual: " + atn.getEstado());
        }

        atn.setEstado(EstadoAtencionIncidencia.EN_ATENCION);
        atn.setMotivoEspera(null);
        atn.setUpdatedBy(username);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.REANUDACION,
                "Reanudación de actuaciones operativas en sitio.",
                EstadoAtencionIncidencia.EN_ESPERA.name(), EstadoAtencionIncidencia.EN_ATENCION.name(),
                atn.getResponsableOperativo(), u, true);

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse reasignar(UUID atencionId, AtencionReasignarRequest request, String username) {
        log.info("Reasignando recursos en atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() == EstadoAtencionIncidencia.FINALIZADA || atn.getEstado() == EstadoAtencionIncidencia.CANCELADA) {
            throw new IllegalStateException("No se pueden reasignar recursos en una atención finalizada o cancelada.");
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        // 1. Reasignar unidad
        if (request.getUnidadNuevaId() != null) {
            UnidadMovilSerenazgo umNueva = validarYObtenerUnidad(request.getUnidadNuevaId());
            String codigoAnt = atn.getUnidadMovil() != null ? atn.getUnidadMovil().getCodigoInterno() : "Sin vehículo";
            atn.setUnidadMovil(umNueva);
            registrarEvento(atn, TipoEventoAtencion.REASIGNACION_UNIDAD,
                    "Sustitución de móvil: Anterior [" + codigoAnt + "] -> Nueva [" + umNueva.getCodigoInterno() + "]. Motivo: " + request.getMotivo().trim(),
                    atn.getEstado().name(), atn.getEstado().name(), atn.getResponsableOperativo(), u, false);
        }

        // 2. Reasignar responsable
        if (request.getResponsableNuevoId() != null) {
            PersonalSerenazgo respNuevo = validarYObtenerPersonal(request.getResponsableNuevoId());
            String antNombre = atn.getResponsableOperativo() != null ? atn.getResponsableOperativo().getCodigoInterno() : "Sin responsable";
            atn.setResponsableOperativo(respNuevo);

            // Ajustar flags de responsable en tabla de personal
            List<AtencionPersonalSerenazgo> lista = atencionPersonalRepository.findByAtencionIdAndActivoTrue(atn.getId());
            for (AtencionPersonalSerenazgo ap : lista) {
                if (ap.getPersonal().getId().equals(respNuevo.getId())) {
                    ap.setEsResponsable(true);
                    ap.setFuncionOperativa(FuncionOperativaPersonal.JEFE_GRUPO);
                } else {
                    ap.setEsResponsable(false);
                }
                atencionPersonalRepository.save(ap);
            }

            registrarEvento(atn, TipoEventoAtencion.CAMBIO_RESPONSABLE,
                    "Cambio de líder operativo: Anterior [" + antNombre + "] -> Nuevo [" + respNuevo.getCodigoInterno() + "]. Motivo: " + request.getMotivo().trim(),
                    atn.getEstado().name(), atn.getEstado().name(), respNuevo, u, false);
        }

        // 3. Sustituir personal individual
        if (request.getPersonalAnteriorId() != null && request.getPersonalNuevoId() != null) {
            PersonalSerenazgo pNuevo = validarYObtenerPersonal(request.getPersonalNuevoId());

            Optional<AtencionPersonalSerenazgo> apAntOpt = atencionPersonalRepository
                    .findByAtencionIdAndPersonalIdAndActivoTrue(atn.getId(), request.getPersonalAnteriorId());

            if (apAntOpt.isPresent()) {
                AtencionPersonalSerenazgo apAnt = apAntOpt.get();
                apAnt.setActivo(false);
                apAnt.setFechaDesasignacion(OffsetDateTime.now(ZONA_PERU));
                apAnt.setMotivoDesasignacion(request.getMotivo().trim());
                atencionPersonalRepository.save(apAnt);

                AtencionPersonalSerenazgo apNuevo = AtencionPersonalSerenazgo.builder()
                        .atencion(atn)
                        .personal(pNuevo)
                        .funcionOperativa(request.getFuncionNueva() != null ? request.getFuncionNueva() : apAnt.getFuncionOperativa())
                        .esResponsable(apAnt.getEsResponsable())
                        .activo(true)
                        .fechaAsignacion(OffsetDateTime.now(ZONA_PERU))
                        .observaciones("Reemplazo de " + apAnt.getPersonal().getCodigoInterno())
                        .build();
                atencionPersonalRepository.save(apNuevo);

                registrarEvento(atn, TipoEventoAtencion.REASIGNACION_PERSONAL,
                        "Sustitución de efectivo: Retirado [" + apAnt.getPersonal().getCodigoInterno() + "] -> Ingresado [" + pNuevo.getCodigoInterno() + "]. Motivo: " + request.getMotivo().trim(),
                        atn.getEstado().name(), atn.getEstado().name(), atn.getResponsableOperativo(), u, false);
            }
        }

        atn.setUpdatedBy(username);
        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse escalar(UUID atencionId, AtencionEscalarRequest request, String username) {
        log.info("Escalando atención ID: {} a: {} por: {}", atencionId, request.getEntidadDestinataria(), username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        AtencionEscalamientoSerenazgo esc = AtencionEscalamientoSerenazgo.builder()
                .atencion(atn)
                .entidadDestinataria(request.getEntidadDestinataria())
                .motivo(request.getMotivo().trim())
                .canalComunicacion(StringUtils.hasText(request.getCanalComunicacion()) ? request.getCanalComunicacion().trim() : "TELEFONO_DIRECTO")
                .referenciaComunicacion(request.getReferenciaComunicacion() != null ? request.getReferenciaComunicacion().trim() : null)
                .contactoNombre(request.getContactoNombre() != null ? request.getContactoNombre().trim() : null)
                .usuario(u)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .confirmacionRecepcion(false)
                .build();
        atencionEscalamientoRepository.save(esc);

        registrarEvento(atn, TipoEventoAtencion.ESCALAMIENTO_INSTITUCIONAL,
                "Coordinación y escalamiento derivado a " + request.getEntidadDestinataria().getDescripcion() + ". Motivo: " + request.getMotivo().trim(),
                atn.getEstado().name(), atn.getEstado().name(), atn.getResponsableOperativo(), u, false);

        atn.setUpdatedBy(username);
        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse finalizarAtencion(UUID atencionId, AtencionFinalizarRequest request, String username) {
        log.info("Finalizando atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() == EstadoAtencionIncidencia.FINALIZADA || atn.getEstado() == EstadoAtencionIncidencia.CANCELADA) {
            throw new IllegalStateException("La atención ya se encuentra en un estado terminal (" + atn.getEstado() + ").");
        }

        String estadoAnt = atn.getEstado().name();
        atn.setEstado(EstadoAtencionIncidencia.FINALIZADA);
        atn.setFechaHoraFinalizacion(OffsetDateTime.now(ZONA_PERU));
        atn.setResultadoAdministrativo(request.getResultadoAdministrativo());
        atn.setMotivoFinalizacion(request.getMotivoFinalizacion().trim());
        if (StringUtils.hasText(request.getObservacionesOperativas())) {
            atn.setObservacionesOperativas(request.getObservacionesOperativas().trim());
        }
        atn.setUpdatedBy(username);

        // Liberar recursos: devolver equipamiento y marcar fin de asignación
        List<AtencionEquipamientoSerenazgo> equipos = atencionEquipamientoRepository.findByAtencionIdAndDevueltoFalse(atn.getId());
        for (AtencionEquipamientoSerenazgo ae : equipos) {
            ae.setDevuelto(true);
            ae.setFechaDevolucion(OffsetDateTime.now(ZONA_PERU));
            atencionEquipamientoRepository.save(ae);
        }

        List<AtencionPersonalSerenazgo> efectivos = atencionPersonalRepository.findByAtencionIdAndActivoTrue(atn.getId());
        for (AtencionPersonalSerenazgo ap : efectivos) {
            ap.setActivo(false);
            ap.setFechaDesasignacion(OffsetDateTime.now(ZONA_PERU));
            ap.setMotivoDesasignacion("Finalización de la orden de atención");
            atencionPersonalRepository.save(ap);
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.FINALIZACION,
                "Atención operativa finalizada con resultado administrativo: " + request.getResultadoAdministrativo().getDescripcion() + ". Resumen: " + request.getMotivoFinalizacion().trim(),
                estadoAnt, EstadoAtencionIncidencia.FINALIZADA.name(), atn.getResponsableOperativo(), u, true);

        // Si se solicitó resolver la incidencia vinculada
        if (Boolean.TRUE.equals(request.getResolverIncidencia())) {
            IncidenciaSeguridadCiudadana inc = atn.getIncidencia();
            inc.setEstado(EstadoIncidenciaSeguridad.RESUELTA);
            inc.setResultadoAtencion("Atención " + atn.getCodigo() + " - " + request.getResultadoAdministrativo().getDescripcion() + ": " + request.getMotivoFinalizacion().trim());
            inc.setFechaCierre(OffsetDateTime.now(ZONA_PERU));
            inc.setUpdatedBy(username);
            incidenciaRepository.save(inc);
        }

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional
    public AtencionDetailResponse cancelarAtencion(UUID atencionId, AtencionCancelarRequest request, String username) {
        log.info("Cancelando atención ID: {} por: {}", atencionId, username);

        AtencionIncidenciaSerenazgo atn = atencionRepository.findById(atencionId)
                .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + atencionId));

        if (atn.getEstado() == EstadoAtencionIncidencia.FINALIZADA || atn.getEstado() == EstadoAtencionIncidencia.CANCELADA) {
            throw new IllegalStateException("No se puede cancelar una atención que ya está " + atn.getEstado());
        }

        String estadoAnt = atn.getEstado().name();
        atn.setEstado(EstadoAtencionIncidencia.CANCELADA);
        atn.setMotivoCancelacion(request.getMotivoCancelacion().trim());
        atn.setFechaHoraFinalizacion(OffsetDateTime.now(ZONA_PERU));
        atn.setUpdatedBy(username);

        // Liberar recursos
        List<AtencionEquipamientoSerenazgo> equipos = atencionEquipamientoRepository.findByAtencionIdAndDevueltoFalse(atn.getId());
        for (AtencionEquipamientoSerenazgo ae : equipos) {
            ae.setDevuelto(true);
            ae.setFechaDevolucion(OffsetDateTime.now(ZONA_PERU));
            atencionEquipamientoRepository.save(ae);
        }

        List<AtencionPersonalSerenazgo> efectivos = atencionPersonalRepository.findByAtencionIdAndActivoTrue(atn.getId());
        for (AtencionPersonalSerenazgo ap : efectivos) {
            ap.setActivo(false);
            ap.setFechaDesasignacion(OffsetDateTime.now(ZONA_PERU));
            ap.setMotivoDesasignacion("Cancelación de la orden de atención");
            atencionPersonalRepository.save(ap);
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        registrarEvento(atn, TipoEventoAtencion.CANCELACION,
                "Cancelación de atención operativa. Motivo: " + request.getMotivoCancelacion().trim(),
                estadoAnt, EstadoAtencionIncidencia.CANCELADA.name(), atn.getResponsableOperativo(), u, false);

        // Si la incidencia estaba ASIGNADA y no quedan más atenciones activas, revertir a EN_EVALUACION
        IncidenciaSeguridadCiudadana inc = atn.getIncidencia();
        if (inc.getEstado() == EstadoIncidenciaSeguridad.ASIGNADA || inc.getEstado() == EstadoIncidenciaSeguridad.EN_ATENCION) {
            boolean quedanOtras = atencionRepository.existsByIncidenciaIdAndEstadoNotIn(inc.getId(), ESTADOS_FINALIZADOS);
            if (!quedanOtras) {
                inc.setEstado(EstadoIncidenciaSeguridad.EN_EVALUACION);
                inc.setUpdatedBy(username);
                incidenciaRepository.save(inc);
            }
        }

        atn = atencionRepository.save(atn);
        return mapearDetalle(atn);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUnidadDisponible(UUID unidadId) {
        if (unidadId == null) return false;
        UnidadMovilSerenazgo um = unidadMovilRepository.findById(unidadId).orElse(null);
        if (um == null || um.getEstadoOperativo() == null || !um.getEstadoOperativo().isDisponiblePatrullaje()) {
            return false;
        }
        return !atencionRepository.existsByUnidadMovilIdAndEstadoNotIn(unidadId, ESTADOS_FINALIZADOS);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPersonalDisponible(UUID personalId) {
        if (personalId == null) return false;
        PersonalSerenazgo p = personalRepository.findById(personalId).orElse(null);
        if (p == null || p.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            return false;
        }
        return !atencionRepository.isPersonalEnAtencionActiva(personalId, ESTADOS_FINALIZADOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalSerenazgo> listarPersonalDisponible() {
        return personalRepository.findTop200ByEstadoOrderByCodigoInternoAsc(EstadoPersonalSerenazgo.ACTIVO).stream()
                .filter(p -> !atencionRepository.isPersonalEnAtencionActiva(p.getId(), ESTADOS_FINALIZADOS))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadMovilSerenazgo> listarUnidadesDisponibles() {
        return unidadMovilRepository.findTop200ByEstadoOperativoInOrderByCodigoInternoAsc(java.util.List.of(EstadoUnidadMovil.DISPONIBLE, EstadoUnidadMovil.OPERATIVA)).stream()
                .filter(u -> u.getEstadoOperativo() != null && u.getEstadoOperativo().isDisponiblePatrullaje())
                .filter(u -> !atencionRepository.existsByUnidadMovilIdAndEstadoNotIn(u.getId(), ESTADOS_FINALIZADOS))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipamientoSerenazgo> listarEquipamientoDisponible() {
        return equipamientoRepository.findTop200ByEstadoOrderByCodigoInternoAsc(EstadoEquipamientoSerenazgo.DISPONIBLE);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(AtencionFilter filtro) {
        List<AtencionResponse> lista = atencionRepository.findAll(crearSpecification(filtro), Sort.by(Sort.Direction.DESC, "fechaHoraAsignacion"))
                .stream().map(this::mapearResumen).collect(Collectors.toList());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, new java.awt.Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, new java.awt.Color(71, 85, 105));
            Font fontAviso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.NORMAL, new java.awt.Color(185, 28, 28));
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.NORMAL, java.awt.Color.WHITE);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, java.awt.Color.BLACK);

            Paragraph p1 = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — HUAMANGA — AYACUCHO", fontTitulo);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("Módulo 3: Seguridad Ciudadana y Serenazgo | Reporte Oficial de Atenciones Operativas", fontSub);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(4);
            document.add(p2);

            Paragraph pAviso = new Paragraph("AVISO OFICIAL: Registro administrativo preventivo de Serenazgo. No constituye peritaje policial ni sustituye al 105 (PNP) ni al SAMU (106).", fontAviso);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(10);
            document.add(pAviso);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10, 10, 10, 10, 12, 10, 12, 10, 16});

            String[] headers = {"Cód. Atención", "Incidencia", "Fecha Asignación", "Prioridad", "Sector", "Estado", "Móvil / Vehículo", "Responsable", "Resultado"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                cell.setBackgroundColor(new java.awt.Color(29, 78, 216));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (AtencionResponse atn : lista) {
                table.addCell(new PdfPCell(new Phrase(atn.getCodigo(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getIncidenciaCodigo(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getFechaHoraAsignacion() != null ? atn.getFechaHoraAsignacion().format(FORMATO_FECHA_HORA) : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getPrioridadOperativaDesc(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getSectorNombre() != null ? atn.getSectorNombre() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getEstadoDesc(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getUnidadMovilCodigo() != null ? atn.getUnidadMovilCodigo() : "A pie / Sin vehículo", fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getResponsableOperativoNombre() != null ? atn.getResponsableOperativoNombre() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(atn.getResultadoAdministrativoDesc() != null ? atn.getResultadoAdministrativoDesc() : "-", fontBody)));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al exportar PDF de atenciones", e);
            throw new RuntimeException("Error al generar PDF de atenciones operativas", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(AtencionFilter filtro) {
        List<AtencionResponse> lista = atencionRepository.findAll(crearSpecification(filtro), Sort.by(Sort.Direction.DESC, "fechaHoraAsignacion"))
                .stream().map(this::mapearResumen).collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Atenciones Serenazgo");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setBold(true);
            fontH.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontH);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {
                    "Código Atención", "Código Incidencia", "Fecha Asignación", "Fecha Despacho", "Fecha Inicio",
                    "Fecha Finalización", "Estado", "Prioridad", "Canal Despacho", "Sector", "Zona",
                    "Unidad Móvil", "Líder / Responsable", "Efectivos Asignados", "Resultado Administrativo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (AtencionResponse atn : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(atn.getCodigo());
                row.createCell(1).setCellValue(atn.getIncidenciaCodigo());
                row.createCell(2).setCellValue(atn.getFechaHoraAsignacion() != null ? atn.getFechaHoraAsignacion().format(FORMATO_FECHA_HORA) : "");
                row.createCell(3).setCellValue(atn.getFechaHoraDespacho() != null ? atn.getFechaHoraDespacho().format(FORMATO_FECHA_HORA) : "");
                row.createCell(4).setCellValue(atn.getFechaHoraInicio() != null ? atn.getFechaHoraInicio().format(FORMATO_FECHA_HORA) : "");
                row.createCell(5).setCellValue(atn.getFechaHoraFinalizacion() != null ? atn.getFechaHoraFinalizacion().format(FORMATO_FECHA_HORA) : "");
                row.createCell(6).setCellValue(atn.getEstadoDesc());
                row.createCell(7).setCellValue(atn.getPrioridadOperativaDesc());
                row.createCell(8).setCellValue(atn.getCanalComunicacionDesc());
                row.createCell(9).setCellValue(atn.getSectorNombre() != null ? atn.getSectorNombre() : "-");
                row.createCell(10).setCellValue(atn.getZonaNombre() != null ? atn.getZonaNombre() : "-");
                row.createCell(11).setCellValue(atn.getUnidadMovilCodigo() != null ? atn.getUnidadMovilCodigo() : "Sin vehículo");
                row.createCell(12).setCellValue(atn.getResponsableOperativoNombre() != null ? atn.getResponsableOperativoNombre() : "-");
                row.createCell(13).setCellValue(atn.getCantidadEfectivos());
                row.createCell(14).setCellValue(atn.getResultadoAdministrativoDesc() != null ? atn.getResultadoAdministrativoDesc() : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar Excel de atenciones", e);
            throw new RuntimeException("Error al generar Excel de atenciones operativas", e);
        }
    }

    // =========================================================================
    // Helpers de Validación y Mapeo
    // =========================================================================

    private UnidadMovilSerenazgo validarYObtenerUnidad(UUID unidadId) {
        UnidadMovilSerenazgo um = unidadMovilRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException("La unidad móvil especificada no existe."));
        if (um.getEstadoOperativo() == null || !um.getEstadoOperativo().isDisponiblePatrullaje()) {
            throw new IllegalStateException("La unidad móvil " + um.getCodigoInterno() + " no está operativa para patrullaje (Estado actual: " + um.getEstadoOperativo() + ").");
        }
        if (atencionRepository.existsByUnidadMovilIdAndEstadoNotIn(unidadId, ESTADOS_FINALIZADOS)) {
            throw new IllegalStateException("La unidad móvil " + um.getCodigoInterno() + " ya se encuentra en servicio en otra atención activa.");
        }
        return um;
    }

    private PersonalSerenazgo validarYObtenerPersonal(UUID personalId) {
        PersonalSerenazgo p = personalRepository.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("El efectivo de Serenazgo no existe."));
        if (p.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            throw new IllegalStateException("El efectivo " + p.getCodigoInterno() + " no se encuentra en estado ACTIVO.");
        }
        if (atencionRepository.isPersonalEnAtencionActiva(personalId, ESTADOS_FINALIZADOS)) {
            throw new IllegalStateException("El efectivo " + p.getCodigoInterno() + " ya se encuentra asignado a otra atención activa en curso.");
        }
        return p;
    }

    private void registrarEvento(AtencionIncidenciaSerenazgo atn,
                                 TipoEventoAtencion tipo,
                                 String descripcion,
                                 String ant,
                                 String nue,
                                 PersonalSerenazgo per,
                                 Usuario usu,
                                 boolean visible) {
        AtencionEventoSerenazgo ev = AtencionEventoSerenazgo.builder()
                .atencion(atn)
                .tipoEvento(tipo)
                .descripcion(descripcion)
                .estadoAnterior(ant)
                .estadoNuevo(nue)
                .personal(per)
                .usuario(usu)
                .visibleCiudadano(visible)
                .fechaEvento(OffsetDateTime.now(ZONA_PERU))
                .build();
        atencionEventoRepository.save(ev);
    }

    private Specification<AtencionIncidenciaSerenazgo> crearSpecification(AtencionFilter filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return cb.conjunction();
            }

            if (StringUtils.hasText(filtro.getTermino())) {
                String term = "%" + filtro.getTermino().trim().toLowerCase() + "%";
                Join<AtencionIncidenciaSerenazgo, IncidenciaSeguridadCiudadana> incJoin = root.join("incidencia", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), term),
                        cb.like(cb.lower(incJoin.get("codigo")), term),
                        cb.like(cb.lower(incJoin.get("titulo")), term),
                        cb.like(cb.lower(incJoin.get("ubicacionReferencia")), term)
                ));
            } else if (StringUtils.hasText(filtro.getCodigo())) {
                String term = "%" + filtro.getCodigo().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("codigo")), term));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getPrioridad() != null) {
                predicates.add(cb.equal(root.get("prioridadOperativa"), filtro.getPrioridad()));
            }

            if (filtro.getSectorId() != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
            }

            if (filtro.getUnidadId() != null) {
                predicates.add(cb.equal(root.get("unidadMovil").get("id"), filtro.getUnidadId()));
            }

            if (filtro.getPersonalId() != null) {
                Join<AtencionIncidenciaSerenazgo, AtencionPersonalSerenazgo> perJoin = root.join("personalAsignado", JoinType.INNER);
                predicates.add(cb.and(
                        cb.equal(perJoin.get("personal").get("id"), filtro.getPersonalId()),
                        cb.isTrue(perJoin.get("activo"))
                ));
            }

            if (filtro.getDesde() != null) {
                OffsetDateTime desdeDt = filtro.getDesde().atStartOfDay(ZONA_PERU).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHoraAsignacion"), desdeDt));
            }

            if (filtro.getHasta() != null) {
                OffsetDateTime hastaDt = filtro.getHasta().plusDays(1).atStartOfDay(ZONA_PERU).toOffsetDateTime();
                predicates.add(cb.lessThan(root.get("fechaHoraAsignacion"), hastaDt));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AtencionResponse mapearResumen(AtencionIncidenciaSerenazgo a) {
        IncidenciaSeguridadCiudadana inc = a.getIncidencia();

        String respNombre = "-";
        if (a.getResponsableOperativo() != null && a.getResponsableOperativo().getPersona() != null) {
            respNombre = a.getResponsableOperativo().getPersona().getNombreCompleto();
        }

        int cantEfectivos = (int) a.getPersonalAsignado().stream().filter(AtencionPersonalSerenazgo::getActivo).count();

        return AtencionResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .incidenciaId(inc.getId())
                .incidenciaCodigo(inc.getCodigo())
                .incidenciaTitulo(inc.getTitulo())
                .incidenciaTipo(inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getNombre() : "-")
                .incidenciaUbicacion(inc.getUbicacionReferencia())
                .sectorId(a.getSector() != null ? a.getSector().getId() : null)
                .sectorNombre(a.getSector() != null ? a.getSector().getNombre() : "-")
                .zonaId(a.getZona() != null ? a.getZona().getId() : null)
                .zonaNombre(a.getZona() != null ? a.getZona().getNombre() : "-")
                .fechaHoraAsignacion(a.getFechaHoraAsignacion())
                .fechaHoraDespacho(a.getFechaHoraDespacho())
                .fechaHoraAceptacion(a.getFechaHoraAceptacion())
                .fechaHoraInicio(a.getFechaHoraInicio())
                .fechaHoraFinalizacion(a.getFechaHoraFinalizacion())
                .estado(a.getEstado())
                .estadoDesc(a.getEstado().getDescripcion())
                .prioridadOperativa(a.getPrioridadOperativa())
                .prioridadOperativaDesc(a.getPrioridadOperativa().getDescripcion())
                .canalComunicacion(a.getCanalComunicacion())
                .canalComunicacionDesc(a.getCanalComunicacion().getDescripcion())
                .unidadMovilId(a.getUnidadMovil() != null ? a.getUnidadMovil().getId() : null)
                .unidadMovilCodigo(a.getUnidadMovil() != null ? a.getUnidadMovil().getCodigoInterno() : null)
                .unidadMovilPlaca(a.getUnidadMovil() != null ? a.getUnidadMovil().getPlaca() : null)
                .responsableOperativoId(a.getResponsableOperativo() != null ? a.getResponsableOperativo().getId() : null)
                .responsableOperativoNombre(respNombre)
                .cantidadEfectivos(cantEfectivos)
                .resultadoAdministrativo(a.getResultadoAdministrativo())
                .resultadoAdministrativoDesc(a.getResultadoAdministrativo() != null ? a.getResultadoAdministrativo().getDescripcion() : null)
                .motivoFinalizacion(a.getMotivoFinalizacion())
                .supervisorAsignadorNombre(a.getSupervisorAsignador() != null ? a.getSupervisorAsignador().getUsername() : "-")
                .build();
    }

    private AtencionDetailResponse mapearDetalle(AtencionIncidenciaSerenazgo a) {
        IncidenciaSeguridadCiudadana inc = a.getIncidencia();

        String respNombre = "-";
        String respCod = "-";
        if (a.getResponsableOperativo() != null) {
            respCod = a.getResponsableOperativo().getCodigoInterno();
            if (a.getResponsableOperativo().getPersona() != null) {
                respNombre = a.getResponsableOperativo().getPersona().getNombreCompleto();
            }
        }

        List<AtencionPersonalResponse> perList = atencionPersonalRepository.findByAtencionIdOrderByFechaAsignacionAsc(a.getId()).stream().map(ap -> {
            String pNom = "-";
            String cNom = "-";
            if (ap.getPersonal() != null) {
                if (ap.getPersonal().getPersona() != null) pNom = ap.getPersonal().getPersona().getNombreCompleto();
                if (ap.getPersonal().getCargoOperativo() != null) cNom = ap.getPersonal().getCargoOperativo().getNombre();
            }
            return AtencionPersonalResponse.builder()
                    .id(ap.getId())
                    .personalId(ap.getPersonal() != null ? ap.getPersonal().getId() : null)
                    .codigoInterno(ap.getPersonal() != null ? ap.getPersonal().getCodigoInterno() : "-")
                    .nombreCompleto(pNom)
                    .cargoNombre(cNom)
                    .funcionOperativa(ap.getFuncionOperativa())
                    .funcionOperativaDesc(ap.getFuncionOperativa().getDescripcion())
                    .esResponsable(ap.getEsResponsable())
                    .fechaAsignacion(ap.getFechaAsignacion())
                    .fechaDesasignacion(ap.getFechaDesasignacion())
                    .motivoDesasignacion(ap.getMotivoDesasignacion())
                    .activo(ap.getActivo())
                    .observaciones(ap.getObservaciones())
                    .build();
        }).collect(Collectors.toList());

        List<AtencionEquipamientoResponse> eqList = atencionEquipamientoRepository.findByAtencionIdOrderByFechaAsignacionAsc(a.getId()).stream().map(ae -> {
            String eCod = "-";
            String eDesc = "-";
            String tDesc = "-";
            if (ae.getEquipamiento() != null) {
                eCod = ae.getEquipamiento().getCodigoInterno();
                eDesc = ae.getEquipamiento().getDescripcion();
                if (ae.getEquipamiento().getTipoEquipo() != null) tDesc = ae.getEquipamiento().getTipoEquipo().getDescripcion();
            }
            return AtencionEquipamientoResponse.builder()
                    .id(ae.getId())
                    .equipamientoId(ae.getEquipamiento() != null ? ae.getEquipamiento().getId() : null)
                    .codigoInterno(eCod)
                    .descripcion(eDesc)
                    .tipoEquipoDesc(tDesc)
                    .cantidad(ae.getCantidad())
                    .fechaAsignacion(ae.getFechaAsignacion())
                    .fechaDevolucion(ae.getFechaDevolucion())
                    .devuelto(ae.getDevuelto())
                    .observaciones(ae.getObservaciones())
                    .build();
        }).collect(Collectors.toList());

        List<AtencionEventoResponse> evList = atencionEventoRepository.findByAtencionIdOrderByFechaEventoAsc(a.getId()).stream()
                .sorted(Comparator.comparing(AtencionEventoSerenazgo::getFechaEvento).reversed())
                .map(ev -> AtencionEventoResponse.builder()
                        .id(ev.getId())
                        .tipoEvento(ev.getTipoEvento())
                        .tipoEventoDesc(ev.getTipoEvento().getDescripcion())
                        .descripcion(ev.getDescripcion())
                        .estadoAnterior(ev.getEstadoAnterior())
                        .estadoNuevo(ev.getEstadoNuevo())
                        .personalNombre(ev.getPersonal() != null && ev.getPersonal().getPersona() != null
                                ? ev.getPersonal().getPersona().getNombreCompleto() : "-")
                        .usuarioNombre(ev.getUsuario() != null ? ev.getUsuario().getUsername() : "-")
                        .fechaEvento(ev.getFechaEvento())
                        .detallesJson(ev.getDetallesJson())
                        .visibleCiudadano(ev.getVisibleCiudadano())
                        .build())
                .collect(Collectors.toList());

        List<AtencionEscalamientoResponse> escList = atencionEscalamientoRepository.findByAtencionIdOrderByFechaEscalamientoDesc(a.getId()).stream()
                .sorted(Comparator.comparing(AtencionEscalamientoSerenazgo::getFechaEscalamiento).reversed())
                .map(esc -> AtencionEscalamientoResponse.builder()
                        .id(esc.getId())
                        .entidadDestinataria(esc.getEntidadDestinataria())
                        .entidadDestinatariaDesc(esc.getEntidadDestinataria().getDescripcion())
                        .motivo(esc.getMotivo())
                        .canalComunicacion(esc.getCanalComunicacion())
                        .referenciaComunicacion(esc.getReferenciaComunicacion())
                        .contactoNombre(esc.getContactoNombre())
                        .fechaEscalamiento(esc.getFechaEscalamiento())
                        .confirmacionRecepcion(esc.getConfirmacionRecepcion())
                        .fechaConfirmacion(esc.getFechaConfirmacion())
                        .usuarioNombre(esc.getUsuario() != null ? esc.getUsuario().getUsername() : "-")
                        .observaciones(esc.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        return AtencionDetailResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .incidenciaId(inc.getId())
                .incidenciaCodigo(inc.getCodigo())
                .incidenciaTitulo(inc.getTitulo())
                .incidenciaDescripcion(inc.getDescripcion())
                .incidenciaTipo(inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getNombre() : "-")
                .incidenciaPrioridad(inc.getPrioridad().getDescripcion())
                .incidenciaUbicacion(inc.getUbicacionReferencia())
                .incidenciaLatitud(inc.getLatitud())
                .incidenciaLongitud(inc.getLongitud())
                .incidenciaFechaReporte(inc.getFechaReporte())
                .incidenciaFechaOcurrencia(inc.getFechaOcurrencia())
                .incidenciaEstado(inc.getEstado().name())
                .sectorId(a.getSector() != null ? a.getSector().getId() : null)
                .sectorNombre(a.getSector() != null ? a.getSector().getNombre() : "-")
                .zonaId(a.getZona() != null ? a.getZona().getId() : null)
                .zonaNombre(a.getZona() != null ? a.getZona().getNombre() : "-")
                .fechaHoraAsignacion(a.getFechaHoraAsignacion())
                .fechaHoraDespacho(a.getFechaHoraDespacho())
                .fechaHoraAceptacion(a.getFechaHoraAceptacion())
                .fechaHoraRechazo(a.getFechaHoraRechazo())
                .fechaHoraInicio(a.getFechaHoraInicio())
                .fechaHoraFinalizacion(a.getFechaHoraFinalizacion())
                .estado(a.getEstado())
                .estadoDesc(a.getEstado().getDescripcion())
                .prioridadOperativa(a.getPrioridadOperativa())
                .prioridadOperativaDesc(a.getPrioridadOperativa().getDescripcion())
                .canalComunicacion(a.getCanalComunicacion())
                .canalComunicacionDesc(a.getCanalComunicacion().getDescripcion())
                .supervisorAsignadorId(a.getSupervisorAsignador() != null ? a.getSupervisorAsignador().getId() : null)
                .supervisorAsignadorNombre(a.getSupervisorAsignador() != null ? a.getSupervisorAsignador().getUsername() : "-")
                .unidadMovilId(a.getUnidadMovil() != null ? a.getUnidadMovil().getId() : null)
                .unidadMovilCodigo(a.getUnidadMovil() != null ? a.getUnidadMovil().getCodigoInterno() : null)
                .unidadMovilPlaca(a.getUnidadMovil() != null ? a.getUnidadMovil().getPlaca() : null)
                .unidadMovilTipo(a.getUnidadMovil() != null && a.getUnidadMovil().getTipoUnidad() != null ? a.getUnidadMovil().getTipoUnidad().name() : null)
                .responsableOperativoId(a.getResponsableOperativo() != null ? a.getResponsableOperativo().getId() : null)
                .responsableOperativoNombre(respNombre)
                .responsableOperativoCodigo(respCod)
                .instruccionesDespacho(a.getInstruccionesDespacho())
                .motivoRechazo(a.getMotivoRechazo())
                .motivoEspera(a.getMotivoEspera())
                .motivoCancelacion(a.getMotivoCancelacion())
                .resultadoAdministrativo(a.getResultadoAdministrativo())
                .resultadoAdministrativoDesc(a.getResultadoAdministrativo() != null ? a.getResultadoAdministrativo().getDescripcion() : null)
                .motivoFinalizacion(a.getMotivoFinalizacion())
                .observacionesOperativas(a.getObservacionesOperativas())
                .personalAsignado(perList)
                .equipamientoAsignado(eqList)
                .eventos(evList)
                .escalamientos(escList)
                .version(a.getVersion())
                .build();
    }
}

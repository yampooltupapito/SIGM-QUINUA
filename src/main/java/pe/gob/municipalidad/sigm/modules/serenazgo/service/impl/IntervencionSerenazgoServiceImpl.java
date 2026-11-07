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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IntervencionSerenazgoService;

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
public class IntervencionSerenazgoServiceImpl implements IntervencionSerenazgoService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IntervencionSerenazgoRepository intervencionRepository;
    private final ParticipanteIntervencionSerenazgoRepository participanteRepository;
    private final IntervencionActuacionRepository actuacionRepository;
    private final IntervencionComunicacionRepository comunicacionRepository;
    private final IntervencionEquipamientoRepository intervencionEquipamientoRepository;
    private final IntervencionDocumentoRepository documentoRepository;
    private final IntervencionHistorialRepository historialRepository;

    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public IntervencionDetailResponse crearIntervencion(IntervencionCrearRequest request, String username) {
        log.info("Iniciando creación de intervención por usuario: {}", username);

        // 1. Validar Incidencia origen
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(request.getIncidenciaId())
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + request.getIncidenciaId()));

        // 2. Validar Atención si fue especificada
        AtencionIncidenciaSerenazgo atn = null;
        if (request.getAtencionId() != null) {
            atn = atencionRepository.findById(request.getAtencionId())
                    .orElseThrow(() -> new NoSuchElementException("Atención no encontrada con ID: " + request.getAtencionId()));
            if (!atn.getIncidencia().getId().equals(inc.getId())) {
                throw new IllegalArgumentException("La orden de atención " + atn.getCodigo()
                        + " no corresponde a la incidencia " + inc.getCodigo());
            }
        }

        // 3. Validar Personal Responsable
        PersonalSerenazgo resp = personalRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new NoSuchElementException("Efectivo de Serenazgo responsable no encontrado"));

        // 4. Validar Unidad Móvil opcional
        UnidadMovilSerenazgo um = null;
        if (request.getUnidadMovilId() != null) {
            um = unidadMovilRepository.findById(request.getUnidadMovilId())
                    .orElseThrow(() -> new NoSuchElementException("Unidad móvil no encontrada"));
        }

        // 5. Validar Sector y Zona
        SectorPatrullaje sec = null;
        if (request.getSectorId() != null) {
            sec = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado"));
        } else if (inc.getSector() != null) {
            sec = inc.getSector();
        }

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));
            if (sec != null && zona.getSector() != null && !zona.getSector().getId().equals(sec.getId())) {
                throw new IllegalArgumentException("La zona seleccionada no pertenece al sector indicado.");
            }
        } else if (inc.getZona() != null) {
            zona = inc.getZona();
        }

        // 6. Fechas y control temporal
        OffsetDateTime fechaIni = request.getFechaInicio() != null
                ? request.getFechaInicio().atZone(ZONA_PERU).toOffsetDateTime()
                : OffsetDateTime.now(ZONA_PERU);

        OffsetDateTime fechaFin = request.getFechaFin() != null
                ? request.getFechaFin().atZone(ZONA_PERU).toOffsetDateTime()
                : null;

        if (fechaFin != null && fechaFin.isBefore(fechaIni)) {
            throw new IllegalArgumentException("La fecha y hora de finalización no puede ser anterior al inicio de la actuación.");
        }

        // 7. Ubicación y coordenadas
        String ubicacion = StringUtils.hasText(request.getUbicacionDescripcion())
                ? request.getUbicacionDescripcion().trim()
                : inc.getUbicacionReferencia();

        if (request.getLatitud() != null && (request.getLatitud() < -90.0 || request.getLatitud() > 90.0)) {
            throw new IllegalArgumentException("Latitud fuera de rango (-90 a 90).");
        }
        if (request.getLongitud() != null && (request.getLongitud() < -180.0 || request.getLongitud() > 180.0)) {
            throw new IllegalArgumentException("Longitud fuera de rango (-180 a 180).");
        }

        // 8. Generar Código Correlativo
        String codigo = generarCodigoIntervencion();

        // 9. Determinar Estado inicial
        EstadoIntervencionSerenazgo estadoInicial = Boolean.TRUE.equals(request.getGuardarComoBorrador())
                ? EstadoIntervencionSerenazgo.BORRADOR
                : EstadoIntervencionSerenazgo.REGISTRADA;

        IntervencionSerenazgo inter = IntervencionSerenazgo.builder()
                .codigo(codigo)
                .incidencia(inc)
                .atencion(atn)
                .tipoIntervencion(request.getTipoIntervencion())
                .fechaInicio(fechaIni)
                .fechaFin(fechaFin)
                .responsable(resp)
                .unidadMovil(um)
                .sector(sec)
                .zona(zona)
                .ubicacionDescripcion(ubicacion)
                .latitud(request.getLatitud() != null ? request.getLatitud() : (inc.getLatitud() != null ? inc.getLatitud().doubleValue() : null))
                .longitud(request.getLongitud() != null ? request.getLongitud() : (inc.getLongitud() != null ? inc.getLongitud().doubleValue() : null))
                .descripcionActuacion(request.getDescripcionActuacion().trim())
                .resultado(request.getResultado())
                .estado(estadoInicial)
                .nivelConfidencialidad(request.getNivelConfidencialidad() != null ? request.getNivelConfidencialidad() : NivelConfidencialidad.PUBLICO)
                .registroRetrospectivo(Boolean.TRUE.equals(request.getRegistroRetrospectivo()))
                .motivoRegistroRetrospectivo(request.getMotivoRegistroRetrospectivo())
                .observaciones(request.getObservaciones())
                .version(0L)
                .createdBy(username)
                .updatedBy(username)
                .build();

        final IntervencionSerenazgo savedInter = intervencionRepository.save(inter);

        // 10. Asignar Responsable en la lista de participantes si no está
        ParticipanteIntervencionSerenazgo partResp = ParticipanteIntervencionSerenazgo.builder()
                .intervencion(savedInter)
                .personal(resp)
                .funcionDesempenada("RESPONSABLE OPERATIVO")
                .observaciones("Líder de la actuación preventiva")
                .fechaRegistro(OffsetDateTime.now(ZONA_PERU))
                .build();
        participanteRepository.save(partResp);

        // 11. Registrar personal participante adicional
        if (request.getPersonalParticipanteIds() != null && !request.getPersonalParticipanteIds().isEmpty()) {
            for (int i = 0; i < request.getPersonalParticipanteIds().size(); i++) {
                UUID pid = request.getPersonalParticipanteIds().get(i);
                if (pid.equals(resp.getId())) continue;

                PersonalSerenazgo p = personalRepository.findById(pid).orElse(null);
                if (p != null) {
                    String func = (request.getFuncionesParticipantes() != null && request.getFuncionesParticipantes().size() > i)
                            ? request.getFuncionesParticipantes().get(i) : "PATRULLERO";

                    ParticipanteIntervencionSerenazgo part = ParticipanteIntervencionSerenazgo.builder()
                            .intervencion(savedInter)
                            .personal(p)
                            .funcionDesempenada(func)
                            .fechaRegistro(OffsetDateTime.now(ZONA_PERU))
                            .build();
                    participanteRepository.save(part);
                }
            }
        }

        // 12. Asignar equipamiento si se proporcionó
        if (request.getEquipamientoIds() != null) {
            for (UUID eqId : request.getEquipamientoIds()) {
                equipamientoRepository.findById(eqId).ifPresent(eq -> {
                    IntervencionEquipamientoSerenazgo ie = IntervencionEquipamientoSerenazgo.builder()
                            .intervencion(savedInter)
                            .equipamiento(eq)
                            .cantidad(1)
                            .estadoUso("UTILIZADO_CONFORME")
                            .build();
                    intervencionEquipamientoRepository.save(ie);
                });
            }
        }

        // 13. Registrar evento en historial
        registrarHistorial(savedInter, AccionHistorialIntervencion.CREACION, null, estadoInicial.name(),
                "Intervención operativa " + codigo + " generada en el sistema. Tipo: " + savedInter.getTipoIntervencion().getDescripcion(), username);

        log.info("Intervención {} registrada exitosamente con ID: {}", codigo, savedInter.getId());
        return mapearDetalle(savedInter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse actualizarIntervencion(UUID id, IntervencionEditarRequest request, String username) {
        log.info("Actualizando intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));

        if (!inter.getEstado().isEditable()) {
            throw new IllegalStateException("Solo se pueden editar intervenciones en estado BORRADOR u OBSERVADA. Estado actual: " + inter.getEstado());
        }

        // Si estaba observada, registrar motivo de subsanación
        if (inter.getEstado() == EstadoIntervencionSerenazgo.OBSERVADA) {
            if (!StringUtils.hasText(request.getMotivoSubsanacion())) {
                throw new IllegalArgumentException("Debe ingresar el detalle o motivo de subsanación para una intervención observada.");
            }
        }

        if (request.getTipoIntervencion() != null) {
            inter.setTipoIntervencion(request.getTipoIntervencion());
        }

        if (request.getResponsableId() != null && !request.getResponsableId().equals(inter.getResponsable().getId())) {
            PersonalSerenazgo nuevoResp = personalRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("Nuevo responsable no encontrado"));
            inter.setResponsable(nuevoResp);
        }

        if (request.getUnidadMovilId() != null) {
            UnidadMovilSerenazgo um = unidadMovilRepository.findById(request.getUnidadMovilId())
                    .orElseThrow(() -> new NoSuchElementException("Unidad móvil no encontrada"));
            inter.setUnidadMovil(um);
        } else {
            inter.setUnidadMovil(null);
        }

        if (request.getSectorId() != null) {
            SectorPatrullaje sec = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado"));
            inter.setSector(sec);
        }

        if (request.getZonaId() != null) {
            ZonaPatrullaje zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));
            inter.setZona(zona);
        }

        if (request.getFechaInicio() != null) {
            inter.setFechaInicio(request.getFechaInicio().atZone(ZONA_PERU).toOffsetDateTime());
        }
        if (request.getFechaFin() != null) {
            OffsetDateTime fin = request.getFechaFin().atZone(ZONA_PERU).toOffsetDateTime();
            if (fin.isBefore(inter.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
            }
            inter.setFechaFin(fin);
        }

        if (StringUtils.hasText(request.getUbicacionDescripcion())) {
            inter.setUbicacionDescripcion(request.getUbicacionDescripcion().trim());
        }
        if (request.getLatitud() != null) inter.setLatitud(request.getLatitud());
        if (request.getLongitud() != null) inter.setLongitud(request.getLongitud());

        if (StringUtils.hasText(request.getDescripcionActuacion())) {
            inter.setDescripcionActuacion(request.getDescripcionActuacion().trim());
        }
        if (request.getResultado() != null) {
            inter.setResultado(request.getResultado());
        }
        if (request.getNivelConfidencialidad() != null) {
            inter.setNivelConfidencialidad(request.getNivelConfidencialidad());
        }
        if (request.getObservaciones() != null) {
            inter.setObservaciones(request.getObservaciones());
        }

        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        String motivoHistorial = inter.getEstado() == EstadoIntervencionSerenazgo.OBSERVADA
                ? "Subsanación y actualización: " + request.getMotivoSubsanacion()
                : "Actualización de datos generales de la intervención";

        registrarHistorial(inter, AccionHistorialIntervencion.EDICION, inter.getEstado().name(), inter.getEstado().name(),
                motivoHistorial, username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional(readOnly = true)
    public IntervencionDetailResponse obtenerPorId(UUID id, String username) {
        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));
        return mapearDetalle(inter);
    }

    @Override
    @Transactional(readOnly = true)
    public IntervencionDetailResponse obtenerPorCodigo(String codigo, String username) {
        IntervencionSerenazgo inter = intervencionRepository.findByCodigo(codigo)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con código: " + codigo));
        return mapearDetalle(inter);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IntervencionResponse> listarIntervenciones(IntervencionFilter filtro, Pageable pageable) {
        log.debug("Listando intervenciones con filtros...");
        Pageable ordenado = pageable;
        if (pageable.getSort().isUnsorted()) {
            ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "fechaInicio"));
        }
        return intervencionRepository.findAll(crearSpecification(filtro), ordenado)
                .map(this::mapearResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardIntervencionesResponse obtenerDashboard() {
        long total = intervencionRepository.count();
        long borradores = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.BORRADOR);
        long registradas = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.REGISTRADA);
        long enRevision = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.EN_REVISION);
        long validadas = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.VALIDADA);
        long observadas = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.OBSERVADA);
        long archivadas = intervencionRepository.countByEstado(EstadoIntervencionSerenazgo.ARCHIVADA);

        long totalActuaciones = actuacionRepository.count();
        long totalComunicaciones = comunicacionRepository.count();

        return DashboardIntervencionesResponse.builder()
                .total(total)
                .borradores(borradores)
                .registradas(registradas)
                .enRevision(enRevision)
                .validadas(validadas)
                .observadas(observadas)
                .archivadas(archivadas)
                .totalActuaciones(totalActuaciones)
                .totalComunicaciones(totalComunicaciones)
                .build();
    }

    @Override
    @Transactional
    public IntervencionDetailResponse agregarParticipante(UUID intervencionId, IntervencionParticipanteRequest request, String username) {
        log.info("Agregando participante a intervención ID: {} por: {}", intervencionId, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        if (!inter.getEstado().isEditable()) {
            throw new IllegalStateException("Solo se pueden modificar participantes en estado BORRADOR u OBSERVADA.");
        }

        PersonalSerenazgo p = personalRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new NoSuchElementException("Personal no encontrado"));

        boolean yaExiste = participanteRepository.findByIntervencionId(intervencionId).stream()
                .anyMatch(part -> part.getPersonal().getId().equals(p.getId()));

        if (yaExiste) {
            throw new IllegalArgumentException("El efectivo de Serenazgo ya se encuentra registrado como participante.");
        }

        ParticipanteIntervencionSerenazgo part = ParticipanteIntervencionSerenazgo.builder()
                .intervencion(inter)
                .personal(p)
                .funcionDesempenada(request.getFuncionDesempenada().trim())
                .observaciones(request.getObservaciones())
                .fechaRegistro(OffsetDateTime.now(ZONA_PERU))
                .build();
        participanteRepository.save(part);

        String nomPart = (p.getPersona() != null) ? p.getPersona().getNombreCompleto() : p.getCodigoInterno();
        registrarHistorial(inter, AccionHistorialIntervencion.AGREGAR_PARTICIPANTE, inter.getEstado().name(), inter.getEstado().name(),
                "Incorporación de efectivo " + nomPart + " (" + request.getFuncionDesempenada() + ")", username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse removerParticipante(UUID intervencionId, UUID personalId, String username) {
        log.info("Removiendo participante ID: {} de intervención ID: {}", personalId, intervencionId);

        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        if (!inter.getEstado().isEditable()) {
            throw new IllegalStateException("Solo se pueden remover participantes en estado BORRADOR u OBSERVADA.");
        }

        if (inter.getResponsable().getId().equals(personalId)) {
            throw new IllegalArgumentException("No se puede desvincular al sereno responsable operativo designado.");
        }

        participanteRepository.findByIntervencionId(intervencionId).stream()
                .filter(p -> p.getPersonal().getId().equals(personalId))
                .findFirst()
                .ifPresent(part -> {
                    participanteRepository.delete(part);
                    String nomDesv = (part.getPersonal().getPersona() != null) ? part.getPersonal().getPersona().getNombreCompleto() : part.getPersonal().getCodigoInterno();
                    registrarHistorial(inter, AccionHistorialIntervencion.REMOVER_PARTICIPANTE, inter.getEstado().name(), inter.getEstado().name(),
                            "Desvinculación de efectivo " + nomDesv, username);
                });

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse registrarActuacion(UUID intervencionId, IntervencionActuacionRequest request, String username) {
        log.info("Registrando actuación en intervención ID: {} por: {}", intervencionId, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        if (inter.getEstado() == EstadoIntervencionSerenazgo.ARCHIVADA) {
            throw new IllegalStateException("No se pueden registrar actuaciones en una intervención archivada.");
        }

        PersonalSerenazgo respAct = null;
        if (request.getPersonalResponsableId() != null) {
            respAct = personalRepository.findById(request.getPersonalResponsableId()).orElse(null);
        }
        if (respAct == null) {
            respAct = inter.getResponsable();
        }

        OffsetDateTime fechaHora = request.getFechaHora() != null
                ? request.getFechaHora().atZone(ZONA_PERU).toOffsetDateTime()
                : OffsetDateTime.now(ZONA_PERU);

        IntervencionActuacionSerenazgo act = IntervencionActuacionSerenazgo.builder()
                .intervencion(inter)
                .tipoActuacion(request.getTipoActuacion())
                .descripcion(request.getDescripcion().trim())
                .fechaHora(fechaHora)
                .personalResponsable(respAct)
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .build();
        actuacionRepository.save(act);

        registrarHistorial(inter, AccionHistorialIntervencion.REGISTRAR_ACTUACION, inter.getEstado().name(), inter.getEstado().name(),
                "Actuación registrada: " + request.getTipoActuacion().getDescripcion(), username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse registrarComunicacion(UUID intervencionId, IntervencionComunicacionRequest request, String username) {
        log.info("Registrando comunicación en intervención ID: {} por: {}", intervencionId, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        if (inter.getEstado() == EstadoIntervencionSerenazgo.ARCHIVADA) {
            throw new IllegalStateException("No se pueden registrar comunicaciones en una intervención archivada.");
        }

        PersonalSerenazgo perCom = null;
        if (request.getPersonalComunicadorId() != null) {
            perCom = personalRepository.findById(request.getPersonalComunicadorId()).orElse(null);
        }
        if (perCom == null) {
            perCom = inter.getResponsable();
        }

        OffsetDateTime fechaCom = request.getFechaComunicacion() != null
                ? request.getFechaComunicacion().atZone(ZONA_PERU).toOffsetDateTime()
                : OffsetDateTime.now(ZONA_PERU);

        IntervencionComunicacionSerenazgo com = IntervencionComunicacionSerenazgo.builder()
                .intervencion(inter)
                .entidadDestinataria(request.getEntidadDestinataria())
                .motivo(request.getMotivo().trim())
                .fechaComunicacion(fechaCom)
                .canal(request.getCanal() != null ? request.getCanal() : CanalComunicacionDespacho.RADIO_TETRA_VHF)
                .personalComunicador(perCom)
                .numeroReferencia(request.getNumeroReferencia())
                .estadoComunicacion(request.getEstadoComunicacion() != null ? request.getEstadoComunicacion() : EstadoComunicacionIntervencion.ENVIADA)
                .confirmadoPor(request.getConfirmadoPor())
                .resultadoComunicacion(request.getResultadoComunicacion())
                .createdBy(username)
                .build();
        comunicacionRepository.save(com);

        registrarHistorial(inter, AccionHistorialIntervencion.REGISTRAR_COMUNICACION, inter.getEstado().name(), inter.getEstado().name(),
                "Comunicación cursada a " + request.getEntidadDestinataria().getDescripcion()
                        + " (" + com.getEstadoComunicacion().getDescripcion() + ")", username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse actualizarEstadoComunicacion(UUID comunicacionId, EstadoComunicacionIntervencion estado,
                                                                 String confirmadoPor, String resultado, String username) {
        log.info("Actualizando comunicación ID: {} a estado: {}", comunicacionId, estado);

        IntervencionComunicacionSerenazgo com = comunicacionRepository.findById(comunicacionId)
                .orElseThrow(() -> new NoSuchElementException("Registro de comunicación no encontrado"));

        com.setEstadoComunicacion(estado);
        if (StringUtils.hasText(confirmadoPor)) {
            com.setConfirmadoPor(confirmadoPor.trim());
        }
        if (StringUtils.hasText(resultado)) {
            com.setResultadoComunicacion(resultado.trim());
        }
        com = comunicacionRepository.save(com);

        registrarHistorial(com.getIntervencion(), AccionHistorialIntervencion.ACTUALIZAR_COMUNICACION,
                com.getIntervencion().getEstado().name(), com.getIntervencion().getEstado().name(),
                "Actualización de comunicación con " + com.getEntidadDestinataria().getDescripcion()
                        + ": Estado ahora es " + estado.getDescripcion(), username);

        return mapearDetalle(com.getIntervencion());
    }

    @Override
    @Transactional
    public IntervencionDetailResponse asociarEquipamiento(UUID intervencionId, UUID equipamientoId, Integer cantidad,
                                                         String estadoUso, String observaciones, String username) {
        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        EquipamientoSerenazgo eq = equipamientoRepository.findById(equipamientoId)
                .orElseThrow(() -> new NoSuchElementException("Equipamiento no encontrado"));

        IntervencionEquipamientoSerenazgo ie = IntervencionEquipamientoSerenazgo.builder()
                .intervencion(inter)
                .equipamiento(eq)
                .cantidad(cantidad != null && cantidad > 0 ? cantidad : 1)
                .estadoUso(StringUtils.hasText(estadoUso) ? estadoUso.trim() : "UTILIZADO_CONFORME")
                .observaciones(observaciones)
                .build();
        intervencionEquipamientoRepository.save(ie);

        registrarHistorial(inter, AccionHistorialIntervencion.ASIGNAR_EQUIPAMIENTO, inter.getEstado().name(), inter.getEstado().name(),
                "Equipamiento incorporado: " + eq.getDescripcion(), username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse adjuntarDocumento(UUID intervencionId, TipoDocumentoIntervencion tipo,
                                                       String nombreArchivo, String rutaArchivo, String mimeType,
                                                       Long tamanioBytes, String descripcion, Boolean visibleCiudadano,
                                                       String username) {
        IntervencionSerenazgo inter = intervencionRepository.findById(intervencionId)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        IntervencionDocumentoSerenazgo doc = IntervencionDocumentoSerenazgo.builder()
                .intervencion(inter)
                .tipoDocumento(tipo)
                .nombreArchivo(nombreArchivo)
                .rutaArchivo(rutaArchivo)
                .mimeType(mimeType)
                .tamanioBytes(tamanioBytes)
                .descripcion(descripcion)
                .visibleCiudadano(Boolean.TRUE.equals(visibleCiudadano))
                .createdBy(username)
                .build();
        documentoRepository.save(doc);

        registrarHistorial(inter, AccionHistorialIntervencion.ADJUNTAR_DOCUMENTO, inter.getEstado().name(), inter.getEstado().name(),
                "Documento administrativo anexo: " + nombreArchivo + " (" + tipo.getDescripcion() + ")", username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse enviarARevision(UUID id, String username) {
        log.info("Enviando a revisión intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));

        if (inter.getEstado() != EstadoIntervencionSerenazgo.BORRADOR && inter.getEstado() != EstadoIntervencionSerenazgo.REGISTRADA && inter.getEstado() != EstadoIntervencionSerenazgo.OBSERVADA) {
            throw new IllegalStateException("Solo intervenciones en BORRADOR, REGISTRADA u OBSERVADA pueden enviarse a revisión.");
        }

        String estadoAnt = inter.getEstado().name();
        inter.setEstado(EstadoIntervencionSerenazgo.EN_REVISION);
        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        registrarHistorial(inter, AccionHistorialIntervencion.ENVIO_REVISION, estadoAnt, EstadoIntervencionSerenazgo.EN_REVISION.name(),
                "Expediente de intervención puesto a disposición de supervisión para control de integridad administrativa.", username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse observarIntervencion(UUID id, IntervencionObservarRequest request, String username) {
        log.info("Observando intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));

        if (inter.getEstado() != EstadoIntervencionSerenazgo.EN_REVISION && inter.getEstado() != EstadoIntervencionSerenazgo.REGISTRADA) {
            throw new IllegalStateException("Solo intervenciones en estado EN_REVISION o REGISTRADA pueden ser observadas.");
        }

        String estadoAnt = inter.getEstado().name();
        inter.setEstado(EstadoIntervencionSerenazgo.OBSERVADA);
        inter.setMotivoObservacion(request.getMotivo().trim());
        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        registrarHistorial(inter, AccionHistorialIntervencion.OBSERVACION, estadoAnt, EstadoIntervencionSerenazgo.OBSERVADA.name(),
                "Observación por supervisor: " + request.getMotivo().trim(), username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse subsanarIntervencion(UUID id, String motivoSubsanacion, String username) {
        log.info("Subsanando intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada"));

        if (inter.getEstado() != EstadoIntervencionSerenazgo.OBSERVADA) {
            throw new IllegalStateException("Solo intervenciones en estado OBSERVADA pueden ser subsanadas.");
        }

        inter.setEstado(EstadoIntervencionSerenazgo.EN_REVISION);
        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        registrarHistorial(inter, AccionHistorialIntervencion.SUBSANACION, EstadoIntervencionSerenazgo.OBSERVADA.name(),
                EstadoIntervencionSerenazgo.EN_REVISION.name(),
                "Observaciones subsanadas por el operador/efectivo: " + motivoSubsanacion, username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse validarIntervencion(UUID id, IntervencionValidarRequest request, String username) {
        log.info("Validando administrativamente intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));

        if (inter.getEstado() == EstadoIntervencionSerenazgo.VALIDADA || inter.getEstado() == EstadoIntervencionSerenazgo.ARCHIVADA) {
            throw new IllegalStateException("La intervención ya se encuentra validada o archivada.");
        }

        // Validaciones de integridad para aprobar
        if (!StringUtils.hasText(inter.getDescripcionActuacion())) {
            throw new IllegalStateException("No se puede validar una intervención sin descripción objetiva de actuaciones.");
        }
        if (inter.getResponsable() == null) {
            throw new IllegalStateException("No se puede validar una intervención sin sereno responsable designado.");
        }

        Usuario validador = usuarioRepository.findByUsername(username).orElse(null);

        String estadoAnt = inter.getEstado().name();
        inter.setEstado(EstadoIntervencionSerenazgo.VALIDADA);
        inter.setFechaValidacion(OffsetDateTime.now(ZONA_PERU));
        inter.setUsuarioValidador(validador);
        if (inter.getFechaFin() == null) {
            inter.setFechaFin(OffsetDateTime.now(ZONA_PERU));
        }
        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        String obs = (request != null && StringUtils.hasText(request.getObservaciones())) ? request.getObservaciones().trim() : "Validación administrativa conforme";
        registrarHistorial(inter, AccionHistorialIntervencion.VALIDACION, estadoAnt, EstadoIntervencionSerenazgo.VALIDADA.name(),
                "Validación administrativa de supervisión aprobada por " + username + ". Nota: " + obs, username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional
    public IntervencionDetailResponse archivarIntervencion(UUID id, IntervencionArchivarRequest request, String username) {
        log.info("Archivando intervención ID: {} por: {}", id, username);

        IntervencionSerenazgo inter = intervencionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Intervención no encontrada con ID: " + id));

        if (inter.getEstado() != EstadoIntervencionSerenazgo.VALIDADA) {
            throw new IllegalStateException("Solo se pueden archivar intervenciones previamente VALIDADAS.");
        }

        String estadoAnt = inter.getEstado().name();
        inter.setEstado(EstadoIntervencionSerenazgo.ARCHIVADA);
        inter.setMotivoArchivo(request.getMotivo().trim());
        inter.setUpdatedBy(username);
        inter = intervencionRepository.save(inter);

        registrarHistorial(inter, AccionHistorialIntervencion.ARCHIVADO, estadoAnt, EstadoIntervencionSerenazgo.ARCHIVADA.name(),
                "Expediente archivado administrativamente. Motivo: " + request.getMotivo().trim(), username);

        return mapearDetalle(inter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(IntervencionFilter filtro) {
        List<IntervencionResponse> lista = intervencionRepository.findAll(crearSpecification(filtro), Sort.by(Sort.Direction.DESC, "fechaInicio"))
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

            Paragraph p2 = new Paragraph("Módulo 3: Seguridad Ciudadana y Serenazgo | Registro Oficial de Intervenciones y Actuaciones", fontSub);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(4);
            document.add(p2);

            Paragraph pAviso = new Paragraph("AVISO OFICIAL: Registro administrativo preventivo de Serenazgo. No constituye peritaje policial, investigación criminal ni sustituye a la PNP (105).", fontAviso);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(10);
            document.add(pAviso);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{11, 11, 11, 12, 11, 11, 11, 11, 11});

            String[] headers = {"Cód. Intervención", "Incidencia Origen", "Fecha / Hora", "Tipo Actuación", "Sector", "Responsable", "Resultado", "Confidencialidad", "Estado"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                cell.setBackgroundColor(new java.awt.Color(29, 78, 216));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (IntervencionResponse itn : lista) {
                table.addCell(new PdfPCell(new Phrase(itn.getCodigo(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getIncidenciaCodigo() != null ? itn.getIncidenciaCodigo() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getFechaInicioFormateada() != null ? itn.getFechaInicioFormateada() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getTipoIntervencionDesc(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getSectorNombre() != null ? itn.getSectorNombre() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getResponsableNombre() != null ? itn.getResponsableNombre() : "-", fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getResultadoDesc(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getNivelConfidencialidadDesc(), fontBody)));
                table.addCell(new PdfPCell(new Phrase(itn.getEstadoDesc(), fontBody)));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al exportar PDF de intervenciones", e);
            throw new RuntimeException("Error al generar PDF de intervenciones operativas", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(IntervencionFilter filtro) {
        List<IntervencionResponse> lista = intervencionRepository.findAll(crearSpecification(filtro), Sort.by(Sort.Direction.DESC, "fechaInicio"))
                .stream().map(this::mapearResumen).collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Intervenciones Serenazgo");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setBold(true);
            fontH.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontH);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - REPORTE DE INTERVENCIONES DE SERENAZGO");

            Row advRow = sheet.createRow(1);
            Cell advCell = advRow.createCell(0);
            advCell.setCellValue("AVISO: Registro administrativo de Serenazgo. No constituye atestado ni diligencia judicial.");

            Row headerRow = sheet.createRow(3);
            String[] headers = {"Código", "Incidencia", "Atención", "Tipo Intervención", "Fecha Inicio", "Fecha Fin", "Sector", "Zona", "Responsable", "Vehículo / Móvil", "Resultado Administrativo", "Confidencialidad", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            for (IntervencionResponse itn : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(itn.getCodigo());
                row.createCell(1).setCellValue(itn.getIncidenciaCodigo() != null ? itn.getIncidenciaCodigo() : "-");
                row.createCell(2).setCellValue(itn.getAtencionCodigo() != null ? itn.getAtencionCodigo() : "-");
                row.createCell(3).setCellValue(itn.getTipoIntervencionDesc());
                row.createCell(4).setCellValue(itn.getFechaInicioFormateada() != null ? itn.getFechaInicioFormateada() : "-");
                row.createCell(5).setCellValue(itn.getFechaFinFormateada() != null ? itn.getFechaFinFormateada() : "-");
                row.createCell(6).setCellValue(itn.getSectorNombre() != null ? itn.getSectorNombre() : "-");
                row.createCell(7).setCellValue(itn.getZonaNombre() != null ? itn.getZonaNombre() : "-");
                row.createCell(8).setCellValue(itn.getResponsableNombre() != null ? itn.getResponsableNombre() : "-");
                row.createCell(9).setCellValue(itn.getUnidadMovilCodigo() != null ? itn.getUnidadMovilCodigo() : "Pedestre / A pie");
                row.createCell(10).setCellValue(itn.getResultadoDesc());
                row.createCell(11).setCellValue(itn.getNivelConfidencialidadDesc());
                row.createCell(12).setCellValue(itn.getEstadoDesc());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar Excel de intervenciones", e);
            throw new RuntimeException("Error al generar Excel de intervenciones", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(UUID id) {
        IntervencionDetailResponse det = obtenerPorId(id, "SISTEMA");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, new java.awt.Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new java.awt.Color(71, 85, 105));
            Font fontAviso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.NORMAL, new java.awt.Color(185, 28, 28));
            Font fontSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, new java.awt.Color(29, 78, 216));
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, java.awt.Color.BLACK);

            Paragraph p1 = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("EXPEDIENTE ADMINISTRATIVO DE INTERVENCIÓN OPERATIVA: " + det.getCodigo(), fontSec);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(4);
            document.add(p2);

            Paragraph pAviso = new Paragraph("AVISO OFICIAL: Registro administrativo preventivo de Serenazgo. No constituye peritaje policial, investigación criminal ni sustituye a la PNP (105).", fontAviso);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(10);
            document.add(pAviso);

            PdfPTable tInfo = new PdfPTable(4);
            tInfo.setWidthPercentage(100);
            tInfo.setWidths(new float[]{25, 25, 25, 25});

            tInfo.addCell(new PdfPCell(new Phrase("Estado:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getEstadoDesc(), fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Resultado:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getResultadoDesc(), fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Fecha Inicio:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaInicioFormateada() != null ? det.getFechaInicioFormateada() : "-", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Fecha Fin:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaFinFormateada() != null ? det.getFechaFinFormateada() : "En desarrollo", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Incidencia Origen:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getIncidenciaCodigo() + " - " + det.getIncidenciaTitulo(), fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Orden Atención:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getAtencionCodigo() != null ? det.getAtencionCodigo() : "Directa / Sin orden", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Sector / Zona:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase((det.getSectorNombre() != null ? det.getSectorNombre() : "-") + " / " + (det.getZonaNombre() != null ? det.getZonaNombre() : "-"), fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Confidencialidad:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getNivelConfidencialidadDesc(), fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Sereno Responsable:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getResponsableNombre() + " (" + det.getResponsableCodigo() + ")", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Unidad Móvil:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getUnidadMovilCodigo() != null ? det.getUnidadMovilCodigo() + " [" + det.getUnidadMovilPlaca() + "]" : "Pedestre / A pie", fontNormal)));

            document.add(tInfo);

            Paragraph pAct = new Paragraph("\nDESCRIPCIÓN DE LAS ACTUACIONES OPERATIVAS REALIZADAS:", fontSec);
            document.add(pAct);
            Paragraph pActDesc = new Paragraph(det.getDescripcionActuacion(), fontNormal);
            pActDesc.setSpacingAfter(10);
            document.add(pActDesc);

            // Actuaciones
            if (!det.getActuaciones().isEmpty()) {
                document.add(new Paragraph("DETALLE CRONOLÓGICO DE ACTUACIONES:", fontSec));
                PdfPTable tAct = new PdfPTable(3);
                tAct.setWidthPercentage(100);
                tAct.setWidths(new float[]{20, 25, 55});
                tAct.addCell(new PdfPCell(new Phrase("Fecha / Hora", fontBold)));
                tAct.addCell(new PdfPCell(new Phrase("Tipo Actuación", fontBold)));
                tAct.addCell(new PdfPCell(new Phrase("Detalle", fontBold)));

                for (IntervencionActuacionResponse a : det.getActuaciones()) {
                    tAct.addCell(new PdfPCell(new Phrase(a.getFechaHoraFormateada(), fontNormal)));
                    tAct.addCell(new PdfPCell(new Phrase(a.getTipoActuacionDesc(), fontNormal)));
                    tAct.addCell(new PdfPCell(new Phrase(a.getDescripcion(), fontNormal)));
                }
                document.add(tAct);
            }

            // Comunicaciones
            if (!det.getComunicaciones().isEmpty()) {
                document.add(new Paragraph("\nCOMUNICACIONES INSTITUCIONALES:", fontSec));
                PdfPTable tCom = new PdfPTable(4);
                tCom.setWidthPercentage(100);
                tCom.setWidths(new float[]{20, 25, 30, 25});
                tCom.addCell(new PdfPCell(new Phrase("Fecha", fontBold)));
                tCom.addCell(new PdfPCell(new Phrase("Entidad", fontBold)));
                tCom.addCell(new PdfPCell(new Phrase("Motivo", fontBold)));
                tCom.addCell(new PdfPCell(new Phrase("Estado", fontBold)));

                for (IntervencionComunicacionResponse c : det.getComunicaciones()) {
                    tCom.addCell(new PdfPCell(new Phrase(c.getFechaComunicacionFormateada(), fontNormal)));
                    tCom.addCell(new PdfPCell(new Phrase(c.getEntidadDestinatariaDesc(), fontNormal)));
                    tCom.addCell(new PdfPCell(new Phrase(c.getMotivo(), fontNormal)));
                    tCom.addCell(new PdfPCell(new Phrase(c.getEstadoComunicacionDesc(), fontNormal)));
                }
                document.add(tCom);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al generar Ficha PDF de intervención", e);
            throw new RuntimeException("Error al generar Ficha PDF", e);
        }
    }

    private synchronized String generarCodigoIntervencion() {
        int anio = LocalDate.now(ZONA_PERU).getYear();
        Long valor = intervencionRepository.obtenerSiguienteValorSecuencia();
        long seq = (valor != null) ? valor : 1L;
        return String.format("INT-%d-%05d", anio, seq);
    }

    private void registrarHistorial(IntervencionSerenazgo inter, AccionHistorialIntervencion accion,
                                    String estadoAnt, String estadoNuevo, String motivo, String usuario) {
        IntervencionHistorialSerenazgo h = IntervencionHistorialSerenazgo.builder()
                .intervencion(inter)
                .accion(accion)
                .estadoAnterior(estadoAnt)
                .estadoNuevo(estadoNuevo)
                .motivo(motivo)
                .usuario(usuario != null ? usuario : "SISTEMA")
                .fecha(OffsetDateTime.now(ZONA_PERU))
                .build();
        historialRepository.save(h);
    }

    private Specification<IntervencionSerenazgo> crearSpecification(IntervencionFilter filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return cb.conjunction();
            }

            if (StringUtils.hasText(filtro.getTermino())) {
                String term = "%" + filtro.getTermino().trim().toLowerCase() + "%";
                Join<IntervencionSerenazgo, IncidenciaSeguridadCiudadana> incJoin = root.join("incidencia", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), term),
                        cb.like(cb.lower(incJoin.get("codigo")), term),
                        cb.like(cb.lower(incJoin.get("titulo")), term),
                        cb.like(cb.lower(root.get("ubicacionDescripcion")), term),
                        cb.like(cb.lower(root.get("descripcionActuacion")), term)
                ));
            } else if (StringUtils.hasText(filtro.getCodigo())) {
                String term = "%" + filtro.getCodigo().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("codigo")), term));
            }

            if (filtro.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipoIntervencion"), filtro.getTipo()));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getSectorId() != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
            }

            if (filtro.getZonaId() != null) {
                predicates.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
            }

            if (filtro.getIncidenciaId() != null) {
                predicates.add(cb.equal(root.get("incidencia").get("id"), filtro.getIncidenciaId()));
            }

            if (filtro.getAtencionId() != null) {
                predicates.add(cb.equal(root.get("atencion").get("id"), filtro.getAtencionId()));
            }

            if (filtro.getPersonalId() != null) {
                Join<IntervencionSerenazgo, ParticipanteIntervencionSerenazgo> partJoin = root.join("participantes", JoinType.INNER);
                predicates.add(cb.or(
                        cb.equal(root.get("responsable").get("id"), filtro.getPersonalId()),
                        cb.equal(partJoin.get("personal").get("id"), filtro.getPersonalId())
                ));
            }

            if (filtro.getDesde() != null) {
                OffsetDateTime desdeDt = filtro.getDesde().atStartOfDay(ZONA_PERU).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), desdeDt));
            }

            if (filtro.getHasta() != null) {
                OffsetDateTime hastaDt = filtro.getHasta().plusDays(1).atStartOfDay(ZONA_PERU).toOffsetDateTime();
                predicates.add(cb.lessThan(root.get("fechaInicio"), hastaDt));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private IntervencionResponse mapearResumen(IntervencionSerenazgo i) {
        String respNombre = (i.getResponsable() != null && i.getResponsable().getPersona() != null)
                ? i.getResponsable().getPersona().getNombreCompleto() : "-";
        String respCod = (i.getResponsable() != null) ? i.getResponsable().getCodigoInterno() : "-";

        return IntervencionResponse.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .incidenciaId(i.getIncidencia().getId())
                .incidenciaCodigo(i.getIncidencia().getCodigo())
                .incidenciaTitulo(i.getIncidencia().getTitulo())
                .atencionId(i.getAtencion() != null ? i.getAtencion().getId() : null)
                .atencionCodigo(i.getAtencion() != null ? i.getAtencion().getCodigo() : null)
                .tipoIntervencion(i.getTipoIntervencion())
                .tipoIntervencionDesc(i.getTipoIntervencion() != null ? i.getTipoIntervencion().getDescripcion() : "-")
                .fechaInicio(i.getFechaInicio())
                .fechaInicioFormateada(i.getFechaInicio() != null ? i.getFechaInicio().format(FORMATO_FECHA_HORA) : "-")
                .fechaFin(i.getFechaFin())
                .fechaFinFormateada(i.getFechaFin() != null ? i.getFechaFin().format(FORMATO_FECHA_HORA) : "-")
                .sectorId(i.getSector() != null ? i.getSector().getId() : null)
                .sectorNombre(i.getSector() != null ? i.getSector().getNombre() : "-")
                .zonaId(i.getZona() != null ? i.getZona().getId() : null)
                .zonaNombre(i.getZona() != null ? i.getZona().getNombre() : "-")
                .ubicacionDescripcion(i.getUbicacionDescripcion())
                .responsableId(i.getResponsable() != null ? i.getResponsable().getId() : null)
                .responsableNombre(respNombre)
                .responsableCodigo(respCod)
                .unidadMovilId(i.getUnidadMovil() != null ? i.getUnidadMovil().getId() : null)
                .unidadMovilCodigo(i.getUnidadMovil() != null ? i.getUnidadMovil().getCodigoInterno() : null)
                .unidadMovilPlaca(i.getUnidadMovil() != null ? i.getUnidadMovil().getPlaca() : null)
                .resultado(i.getResultado())
                .resultadoDesc(i.getResultado() != null ? i.getResultado().getDescripcion() : "-")
                .estado(i.getEstado())
                .estadoDesc(i.getEstado() != null ? i.getEstado().getDescripcion() : "-")
                .nivelConfidencialidad(i.getNivelConfidencialidad())
                .nivelConfidencialidadDesc(i.getNivelConfidencialidad() != null ? i.getNivelConfidencialidad().getDescripcion() : "-")
                .cantidadParticipantes(i.getParticipantes() != null ? i.getParticipantes().size() : 0)
                .cantidadActuaciones(i.getActuaciones() != null ? i.getActuaciones().size() : 0)
                .cantidadComunicaciones(i.getComunicaciones() != null ? i.getComunicaciones().size() : 0)
                .cantidadDocumentos(i.getDocumentos() != null ? i.getDocumentos().size() : 0)
                .registroRetrospectivo(i.getRegistroRetrospectivo())
                .fechaValidacion(i.getFechaValidacion())
                .usuarioValidadorNombre(i.getUsuarioValidador() != null ? i.getUsuarioValidador().getUsername() : null)
                .build();
    }

    private IntervencionDetailResponse mapearDetalle(IntervencionSerenazgo i) {
        String respNombre = (i.getResponsable() != null && i.getResponsable().getPersona() != null)
                ? i.getResponsable().getPersona().getNombreCompleto() : "-";
        String respCod = (i.getResponsable() != null) ? i.getResponsable().getCodigoInterno() : "-";
        String respCargo = (i.getResponsable() != null && i.getResponsable().getCargoOperativo() != null) ? i.getResponsable().getCargoOperativo().getNombre() : "-";

        // Consultar colecciones hijas directamente desde sus repositorios para evitar desincronizaciones de caché
        List<IntervencionParticipanteResponse> participantes = participanteRepository.findByIntervencionId(i.getId()).stream()
                .map(p -> IntervencionParticipanteResponse.builder()
                        .id(p.getId())
                        .personalId(p.getPersonal().getId())
                        .personalNombre(p.getPersonal().getPersona() != null ? p.getPersonal().getPersona().getNombreCompleto() : "-")
                        .personalCodigo(p.getPersonal().getCodigoInterno())
                        .personalCargo(p.getPersonal().getCargoOperativo() != null ? p.getPersonal().getCargoOperativo().getNombre() : "-")
                        .funcionDesempenada(p.getFuncionDesempenada())
                        .fechaRegistro(p.getFechaRegistro())
                        .fechaRegistroFormateada(p.getFechaRegistro() != null ? p.getFechaRegistro().format(FORMATO_FECHA_HORA) : "-")
                        .observaciones(p.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        List<IntervencionActuacionResponse> actuaciones = actuacionRepository.findByIntervencionIdOrderByFechaHoraAsc(i.getId()).stream()
                .map(a -> IntervencionActuacionResponse.builder()
                        .id(a.getId())
                        .tipoActuacion(a.getTipoActuacion())
                        .tipoActuacionDesc(a.getTipoActuacion() != null ? a.getTipoActuacion().getDescripcion() : "-")
                        .descripcion(a.getDescripcion())
                        .fechaHora(a.getFechaHora())
                        .fechaHoraFormateada(a.getFechaHora() != null ? a.getFechaHora().format(FORMATO_FECHA_HORA) : "-")
                        .personalResponsableId(a.getPersonalResponsable() != null ? a.getPersonalResponsable().getId() : null)
                        .personalResponsableNombre(a.getPersonalResponsable() != null && a.getPersonalResponsable().getPersona() != null ? a.getPersonalResponsable().getPersona().getNombreCompleto() : "-")
                        .observaciones(a.getObservaciones())
                        .createdBy(a.getCreatedBy())
                        .build())
                .collect(Collectors.toList());

        List<IntervencionComunicacionResponse> comunicaciones = comunicacionRepository.findByIntervencionIdOrderByFechaComunicacionAsc(i.getId()).stream()
                .map(c -> IntervencionComunicacionResponse.builder()
                        .id(c.getId())
                        .entidadDestinataria(c.getEntidadDestinataria())
                        .entidadDestinatariaDesc(c.getEntidadDestinataria() != null ? c.getEntidadDestinataria().getDescripcion() : "-")
                        .motivo(c.getMotivo())
                        .fechaComunicacion(c.getFechaComunicacion())
                        .fechaComunicacionFormateada(c.getFechaComunicacion() != null ? c.getFechaComunicacion().format(FORMATO_FECHA_HORA) : "-")
                        .canal(c.getCanal())
                        .canalDesc(c.getCanal() != null ? c.getCanal().getDescripcion() : "-")
                        .personalComunicadorId(c.getPersonalComunicador() != null ? c.getPersonalComunicador().getId() : null)
                        .personalComunicadorNombre(c.getPersonalComunicador() != null && c.getPersonalComunicador().getPersona() != null ? c.getPersonalComunicador().getPersona().getNombreCompleto() : "-")
                        .numeroReferencia(c.getNumeroReferencia())
                        .estadoComunicacion(c.getEstadoComunicacion())
                        .estadoComunicacionDesc(c.getEstadoComunicacion() != null ? c.getEstadoComunicacion().getDescripcion() : "-")
                        .confirmadoPor(c.getConfirmadoPor())
                        .resultadoComunicacion(c.getResultadoComunicacion())
                        .createdBy(c.getCreatedBy())
                        .build())
                .collect(Collectors.toList());

        List<IntervencionEquipamientoResponse> equipamiento = intervencionEquipamientoRepository.findByIntervencionId(i.getId()).stream()
                .map(ie -> IntervencionEquipamientoResponse.builder()
                        .id(ie.getId())
                        .equipamientoId(ie.getEquipamiento().getId())
                        .equipamientoNombre(ie.getEquipamiento().getDescripcion())
                        .equipamientoCodigo(ie.getEquipamiento().getCodigoPatrimonial() != null ? ie.getEquipamiento().getCodigoPatrimonial() : ie.getEquipamiento().getCodigoInterno())
                        .equipamientoTipo(ie.getEquipamiento().getTipoEquipo() != null ? ie.getEquipamiento().getTipoEquipo().getDescripcion() : "-")
                        .cantidad(ie.getCantidad())
                        .estadoUso(ie.getEstadoUso())
                        .observaciones(ie.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        List<IntervencionDocumentoResponse> documentos = documentoRepository.findByIntervencionIdOrderByCreatedAtAsc(i.getId()).stream()
                .map(d -> IntervencionDocumentoResponse.builder()
                        .id(d.getId())
                        .tipoDocumento(d.getTipoDocumento())
                        .tipoDocumentoDesc(d.getTipoDocumento() != null ? d.getTipoDocumento().getDescripcion() : "-")
                        .nombreArchivo(d.getNombreArchivo())
                        .rutaArchivo(d.getRutaArchivo())
                        .mimeType(d.getMimeType())
                        .tamanioBytes(d.getTamanioBytes())
                        .descripcion(d.getDescripcion())
                        .visibleCiudadano(d.getVisibleCiudadano())
                        .createdAt(d.getCreatedAt())
                        .createdAtFormateada(d.getCreatedAt() != null ? d.getCreatedAt().format(FORMATO_FECHA_HORA) : "-")
                        .createdBy(d.getCreatedBy())
                        .build())
                .collect(Collectors.toList());

        List<IntervencionHistorialResponse> historial = historialRepository.findByIntervencionIdOrderByFechaAsc(i.getId()).stream()
                .map(h -> IntervencionHistorialResponse.builder()
                        .id(h.getId())
                        .accion(h.getAccion())
                        .accionDesc(h.getAccion() != null ? h.getAccion().getDescripcion() : "-")
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .motivo(h.getMotivo())
                        .usuario(h.getUsuario())
                        .fecha(h.getFecha())
                        .fechaFormateada(h.getFecha() != null ? h.getFecha().format(FORMATO_FECHA_HORA) : "-")
                        .build())
                .collect(Collectors.toList());

        return IntervencionDetailResponse.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .incidenciaId(i.getIncidencia().getId())
                .incidenciaCodigo(i.getIncidencia().getCodigo())
                .incidenciaTitulo(i.getIncidencia().getTitulo())
                .incidenciaTipo(i.getIncidencia().getTipoIncidencia() != null ? i.getIncidencia().getTipoIncidencia().getNombre() : "-")
                .incidenciaUbicacion(i.getIncidencia().getUbicacionReferencia())
                .atencionId(i.getAtencion() != null ? i.getAtencion().getId() : null)
                .atencionCodigo(i.getAtencion() != null ? i.getAtencion().getCodigo() : null)
                .atencionEstado(i.getAtencion() != null ? i.getAtencion().getEstado().getDescripcion() : null)
                .tipoIntervencion(i.getTipoIntervencion())
                .tipoIntervencionDesc(i.getTipoIntervencion() != null ? i.getTipoIntervencion().getDescripcion() : "-")
                .fechaInicio(i.getFechaInicio())
                .fechaInicioFormateada(i.getFechaInicio() != null ? i.getFechaInicio().format(FORMATO_FECHA_HORA) : "-")
                .fechaFin(i.getFechaFin())
                .fechaFinFormateada(i.getFechaFin() != null ? i.getFechaFin().format(FORMATO_FECHA_HORA) : "-")
                .sectorId(i.getSector() != null ? i.getSector().getId() : null)
                .sectorNombre(i.getSector() != null ? i.getSector().getNombre() : "-")
                .zonaId(i.getZona() != null ? i.getZona().getId() : null)
                .zonaNombre(i.getZona() != null ? i.getZona().getNombre() : "-")
                .ubicacionDescripcion(i.getUbicacionDescripcion())
                .latitud(i.getLatitud())
                .longitud(i.getLongitud())
                .responsableId(i.getResponsable() != null ? i.getResponsable().getId() : null)
                .responsableNombre(respNombre)
                .responsableCodigo(respCod)
                .responsableCargo(respCargo)
                .unidadMovilId(i.getUnidadMovil() != null ? i.getUnidadMovil().getId() : null)
                .unidadMovilCodigo(i.getUnidadMovil() != null ? i.getUnidadMovil().getCodigoInterno() : null)
                .unidadMovilPlaca(i.getUnidadMovil() != null ? i.getUnidadMovil().getPlaca() : null)
                .unidadMovilTipo(i.getUnidadMovil() != null && i.getUnidadMovil().getTipoUnidad() != null ? i.getUnidadMovil().getTipoUnidad().name() : null)
                .descripcionActuacion(i.getDescripcionActuacion())
                .resultado(i.getResultado())
                .resultadoDesc(i.getResultado() != null ? i.getResultado().getDescripcion() : "-")
                .estado(i.getEstado())
                .estadoDesc(i.getEstado() != null ? i.getEstado().getDescripcion() : "-")
                .nivelConfidencialidad(i.getNivelConfidencialidad())
                .nivelConfidencialidadDesc(i.getNivelConfidencialidad() != null ? i.getNivelConfidencialidad().getDescripcion() : "-")
                .fechaValidacion(i.getFechaValidacion())
                .fechaValidacionFormateada(i.getFechaValidacion() != null ? i.getFechaValidacion().format(FORMATO_FECHA_HORA) : "-")
                .usuarioValidadorNombre(i.getUsuarioValidador() != null ? i.getUsuarioValidador().getUsername() : null)
                .motivoObservacion(i.getMotivoObservacion())
                .motivoArchivo(i.getMotivoArchivo())
                .registroRetrospectivo(i.getRegistroRetrospectivo())
                .motivoRegistroRetrospectivo(i.getMotivoRegistroRetrospectivo())
                .observaciones(i.getObservaciones())
                .version(i.getVersion())
                .createdAt(i.getCreatedAt())
                .createdAtFormateada(i.getCreatedAt() != null ? i.getCreatedAt().format(FORMATO_FECHA_HORA) : "-")
                .updatedAt(i.getUpdatedAt())
                .createdBy(i.getCreatedBy())
                .updatedBy(i.getUpdatedBy())
                .participantes(participantes)
                .actuaciones(actuaciones)
                .comunicaciones(comunicaciones)
                .equipamiento(equipamiento)
                .documentos(documentos)
                .historial(historial)
                .build();
    }
}

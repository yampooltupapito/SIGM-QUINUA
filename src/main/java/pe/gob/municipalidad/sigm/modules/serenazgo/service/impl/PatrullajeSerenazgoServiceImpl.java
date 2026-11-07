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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PatrullajeSerenazgoService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrullajeSerenazgoServiceImpl implements PatrullajeSerenazgoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PatrullajeSerenazgoRepository patrullajeRepository;
    private final RutaPatrullajeRepository rutaRepository;
    private final PuntoControlRutaRepository puntoControlRepository;
    private final PatrullajeVisitaPuntoRepository visitaPuntoRepository;
    private final PatrullajeNovedadRepository novedadRepository;
    private final PatrullajeEquipamientoRepository equipamientoPatrullajeRepository;
    private final PatrullajeHistorialRepository historialRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadMovilRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final ProgramacionServicioSerenazgoRepository programacionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PatrullajeResponse> listarPatrullajes(PatrullajeFilter filter, Pageable pageable) {
        return patrullajeRepository.findAll(crearSpecification(filter), pageable)
                .map(this::mapearResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatrullajeResponse> listarPatrullajesParaExportacion(PatrullajeFilter filter) {
        return patrullajeRepository.findAll(crearSpecification(filter), Sort.by(Sort.Direction.DESC, "fechaHoraInicioProgramada"))
                .stream()
                .map(this::mapearResumen)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PatrullajeDetailResponse obtenerPatrullajePorId(UUID id) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el patrullaje con ID: " + id));
        return mapearDetalle(pat);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPatrullajesResponse obtenerDashboardKpis() {
        long total = patrullajeRepository.count();
        long prog = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.PROGRAMADO);
        long asig = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.ASIGNADO);
        long curso = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.EN_CURSO);
        long pausa = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.EN_PAUSA);
        long fin = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.FINALIZADO);
        long canc = patrullajeRepository.countByEstado(EstadoPatrullajeSerenazgo.CANCELADO);

        return DashboardPatrullajesResponse.builder()
                .total(total)
                .programados(prog)
                .asignados(asig)
                .enCurso(curso)
                .enPausa(pausa)
                .finalizados(fin)
                .cancelados(canc)
                .build();
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse crearPatrullaje(PatrullajeCrearRequest request, String username) {
        validarFechasProgramadas(request);

        SectorPatrullaje sector = sectorRepository.findById(request.getSectorId())
                .orElseThrow(() -> new IllegalArgumentException("Sector no válido con ID: " + request.getSectorId()));

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Zona no válida con ID: " + request.getZonaId()));
            if (!zona.getSector().getId().equals(sector.getId())) {
                throw new IllegalArgumentException("La zona seleccionada no pertenece al sector indicado.");
            }
        }

        RutaPatrullaje ruta = null;
        if (request.getRutaId() != null) {
            ruta = rutaRepository.findById(request.getRutaId())
                    .orElseThrow(() -> new IllegalArgumentException("Ruta no válida con ID: " + request.getRutaId()));
            if (!ruta.getSector().getId().equals(sector.getId())) {
                throw new IllegalArgumentException("La ruta seleccionada no corresponde al sector territorial.");
            }
        }

        PersonalSerenazgo responsable = personalRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new IllegalArgumentException("Personal responsable no encontrado con ID: " + request.getResponsableId()));

        validarDisponibilidadPersonal(responsable, null);

        UnidadMovilSerenazgo unidad = null;
        if (request.getUnidadMovilId() != null) {
            unidad = unidadMovilRepository.findById(request.getUnidadMovilId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidad móvil no encontrada con ID: " + request.getUnidadMovilId()));
            validarDisponibilidadUnidad(unidad, null);
        }

        ProgramacionServicioSerenazgo programacion = null;
        if (request.getProgramacionServicioId() != null) {
            programacion = programacionRepository.findById(request.getProgramacionServicioId())
                    .orElseThrow(() -> new IllegalArgumentException("Programación de servicio no encontrada con ID: " + request.getProgramacionServicioId()));
        }

        if (Boolean.TRUE.equals(request.getEsExtraordinario()) &&
                (request.getMotivoExtraordinario() == null || request.getMotivoExtraordinario().trim().isEmpty())) {
            throw new IllegalArgumentException("Para patrullajes extraordinarios se requiere fundamentar el motivo y justificación.");
        }

        String codigoCorrelativo = generarCodigoPatrullaje();

        OffsetDateTime inicioProg = request.getFechaHoraInicioProgramada().atZone(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime finProg = request.getFechaHoraFinProgramada().atZone(ZONA_LIMA).toOffsetDateTime();

        EstadoPatrullajeSerenazgo estadoInicial = (request.getParticipantesIds() != null && !request.getParticipantesIds().isEmpty()) || unidad != null
                ? EstadoPatrullajeSerenazgo.ASIGNADO
                : EstadoPatrullajeSerenazgo.PROGRAMADO;

        PatrullajeSerenazgo pat = PatrullajeSerenazgo.builder()
                .codigo(codigoCorrelativo)
                .tipoPatrullaje(request.getTipoPatrullaje())
                .sector(sector)
                .zona(zona)
                .ruta(ruta)
                .programacionServicio(programacion)
                .unidadMovil(unidad)
                .responsable(responsable)
                .fechaHoraInicioProgramada(inicioProg)
                .fechaHoraFinProgramada(finProg)
                .fechaInicio(inicioProg)
                .fechaFin(finProg)
                .objetivoOperativo(request.getObjetivoOperativo().trim())
                .descripcion(request.getDescripcion())
                .observaciones(request.getObservaciones())
                .esExtraordinario(Boolean.TRUE.equals(request.getEsExtraordinario()))
                .motivoExtraordinario(request.getMotivoExtraordinario())
                .estado(estadoInicial)
                .createdBy(username)
                .updatedBy(username)
                .build();

        PatrullajeSerenazgo guardado = patrullajeRepository.save(pat);

        // Asignar participantes de cuadrilla
        if (request.getParticipantesIds() != null && !request.getParticipantesIds().isEmpty()) {
            for (UUID partId : request.getParticipantesIds()) {
                if (partId.equals(responsable.getId())) continue;
                PersonalSerenazgo part = personalRepository.findById(partId)
                        .orElseThrow(() -> new IllegalArgumentException("Personal participante no encontrado: " + partId));
                validarDisponibilidadPersonal(part, guardado.getId());

                ParticipantePatrullajeSerenazgo pEntity = ParticipantePatrullajeSerenazgo.builder()
                        .patrullaje(guardado)
                        .personal(part)
                        .funcionDesempenada(part.getCargoOperativo() != null ? part.getCargoOperativo().getNombre() : "Patrullero")
                        .fechaIncorporacion(OffsetDateTime.now(ZONA_LIMA))
                        .build();
                guardado.getParticipantes().add(pEntity);
            }
        }

        // Asignar equipamiento si se especificó
        if (request.getEquipamientoIds() != null && !request.getEquipamientoIds().isEmpty()) {
            for (UUID eqId : request.getEquipamientoIds()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(eqId)
                        .orElseThrow(() -> new IllegalArgumentException("Equipamiento no encontrado: " + eqId));
                PatrullajeEquipamiento pe = PatrullajeEquipamiento.builder()
                        .patrullaje(guardado)
                        .equipamiento(eq)
                        .cantidad(1)
                        .estadoEntrega("ENTREGADO_OPERATIVO")
                        .observaciones("Asignado en planificación")
                        .build();
                guardado.getEquipos().add(pe);
            }
        }

        // Registrar Historial
        registrarHistorial(guardado, AccionHistorialPatrullaje.CREACION.name(), null, estadoInicial.name(),
                "Registro y planificación de patrullaje preventivo. Código: " + guardado.getCodigo(), username);

        log.info("Patrullaje creado exitosamente con código {} por el usuario {}", guardado.getCodigo(), username);
        return mapearDetalle(guardado);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse asignarRecursos(UUID id, PatrullajeAsignarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO || pat.getEstado() == EstadoPatrullajeSerenazgo.CANCELADO) {
            throw new IllegalStateException("No se pueden modificar los recursos de un patrullaje finalizado o cancelado.");
        }

        PersonalSerenazgo responsable = personalRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new IllegalArgumentException("Responsable no encontrado con ID: " + request.getResponsableId()));
        validarDisponibilidadPersonal(responsable, pat.getId());
        pat.setResponsable(responsable);

        if (request.getUnidadMovilId() != null) {
            UnidadMovilSerenazgo unidad = unidadMovilRepository.findById(request.getUnidadMovilId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidad móvil no encontrada: " + request.getUnidadMovilId()));
            validarDisponibilidadUnidad(unidad, pat.getId());
            pat.setUnidadMovil(unidad);
        } else {
            pat.setUnidadMovil(null);
        }

        // Reasignar cuadrilla
        pat.getParticipantes().clear();
        if (request.getParticipantesIds() != null) {
            for (UUID pId : request.getParticipantesIds()) {
                if (pId.equals(responsable.getId())) continue;
                PersonalSerenazgo part = personalRepository.findById(pId)
                        .orElseThrow(() -> new IllegalArgumentException("Personal no encontrado: " + pId));
                validarDisponibilidadPersonal(part, pat.getId());
                pat.getParticipantes().add(ParticipantePatrullajeSerenazgo.builder()
                        .patrullaje(pat)
                        .personal(part)
                        .funcionDesempenada(part.getCargoOperativo() != null ? part.getCargoOperativo().getNombre() : "Patrullero")
                        .fechaIncorporacion(OffsetDateTime.now(ZONA_LIMA))
                        .build());
            }
        }

        // Reasignar equipamiento
        pat.getEquipos().clear();
        if (request.getEquipamientoIds() != null) {
            for (UUID eqId : request.getEquipamientoIds()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(eqId)
                        .orElseThrow(() -> new IllegalArgumentException("Equipamiento no encontrado: " + eqId));
                pat.getEquipos().add(PatrullajeEquipamiento.builder()
                        .patrullaje(pat)
                        .equipamiento(eq)
                        .cantidad(1)
                        .estadoEntrega("ENTREGADO_OPERATIVO")
                        .observaciones("Asignado para guardia")
                        .build());
            }
        }

        if (request.getObservaciones() != null) {
            pat.setObservaciones(request.getObservaciones());
        }

        String estadoAnterior = pat.getEstado().name();
        if (pat.getEstado() == EstadoPatrullajeSerenazgo.PROGRAMADO || pat.getEstado() == EstadoPatrullajeSerenazgo.BORRADOR) {
            pat.setEstado(EstadoPatrullajeSerenazgo.ASIGNADO);
        }
        pat.setUpdatedBy(username);

        registrarHistorial(pat, AccionHistorialPatrullaje.ASIGNACION.name(), estadoAnterior, pat.getEstado().name(),
                "Asignación de recursos humanos, unidad móvil y equipamiento operativo", username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse iniciarPatrullaje(UUID id, PatrullajeIniciarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.PROGRAMADO && pat.getEstado() != EstadoPatrullajeSerenazgo.ASIGNADO) {
            throw new IllegalStateException("Solo se puede iniciar un patrullaje en estado PROGRAMADO o ASIGNADO. Estado actual: " + pat.getEstado());
        }

        validarDisponibilidadPersonal(pat.getResponsable(), pat.getId());
        if (pat.getUnidadMovil() != null) {
            validarDisponibilidadUnidad(pat.getUnidadMovil(), pat.getId());
        }

        OffsetDateTime inicioReal = (request != null && request.getFechaHoraInicioReal() != null)
                ? request.getFechaHoraInicioReal().atZone(ZONA_LIMA).toOffsetDateTime()
                : OffsetDateTime.now(ZONA_LIMA);

        pat.setFechaHoraInicioReal(inicioReal);
        pat.setFechaInicio(inicioReal);

        if (request != null && request.getKmInicio() != null) {
            pat.setKmInicio(request.getKmInicio());
        }

        if (request != null && request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            pat.setObservaciones((pat.getObservaciones() != null ? pat.getObservaciones() + " | " : "") + request.getObservaciones());
        }

        String estadoAnterior = pat.getEstado().name();
        pat.setEstado(EstadoPatrullajeSerenazgo.EN_CURSO);
        pat.setUpdatedBy(username);

        registrarHistorial(pat, AccionHistorialPatrullaje.INICIO.name(), estadoAnterior, EstadoPatrullajeSerenazgo.EN_CURSO.name(),
                "Inicio efectivo del patrullaje en campo por " + pat.getResponsable().getPersona().getNombreCompleto(), username);

        log.info("Patrullaje {} iniciado exitosamente por {}", pat.getCodigo(), username);
        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse registrarPasoPunto(UUID id, PatrullajeRegistrarVisitaRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.EN_CURSO) {
            throw new IllegalStateException("Solo se pueden registrar visitas a puntos de control en patrullajes EN CURSO.");
        }

        PuntoControlRuta punto = puntoControlRepository.findById(request.getPuntoControlId())
                .orElseThrow(() -> new IllegalArgumentException("Punto de control no válido con ID: " + request.getPuntoControlId()));

        if (pat.getRuta() == null || !punto.getRuta().getId().equals(pat.getRuta().getId())) {
            throw new IllegalArgumentException("El punto de control no pertenece a la ruta asignada al patrullaje.");
        }

        if (request.getEstadoVisita() == EstadoVisitaPunto.OMITIDO &&
                (request.getMotivoOmision() == null || request.getMotivoOmision().trim().isEmpty())) {
            throw new IllegalArgumentException("Para omitir un punto de control es obligatorio detallar el motivo de la omisión.");
        }

        // Buscar si ya fue registrado previamente para actualizarlo o registrar nuevo
        Optional<PatrullajeVisitaPunto> visitaPrevia = visitaPuntoRepository.findByPatrullajeIdAndPuntoControlId(pat.getId(), punto.getId());
        PatrullajeVisitaPunto visita;
        if (visitaPrevia.isPresent()) {
            visita = visitaPrevia.get();
            visita.setEstadoVisita(request.getEstadoVisita());
            visita.setMetodoRegistro(request.getMetodoRegistro() != null ? request.getMetodoRegistro() : MetodoRegistroVisita.MANUAL);
            visita.setMotivoOmision(request.getMotivoOmision());
            visita.setLatitudRegistro(request.getLatitudRegistro());
            visita.setLongitudRegistro(request.getLongitudRegistro());
            visita.setObservaciones(request.getObservaciones());
            visita.setFechaHoraRegistro(OffsetDateTime.now(ZONA_LIMA));
        } else {
            int ordenVisita = pat.getVisitasPuntos().size() + 1;
            visita = PatrullajeVisitaPunto.builder()
                    .patrullaje(pat)
                    .puntoControl(punto)
                    .ordenVisita(ordenVisita)
                    .fechaHoraRegistro(OffsetDateTime.now(ZONA_LIMA))
                    .personalRegistro(pat.getResponsable())
                    .metodoRegistro(request.getMetodoRegistro() != null ? request.getMetodoRegistro() : MetodoRegistroVisita.MANUAL)
                    .estadoVisita(request.getEstadoVisita())
                    .motivoOmision(request.getMotivoOmision())
                    .latitudRegistro(request.getLatitudRegistro())
                    .longitudRegistro(request.getLongitudRegistro())
                    .observaciones(request.getObservaciones())
                    .build();
            pat.getVisitasPuntos().add(visita);
        }

        visitaPuntoRepository.save(visita);
        pat.setUpdatedBy(username);

        String desc = request.getEstadoVisita() == EstadoVisitaPunto.VISITADO
                ? "Paso registrado por punto " + punto.getCodigo() + " - " + punto.getNombre() + " (" + visita.getMetodoRegistro().getDescripcion() + ")"
                : "Punto omitido con justificación: " + punto.getCodigo() + " - Motivo: " + request.getMotivoOmision();

        registrarHistorial(pat, AccionHistorialPatrullaje.VISITA_PUNTO.name(), pat.getEstado().name(), pat.getEstado().name(), desc, username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse registrarNovedad(UUID id, PatrullajeRegistrarNovedadRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.EN_CURSO && pat.getEstado() != EstadoPatrullajeSerenazgo.EN_PAUSA) {
            throw new IllegalStateException("Solo se pueden registrar novedades durante patrullajes EN CURSO o EN PAUSA.");
        }

        IncidenciaSeguridadCiudadana inc = null;
        if (request.getIncidenciaVinculadaId() != null) {
            inc = incidenciaRepository.findById(request.getIncidenciaVinculadaId())
                    .orElseThrow(() -> new IllegalArgumentException("Incidencia vinculada no encontrada: " + request.getIncidenciaVinculadaId()));
            inc.setPatrullaje(pat);
        }

        PatrullajeNovedad nov = PatrullajeNovedad.builder()
                .patrullaje(pat)
                .tipoNovedad(request.getTipoNovedad())
                .fechaHora(OffsetDateTime.now(ZONA_LIMA))
                .descripcion(request.getDescripcion().trim())
                .personalReporta(pat.getResponsable())
                .incidenciaVinculada(inc)
                .observaciones(request.getObservaciones())
                .build();

        pat.getNovedades().add(nov);
        novedadRepository.save(nov);
        pat.setUpdatedBy(username);

        registrarHistorial(pat, AccionHistorialPatrullaje.REGISTRO_NOVEDAD.name(), pat.getEstado().name(), pat.getEstado().name(),
                "Novedad reportada: " + request.getTipoNovedad().getDescripcion() + " - " + request.getDescripcion(), username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse pausarPatrullaje(UUID id, PatrullajePausarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.EN_CURSO) {
            throw new IllegalStateException("Solo se puede pausar un patrullaje que se encuentre EN CURSO.");
        }

        pat.setMotivoPausa(request.getMotivo().trim());
        String estadoAnterior = pat.getEstado().name();
        pat.setEstado(EstadoPatrullajeSerenazgo.EN_PAUSA);
        pat.setUpdatedBy(username);

        // También registrar como novedad operativa
        PatrullajeNovedad nov = PatrullajeNovedad.builder()
                .patrullaje(pat)
                .tipoNovedad(TipoNovedadPatrullaje.PAUSA_OPERATIVA)
                .fechaHora(OffsetDateTime.now(ZONA_LIMA))
                .descripcion("Pausa operativa: " + request.getMotivo().trim())
                .personalReporta(pat.getResponsable())
                .build();
        pat.getNovedades().add(nov);
        novedadRepository.save(nov);

        registrarHistorial(pat, AccionHistorialPatrullaje.PAUSA.name(), estadoAnterior, EstadoPatrullajeSerenazgo.EN_PAUSA.name(),
                "Pausa operativa declarada. Motivo: " + request.getMotivo().trim(), username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse reanudarPatrullaje(UUID id, PatrullajeReanudarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.EN_PAUSA) {
            throw new IllegalStateException("Solo se puede reanudar un patrullaje que se encuentre EN PAUSA.");
        }

        String estadoAnterior = pat.getEstado().name();
        pat.setEstado(EstadoPatrullajeSerenazgo.EN_CURSO);
        pat.setMotivoPausa(null);
        pat.setUpdatedBy(username);

        String obs = (request != null && request.getObservaciones() != null) ? request.getObservaciones() : "Sin observaciones adicionales";
        registrarHistorial(pat, AccionHistorialPatrullaje.REANUDACION.name(), estadoAnterior, EstadoPatrullajeSerenazgo.EN_CURSO.name(),
                "Reanudación del patrullaje preventivo en ruta. " + obs, username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse cambiarRuta(UUID id, PatrullajeCambioRutaRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO || pat.getEstado() == EstadoPatrullajeSerenazgo.CANCELADO) {
            throw new IllegalStateException("No se puede cambiar la ruta de un patrullaje finalizado o cancelado.");
        }

        RutaPatrullaje nuevaRuta = rutaRepository.findById(request.getNuevaRutaId())
                .orElseThrow(() -> new IllegalArgumentException("Nueva ruta no encontrada con ID: " + request.getNuevaRutaId()));

        String rutaAnteriorNombre = pat.getRuta() != null ? pat.getRuta().getNombre() : "Sin ruta fija";
        pat.setRuta(nuevaRuta);
        pat.setCambioRutaMotivo(request.getMotivo().trim());
        pat.setUpdatedBy(username);

        // Registrar como novedad
        PatrullajeNovedad nov = PatrullajeNovedad.builder()
                .patrullaje(pat)
                .tipoNovedad(TipoNovedadPatrullaje.CAMBIO_RECORRIDO)
                .fechaHora(OffsetDateTime.now(ZONA_LIMA))
                .descripcion("Cambio de ruta autorizado de [" + rutaAnteriorNombre + "] a [" + nuevaRuta.getNombre() + "]. Motivo: " + request.getMotivo().trim())
                .personalReporta(pat.getResponsable())
                .build();
        pat.getNovedades().add(nov);
        novedadRepository.save(nov);

        registrarHistorial(pat, AccionHistorialPatrullaje.CAMBIO_RUTA.name(), pat.getEstado().name(), pat.getEstado().name(),
                "Cambio de ruta autorizado a: " + nuevaRuta.getNombre() + ". Justificación: " + request.getMotivo().trim(), username);

        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse finalizarPatrullaje(UUID id, PatrullajeFinalizarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() != EstadoPatrullajeSerenazgo.EN_CURSO && pat.getEstado() != EstadoPatrullajeSerenazgo.EN_PAUSA) {
            throw new IllegalStateException("Solo se puede finalizar un patrullaje que se encuentre EN CURSO o EN PAUSA.");
        }

        OffsetDateTime finReal = (request != null && request.getFechaHoraFinReal() != null)
                ? request.getFechaHoraFinReal().atZone(ZONA_LIMA).toOffsetDateTime()
                : OffsetDateTime.now(ZONA_LIMA);

        pat.setFechaHoraFinReal(finReal);
        pat.setFechaFin(finReal);

        if (request != null && request.getKmFin() != null) {
            if (pat.getKmInicio() != null && request.getKmFin().compareTo(pat.getKmInicio()) < 0) {
                throw new IllegalArgumentException("El kilometraje final no puede ser inferior al inicial.");
            }
            pat.setKmFin(request.getKmFin());
        }

        pat.setResultadoAdministrativo(request.getResultadoAdministrativo().trim());
        if (request.getObservaciones() != null) {
            pat.setObservaciones((pat.getObservaciones() != null ? pat.getObservaciones() + " | " : "") + request.getObservaciones());
        }

        // Marcar devolución de equipamiento
        for (PatrullajeEquipamiento eq : pat.getEquipos()) {
            if (eq.getFechaDevolucion() == null) {
                eq.setFechaDevolucion(finReal);
                eq.setEstadoDevolucion("DEVUELTO_CONFORME");
            }
        }

        String estadoAnterior = pat.getEstado().name();
        pat.setEstado(EstadoPatrullajeSerenazgo.FINALIZADO);
        pat.setUpdatedBy(username);

        registrarHistorial(pat, AccionHistorialPatrullaje.FINALIZACION.name(), estadoAnterior, EstadoPatrullajeSerenazgo.FINALIZADO.name(),
                "Finalización formal del patrullaje. Resultado: " + request.getResultadoAdministrativo().trim(), username);

        log.info("Patrullaje {} finalizado con éxito por {}", pat.getCodigo(), username);
        return mapearDetalle(pat);
    }

    @Override
    @Transactional
    public PatrullajeDetailResponse cancelarPatrullaje(UUID id, PatrullajeCancelarRequest request, String username) {
        PatrullajeSerenazgo pat = patrullajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patrullaje no encontrado: " + id));

        if (pat.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO || pat.getEstado() == EstadoPatrullajeSerenazgo.CANCELADO) {
            throw new IllegalStateException("El patrullaje ya se encuentra cerrado o cancelado.");
        }

        pat.setMotivoCancelacion(request.getMotivo().trim());
        String estadoAnterior = pat.getEstado().name();
        pat.setEstado(EstadoPatrullajeSerenazgo.CANCELADO);
        pat.setUpdatedBy(username);

        // Liberar equipamiento
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        for (PatrullajeEquipamiento eq : pat.getEquipos()) {
            if (eq.getFechaDevolucion() == null) {
                eq.setFechaDevolucion(ahora);
                eq.setEstadoDevolucion("DEVUELTO_POR_CANCELACION");
            }
        }

        registrarHistorial(pat, AccionHistorialPatrullaje.CANCELACION.name(), estadoAnterior, EstadoPatrullajeSerenazgo.CANCELADO.name(),
                "Cancelación de patrullaje justificada: " + request.getMotivo().trim(), username);

        log.info("Patrullaje {} cancelado por {}. Motivo: {}", pat.getCodigo(), username, request.getMotivo());
        return mapearDetalle(pat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaPatrullajeResponse> listarRutasPorSector(UUID sectorId) {
        return rutaRepository.findBySectorIdAndEstado(sectorId, "ACTIVA").stream()
                .map(this::mapearRuta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaPatrullajeResponse> listarRutasActivas() {
        return rutaRepository.findByEstadoOrderByNombreAsc("ACTIVA").stream()
                .map(this::mapearRuta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RutaPatrullajeResponse obtenerRutaPorId(UUID rutaId) {
        RutaPatrullaje r = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + rutaId));
        return mapearRuta(r);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(PatrullajeFilter filter) {
        List<PatrullajeResponse> lista = listarPatrullajesParaExportacion(filter);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Patrullajes y Recorridos");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setBold(true);
            fontH.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontH);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - SUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("REPORTE OFICIAL DE GESTIÓN DE PATRULLAJES, RUTAS Y RECORRIDOS PREVENTIVOS");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("AVISO: Servicio preventivo de Serenazgo en el marco de la Ley N° 31297. No constituye atestado ni intervención judicial.");

            Row headerRow = sheet.createRow(4);
            String[] headers = {"Código", "Modalidad", "Sector Territorial", "Zona / Cuadrante", "Ruta Oficial", "Responsable", "Móvil / Placa", "Inicio Programado", "Fin Programado", "Inicio Real", "Fin Real", "Checkpoints Visitados", "Novedades", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (PatrullajeResponse p : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getCodigo());
                row.createCell(1).setCellValue(p.getTipoPatrullajeDescripcion());
                row.createCell(2).setCellValue(p.getSectorNombre() != null ? p.getSectorNombre() : "-");
                row.createCell(3).setCellValue(p.getZonaNombre() != null ? p.getZonaNombre() : "-");
                row.createCell(4).setCellValue(p.getRutaNombre() != null ? p.getRutaNombre() : "Sin ruta predefinida");
                row.createCell(5).setCellValue(p.getResponsableNombre() != null ? p.getResponsableNombre() : "-");
                row.createCell(6).setCellValue(p.getUnidadPlaca() != null ? p.getUnidadPlaca() : "A pie / Pedestre");
                row.createCell(7).setCellValue(p.getFechaHoraInicioProgramada() != null ? p.getFechaHoraInicioProgramada().format(FORMATO_FECHA_HORA) : "-");
                row.createCell(8).setCellValue(p.getFechaHoraFinProgramada() != null ? p.getFechaHoraFinProgramada().format(FORMATO_FECHA_HORA) : "-");
                row.createCell(9).setCellValue(p.getFechaHoraInicioReal() != null ? p.getFechaHoraInicioReal().format(FORMATO_FECHA_HORA) : "-");
                row.createCell(10).setCellValue(p.getFechaHoraFinReal() != null ? p.getFechaHoraFinReal().format(FORMATO_FECHA_HORA) : "-");
                row.createCell(11).setCellValue(p.getPuntosVisitados() + " / " + p.getTotalPuntosRuta());
                row.createCell(12).setCellValue(p.getTotalNovedades());
                row.createCell(13).setCellValue(p.getEstadoDescripcion());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar Excel de patrullajes", e);
            throw new RuntimeException("Error al exportar planilla de patrullajes", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(UUID id) {
        PatrullajeDetailResponse det = obtenerPatrullajePorId(id);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.NORMAL, new java.awt.Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new java.awt.Color(71, 85, 105));
            Font fontAviso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.NORMAL, new java.awt.Color(185, 28, 28));
            Font fontSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, new java.awt.Color(29, 78, 216));
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, java.awt.Color.BLACK);

            Paragraph p1 = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph pSub = new Paragraph("SUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO - SIGM-QUINUA", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            document.add(pSub);

            Paragraph p2 = new Paragraph("FICHA DE CONTROL Y SEGUIMIENTO OPERATIVO DE PATRULLAJE: " + det.getCodigo(), fontSec);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(4);
            document.add(p2);

            Paragraph pAviso = new Paragraph("AVISO OFICIAL: Registro administrativo preventivo de Serenazgo en concordancia con la Ley N° 31297. No constituye atestado penal ni diligencia de instrucción fiscal.", fontAviso);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(8);
            document.add(pAviso);

            // Tabla de datos principales
            PdfPTable tInfo = new PdfPTable(4);
            tInfo.setWidthPercentage(100);
            tInfo.setWidths(new float[]{25, 25, 25, 25});

            tInfo.addCell(new PdfPCell(new Phrase("Código Patrullaje:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getCodigo(), fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Estado Operativo:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getEstadoDescripcion(), fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Modalidad:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getTipoPatrullajeDescripcion(), fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Tipo Especial:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(Boolean.TRUE.equals(det.getEsExtraordinario()) ? "EXTRAORDINARIO" : "ORDINARIO", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Sector Territorial:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getSectorNombre() != null ? det.getSectorNombre() : "-", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Zona / Cuadrante:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getZonaNombre() != null ? det.getZonaNombre() : "Sector General", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Ruta Autorizada:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getRutaNombre() != null ? det.getRutaNombre() : "Recorrido Preventivo Flexible", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Distancia Ruta:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getRutaDistanciaKm() != null ? det.getRutaDistanciaKm() + " km aprox." : "-", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Inicio Programado:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaHoraInicioProgramada() != null ? det.getFechaHoraInicioProgramada().format(FORMATO_FECHA_HORA) : "-", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Fin Programado:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaHoraFinProgramada() != null ? det.getFechaHoraFinProgramada().format(FORMATO_FECHA_HORA) : "-", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Inicio Real:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaHoraInicioReal() != null ? det.getFechaHoraInicioReal().format(FORMATO_FECHA_HORA) : "No iniciado", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Fin Real:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getFechaHoraFinReal() != null ? det.getFechaHoraFinReal().format(FORMATO_FECHA_HORA) : "En proceso", fontNormal)));

            tInfo.addCell(new PdfPCell(new Phrase("Sereno Responsable:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getResponsableNombre() + " (" + det.getResponsableCodigo() + ")", fontNormal)));
            tInfo.addCell(new PdfPCell(new Phrase("Unidad Móvil:", fontBold)));
            tInfo.addCell(new PdfPCell(new Phrase(det.getUnidadPlaca() != null ? det.getUnidadTipo() + " [" + det.getUnidadPlaca() + "]" : "Sin vehículo (Patrullaje a pie)", fontNormal)));

            if (det.getKmInicio() != null || det.getKmFin() != null) {
                tInfo.addCell(new PdfPCell(new Phrase("Kilometraje Inicio:", fontBold)));
                tInfo.addCell(new PdfPCell(new Phrase(det.getKmInicio() != null ? det.getKmInicio().toString() : "-", fontNormal)));
                tInfo.addCell(new PdfPCell(new Phrase("Kilometraje Fin:", fontBold)));
                tInfo.addCell(new PdfPCell(new Phrase(det.getKmFin() != null ? det.getKmFin().toString() : "-", fontNormal)));
            }

            document.add(tInfo);

            Paragraph pObj = new Paragraph("\nOBJETIVO OPERATIVO DEL PATRULLAJE:", fontSec);
            document.add(pObj);
            Paragraph pObjText = new Paragraph(det.getObjetivoOperativo() != null ? det.getObjetivoOperativo() : "Patrullaje preventivo ordinario de disuasión y orden público.", fontNormal);
            pObjText.setSpacingAfter(8);
            document.add(pObjText);

            if (det.getResultadoAdministrativo() != null && !det.getResultadoAdministrativo().isBlank()) {
                Paragraph pRes = new Paragraph("RESULTADO ADMINISTRATIVO Y CONCLUSIONES:", fontSec);
                document.add(pRes);
                Paragraph pResText = new Paragraph(det.getResultadoAdministrativo(), fontNormal);
                pResText.setSpacingAfter(8);
                document.add(pResText);
            }

            // Puntos de Control y Visitas
            if (!det.getPuntosControlRuta().isEmpty() || !det.getVisitasPuntos().isEmpty()) {
                Paragraph pPuntos = new Paragraph("PASOS POR PUNTOS DE CONTROL EN RUTA:", fontSec);
                pPuntos.setSpacingAfter(4);
                document.add(pPuntos);

                PdfPTable tPts = new PdfPTable(5);
                tPts.setWidthPercentage(100);
                tPts.setWidths(new float[]{10, 30, 20, 20, 20});

                tPts.addCell(new PdfPCell(new Phrase("Orden", fontBold)));
                tPts.addCell(new PdfPCell(new Phrase("Hito / Punto de Control", fontBold)));
                tPts.addCell(new PdfPCell(new Phrase("Ubicación Referencial", fontBold)));
                tPts.addCell(new PdfPCell(new Phrase("Hora Registro", fontBold)));
                tPts.addCell(new PdfPCell(new Phrase("Estado Visita", fontBold)));

                for (PuntoControlResponse pc : det.getPuntosControlRuta()) {
                    Optional<PatrullajeVisitaPuntoResponse> vis = det.getVisitasPuntos().stream()
                            .filter(v -> v.getPuntoControlId().equals(pc.getId()))
                            .findFirst();

                    tPts.addCell(new PdfPCell(new Phrase(String.valueOf(pc.getOrden()), fontNormal)));
                    tPts.addCell(new PdfPCell(new Phrase(pc.getCodigo() + " - " + pc.getNombre(), fontNormal)));
                    tPts.addCell(new PdfPCell(new Phrase(pc.getReferenciaUbicacion() != null ? pc.getReferenciaUbicacion() : "-", fontNormal)));

                    if (vis.isPresent()) {
                        tPts.addCell(new PdfPCell(new Phrase(vis.get().getFechaHoraRegistro().format(FORMATO_FECHA_HORA), fontNormal)));
                        String estVis = vis.get().getEstadoVisita() == EstadoVisitaPunto.VISITADO ? "VISITADO (" + vis.get().getMetodoRegistro().name() + ")" : "OMITIDO: " + vis.get().getMotivoOmision();
                        tPts.addCell(new PdfPCell(new Phrase(estVis, fontNormal)));
                    } else {
                        tPts.addCell(new PdfPCell(new Phrase("-", fontNormal)));
                        tPts.addCell(new PdfPCell(new Phrase("PENDIENTE", fontNormal)));
                    }
                }
                document.add(tPts);
            }

            // Novedades
            if (!det.getNovedades().isEmpty()) {
                Paragraph pNov = new Paragraph("\nNOVEDADES OPERATIVAS REPORTADAS (" + det.getNovedades().size() + "):", fontSec);
                pNov.setSpacingAfter(4);
                document.add(pNov);

                PdfPTable tNov = new PdfPTable(4);
                tNov.setWidthPercentage(100);
                tNov.setWidths(new float[]{18, 22, 45, 15});

                tNov.addCell(new PdfPCell(new Phrase("Fecha / Hora", fontBold)));
                tNov.addCell(new PdfPCell(new Phrase("Tipo de Novedad", fontBold)));
                tNov.addCell(new PdfPCell(new Phrase("Descripción del Suceso", fontBold)));
                tNov.addCell(new PdfPCell(new Phrase("Incidencia", fontBold)));

                for (PatrullajeNovedadResponse n : det.getNovedades()) {
                    tNov.addCell(new PdfPCell(new Phrase(n.getFechaHora().format(FORMATO_FECHA_HORA), fontNormal)));
                    tNov.addCell(new PdfPCell(new Phrase(n.getTipoNovedadDescripcion(), fontNormal)));
                    tNov.addCell(new PdfPCell(new Phrase(n.getDescripcion(), fontNormal)));
                    tNov.addCell(new PdfPCell(new Phrase(n.getIncidenciaVinculadaCodigo() != null ? n.getIncidenciaVinculadaCodigo() : "-", fontNormal)));
                }
                document.add(tNov);
            }

            // Cuadrilla participante
            if (!det.getParticipantes().isEmpty()) {
                Paragraph pPart = new Paragraph("\nPERSONAL DE CUADRILLA PARTICIPANTE:", fontSec);
                pPart.setSpacingAfter(4);
                document.add(pPart);

                PdfPTable tPart = new PdfPTable(3);
                tPart.setWidthPercentage(100);
                tPart.setWidths(new float[]{25, 45, 30});

                tPart.addCell(new PdfPCell(new Phrase("Código", fontBold)));
                tPart.addCell(new PdfPCell(new Phrase("Nombres y Apellidos", fontBold)));
                tPart.addCell(new PdfPCell(new Phrase("Función / Cargo", fontBold)));

                for (PatrullajeParticipanteResponse part : det.getParticipantes()) {
                    tPart.addCell(new PdfPCell(new Phrase(part.getCodigoInterno(), fontNormal)));
                    tPart.addCell(new PdfPCell(new Phrase(part.getNombreCompleto(), fontNormal)));
                    tPart.addCell(new PdfPCell(new Phrase(part.getFuncionDesempenada(), fontNormal)));
                }
                document.add(tPart);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de patrullaje", e);
            throw new RuntimeException("Error al generar la ficha técnica de patrullaje en PDF", e);
        }
    }

    // =========================================================================
    // Métodos Auxiliares y Validaciones de Dominio
    // =========================================================================

    private void validarFechasProgramadas(PatrullajeCrearRequest request) {
        if (request.getFechaHoraInicioProgramada() == null || request.getFechaHoraFinProgramada() == null) {
            throw new IllegalArgumentException("Las fechas y horas programadas son requeridas.");
        }
        if (request.getFechaHoraFinProgramada().isBefore(request.getFechaHoraInicioProgramada())) {
            throw new IllegalArgumentException("La fecha y hora de fin programada no puede ser anterior a la de inicio.");
        }
    }

    private void validarDisponibilidadPersonal(PersonalSerenazgo personal, UUID patrullajeIdExcluir) {
        if (personal.getEstado() != null && personal.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            throw new IllegalStateException("El efectivo " + personal.getPersona().getNombreCompleto() + " no se encuentra en estado ACTIVO.");
        }

        List<EstadoPatrullajeSerenazgo> estadosActivos = List.of(
                EstadoPatrullajeSerenazgo.EN_CURSO,
                EstadoPatrullajeSerenazgo.EN_PAUSA
        );

        boolean conflicto = patrullajeRepository.existePatrullajeActivoParaResponsable(personal.getId(), estadosActivos, patrullajeIdExcluir);
        if (conflicto) {
            throw new IllegalStateException("El efectivo " + personal.getPersona().getNombreCompleto() + " ya se encuentra asignado a un patrullaje activo en curso.");
        }
    }

    private void validarDisponibilidadUnidad(UnidadMovilSerenazgo unidad, UUID patrullajeIdExcluir) {
        if (unidad.getEstadoOperativo() != null && !unidad.getEstadoOperativo().isDisponiblePatrullaje()) {
            throw new IllegalStateException("La unidad móvil con placa " + unidad.getPlaca() + " no está disponible para servicio. Estado: " + unidad.getEstadoOperativo());
        }

        List<EstadoPatrullajeSerenazgo> estadosActivos = List.of(
                EstadoPatrullajeSerenazgo.EN_CURSO,
                EstadoPatrullajeSerenazgo.EN_PAUSA
        );

        boolean conflicto = patrullajeRepository.existePatrullajeActivoParaUnidad(unidad.getId(), estadosActivos, patrullajeIdExcluir);
        if (conflicto) {
            throw new IllegalStateException("La unidad móvil " + unidad.getPlaca() + " ya se encuentra asignada a un patrullaje activo en curso.");
        }
    }

    private synchronized String generarCodigoPatrullaje() {
        Long seq = patrullajeRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        return String.format("PAT-%d-%05d", anio, seq);
    }

    private void registrarHistorial(PatrullajeSerenazgo pat, String accion, String estadoAnt, String estadoNuevo, String descripcion, String username) {
        PatrullajeHistorial h = PatrullajeHistorial.builder()
                .patrullaje(pat)
                .accion(accion)
                .estadoAnterior(estadoAnt)
                .estadoNuevo(estadoNuevo)
                .descripcion(descripcion)
                .username(username != null ? username : "SISTEMA")
                .fecha(OffsetDateTime.now(ZONA_LIMA))
                .build();
        PatrullajeHistorial guardado = historialRepository.save(h);
        if (pat.getHistorial() != null) {
            pat.getHistorial().add(guardado);
        }
    }

    private Specification<PatrullajeSerenazgo> crearSpecification(PatrullajeFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f == null) return cb.and(predicates.toArray(new Predicate[0]));

            if (f.getCodigo() != null && !f.getCodigo().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("codigo")), "%" + f.getCodigo().trim().toLowerCase() + "%"));
            }

            if (f.getTermino() != null && !f.getTermino().isBlank()) {
                String term = "%" + f.getTermino().trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigo")), term);
                Predicate porObj = cb.like(cb.lower(root.get("objetivoOperativo")), term);
                predicates.add(cb.or(porCodigo, porObj));
            }

            if (f.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), f.getEstado()));
            }

            if (f.getTipoPatrullaje() != null) {
                predicates.add(cb.equal(root.get("tipoPatrullaje"), f.getTipoPatrullaje()));
            }

            if (f.getSectorId() != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), f.getSectorId()));
            }

            if (f.getZonaId() != null) {
                predicates.add(cb.equal(root.get("zona").get("id"), f.getZonaId()));
            }

            if (f.getRutaId() != null) {
                predicates.add(cb.equal(root.get("ruta").get("id"), f.getRutaId()));
            }

            if (f.getResponsableId() != null) {
                predicates.add(cb.equal(root.get("responsable").get("id"), f.getResponsableId()));
            }

            if (f.getUnidadMovilId() != null) {
                predicates.add(cb.equal(root.get("unidadMovil").get("id"), f.getUnidadMovilId()));
            }

            if (f.getDesde() != null) {
                OffsetDateTime desdeDt = f.getDesde().atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHoraInicioProgramada"), desdeDt));
            }

            if (f.getHasta() != null) {
                OffsetDateTime hastaDt = f.getHasta().plusDays(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.lessThan(root.get("fechaHoraInicioProgramada"), hastaDt));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PatrullajeResponse mapearResumen(PatrullajeSerenazgo p) {
        int totalPuntos = p.getRuta() != null && p.getRuta().getPuntosControl() != null ? p.getRuta().getPuntosControl().size() : 0;
        int puntosVis = p.getVisitasPuntos() != null
                ? (int) p.getVisitasPuntos().stream().filter(v -> v.getEstadoVisita() == EstadoVisitaPunto.VISITADO).count()
                : 0;
        int puntosOm = p.getVisitasPuntos() != null
                ? (int) p.getVisitasPuntos().stream().filter(v -> v.getEstadoVisita() == EstadoVisitaPunto.OMITIDO).count()
                : 0;

        return PatrullajeResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .tipoPatrullaje(p.getTipoPatrullaje())
                .tipoPatrullajeDescripcion(p.getTipoPatrullaje() != null ? p.getTipoPatrullaje().getDescripcion() : "-")
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().getDescripcion() : "-")
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorCodigo(p.getSector() != null ? p.getSector().getCodigo() : "-")
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "-")
                .zonaId(p.getZona() != null ? p.getZona().getId() : null)
                .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                .rutaId(p.getRuta() != null ? p.getRuta().getId() : null)
                .rutaCodigo(p.getRuta() != null ? p.getRuta().getCodigo() : null)
                .rutaNombre(p.getRuta() != null ? p.getRuta().getNombre() : null)
                .responsableId(p.getResponsable() != null ? p.getResponsable().getId() : null)
                .responsableCodigo(p.getResponsable() != null ? p.getResponsable().getCodigoInterno() : "-")
                .responsableNombre(p.getResponsable() != null && p.getResponsable().getPersona() != null ? p.getResponsable().getPersona().getNombreCompleto() : "-")
                .unidadMovilId(p.getUnidadMovil() != null ? p.getUnidadMovil().getId() : null)
                .unidadPlaca(p.getUnidadMovil() != null ? p.getUnidadMovil().getPlaca() : null)
                .unidadTipo(p.getUnidadMovil() != null && p.getUnidadMovil().getTipoUnidad() != null ? p.getUnidadMovil().getTipoUnidad().getDescripcion() : null)
                .fechaHoraInicioProgramada(p.getFechaHoraInicioProgramada() != null ? p.getFechaHoraInicioProgramada() : p.getFechaInicio())
                .fechaHoraFinProgramada(p.getFechaHoraFinProgramada() != null ? p.getFechaHoraFinProgramada() : p.getFechaFin())
                .fechaHoraInicioReal(p.getFechaHoraInicioReal())
                .fechaHoraFinReal(p.getFechaHoraFinReal())
                .objetivoOperativo(p.getObjetivoOperativo())
                .esExtraordinario(p.getEsExtraordinario())
                .totalPuntosRuta(totalPuntos)
                .puntosVisitados(puntosVis)
                .puntosOmitidos(puntosOm)
                .totalNovedades(p.getNovedades() != null ? p.getNovedades().size() : 0)
                .totalParticipantes(p.getParticipantes() != null ? p.getParticipantes().size() : 0)
                .kmInicio(p.getKmInicio())
                .kmFin(p.getKmFin())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PatrullajeDetailResponse mapearDetalle(PatrullajeSerenazgo p) {
        PatrullajeDetailResponse det = PatrullajeDetailResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .tipoPatrullaje(p.getTipoPatrullaje())
                .tipoPatrullajeDescripcion(p.getTipoPatrullaje() != null ? p.getTipoPatrullaje().getDescripcion() : "-")
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().getDescripcion() : "-")
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorCodigo(p.getSector() != null ? p.getSector().getCodigo() : "-")
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "-")
                .zonaId(p.getZona() != null ? p.getZona().getId() : null)
                .zonaCodigo(p.getZona() != null ? p.getZona().getCodigo() : null)
                .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                .rutaId(p.getRuta() != null ? p.getRuta().getId() : null)
                .rutaCodigo(p.getRuta() != null ? p.getRuta().getCodigo() : null)
                .rutaNombre(p.getRuta() != null ? p.getRuta().getNombre() : null)
                .rutaDistanciaKm(p.getRuta() != null ? p.getRuta().getDistanciaEstimadaKm() : null)
                .rutaTiempoMin(p.getRuta() != null ? p.getRuta().getTiempoEstimadoMin() : null)
                .programacionServicioId(p.getProgramacionServicio() != null ? p.getProgramacionServicio().getId() : null)
                .programacionServicioCodigo(p.getProgramacionServicio() != null ? p.getProgramacionServicio().getCodigo() : null)
                .oficinaResponsableId(p.getOficinaResponsableId())
                .responsableId(p.getResponsable() != null ? p.getResponsable().getId() : null)
                .responsableCodigo(p.getResponsable() != null ? p.getResponsable().getCodigoInterno() : "-")
                .responsableNombre(p.getResponsable() != null && p.getResponsable().getPersona() != null ? p.getResponsable().getPersona().getNombreCompleto() : "-")
                .responsableCargo(p.getResponsable() != null && p.getResponsable().getCargoOperativo() != null ? p.getResponsable().getCargoOperativo().getNombre() : "-")
                .unidadMovilId(p.getUnidadMovil() != null ? p.getUnidadMovil().getId() : null)
                .unidadPlaca(p.getUnidadMovil() != null ? p.getUnidadMovil().getPlaca() : null)
                .unidadTipo(p.getUnidadMovil() != null && p.getUnidadMovil().getTipoUnidad() != null ? p.getUnidadMovil().getTipoUnidad().getDescripcion() : null)
                .unidadCodigoInterno(p.getUnidadMovil() != null ? p.getUnidadMovil().getCodigoInterno() : null)
                .fechaHoraInicioProgramada(p.getFechaHoraInicioProgramada() != null ? p.getFechaHoraInicioProgramada() : p.getFechaInicio())
                .fechaHoraFinProgramada(p.getFechaHoraFinProgramada() != null ? p.getFechaHoraFinProgramada() : p.getFechaFin())
                .fechaHoraInicioReal(p.getFechaHoraInicioReal())
                .fechaHoraFinReal(p.getFechaHoraFinReal())
                .objetivoOperativo(p.getObjetivoOperativo())
                .descripcion(p.getDescripcion())
                .resultadoAdministrativo(p.getResultadoAdministrativo())
                .observaciones(p.getObservaciones())
                .kmInicio(p.getKmInicio())
                .kmFin(p.getKmFin())
                .motivoPausa(p.getMotivoPausa())
                .motivoCancelacion(p.getMotivoCancelacion())
                .cambioRutaMotivo(p.getCambioRutaMotivo())
                .esExtraordinario(p.getEsExtraordinario())
                .motivoExtraordinario(p.getMotivoExtraordinario())
                .version(p.getVersion())
                .createdBy(p.getCreatedBy())
                .updatedBy(p.getUpdatedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();

        if (p.getKmInicio() != null && p.getKmFin() != null) {
            det.setDistanciaRecorridaEstimadaKm(p.getKmFin().subtract(p.getKmInicio()));
        }

        // Puntos de la ruta
        if (p.getRuta() != null && p.getRuta().getPuntosControl() != null) {
            List<PuntoControlResponse> pcList = p.getRuta().getPuntosControl().stream()
                    .map(pt -> PuntoControlResponse.builder()
                            .id(pt.getId())
                            .orden(pt.getOrden())
                            .codigo(pt.getCodigo())
                            .nombre(pt.getNombre())
                            .descripcion(pt.getDescripcion())
                            .referenciaUbicacion(pt.getReferenciaUbicacion())
                            .latitud(pt.getLatitud())
                            .longitud(pt.getLongitud())
                            .tiempoEstimadoDesdeInicioMin(pt.getTiempoEstimadoDesdeInicioMin())
                            .activo(pt.getActivo())
                            .build())
                    .collect(Collectors.toList());
            det.setPuntosControlRuta(pcList);
            det.setTotalPuntosRuta(pcList.size());
        }

        // Visitas realizadas
        if (p.getVisitasPuntos() != null) {
            List<PatrullajeVisitaPuntoResponse> visList = p.getVisitasPuntos().stream()
                    .map(v -> PatrullajeVisitaPuntoResponse.builder()
                            .id(v.getId())
                            .puntoControlId(v.getPuntoControl().getId())
                            .orden(v.getPuntoControl().getOrden())
                            .codigoPunto(v.getPuntoControl().getCodigo())
                            .nombrePunto(v.getPuntoControl().getNombre())
                            .referenciaUbicacion(v.getPuntoControl().getReferenciaUbicacion())
                            .ordenVisita(v.getOrdenVisita())
                            .fechaHoraRegistro(v.getFechaHoraRegistro())
                            .personalRegistroId(v.getPersonalRegistro().getId())
                            .personalRegistroNombre(v.getPersonalRegistro().getPersona().getNombreCompleto())
                            .metodoRegistro(v.getMetodoRegistro())
                            .estadoVisita(v.getEstadoVisita())
                            .motivoOmision(v.getMotivoOmision())
                            .latitudRegistro(v.getLatitudRegistro())
                            .longitudRegistro(v.getLongitudRegistro())
                            .observaciones(v.getObservaciones())
                            .build())
                    .collect(Collectors.toList());
            det.setVisitasPuntos(visList);

            int visitados = (int) visList.stream().filter(v -> v.getEstadoVisita() == EstadoVisitaPunto.VISITADO).count();
            int omitidos = (int) visList.stream().filter(v -> v.getEstadoVisita() == EstadoVisitaPunto.OMITIDO).count();
            det.setPuntosVisitados(visitados);
            det.setPuntosOmitidos(omitidos);
            det.setPuntosPendientes(Math.max(0, det.getTotalPuntosRuta() - visitados - omitidos));

            if (det.getTotalPuntosRuta() > 0) {
                double perc = ((double) visitados / det.getTotalPuntosRuta()) * 100.0;
                det.setPorcentajeAvance(Math.round(perc * 10.0) / 10.0);
            }
        }

        // Participantes
        if (p.getParticipantes() != null) {
            List<PatrullajeParticipanteResponse> partList = p.getParticipantes().stream()
                    .map(part -> PatrullajeParticipanteResponse.builder()
                            .id(part.getId())
                            .personalId(part.getPersonal().getId())
                            .codigoInterno(part.getPersonal().getCodigoInterno())
                            .nombreCompleto(part.getPersonal().getPersona().getNombreCompleto())
                            .cargo(part.getPersonal().getCargoOperativo() != null ? part.getPersonal().getCargoOperativo().getNombre() : "-")
                            .funcionDesempenada(part.getFuncionDesempenada())
                            .fechaIncorporacion(part.getFechaIncorporacion())
                            .fechaRetiro(part.getFechaRetiro())
                            .build())
                    .collect(Collectors.toList());
            det.setParticipantes(partList);
        }

        // Equipamiento
        if (p.getEquipos() != null) {
            List<PatrullajeEquipamientoResponse> eqList = p.getEquipos().stream()
                    .map(eq -> PatrullajeEquipamientoResponse.builder()
                            .id(eq.getId())
                            .equipamientoId(eq.getEquipamiento().getId())
                            .codigoPatrimonial(eq.getEquipamiento().getCodigoPatrimonial())
                            .codigoInterno(eq.getEquipamiento().getCodigoInterno())
                            .tipoEquipo(eq.getEquipamiento().getTipoEquipo() != null ? eq.getEquipamiento().getTipoEquipo().getDescripcion() : "-")
                            .descripcion(eq.getEquipamiento().getDescripcion())
                            .cantidad(eq.getCantidad())
                            .estadoEntrega(eq.getEstadoEntrega())
                            .estadoDevolucion(eq.getEstadoDevolucion())
                            .fechaDevolucion(eq.getFechaDevolucion())
                            .observaciones(eq.getObservaciones())
                            .build())
                    .collect(Collectors.toList());
            det.setEquipamiento(eqList);
        }

        // Novedades
        if (p.getNovedades() != null) {
            List<PatrullajeNovedadResponse> novList = p.getNovedades().stream()
                    .map(nov -> PatrullajeNovedadResponse.builder()
                            .id(nov.getId())
                            .tipoNovedad(nov.getTipoNovedad())
                            .tipoNovedadDescripcion(nov.getTipoNovedad() != null ? nov.getTipoNovedad().getDescripcion() : "-")
                            .fechaHora(nov.getFechaHora())
                            .descripcion(nov.getDescripcion())
                            .personalReportaId(nov.getPersonalReporta() != null ? nov.getPersonalReporta().getId() : null)
                            .personalReportaNombre(nov.getPersonalReporta() != null && nov.getPersonalReporta().getPersona() != null ? nov.getPersonalReporta().getPersona().getNombreCompleto() : "-")
                            .incidenciaVinculadaId(nov.getIncidenciaVinculada() != null ? nov.getIncidenciaVinculada().getId() : null)
                            .incidenciaVinculadaCodigo(nov.getIncidenciaVinculada() != null ? nov.getIncidenciaVinculada().getCodigo() : null)
                            .observaciones(nov.getObservaciones())
                            .build())
                    .collect(Collectors.toList());
            det.setNovedades(novList);
        }

        // Historial
        List<PatrullajeHistorial> histEntities = (p.getHistorial() != null && !p.getHistorial().isEmpty())
                ? p.getHistorial()
                : (p.getId() != null ? historialRepository.findByPatrullajeIdOrderByFechaDesc(p.getId()) : Collections.emptyList());

        if (histEntities != null) {
            List<PatrullajeHistorialResponse> histList = histEntities.stream()
                    .map(h -> PatrullajeHistorialResponse.builder()
                            .id(h.getId())
                            .accion(h.getAccion())
                            .estadoAnterior(h.getEstadoAnterior())
                            .estadoNuevo(h.getEstadoNuevo())
                            .descripcion(h.getDescripcion())
                            .username(h.getUsername())
                            .fecha(h.getFecha())
                            .build())
                    .collect(Collectors.toList());
            det.setHistorial(histList);
        }

        return det;
    }

    private RutaPatrullajeResponse mapearRuta(RutaPatrullaje r) {
        List<PuntoControlResponse> pcList = (r.getPuntosControl() != null)
                ? r.getPuntosControl().stream()
                .map(pt -> PuntoControlResponse.builder()
                        .id(pt.getId())
                        .orden(pt.getOrden())
                        .codigo(pt.getCodigo())
                        .nombre(pt.getNombre())
                        .descripcion(pt.getDescripcion())
                        .referenciaUbicacion(pt.getReferenciaUbicacion())
                        .latitud(pt.getLatitud())
                        .longitud(pt.getLongitud())
                        .tiempoEstimadoDesdeInicioMin(pt.getTiempoEstimadoDesdeInicioMin())
                        .activo(pt.getActivo())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return RutaPatrullajeResponse.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .sectorId(r.getSector() != null ? r.getSector().getId() : null)
                .sectorNombre(r.getSector() != null ? r.getSector().getNombre() : null)
                .zonaId(r.getZona() != null ? r.getZona().getId() : null)
                .zonaNombre(r.getZona() != null ? r.getZona().getNombre() : null)
                .distanciaEstimadaKm(r.getDistanciaEstimadaKm())
                .tiempoEstimadoMin(r.getTiempoEstimadoMin())
                .estado(r.getEstado())
                .versionRuta(r.getVersionRuta())
                .totalPuntos(pcList.size())
                .puntosControl(pcList)
                .build();
    }
}

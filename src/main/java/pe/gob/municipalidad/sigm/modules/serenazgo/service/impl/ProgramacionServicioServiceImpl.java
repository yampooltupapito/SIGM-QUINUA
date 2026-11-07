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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ProgramacionServicioService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramacionServicioServiceImpl implements ProgramacionServicioService {

    private static final ZoneOffset OFFSET_LIMA = ZoneOffset.of("-05:00");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProgramacionServicioSerenazgoRepository programacionRepository;
    private final TurnoSerenazgoRepository turnoRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final ProgramacionPersonalRepository programacionPersonalRepository;
    private final ProgramacionUnidadRepository programacionUnidadRepository;
    private final ProgramacionEquipamientoRepository programacionEquipamientoRepository;
    private final ProgramacionHistorialRepository historialRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ProgramacionResponse crearProgramacion(ProgramacionCreateRequest request, String username) {
        log.info("Creando nueva programación de servicio para fecha: {} por usuario: {}", request.getFechaServicio(), username);

        // 1. Obtener y validar turno
        TurnoSerenazgo turno = turnoRepository.findById(request.getTurnoId())
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + request.getTurnoId()));

        if (turno.getEstado() != EstadoTurnoSerenazgo.ACTIVO) {
            throw new IllegalStateException("El turno seleccionado no se encuentra activo: " + turno.getNombre());
        }

        // 2. Calcular inicio y fin con regla de cruce de medianoche
        OffsetDateTime[] intervalo = calcularIntervalo(request.getFechaServicio(), turno);
        OffsetDateTime fechaHoraInicio = intervalo[0];
        OffsetDateTime fechaHoraFin = intervalo[1];

        // 3. Validar sector y zona
        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado con ID: " + request.getSectorId()));
            if (sector.getEstado() != EstadoSectorPatrullaje.ACTIVO) {
                throw new IllegalStateException("El sector seleccionado no se encuentra activo: " + sector.getNombre());
            }
        }

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada con ID: " + request.getZonaId()));
            if (sector != null && (zona.getSector() == null || !zona.getSector().getId().equals(sector.getId()))) {
                throw new IllegalArgumentException("La zona seleccionada no pertenece al sector indicado");
            }
        }

        // 4. Validar oficina y responsable
        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("Oficina no encontrada con ID: " + request.getOficinaResponsableId()));
        }

        PersonalSerenazgo responsable = null;
        if (request.getResponsableServicioId() != null) {
            responsable = personalRepository.findById(request.getResponsableServicioId())
                    .orElseThrow(() -> new NoSuchElementException("Personal responsable no encontrado: " + request.getResponsableServicioId()));
        }

        // 5. Validar conflictos de recursos antes de persistir
        validarConflictosRecursos(fechaHoraInicio, fechaHoraFin, request.getPersonalAsignado(),
                request.getUnidadesReservadas(), request.getEquipamientoReservado(), null);

        // 6. Generar código único correlativo
        Long correlativo = jdbcTemplate.queryForObject("SELECT nextval('seq_serenazgo_programacion')", Long.class);
        String codigoFinal = String.format("PRG-%d-%05d", request.getFechaServicio().getYear(), correlativo != null ? correlativo : 1);

        Usuario usuarioCreador = usuarioRepository.findByUsername(username).orElse(null);

        // Determinar efectivo principal para compatibilidad con Prompt 36
        PersonalSerenazgo personalPrincipal = responsable;
        if (personalPrincipal == null && request.getPersonalAsignado() != null && !request.getPersonalAsignado().isEmpty()) {
            UUID pid = request.getPersonalAsignado().get(0).getPersonalId();
            personalPrincipal = personalRepository.findById(pid).orElse(null);
        }

        // Determinar unidad principal para compatibilidad con Prompt 36
        UnidadMovilSerenazgo unidadPrincipal = null;
        if (request.getUnidadesReservadas() != null && !request.getUnidadesReservadas().isEmpty()) {
            UUID uid = request.getUnidadesReservadas().get(0).getUnidadId();
            unidadPrincipal = unidadRepository.findById(uid).orElse(null);
        }

        ProgramacionServicioSerenazgo prog = ProgramacionServicioSerenazgo.builder()
                .codigo(codigoFinal)
                .turno(turno)
                .fechaServicio(request.getFechaServicio())
                .fechaHoraInicio(fechaHoraInicio)
                .fechaHoraFin(fechaHoraFin)
                .sector(sector)
                .zona(zona)
                .unidadMovil(unidadPrincipal)
                .personal(personalPrincipal)
                .responsableServicio(responsable != null ? responsable : personalPrincipal)
                .tipoServicio(request.getTipoServicio() != null ? request.getTipoServicio() : TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoProgramacionServicio.PROGRAMADA)
                .oficinaResponsable(oficina)
                .responsableProgramacion(usuarioCreador)
                .descripcion(request.getDescripcion())
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .build();

        prog = programacionRepository.save(prog);

        // 7. Guardar personal asignado
        if (request.getPersonalAsignado() != null) {
            for (AsignacionPersonalItemRequest item : request.getPersonalAsignado()) {
                PersonalSerenazgo sereno = personalRepository.findById(item.getPersonalId())
                        .orElseThrow(() -> new NoSuchElementException("Personal no encontrado: " + item.getPersonalId()));

                ProgramacionPersonal pp = ProgramacionPersonal.builder()
                        .programacion(prog)
                        .personal(sereno)
                        .funcionOperativa(item.getFuncionOperativa() != null ? item.getFuncionOperativa() : FuncionOperativaPersonal.PATRULLERO)
                        .esResponsable(Boolean.TRUE.equals(item.getEsResponsable()))
                        .estadoAsignacion(EstadoAsignacionPersonal.ASIGNADO)
                        .usuarioResponsable(usuarioCreador)
                        .observaciones(item.getObservaciones())
                        .build();
                programacionPersonalRepository.save(pp);
            }
        }

        // 8. Guardar unidades reservadas
        if (request.getUnidadesReservadas() != null) {
            for (ReservaUnidadItemRequest item : request.getUnidadesReservadas()) {
                UnidadMovilSerenazgo unidad = unidadRepository.findById(item.getUnidadId())
                        .orElseThrow(() -> new NoSuchElementException("Unidad no encontrada: " + item.getUnidadId()));

                ProgramacionUnidad pu = ProgramacionUnidad.builder()
                        .programacion(prog)
                        .unidad(unidad)
                        .estadoReserva(EstadoReservaRecurso.RESERVADA)
                        .observaciones(item.getObservaciones())
                        .build();
                programacionUnidadRepository.save(pu);
            }
        }

        // 9. Guardar equipamiento reservado
        if (request.getEquipamientoReservado() != null) {
            for (ReservaEquipamientoItemRequest item : request.getEquipamientoReservado()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(item.getEquipamientoId())
                        .orElseThrow(() -> new NoSuchElementException("Equipamiento no encontrado: " + item.getEquipamientoId()));

                ProgramacionEquipamiento pe = ProgramacionEquipamiento.builder()
                        .programacion(prog)
                        .equipamiento(eq)
                        .cantidad(item.getCantidad() != null ? item.getCantidad() : 1)
                        .estadoReserva(EstadoReservaRecurso.RESERVADA)
                        .observaciones(item.getObservaciones())
                        .build();
                programacionEquipamientoRepository.save(pe);
            }
        }

        // 10. Bitácora de creación
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CREACION)
                .estadoAnterior("INICIAL")
                .estadoNuevo(prog.getEstado().name())
                .motivo("Alta y programación inicial del servicio")
                .usuario(usuarioCreador)
                .build());

        log.info("Programación de servicio creada exitosamente: {} para fecha {}", prog.getCodigo(), prog.getFechaServicio());
        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse actualizarProgramacion(UUID id, ProgramacionUpdateRequest request, String username) {
        log.info("Actualizando programación ID: {} por usuario: {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() == EstadoProgramacionServicio.FINALIZADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADO) {
            throw new IllegalStateException("No se puede modificar una programación que se encuentra finalizada o cancelada");
        }

        TurnoSerenazgo turno = turnoRepository.findById(request.getTurnoId())
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + request.getTurnoId()));

        OffsetDateTime[] intervalo = calcularIntervalo(request.getFechaServicio(), turno);
        OffsetDateTime fechaHoraInicio = intervalo[0];
        OffsetDateTime fechaHoraFin = intervalo[1];

        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado: " + request.getSectorId()));
        }

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada: " + request.getZonaId()));
            if (sector != null && (zona.getSector() == null || !zona.getSector().getId().equals(sector.getId()))) {
                throw new IllegalArgumentException("La zona seleccionada no pertenece al sector indicado");
            }
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        PersonalSerenazgo responsable = null;
        if (request.getResponsableServicioId() != null) {
            responsable = personalRepository.findById(request.getResponsableServicioId()).orElse(null);
        }

        // Validar conflictos excluyendo este servicio
        validarConflictosRecursos(fechaHoraInicio, fechaHoraFin, request.getPersonalAsignado(),
                request.getUnidadesReservadas(), request.getEquipamientoReservado(), id);

        prog.setTurno(turno);
        prog.setFechaServicio(request.getFechaServicio());
        prog.setFechaHoraInicio(fechaHoraInicio);
        prog.setFechaHoraFin(fechaHoraFin);
        prog.setSector(sector);
        prog.setZona(zona);
        prog.setTipoServicio(request.getTipoServicio());
        prog.setOficinaResponsable(oficina);
        prog.setResponsableServicio(responsable);
        prog.setDescripcion(request.getDescripcion());
        prog.setObservaciones(request.getObservaciones());
        prog.setUpdatedBy(username);

        // Actualizar personal asignado
        programacionPersonalRepository.deleteAll(programacionPersonalRepository.findByProgramacionId(id));
        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        if (request.getPersonalAsignado() != null) {
            for (AsignacionPersonalItemRequest item : request.getPersonalAsignado()) {
                PersonalSerenazgo s = personalRepository.findById(item.getPersonalId()).orElse(null);
                if (s != null) {
                    programacionPersonalRepository.save(ProgramacionPersonal.builder()
                            .programacion(prog)
                            .personal(s)
                            .funcionOperativa(item.getFuncionOperativa())
                            .esResponsable(Boolean.TRUE.equals(item.getEsResponsable()))
                            .estadoAsignacion(EstadoAsignacionPersonal.ASIGNADO)
                            .usuarioResponsable(u)
                            .observaciones(item.getObservaciones())
                            .build());
                }
            }
        }

        // Actualizar unidades
        programacionUnidadRepository.deleteAll(programacionUnidadRepository.findByProgramacionId(id));
        if (request.getUnidadesReservadas() != null) {
            for (ReservaUnidadItemRequest item : request.getUnidadesReservadas()) {
                UnidadMovilSerenazgo um = unidadRepository.findById(item.getUnidadId()).orElse(null);
                if (um != null) {
                    programacionUnidadRepository.save(ProgramacionUnidad.builder()
                            .programacion(prog)
                            .unidad(um)
                            .estadoReserva(EstadoReservaRecurso.RESERVADA)
                            .observaciones(item.getObservaciones())
                            .build());
                }
            }
        }

        // Actualizar equipamiento
        programacionEquipamientoRepository.deleteAll(programacionEquipamientoRepository.findByProgramacionId(id));
        if (request.getEquipamientoReservado() != null) {
            for (ReservaEquipamientoItemRequest item : request.getEquipamientoReservado()) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(item.getEquipamientoId()).orElse(null);
                if (eq != null) {
                    programacionEquipamientoRepository.save(ProgramacionEquipamiento.builder()
                            .programacion(prog)
                            .equipamiento(eq)
                            .cantidad(item.getCantidad())
                            .estadoReserva(EstadoReservaRecurso.RESERVADA)
                            .observaciones(item.getObservaciones())
                            .build());
                }
            }
        }

        prog = programacionRepository.save(prog);

        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CORRECCION_ADMINISTRATIVA)
                .estadoAnterior(prog.getEstado().name())
                .estadoNuevo(prog.getEstado().name())
                .motivo("Actualización administrativa de parámetros del servicio")
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse confirmarProgramacion(UUID id, String motivo, String username) {
        log.info("Confirmando programación ID: {} por usuario: {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() == EstadoProgramacionServicio.CONFIRMADA) {
            return mapearResponse(prog);
        }

        if (prog.getEstado() == EstadoProgramacionServicio.CANCELADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADO ||
                prog.getEstado() == EstadoProgramacionServicio.FINALIZADA) {
            throw new IllegalStateException("No se puede confirmar un servicio cancelado o finalizado");
        }

        // Revalidar conflictos antes de confirmar
        List<ProgramacionPersonal> personal = programacionPersonalRepository.findByProgramacionId(id);
        List<ProgramacionUnidad> unidades = programacionUnidadRepository.findByProgramacionId(id);

        for (ProgramacionPersonal pp : personal) {
            List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosPersonal(
                    pp.getPersonal().getId(), prog.getFechaHoraInicio(), prog.getFechaHoraFin(), prog.getId());
            if (!conf.isEmpty()) {
                throw new IllegalStateException("Conflicto detectado: El efectivo " + pp.getPersonal().getCodigoInterno() +
                        " ya se encuentra programado en el servicio " + conf.get(0).getCodigo());
            }
            pp.setEstadoAsignacion(EstadoAsignacionPersonal.CONFIRMADO);
            programacionPersonalRepository.save(pp);
        }

        for (ProgramacionUnidad pu : unidades) {
            List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosUnidad(
                    pu.getUnidad().getId(), prog.getFechaHoraInicio(), prog.getFechaHoraFin(), prog.getId());
            if (!conf.isEmpty()) {
                throw new IllegalStateException("Conflicto detectado: La unidad " + pu.getUnidad().getPlaca() +
                        " ya está reservada en el servicio " + conf.get(0).getCodigo());
            }
        }

        EstadoProgramacionServicio anterior = prog.getEstado();
        prog.setEstado(EstadoProgramacionServicio.CONFIRMADA);
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CONFIRMACION)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoProgramacionServicio.CONFIRMADA.name())
                .motivo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Confirmación y publicación oficial del rol de servicio")
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse reprogramarServicio(UUID id, ReprogramarServicioRequest request, String username) {
        log.info("Reprogramando servicio ID: {} por usuario: {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() == EstadoProgramacionServicio.FINALIZADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADO) {
            throw new IllegalStateException("No se puede reprogramar un servicio que ya ha finalizado o ha sido cancelado");
        }

        TurnoSerenazgo nuevoTurno = turnoRepository.findById(request.getNuevoTurnoId())
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + request.getNuevoTurnoId()));

        OffsetDateTime[] nuevoIntervalo = calcularIntervalo(request.getNuevaFechaServicio(), nuevoTurno);
        OffsetDateTime nuevaFechaHoraInicio = nuevoIntervalo[0];
        OffsetDateTime nuevaFechaHoraFin = nuevoIntervalo[1];

        // Validar que el personal asignado no tenga conflictos en el nuevo horario
        List<ProgramacionPersonal> personal = programacionPersonalRepository.findByProgramacionId(id);
        for (ProgramacionPersonal pp : personal) {
            List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosPersonal(
                    pp.getPersonal().getId(), nuevaFechaHoraInicio, nuevaFechaHoraFin, prog.getId());
            if (!conf.isEmpty()) {
                throw new IllegalStateException("Conflicto al reprogramar: El sereno " + pp.getPersonal().getCodigoInterno() +
                        " está programado en " + conf.get(0).getCodigo() + " en la nueva fecha/horario");
            }
        }

        // Validar unidades
        List<ProgramacionUnidad> unidades = programacionUnidadRepository.findByProgramacionId(id);
        for (ProgramacionUnidad pu : unidades) {
            List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosUnidad(
                    pu.getUnidad().getId(), nuevaFechaHoraInicio, nuevaFechaHoraFin, prog.getId());
            if (!conf.isEmpty()) {
                throw new IllegalStateException("Conflicto al reprogramar: La unidad " + pu.getUnidad().getPlaca() +
                        " ya está asignada en " + conf.get(0).getCodigo() + " en el nuevo horario");
            }
        }

        SectorPatrullaje nuevoSector = prog.getSector();
        if (request.getNuevoSectorId() != null) {
            nuevoSector = sectorRepository.findById(request.getNuevoSectorId()).orElse(prog.getSector());
        }

        ZonaPatrullaje nuevaZona = prog.getZona();
        if (request.getNuevaZonaId() != null) {
            nuevaZona = zonaRepository.findById(request.getNuevaZonaId()).orElse(null);
        }

        String detalleHistorico = String.format("Fecha previa: %s (%s) -> Nueva fecha: %s (%s)",
                prog.getFechaServicio(), prog.getTurno().getNombre(),
                request.getNuevaFechaServicio(), nuevoTurno.getNombre());

        prog.setTurno(nuevoTurno);
        prog.setFechaServicio(request.getNuevaFechaServicio());
        prog.setFechaHoraInicio(nuevaFechaHoraInicio);
        prog.setFechaHoraFin(nuevaFechaHoraFin);
        prog.setSector(nuevoSector);
        prog.setZona(nuevaZona);
        prog.setMotivoReprogramacion(request.getMotivo().trim());
        prog.setUpdatedBy(username);

        prog = programacionRepository.save(prog);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.REPROGRAMACION)
                .estadoAnterior(prog.getEstado().name())
                .estadoNuevo(prog.getEstado().name())
                .motivo(request.getMotivo().trim())
                .detallesJson(detalleHistorico)
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse cancelarServicio(UUID id, CancelarServicioRequest request, String username) {
        log.info("Cancelando servicio ID: {} por usuario: {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() == EstadoProgramacionServicio.FINALIZADA) {
            throw new IllegalStateException("No se puede cancelar un servicio que ya ha sido finalizado");
        }

        EstadoProgramacionServicio anterior = prog.getEstado();
        prog.setEstado(EstadoProgramacionServicio.CANCELADA);
        prog.setMotivoCancelacion(request.getMotivo().trim());
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        // Liberar unidades
        List<ProgramacionUnidad> unidades = programacionUnidadRepository.findByProgramacionId(id);
        for (ProgramacionUnidad pu : unidades) {
            pu.setEstadoReserva(EstadoReservaRecurso.CANCELADA);
            programacionUnidadRepository.save(pu);
        }

        // Liberar equipamiento
        List<ProgramacionEquipamiento> equipos = programacionEquipamientoRepository.findByProgramacionId(id);
        for (ProgramacionEquipamiento pe : equipos) {
            pe.setEstadoReserva(EstadoReservaRecurso.CANCELADA);
            programacionEquipamientoRepository.save(pe);
        }

        // Cancelar asignaciones de personal
        List<ProgramacionPersonal> personal = programacionPersonalRepository.findByProgramacionId(id);
        for (ProgramacionPersonal pp : personal) {
            pp.setEstadoAsignacion(EstadoAsignacionPersonal.CANCELADO);
            programacionPersonalRepository.save(pp);
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CANCELACION)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoProgramacionServicio.CANCELADA.name())
                .motivo(request.getMotivo().trim())
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse iniciarServicio(UUID id, String username) {
        log.info("Iniciando servicio ID: {} por {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() != EstadoProgramacionServicio.CONFIRMADA &&
                prog.getEstado() != EstadoProgramacionServicio.PROGRAMADA &&
                prog.getEstado() != EstadoProgramacionServicio.PROGRAMADO) {
            throw new IllegalStateException("El servicio debe encontrarse en estado PROGRAMADA o CONFIRMADA para poder iniciarse");
        }

        EstadoProgramacionServicio anterior = prog.getEstado();
        prog.setEstado(EstadoProgramacionServicio.EN_CURSO);
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.INICIO)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoProgramacionServicio.EN_CURSO.name())
                .motivo("Inicio operativo del servicio de guardia")
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse finalizarServicio(UUID id, String observaciones, String username) {
        log.info("Finalizando servicio ID: {} por {}", id, username);

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        if (prog.getEstado() == EstadoProgramacionServicio.FINALIZADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADA ||
                prog.getEstado() == EstadoProgramacionServicio.CANCELADO) {
            throw new IllegalStateException("El servicio ya se encuentra finalizado o cancelado");
        }

        EstadoProgramacionServicio anterior = prog.getEstado();
        prog.setEstado(EstadoProgramacionServicio.FINALIZADA);
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            String obsActual = prog.getObservaciones() != null ? prog.getObservaciones() + "\n" : "";
            prog.setObservaciones(obsActual + "[Cierre de servicio]: " + observaciones.trim());
        }
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        // Liberar reservas
        List<ProgramacionUnidad> unidades = programacionUnidadRepository.findByProgramacionId(id);
        for (ProgramacionUnidad pu : unidades) {
            pu.setEstadoReserva(EstadoReservaRecurso.LIBERADA);
            programacionUnidadRepository.save(pu);
        }

        List<ProgramacionEquipamiento> equipos = programacionEquipamientoRepository.findByProgramacionId(id);
        for (ProgramacionEquipamiento pe : equipos) {
            pe.setEstadoReserva(EstadoReservaRecurso.LIBERADA);
            programacionEquipamientoRepository.save(pe);
        }

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.FINALIZACION)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoProgramacionServicio.FINALIZADA.name())
                .motivo(observaciones != null && !observaciones.trim().isEmpty() ? observaciones.trim() : "Conclusión formal del servicio de guardia")
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionResponse cambiarEstado(UUID id, CambioEstadoProgramacionRequest request, String username) {
        if (request.getNuevoEstado() == EstadoProgramacionServicio.CONFIRMADA) {
            return confirmarProgramacion(id, request.getMotivo(), username);
        } else if (request.getNuevoEstado() == EstadoProgramacionServicio.CANCELADA || request.getNuevoEstado() == EstadoProgramacionServicio.CANCELADO) {
            return cancelarServicio(id, CancelarServicioRequest.builder().motivo(request.getMotivo()).build(), username);
        } else if (request.getNuevoEstado() == EstadoProgramacionServicio.EN_CURSO) {
            return iniciarServicio(id, username);
        } else if (request.getNuevoEstado() == EstadoProgramacionServicio.FINALIZADA) {
            return finalizarServicio(id, request.getMotivo(), username);
        }

        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        EstadoProgramacionServicio anterior = prog.getEstado();
        prog.setEstado(request.getNuevoEstado());
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CORRECCION_ADMINISTRATIVA)
                .estadoAnterior(anterior.name())
                .estadoNuevo(request.getNuevoEstado().name())
                .motivo(request.getMotivo().trim())
                .usuario(u)
                .build());

        return mapearResponse(prog);
    }

    @Override
    @Transactional
    public ProgramacionDetailResponse asignarPersonal(UUID id, AsignacionPersonalItemRequest request, String username) {
        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        PersonalSerenazgo sereno = personalRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new NoSuchElementException("Personal no encontrado: " + request.getPersonalId()));

        if (sereno.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            throw new IllegalStateException("El efectivo no se encuentra en estado ACTIVO: " + sereno.getCodigoInterno());
        }

        // Verificar conflicto en intervalo
        List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosPersonal(
                sereno.getId(), prog.getFechaHoraInicio(), prog.getFechaHoraFin(), prog.getId());
        if (!conf.isEmpty()) {
            throw new IllegalStateException("El efectivo " + sereno.getCodigoInterno() + " ya tiene un servicio activo programado: " + conf.get(0).getCodigo());
        }

        Optional<ProgramacionPersonal> opc = programacionPersonalRepository.findByProgramacionIdAndPersonalId(id, sereno.getId());
        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        if (opc.isPresent()) {
            ProgramacionPersonal pp = opc.get();
            pp.setFuncionOperativa(request.getFuncionOperativa());
            pp.setEsResponsable(Boolean.TRUE.equals(request.getEsResponsable()));
            pp.setEstadoAsignacion(EstadoAsignacionPersonal.ASIGNADO);
            pp.setObservaciones(request.getObservaciones());
            programacionPersonalRepository.save(pp);
        } else {
            programacionPersonalRepository.save(ProgramacionPersonal.builder()
                    .programacion(prog)
                    .personal(sereno)
                    .funcionOperativa(request.getFuncionOperativa() != null ? request.getFuncionOperativa() : FuncionOperativaPersonal.PATRULLERO)
                    .esResponsable(Boolean.TRUE.equals(request.getEsResponsable()))
                    .estadoAsignacion(EstadoAsignacionPersonal.ASIGNADO)
                    .usuarioResponsable(u)
                    .observaciones(request.getObservaciones())
                    .build());
        }

        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.ASIGNACION_PERSONAL)
                .motivo("Asignación de " + sereno.getCodigoInterno() + " como " + request.getFuncionOperativa())
                .usuario(u)
                .build());

        return obtenerDetallePorId(id);
    }

    @Override
    @Transactional
    public ProgramacionDetailResponse desasignarPersonal(UUID id, UUID personalId, String motivo, String username) {
        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        Optional<ProgramacionPersonal> opc = programacionPersonalRepository.findByProgramacionIdAndPersonalId(id, personalId);
        if (opc.isPresent()) {
            ProgramacionPersonal pp = opc.get();
            pp.setEstadoAsignacion(EstadoAsignacionPersonal.CANCELADO);
            programacionPersonalRepository.save(pp);

            Usuario u = usuarioRepository.findByUsername(username).orElse(null);
            historialRepository.save(ProgramacionHistorial.builder()
                    .programacion(prog)
                    .tipoEvento(TipoEventoHistorialProgramacion.DESASIGNACION_PERSONAL)
                    .motivo("Retiro de personal: " + (motivo != null ? motivo : "Reordenamiento operativo"))
                    .usuario(u)
                    .build());
        }

        return obtenerDetallePorId(id);
    }

    @Override
    @Transactional
    public ProgramacionDetailResponse cambiarResponsableServicio(UUID id, UUID nuevoResponsableId, String motivo, String username) {
        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        PersonalSerenazgo nuevoResp = personalRepository.findById(nuevoResponsableId)
                .orElseThrow(() -> new NoSuchElementException("Personal no encontrado: " + nuevoResponsableId));

        prog.setResponsableServicio(nuevoResp);
        prog.setPersonal(nuevoResp);
        prog.setUpdatedBy(username);
        prog = programacionRepository.save(prog);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(ProgramacionHistorial.builder()
                .programacion(prog)
                .tipoEvento(TipoEventoHistorialProgramacion.CAMBIO_RESPONSABLE)
                .motivo("Nuevo responsable asignado: " + nuevoResp.getCodigoInterno() + ". Motivo: " + motivo)
                .usuario(u)
                .build());

        return obtenerDetallePorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramacionDetailResponse obtenerDetallePorId(UUID id) {
        ProgramacionServicioSerenazgo prog = programacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programación no encontrada con ID: " + id));

        List<ProgramacionPersonalResponse> personalDtos = programacionPersonalRepository.findByProgramacionId(id)
                .stream()
                .map(p -> ProgramacionPersonalResponse.builder()
                        .id(p.getId())
                        .personalId(p.getPersonal().getId())
                        .codigoInterno(p.getPersonal().getCodigoInterno())
                        .nombreCompleto(p.getPersonal().getPersona() != null ? p.getPersonal().getPersona().getNombreCompleto() : "N/D")
                        .cargo(p.getPersonal().getCargoOperativo() != null ? p.getPersonal().getCargoOperativo().getNombre() : "Sereno")
                        .funcionOperativa(p.getFuncionOperativa())
                        .funcionOperativaDesc(p.getFuncionOperativa() != null ? p.getFuncionOperativa().getDescripcion() : "Patrullero")
                        .esResponsable(p.getEsResponsable())
                        .estadoAsignacion(p.getEstadoAsignacion())
                        .estadoAsignacionDesc(p.getEstadoAsignacion() != null ? p.getEstadoAsignacion().getDescripcion() : "Asignado")
                        .fechaAsignacion(p.getFechaAsignacion())
                        .observaciones(p.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        List<ProgramacionUnidadResponse> unidadesDtos = programacionUnidadRepository.findByProgramacionId(id)
                .stream()
                .map(u -> ProgramacionUnidadResponse.builder()
                        .id(u.getId())
                        .unidadId(u.getUnidad().getId())
                        .codigo(u.getUnidad().getCodigoInterno())
                        .tipoUnidadDesc(u.getUnidad().getTipoUnidad() != null ? u.getUnidad().getTipoUnidad().getDescripcion() : "Vehículo")
                        .placa(u.getUnidad().getPlaca())
                        .marcaModelo(u.getUnidad().getMarca() + " " + (u.getUnidad().getModelo() != null ? u.getUnidad().getModelo() : ""))
                        .estadoReserva(u.getEstadoReserva())
                        .estadoReservaDesc(u.getEstadoReserva() != null ? u.getEstadoReserva().getDescripcion() : "Reservada")
                        .observaciones(u.getObservaciones())
                        .createdAt(u.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<ProgramacionEquipamientoResponse> equipamientoDtos = programacionEquipamientoRepository.findByProgramacionId(id)
                .stream()
                .map(e -> ProgramacionEquipamientoResponse.builder()
                        .id(e.getId())
                        .equipamientoId(e.getEquipamiento().getId())
                        .codigo(e.getEquipamiento().getCodigoInterno())
                        .tipoEquipamientoDesc(e.getEquipamiento().getTipoEquipo() != null ? e.getEquipamiento().getTipoEquipo().getDescripcion() : "Equipo")
                        .marcaModelo((e.getEquipamiento().getMarca() != null ? e.getEquipamiento().getMarca() : "") + " " + (e.getEquipamiento().getModelo() != null ? e.getEquipamiento().getModelo() : ""))
                        .numeroSerie(e.getEquipamiento().getNumeroSerie())
                        .cantidad(e.getCantidad())
                        .estadoReserva(e.getEstadoReserva())
                        .estadoReservaDesc(e.getEstadoReserva() != null ? e.getEstadoReserva().getDescripcion() : "Reservado")
                        .observaciones(e.getObservaciones())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<ProgramacionHistorialResponse> historialDtos = historialRepository.findByProgramacionIdOrderByCreatedAtDesc(id)
                .stream()
                .map(h -> ProgramacionHistorialResponse.builder()
                        .id(h.getId())
                        .tipoEvento(h.getTipoEvento())
                        .tipoEventoDesc(h.getTipoEvento() != null ? h.getTipoEvento().getDescripcion() : "Evento")
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .motivo(h.getMotivo())
                        .usuarioNombre(h.getUsuario() != null ? h.getUsuario().getUsername() : "Sistema")
                        .detallesJson(h.getDetallesJson())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        int duracion = 0;
        if (prog.getFechaHoraInicio() != null && prog.getFechaHoraFin() != null) {
            duracion = (int) Duration.between(prog.getFechaHoraInicio(), prog.getFechaHoraFin()).toMinutes();
        }

        String horarioFormateado = String.format("%s - %s (%s)",
                prog.getTurno().getHoraInicio().format(FORMATO_HORA),
                prog.getTurno().getHoraFin().format(FORMATO_HORA),
                Boolean.TRUE.equals(prog.getTurno().getCruzaMedianoche()) ? "Cruza Medianoche" : "Mismo Día");

        return ProgramacionDetailResponse.builder()
                .id(prog.getId())
                .codigo(prog.getCodigo())
                .fechaServicio(prog.getFechaServicio())
                .fechaHoraInicio(prog.getFechaHoraInicio())
                .fechaHoraFin(prog.getFechaHoraFin())
                .horarioFormateado(horarioFormateado)
                .duracionMinutos(duracion)
                .turnoId(prog.getTurno().getId())
                .turnoCodigo(prog.getTurno().getCodigo())
                .turnoNombre(prog.getTurno().getNombre())
                .turnoHoraInicio(prog.getTurno().getHoraInicio())
                .turnoHoraFin(prog.getTurno().getHoraFin())
                .turnoCruzaMedianoche(prog.getTurno().getCruzaMedianoche())
                .sectorId(prog.getSector() != null ? prog.getSector().getId() : null)
                .sectorCodigo(prog.getSector() != null ? prog.getSector().getCodigo() : null)
                .sectorNombre(prog.getSector() != null ? prog.getSector().getNombre() : "General / Sin Sector")
                .zonaId(prog.getZona() != null ? prog.getZona().getId() : null)
                .zonaCodigo(prog.getZona() != null ? prog.getZona().getCodigo() : null)
                .zonaNombre(prog.getZona() != null ? prog.getZona().getNombre() : "General")
                .oficinaResponsableId(prog.getOficinaResponsable() != null ? prog.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(prog.getOficinaResponsable() != null ? prog.getOficinaResponsable().getNombre() : "Subgerencia de Serenazgo")
                .tipoServicio(prog.getTipoServicio())
                .tipoServicioDesc(prog.getTipoServicio() != null ? prog.getTipoServicio().getDescripcion() : "Patrullaje Preventivo")
                .estado(prog.getEstado())
                .estadoDesc(prog.getEstado() != null ? prog.getEstado().getDescripcion() : "Programada")
                .responsableServicioId(prog.getResponsableServicio() != null ? prog.getResponsableServicio().getId() : null)
                .responsableServicioNombre(prog.getResponsableServicio() != null && prog.getResponsableServicio().getPersona() != null ?
                        prog.getResponsableServicio().getPersona().getNombreCompleto() : "Por designar")
                .responsableServicioCodigo(prog.getResponsableServicio() != null ? prog.getResponsableServicio().getCodigoInterno() : null)
                .usuarioProgramacionNombre(prog.getResponsableProgramacion() != null ? prog.getResponsableProgramacion().getUsername() : prog.getCreatedBy())
                .descripcion(prog.getDescripcion())
                .observaciones(prog.getObservaciones())
                .motivoReprogramacion(prog.getMotivoReprogramacion())
                .motivoCancelacion(prog.getMotivoCancelacion())
                .version(prog.getVersion())
                .createdAt(prog.getCreatedAt())
                .updatedAt(prog.getUpdatedAt())
                .personalAsignado(personalDtos)
                .unidadesReservadas(unidadesDtos)
                .equipamientoReservado(equipamientoDtos)
                .historial(historialDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgramacionResponse> buscarProgramaciones(String termino,
                                                           LocalDate desde,
                                                           LocalDate hasta,
                                                           EstadoProgramacionServicio estado,
                                                           UUID sectorId,
                                                           UUID turnoId,
                                                           Pageable pageable) {
        Page<ProgramacionServicioSerenazgo> pagina;
        if (termino != null && !termino.trim().isEmpty()) {
            pagina = programacionRepository.buscarConFiltrosConTermino(
                    termino.trim(), desde, hasta, estado, sectorId, turnoId, pageable);
        } else {
            pagina = programacionRepository.buscarConFiltrosSinTermino(
                    desde, hasta, estado, sectorId, turnoId, pageable);
        }

        List<ProgramacionResponse> dtos = pagina.getContent().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pagina.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioEventoResponse> obtenerEventosCalendario(LocalDate desde,
                                                                   LocalDate hasta,
                                                                   UUID sectorId,
                                                                   EstadoProgramacionServicio estado) {
        LocalDate fDesde = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate fHasta = hasta != null ? hasta : fDesde.plusMonths(1).withDayOfMonth(1).minusDays(1);

        List<ProgramacionServicioSerenazgo> lista = programacionRepository.findByFechaServicioBetweenOrderByFechaHoraInicioAsc(fDesde, fHasta);

        return lista.stream()
                .filter(p -> sectorId == null || (p.getSector() != null && p.getSector().getId().equals(sectorId)))
                .filter(p -> estado == null || p.getEstado() == estado)
                .map(p -> {
                    String color = switch (p.getEstado()) {
                        case CONFIRMADA -> "#1d4ed8";
                        case EN_CURSO, PRESENTE -> "#059669";
                        case PROGRAMADA, PROGRAMADO -> "#d97706";
                        case FINALIZADA -> "#64748b";
                        case CANCELADA, CANCELADO -> "#e11d48";
                        case BORRADOR -> "#7c3aed";
                        default -> "#3b82f6";
                    };

                    String secNom = p.getSector() != null ? p.getSector().getNombre() : "General";
                    String title = String.format("%s - %s [%s]", p.getCodigo(), secNom, p.getTurno().getNombre());

                    int perCount = p.getPersonalAsignado() != null ? p.getPersonalAsignado().size() : 0;
                    int uniCount = p.getUnidadesReservadas() != null ? p.getUnidadesReservadas().size() : 0;

                    return CalendarioEventoResponse.builder()
                            .id(p.getId())
                            .codigo(p.getCodigo())
                            .title(title)
                            .start(p.getFechaHoraInicio() != null ? p.getFechaHoraInicio().toString() : p.getFechaServicio().toString())
                            .end(p.getFechaHoraFin() != null ? p.getFechaHoraFin().toString() : p.getFechaServicio().toString())
                            .estado(p.getEstado().name())
                            .estadoDesc(p.getEstado().getDescripcion())
                            .color(color)
                            .sectorNombre(secNom)
                            .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                            .turnoNombre(p.getTurno().getNombre())
                            .personalCount(perCount)
                            .unidadesCount(uniCount)
                            .responsableNombre(p.getResponsableServicio() != null && p.getResponsableServicio().getPersona() != null ?
                                    p.getResponsableServicio().getPersona().getNombreCompleto() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConflictoDisponibilidadResponse> verificarConflictos(LocalDate fecha,
                                                                    UUID turnoId,
                                                                    List<UUID> personalIds,
                                                                    List<UUID> unidadIds,
                                                                    List<UUID> equipamientoIds,
                                                                    UUID excluirId) {
        TurnoSerenazgo turno = turnoRepository.findById(turnoId).orElse(null);
        if (turno == null) {
            return Collections.emptyList();
        }

        OffsetDateTime[] intervalo = calcularIntervalo(fecha, turno);
        OffsetDateTime inicio = intervalo[0];
        OffsetDateTime fin = intervalo[1];

        List<ConflictoDisponibilidadResponse> conflictos = new ArrayList<>();

        if (personalIds != null) {
            for (UUID pid : personalIds) {
                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosPersonal(pid, inicio, fin, excluirId);
                for (ProgramacionServicioSerenazgo c : conf) {
                    PersonalSerenazgo sereno = personalRepository.findById(pid).orElse(null);
                    conflictos.add(ConflictoDisponibilidadResponse.builder()
                            .tipoConflicto("PERSONAL")
                            .recursoId(pid)
                            .recursoIdentificador(sereno != null ? sereno.getCodigoInterno() : "SERENO")
                            .recursoNombre(sereno != null && sereno.getPersona() != null ? sereno.getPersona().getNombreCompleto() : "Personal")
                            .servicioEnConflictoId(c.getId())
                            .servicioEnConflictoCodigo(c.getCodigo())
                            .conflictoInicio(c.getFechaHoraInicio())
                            .conflictoFin(c.getFechaHoraFin())
                            .mensaje("El sereno ya está programado en " + c.getCodigo() + " (" +
                                    c.getFechaHoraInicio().format(FORMATO_HORA) + " - " + c.getFechaHoraFin().format(FORMATO_HORA) + ")")
                            .build());
                }
            }
        }

        if (unidadIds != null) {
            for (UUID uid : unidadIds) {
                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosUnidad(uid, inicio, fin, excluirId);
                for (ProgramacionServicioSerenazgo c : conf) {
                    UnidadMovilSerenazgo um = unidadRepository.findById(uid).orElse(null);
                    conflictos.add(ConflictoDisponibilidadResponse.builder()
                            .tipoConflicto("UNIDAD")
                            .recursoId(uid)
                            .recursoIdentificador(um != null ? um.getPlaca() : "UNIDAD")
                            .recursoNombre(um != null ? um.getCodigoInterno() + " - " + um.getPlaca() : "Unidad")
                            .servicioEnConflictoId(c.getId())
                            .servicioEnConflictoCodigo(c.getCodigo())
                            .conflictoInicio(c.getFechaHoraInicio())
                            .conflictoFin(c.getFechaHoraFin())
                            .mensaje("La unidad vehicular ya se encuentra reservada en " + c.getCodigo())
                            .build());
                }
            }
        }

        if (equipamientoIds != null) {
            for (UUID eid : equipamientoIds) {
                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosEquipamiento(eid, inicio, fin, excluirId);
                for (ProgramacionServicioSerenazgo c : conf) {
                    EquipamientoSerenazgo eq = equipamientoRepository.findById(eid).orElse(null);
                    conflictos.add(ConflictoDisponibilidadResponse.builder()
                            .tipoConflicto("EQUIPAMIENTO")
                            .recursoId(eid)
                            .recursoIdentificador(eq != null ? eq.getCodigoInterno() : "EQUIPO")
                            .recursoNombre(eq != null ? eq.getCodigoInterno() + " (" + (eq.getNumeroSerie() != null ? eq.getNumeroSerie() : "Lote") + ")" : "Equipo")
                            .servicioEnConflictoId(c.getId())
                            .servicioEnConflictoCodigo(c.getCodigo())
                            .conflictoInicio(c.getFechaHoraInicio())
                            .conflictoFin(c.getFechaHoraFin())
                            .mensaje("El equipo individualizado ya se encuentra reservado en " + c.getCodigo())
                            .build());
                }
            }
        }

        return conflictos;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardProgramacionResponse obtenerDashboardProgramacion() {
        long total = programacionRepository.count();
        long borrador = programacionRepository.countByEstado(EstadoProgramacionServicio.BORRADOR);
        long prog = programacionRepository.countByEstadoIn(List.of(EstadoProgramacionServicio.PROGRAMADA, EstadoProgramacionServicio.PROGRAMADO));
        long conf = programacionRepository.countByEstado(EstadoProgramacionServicio.CONFIRMADA);
        long enCurso = programacionRepository.countByEstadoIn(List.of(EstadoProgramacionServicio.EN_CURSO, EstadoProgramacionServicio.PRESENTE));
        long fin = programacionRepository.countByEstado(EstadoProgramacionServicio.FINALIZADA);
        long canc = programacionRepository.countByEstadoIn(List.of(EstadoProgramacionServicio.CANCELADA, EstadoProgramacionServicio.CANCELADO));
        long turnosActivos = turnoRepository.countByEstado(EstadoTurnoSerenazgo.ACTIVO);
        long personalAsignado = programacionPersonalRepository.countByEstadoAsignacion(EstadoAsignacionPersonal.CONFIRMADO) +
                programacionPersonalRepository.countByEstadoAsignacion(EstadoAsignacionPersonal.ASIGNADO);
        long unidadesReservadas = programacionUnidadRepository.countByEstadoReserva(EstadoReservaRecurso.RESERVADA) +
                programacionUnidadRepository.countByEstadoReserva(EstadoReservaRecurso.ASIGNADA);

        return DashboardProgramacionResponse.builder()
                .totalServicios(total)
                .serviciosBorrador(borrador)
                .serviciosProgramados(prog)
                .serviciosConfirmados(conf)
                .serviciosEnCurso(enCurso)
                .serviciosFinalizados(fin)
                .serviciosCancelados(canc)
                .totalTurnosActivos(turnosActivos)
                .personalActualmenteAsignado(personalAsignado)
                .unidadesActualmenteReservadas(unidadesReservadas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarDespachoDiarioPdf(LocalDate fecha) {
        LocalDate fechaTarget = fecha != null ? fecha : LocalDate.now();
        List<ProgramacionServicioSerenazgo> servicios = programacionRepository.findByFechaServicioOrderByTurnoHoraInicioAsc(fechaTarget);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 25, 25, 30, 30); // Horizontal
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(29, 78, 216));
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontRow = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY);

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            doc.add(pHeader);

            Paragraph pSub = new Paragraph("SUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO\nROL DE SERVICIOS Y HOJA DE DESPACHO DIARIO — " + fechaTarget.format(FORMATO_FECHA) + "\n\n", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            doc.add(pSub);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{12, 14, 14, 16, 18, 10, 8, 8});

            String[] headers = {"CÓDIGO", "TURNO / HORARIO", "SECTOR / ZONA", "TIPO SERVICIO", "RESPONSABLE", "UNIDAD", "EFECTIVOS", "ESTADO"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontHeader));
                c.setBackgroundColor(new Color(30, 41, 59));
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(5);
                table.addCell(c);
            }

            if (servicios.isEmpty()) {
                PdfPCell cVacia = new PdfPCell(new Phrase("No existen servicios programados para la fecha seleccionada (" + fechaTarget.format(FORMATO_FECHA) + ")", fontRow));
                cVacia.setColspan(8);
                cVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                cVacia.setPadding(15);
                table.addCell(cVacia);
            } else {
                for (ProgramacionServicioSerenazgo s : servicios) {
                    table.addCell(crearCelda(s.getCodigo(), fontRow, Element.ALIGN_CENTER));
                    String hor = s.getTurno().getNombre() + " (" + s.getTurno().getHoraInicio().format(FORMATO_HORA) + "-" + s.getTurno().getHoraFin().format(FORMATO_HORA) + ")";
                    table.addCell(crearCelda(hor, fontRow, Element.ALIGN_LEFT));
                    String sec = (s.getSector() != null ? s.getSector().getNombre() : "General") + (s.getZona() != null ? " / " + s.getZona().getNombre() : "");
                    table.addCell(crearCelda(sec, fontRow, Element.ALIGN_LEFT));
                    table.addCell(crearCelda(s.getTipoServicio() != null ? s.getTipoServicio().getDescripcion() : "Patrullaje", fontRow, Element.ALIGN_LEFT));
                    String resp = (s.getResponsableServicio() != null && s.getResponsableServicio().getPersona() != null) ?
                            s.getResponsableServicio().getPersona().getNombreCompleto() : "Por designar";
                    table.addCell(crearCelda(resp, fontRow, Element.ALIGN_LEFT));
                    String uni = s.getUnidadMovil() != null ? s.getUnidadMovil().getPlaca() : "-";
                    table.addCell(crearCelda(uni, fontRow, Element.ALIGN_CENTER));
                    int cantPersonal = s.getPersonalAsignado() != null ? s.getPersonalAsignado().size() : 0;
                    table.addCell(crearCelda(String.valueOf(cantPersonal), fontRow, Element.ALIGN_CENTER));
                    table.addCell(crearCelda(s.getEstado().name(), fontRow, Element.ALIGN_CENTER));
                }
            }

            doc.add(table);

            Paragraph pFoot = new Paragraph("\nDocumento de despacho operativo emitido de conformidad con la Ley N° 31297 — Ley del Servicio de Serenazgo Municipal. SIGM-QUINUA.", fontSmall);
            pFoot.setAlignment(Element.ALIGN_CENTER);
            doc.add(pFoot);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de despacho diario", e);
            throw new RuntimeException("Error al generar hoja de despacho diario", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarProgramacionExcel(LocalDate desde, LocalDate hasta) {
        LocalDate fDesde = desde != null ? desde : LocalDate.now().minusWeeks(1);
        LocalDate fHasta = hasta != null ? hasta : LocalDate.now().plusWeeks(2);

        List<ProgramacionServicioSerenazgo> lista = programacionRepository.findByFechaServicioBetweenOrderByFechaHoraInicioAsc(fDesde, fHasta);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Rol de Servicios");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fontH = workbook.createFont();
            fontH.setBold(true);
            fontH.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontH);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] cols = {"CÓDIGO", "FECHA", "INICIO", "FIN", "TURNO", "SECTOR", "ZONA", "TIPO SERVICIO", "RESPONSABLE", "EFECTIVOS", "UNIDADES", "ESTADO"};
            Row r0 = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = r0.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ProgramacionServicioSerenazgo p : lista) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(sanitizar(p.getCodigo()));
                r.createCell(1).setCellValue(p.getFechaServicio() != null ? p.getFechaServicio().toString() : "");
                r.createCell(2).setCellValue(p.getFechaHoraInicio() != null ? p.getFechaHoraInicio().format(FORMATO_FECHA_HORA) : "");
                r.createCell(3).setCellValue(p.getFechaHoraFin() != null ? p.getFechaHoraFin().format(FORMATO_FECHA_HORA) : "");
                r.createCell(4).setCellValue(sanitizar(p.getTurno() != null ? p.getTurno().getNombre() : ""));
                r.createCell(5).setCellValue(sanitizar(p.getSector() != null ? p.getSector().getNombre() : "General"));
                r.createCell(6).setCellValue(sanitizar(p.getZona() != null ? p.getZona().getNombre() : ""));
                r.createCell(7).setCellValue(sanitizar(p.getTipoServicio() != null ? p.getTipoServicio().getDescripcion() : ""));
                String resp = (p.getResponsableServicio() != null && p.getResponsableServicio().getPersona() != null) ?
                        p.getResponsableServicio().getPersona().getNombreCompleto() : "";
                r.createCell(8).setCellValue(sanitizar(resp));
                r.createCell(9).setCellValue(p.getPersonalAsignado() != null ? p.getPersonalAsignado().size() : 0);
                r.createCell(10).setCellValue(p.getUnidadesReservadas() != null ? p.getUnidadesReservadas().size() : 0);
                r.createCell(11).setCellValue(sanitizar(p.getEstado().name()));
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar programación a Excel", e);
            throw new RuntimeException("Error al exportar rol de servicios a Excel", e);
        }
    }

    private OffsetDateTime[] calcularIntervalo(LocalDate fechaServicio, TurnoSerenazgo turno) {
        OffsetDateTime inicio = fechaServicio.atTime(turno.getHoraInicio()).atOffset(OFFSET_LIMA);
        OffsetDateTime fin;
        if (Boolean.TRUE.equals(turno.getCruzaMedianoche()) || turno.getHoraFin().isBefore(turno.getHoraInicio())) {
            fin = fechaServicio.plusDays(1).atTime(turno.getHoraFin()).atOffset(OFFSET_LIMA);
        } else {
            fin = fechaServicio.atTime(turno.getHoraFin()).atOffset(OFFSET_LIMA);
        }
        return new OffsetDateTime[]{inicio, fin};
    }

    private void validarConflictosRecursos(OffsetDateTime inicio,
                                          OffsetDateTime fin,
                                          List<AsignacionPersonalItemRequest> personalItems,
                                          List<ReservaUnidadItemRequest> unidadItems,
                                          List<ReservaEquipamientoItemRequest> equipamientoItems,
                                          UUID excluirId) {
        if (personalItems != null) {
            for (AsignacionPersonalItemRequest item : personalItems) {
                PersonalSerenazgo s = personalRepository.findById(item.getPersonalId())
                        .orElseThrow(() -> new NoSuchElementException("Personal no encontrado: " + item.getPersonalId()));

                if (s.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
                    throw new IllegalStateException("El efectivo " + s.getCodigoInterno() + " no está habilitado (Estado: " + s.getEstado().getDescripcion() + ")");
                }

                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosPersonal(
                        s.getId(), inicio, fin, excluirId);
                if (!conf.isEmpty()) {
                    throw new IllegalStateException("Conflicto horario: El sereno " + s.getCodigoInterno() +
                            " ya está asignado al servicio " + conf.get(0).getCodigo() +
                            " (" + conf.get(0).getFechaHoraInicio().format(FORMATO_HORA) + " - " + conf.get(0).getFechaHoraFin().format(FORMATO_HORA) + ")");
                }
            }
        }

        if (unidadItems != null) {
            for (ReservaUnidadItemRequest item : unidadItems) {
                UnidadMovilSerenazgo um = unidadRepository.findById(item.getUnidadId())
                        .orElseThrow(() -> new NoSuchElementException("Unidad vehicular no encontrada: " + item.getUnidadId()));

                if (um.getEstadoOperativo() == EstadoUnidadMovil.EN_MANTENIMIENTO || um.getEstadoOperativo() == EstadoUnidadMovil.FUERA_DE_SERVICIO || um.getEstadoOperativo() == EstadoUnidadMovil.RETIRADA) {
                    throw new IllegalStateException("La unidad " + um.getPlaca() + " no está disponible operacionalmente (" + um.getEstadoOperativo().getDescripcion() + ")");
                }

                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosUnidad(
                        um.getId(), inicio, fin, excluirId);
                if (!conf.isEmpty()) {
                    throw new IllegalStateException("Conflicto de unidad: El vehículo " + um.getPlaca() +
                            " ya está reservado en el servicio " + conf.get(0).getCodigo());
                }
            }
        }

        if (equipamientoItems != null) {
            for (ReservaEquipamientoItemRequest item : equipamientoItems) {
                EquipamientoSerenazgo eq = equipamientoRepository.findById(item.getEquipamientoId())
                        .orElseThrow(() -> new NoSuchElementException("Equipamiento no encontrado: " + item.getEquipamientoId()));

                List<ProgramacionServicioSerenazgo> conf = programacionRepository.buscarConflictosEquipamiento(
                        eq.getId(), inicio, fin, excluirId);
                if (!conf.isEmpty()) {
                    throw new IllegalStateException("Conflicto de equipamiento: El bien " + eq.getCodigoInterno() +
                            " ya se encuentra reservado en el servicio " + conf.get(0).getCodigo());
                }
            }
        }
    }

    private ProgramacionResponse mapearResponse(ProgramacionServicioSerenazgo p) {
        int perCount = p.getPersonalAsignado() != null && !p.getPersonalAsignado().isEmpty()
                ? p.getPersonalAsignado().size()
                : (p.getId() != null ? programacionPersonalRepository.findByProgramacionId(p.getId()).size() : 0);
        int uniCount = p.getUnidadesReservadas() != null && !p.getUnidadesReservadas().isEmpty()
                ? p.getUnidadesReservadas().size()
                : (p.getId() != null ? programacionUnidadRepository.findByProgramacionId(p.getId()).size() : 0);
        int eqCount = p.getEquipamientoReservado() != null && !p.getEquipamientoReservado().isEmpty()
                ? p.getEquipamientoReservado().size()
                : (p.getId() != null ? programacionEquipamientoRepository.findByProgramacionId(p.getId()).size() : 0);

        String respNom = "Por designar";
        if (p.getResponsableServicio() != null && p.getResponsableServicio().getPersona() != null) {
            respNom = p.getResponsableServicio().getPersona().getNombreCompleto();
        } else if (p.getPersonal() != null && p.getPersonal().getPersona() != null) {
            respNom = p.getPersonal().getPersona().getNombreCompleto();
        }

        return ProgramacionResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .fechaServicio(p.getFechaServicio())
                .fechaHoraInicio(p.getFechaHoraInicio())
                .fechaHoraFin(p.getFechaHoraFin())
                .turnoHoraInicio(p.getTurno() != null ? p.getTurno().getHoraInicio() : null)
                .turnoHoraFin(p.getTurno() != null ? p.getTurno().getHoraFin() : null)
                .turnoCruzaMedianoche(p.getTurno() != null ? p.getTurno().getCruzaMedianoche() : false)
                .turnoNombre(p.getTurno() != null ? p.getTurno().getNombre() : "Turno")
                .turnoId(p.getTurno() != null ? p.getTurno().getId() : null)
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "General")
                .sectorCodigo(p.getSector() != null ? p.getSector().getCodigo() : null)
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                .zonaCodigo(p.getZona() != null ? p.getZona().getCodigo() : null)
                .zonaId(p.getZona() != null ? p.getZona().getId() : null)
                .tipoServicio(p.getTipoServicio())
                .tipoServicioDesc(p.getTipoServicio() != null ? p.getTipoServicio().getDescripcion() : "Patrullaje")
                .estado(p.getEstado())
                .estadoDesc(p.getEstado() != null ? p.getEstado().getDescripcion() : "Programada")
                .responsableServicioNombre(respNom)
                .totalPersonalAsignado(perCount)
                .totalUnidadesReservadas(uniCount)
                .totalEquipamientoReservado(eqCount)
                .descripcion(p.getDescripcion())
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PdfPCell crearCelda(String texto, Font font, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        return c;
    }

    private String sanitizar(String val) {
        if (val == null) return "";
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) {
            return "'" + val;
        }
        return val;
    }
}

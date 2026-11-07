package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.UnidadesMovilesService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnidadesMovilesServiceImpl implements UnidadesMovilesService {

    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final OficinaRepository oficinaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionUnidadMovilRepository asignacionRepository;
    private final HistorialEstadoUnidadMovilRepository historialEstadoRepository;
    private final MantenimientoRecursoRepository mantenimientoRepository;
    private final IncidenciaTecnicaRecursoRepository incidenciaRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;

    @Override
    @Transactional
    public UnidadMovilResponse registrar(UnidadMovilCreateRequest request, String username) {
        log.info("Registrando unidad móvil por usuario: {}", username);

        String placaNormalizada = normalizarPlaca(request.getPlaca());
        if (placaNormalizada != null && unidadRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw new IllegalArgumentException("Ya existe una unidad móvil registrada con la placa: " + placaNormalizada);
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina especificada no existe"));
        }

        Long seq = unidadRepository.obtenerSiguienteValorSecuencia();
        String codigoGenerado = String.format("UNI-%06d", seq);

        UnidadMovilSerenazgo unidad = UnidadMovilSerenazgo.builder()
                .codigoInterno(codigoGenerado)
                .tipoUnidad(request.getTipoUnidad())
                .placa(placaNormalizada)
                .marca(request.getMarca() != null ? request.getMarca().trim() : null)
                .modelo(request.getModelo() != null ? request.getModelo().trim() : null)
                .anio(request.getAnio())
                .color(request.getColor() != null ? request.getColor().trim() : null)
                .numeroMotor(request.getNumeroMotor() != null ? request.getNumeroMotor().trim() : null)
                .numeroChasis(request.getNumeroChasis() != null ? request.getNumeroChasis().trim() : null)
                .codigoPatrimonial(request.getCodigoPatrimonial() != null ? request.getCodigoPatrimonial().trim() : null)
                .estadoOperativo(EstadoUnidadMovil.DISPONIBLE)
                .oficinaResponsable(oficina)
                .kilometrajeActual(request.getKilometrajeActual() != null ? request.getKilometrajeActual() : BigDecimal.ZERO)
                .fechaIncorporacion(request.getFechaIncorporacion() != null ? request.getFechaIncorporacion() : LocalDate.now())
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .build();

        unidad = unidadRepository.save(unidad);

        // Registro en historial de estados
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                .unidad(unidad)
                .estadoAnterior(EstadoUnidadMovil.INACTIVA)
                .estadoNuevo(EstadoUnidadMovil.DISPONIBLE)
                .fechaEfectiva(OffsetDateTime.now())
                .motivo("Alta e incorporación inicial de unidad móvil al cuerpo de serenazgo")
                .usuarioRegistro(usuario)
                .build();
        historialEstadoRepository.save(hist);

        log.info("Unidad móvil registrada con éxito: {} [{}]", unidad.getCodigoInterno(), unidad.getPlaca());
        return mapearResponse(unidad);
    }

    @Override
    @Transactional
    public UnidadMovilResponse actualizar(UUID id, UnidadMovilUpdateRequest request, String username) {
        log.info("Actualizando unidad móvil ID: {} por usuario: {}", id, username);

        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        String placaNormalizada = normalizarPlaca(request.getPlaca());
        if (placaNormalizada != null && unidadRepository.existsByPlacaIgnoreCaseAndIdNot(placaNormalizada, id)) {
            throw new IllegalArgumentException("Ya existe otra unidad móvil registrada con la placa: " + placaNormalizada);
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina especificada no existe"));
        }

        unidad.setTipoUnidad(request.getTipoUnidad());
        unidad.setPlaca(placaNormalizada);
        unidad.setMarca(request.getMarca() != null ? request.getMarca().trim() : null);
        unidad.setModelo(request.getModelo() != null ? request.getModelo().trim() : null);
        unidad.setAnio(request.getAnio());
        unidad.setColor(request.getColor() != null ? request.getColor().trim() : null);
        unidad.setNumeroMotor(request.getNumeroMotor() != null ? request.getNumeroMotor().trim() : null);
        unidad.setNumeroChasis(request.getNumeroChasis() != null ? request.getNumeroChasis().trim() : null);
        unidad.setCodigoPatrimonial(request.getCodigoPatrimonial() != null ? request.getCodigoPatrimonial().trim() : null);
        unidad.setOficinaResponsable(oficina);
        if (request.getKilometrajeActual() != null) {
            unidad.setKilometrajeActual(request.getKilometrajeActual());
        }
        unidad.setObservaciones(request.getObservaciones());
        unidad.setUpdatedBy(username);

        unidad = unidadRepository.save(unidad);
        return mapearResponse(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadMovilDetailResponse obtenerDetalle(UUID id) {
        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        UnidadMovilDetailResponse resp = UnidadMovilDetailResponse.builder()
                .id(unidad.getId())
                .codigoInterno(unidad.getCodigoInterno())
                .tipoUnidad(unidad.getTipoUnidad())
                .tipoUnidadDesc(unidad.getTipoUnidad().getDescripcion())
                .placa(unidad.getPlaca())
                .marca(unidad.getMarca())
                .modelo(unidad.getModelo())
                .anio(unidad.getAnio())
                .color(unidad.getColor())
                .numeroMotor(unidad.getNumeroMotor())
                .numeroChasis(unidad.getNumeroChasis())
                .codigoPatrimonial(unidad.getCodigoPatrimonial())
                .estadoOperativo(unidad.getEstadoOperativo())
                .estadoOperativoDesc(unidad.getEstadoOperativo().getDescripcion())
                .oficinaId(unidad.getOficinaResponsable() != null ? unidad.getOficinaResponsable().getId() : null)
                .oficinaNombre(unidad.getOficinaResponsable() != null ? unidad.getOficinaResponsable().getNombre() : null)
                .kilometrajeActual(unidad.getKilometrajeActual())
                .fechaIncorporacion(unidad.getFechaIncorporacion())
                .observaciones(unidad.getObservaciones())
                .version(unidad.getVersion())
                .createdAt(unidad.getCreatedAt())
                .updatedAt(unidad.getUpdatedAt())
                .createdBy(unidad.getCreatedBy())
                .updatedBy(unidad.getUpdatedBy())
                .disponibleParaAsignacion(unidad.getEstadoOperativo() == EstadoUnidadMovil.DISPONIBLE || unidad.getEstadoOperativo() == EstadoUnidadMovil.OPERATIVA)
                .build();

        // Asignaciones
        List<AsignacionUnidadMovil> asignaciones = asignacionRepository.findByUnidadIdOrderByFechaInicioDesc(id);
        resp.setAsignaciones(asignaciones.stream().map(this::mapearAsignacionResponse).collect(Collectors.toList()));

        Optional<AsignacionUnidadMovil> activaOpt = asignacionRepository.findFirstByUnidadIdAndEstadoAsignacion(id, EstadoAsignacionRecurso.ACTIVA);
        if (activaOpt.isPresent()) {
            AsignacionUnidadMovil act = activaOpt.get();
            resp.setAsignacionActivaId(act.getId());
            if (act.getPersonal() != null && act.getPersonal().getPersona() != null) {
                resp.setResponsableActualNombre(act.getPersonal().getPersona().getNombreCompleto());
            }
        }

        // Mantenimientos
        List<MantenimientoRecurso> mantenimientos = mantenimientoRepository.findByUnidadIdOrderByFechaProgramadaDesc(id);
        resp.setMantenimientos(mantenimientos.stream().map(this::mapearMantenimientoResponse).collect(Collectors.toList()));

        // Incidencias técnicas
        List<IncidenciaTecnicaRecurso> incidencias = incidenciaRepository.findByUnidadIdOrderByFechaReporteDesc(id);
        resp.setIncidencias(incidencias.stream().map(this::mapearIncidenciaResponse).collect(Collectors.toList()));

        // Historial estados
        List<HistorialEstadoUnidadMovil> histEstados = historialEstadoRepository.findByUnidadIdOrderByFechaEfectivaDesc(id);
        resp.setHistorialEstados(histEstados.stream().map(this::mapearHistorialEstadoResponse).collect(Collectors.toList()));

        // Equipamientos instalados / asignados a la unidad
        List<EquipamientoSerenazgo> equipos = equipamientoRepository.findByUnidadMovilId(id);
        resp.setEquipamientosAsignados(equipos.stream().map(this::mapearEquipamientoResponse).collect(Collectors.toList()));

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnidadMovilResponse> listar(UnidadMovilFilter filter, Pageable pageable) {
        List<UnidadMovilSerenazgo> todas = unidadRepository.findAll();

        List<UnidadMovilResponse> filtradas = todas.stream()
                .filter(u -> {
                    if (filter == null) return true;
                    if (filter.getQ() != null && !filter.getQ().trim().isEmpty()) {
                        String q = filter.getQ().trim().toLowerCase();
                        boolean matchCod = u.getCodigoInterno() != null && u.getCodigoInterno().toLowerCase().contains(q);
                        boolean matchPlaca = u.getPlaca() != null && u.getPlaca().toLowerCase().contains(q);
                        boolean matchMarca = u.getMarca() != null && u.getMarca().toLowerCase().contains(q);
                        boolean matchModelo = u.getModelo() != null && u.getModelo().toLowerCase().contains(q);
                        boolean matchPatrimonio = u.getCodigoPatrimonial() != null && u.getCodigoPatrimonial().toLowerCase().contains(q);
                        if (!matchCod && !matchPlaca && !matchMarca && !matchModelo && !matchPatrimonio) return false;
                    }
                    if (filter.getTipoUnidad() != null && u.getTipoUnidad() != filter.getTipoUnidad()) {
                        return false;
                    }
                    if (filter.getEstadoOperativo() != null && u.getEstadoOperativo() != filter.getEstadoOperativo()) {
                        return false;
                    }
                    if (filter.getOficinaId() != null) {
                        if (u.getOficinaResponsable() == null || !u.getOficinaResponsable().getId().equals(filter.getOficinaId())) {
                            return false;
                        }
                    }
                    if (filter.getDisponible() != null) {
                        boolean disp = (u.getEstadoOperativo() == EstadoUnidadMovil.DISPONIBLE || u.getEstadoOperativo() == EstadoUnidadMovil.OPERATIVA);
                        if (filter.getDisponible() != disp) return false;
                    }
                    return true;
                })
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtradas, pageable, filtradas.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtradas.size());
        List<UnidadMovilResponse> pageContent = (start < filtradas.size()) ? filtradas.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtradas.size());
    }

    @Override
    @Transactional
    public UnidadMovilResponse cambiarEstado(UUID id, CambioEstadoUnidadRequest request, String username) {
        log.info("Cambiando estado de unidad ID: {} a {} por {}", id, request.getNuevoEstado(), username);

        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        EstadoUnidadMovil estadoAnterior = unidad.getEstadoOperativo();
        if (estadoAnterior == request.getNuevoEstado()) {
            return mapearResponse(unidad);
        }

        // Si la unidad está asignada y se pretende pasar a mantenimiento o fuera de servicio, verificar
        if (estadoAnterior == EstadoUnidadMovil.ASIGNADA && 
            (request.getNuevoEstado() == EstadoUnidadMovil.EN_MANTENIMIENTO || request.getNuevoEstado() == EstadoUnidadMovil.FUERA_DE_SERVICIO)) {
            // Se debe cerrar la asignación activa previamente o advertir
            Optional<AsignacionUnidadMovil> asigOpt = asignacionRepository.findFirstByUnidadIdAndEstadoAsignacion(id, EstadoAsignacionRecurso.ACTIVA);
            asigOpt.ifPresent(asig -> {
                asig.setEstadoAsignacion(EstadoAsignacionRecurso.CANCELADA);
                asig.setFechaDevolucionReal(OffsetDateTime.now());
                asig.setObservacionesDevolucion("Asignación interrumpida por cambio forzado a: " + request.getNuevoEstado());
                asignacionRepository.save(asig);
            });
        }

        unidad.setEstadoOperativo(request.getNuevoEstado());
        unidad.setUpdatedBy(username);
        unidad = unidadRepository.save(unidad);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                .unidad(unidad)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(request.getNuevoEstado())
                .fechaEfectiva(OffsetDateTime.now())
                .motivo(request.getMotivo())
                .usuarioRegistro(usuario)
                .build();
        historialEstadoRepository.save(hist);

        return mapearResponse(unidad);
    }

    @Override
    @Transactional
    public AsignacionUnidadResponse asignar(UUID id, AsignarUnidadRequest request, String username) {
        log.info("Asignando unidad ID: {} a personal ID: {} por {}", id, request.getPersonalId(), username);

        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        if (unidad.getEstadoOperativo() != EstadoUnidadMovil.DISPONIBLE && unidad.getEstadoOperativo() != EstadoUnidadMovil.OPERATIVA) {
            throw new IllegalStateException("La unidad móvil no se encuentra disponible para asignación. Estado actual: " + unidad.getEstadoOperativo().getDescripcion());
        }

        if (asignacionRepository.existsByUnidadIdAndEstadoAsignacion(id, EstadoAsignacionRecurso.ACTIVA)) {
            throw new IllegalStateException("La unidad ya cuenta con una asignación activa en curso.");
        }

        PersonalSerenazgo personal = personalRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new NoSuchElementException("El personal de serenazgo especificado no existe"));

        if (personal.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            throw new IllegalStateException("El efectivo de serenazgo no está en servicio activo (" + personal.getEstado().getDescripcion() + ")");
        }

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId()).orElse(null);
        } else if (personal.getOficina() != null) {
            oficina = personal.getOficina();
        }

        Usuario usuarioEntrega = usuarioRepository.findByUsername(username).orElse(null);

        BigDecimal kmSalida = request.getKilometrajeSalida() != null ? request.getKilometrajeSalida() : 
                (unidad.getKilometrajeActual() != null ? unidad.getKilometrajeActual() : BigDecimal.ZERO);

        AsignacionUnidadMovil asignacion = AsignacionUnidadMovil.builder()
                .unidad(unidad)
                .personal(personal)
                .oficina(oficina)
                .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : OffsetDateTime.now())
                .fechaFinProgramada(request.getFechaFinProgramada())
                .kilometrajeSalida(kmSalida)
                .motivoServicio(request.getMotivoServicio())
                .estadoAsignacion(EstadoAsignacionRecurso.ACTIVA)
                .estadoInicialUnidad(request.getEstadoInicialUnidad() != null ? request.getEstadoInicialUnidad() : "BUENO")
                .observacionesEntrega(request.getObservacionesEntrega())
                .usuarioEntrega(usuarioEntrega)
                .build();

        asignacion = asignacionRepository.save(asignacion);

        // Transición de estado a ASIGNADA
        EstadoUnidadMovil estAnt = unidad.getEstadoOperativo();
        unidad.setEstadoOperativo(EstadoUnidadMovil.ASIGNADA);
        unidad.setKilometrajeActual(kmSalida);
        unidad.setUpdatedBy(username);
        unidadRepository.save(unidad);

        HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                .unidad(unidad)
                .estadoAnterior(estAnt)
                .estadoNuevo(EstadoUnidadMovil.ASIGNADA)
                .fechaEfectiva(OffsetDateTime.now())
                .motivo("Asignación de servicio a: " + personal.getPersona().getNombreCompleto() + " (" + request.getMotivoServicio() + ")")
                .usuarioRegistro(usuarioEntrega)
                .build();
        historialEstadoRepository.save(hist);

        log.info("Asignación exitosa ID: {}", asignacion.getId());
        return mapearAsignacionResponse(asignacion);
    }

    @Override
    @Transactional
    public AsignacionUnidadResponse devolver(UUID id, DevolverUnidadRequest request, String username) {
        log.info("Registrando devolución de unidad ID: {} por {}", id, username);

        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        AsignacionUnidadMovil asignacion = asignacionRepository.findFirstByUnidadIdAndEstadoAsignacion(id, EstadoAsignacionRecurso.ACTIVA)
                .orElseThrow(() -> new IllegalStateException("La unidad no registra ninguna asignación activa para devolver"));

        Usuario usuarioRecepcion = usuarioRepository.findByUsername(username).orElse(null);

        OffsetDateTime fechaDev = request.getFechaDevolucion() != null ? request.getFechaDevolucion() : OffsetDateTime.now();
        asignacion.setFechaDevolucionReal(fechaDev);
        asignacion.setKilometrajeRetorno(request.getKilometrajeRetorno());
        asignacion.setEstadoAsignacion(EstadoAsignacionRecurso.DEVUELTA);
        asignacion.setEstadoRetornoUnidad(request.getEstadoRetornoUnidad() != null ? request.getEstadoRetornoUnidad() : "CONFORME");
        asignacion.setObservacionesDevolucion(request.getObservacionesDevolucion());
        asignacion.setUsuarioRecepcion(usuarioRecepcion);

        asignacion = asignacionRepository.save(asignacion);

        // Actualizar unidad
        if (request.getKilometrajeRetorno() != null && 
            (unidad.getKilometrajeActual() == null || request.getKilometrajeRetorno().compareTo(unidad.getKilometrajeActual()) > 0)) {
            unidad.setKilometrajeActual(request.getKilometrajeRetorno());
        }
        unidad.setEstadoOperativo(EstadoUnidadMovil.DISPONIBLE);
        unidad.setUpdatedBy(username);
        unidadRepository.save(unidad);

        HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                .unidad(unidad)
                .estadoAnterior(EstadoUnidadMovil.ASIGNADA)
                .estadoNuevo(EstadoUnidadMovil.DISPONIBLE)
                .fechaEfectiva(OffsetDateTime.now())
                .motivo("Devolución y recepción conforme de unidad móvil")
                .usuarioRegistro(usuarioRecepcion)
                .build();
        historialEstadoRepository.save(hist);

        return mapearAsignacionResponse(asignacion);
    }

    @Override
    @Transactional
    public MantenimientoResponse registrarMantenimiento(UUID id, RegistroMantenimientoRequest request, String username) {
        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        Long seq = mantenimientoRepository.obtenerSiguienteValorSecuencia();
        String codigoMnt = String.format("MNT-%06d", seq);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        MantenimientoRecurso mnt = MantenimientoRecurso.builder()
                .codigo(codigoMnt)
                .tipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL)
                .unidad(unidad)
                .tipoMantenimiento(request.getTipoMantenimiento())
                .fechaProgramada(request.getFechaProgramada())
                .kilometrajeMantenimiento(request.getKilometrajeMantenimiento() != null ? request.getKilometrajeMantenimiento() : unidad.getKilometrajeActual())
                .descripcion(request.getDescripcion())
                .tallerProveedor(request.getTallerProveedor())
                .costo(request.getCosto())
                .estado(EstadoMantenimientoRecurso.PROGRAMADO)
                .usuarioRegistro(usuario)
                .build();

        mnt = mantenimientoRepository.save(mnt);

        // Si la fecha programada es hoy, transicionar a EN_MANTENIMIENTO
        if (request.getFechaProgramada().isEqual(LocalDate.now())) {
            EstadoUnidadMovil anterior = unidad.getEstadoOperativo();
            unidad.setEstadoOperativo(EstadoUnidadMovil.EN_MANTENIMIENTO);
            unidadRepository.save(unidad);

            HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                    .unidad(unidad)
                    .estadoAnterior(anterior)
                    .estadoNuevo(EstadoUnidadMovil.EN_MANTENIMIENTO)
                    .fechaEfectiva(OffsetDateTime.now())
                    .motivo("Ingreso a mantenimiento técnico: " + mnt.getCodigo() + " - " + request.getDescripcion())
                    .usuarioRegistro(usuario)
                    .build();
            historialEstadoRepository.save(hist);
        }

        return mapearMantenimientoResponse(mnt);
    }

    @Override
    @Transactional
    public IncidenciaTecnicaResponse registrarIncidencia(UUID id, RegistroIncidenciaTecnicaRequest request, String username) {
        UnidadMovilSerenazgo unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La unidad móvil especificada no existe"));

        Long seq = incidenciaRepository.obtenerSiguienteValorSecuencia();
        String codigoInc = String.format("INC-%06d", seq);

        PersonalSerenazgo reporta = null;
        if (request.getPersonalReportaId() != null) {
            reporta = personalRepository.findById(request.getPersonalReportaId()).orElse(null);
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        IncidenciaTecnicaRecurso inc = IncidenciaTecnicaRecurso.builder()
                .codigo(codigoInc)
                .tipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL)
                .unidad(unidad)
                .tipoIncidencia(request.getTipoIncidencia())
                .fechaReporte(OffsetDateTime.now())
                .descripcion(request.getDescripcion())
                .personalReporta(reporta)
                .nivelGravedad(request.getNivelGravedad() != null ? request.getNivelGravedad() : NivelGravedadIncidencia.MEDIA)
                .estado(EstadoIncidenciaTecnica.REGISTRADA)
                .usuarioRegistro(usuario)
                .build();

        inc = incidenciaRepository.save(inc);

        // Si la gravedad es CRÍTICA, colocar la unidad fuera de servicio preventivamente
        if (request.getNivelGravedad() == NivelGravedadIncidencia.CRITICA) {
            EstadoUnidadMovil anterior = unidad.getEstadoOperativo();
            unidad.setEstadoOperativo(EstadoUnidadMovil.FUERA_DE_SERVICIO);
            unidadRepository.save(unidad);

            HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                    .unidad(unidad)
                    .estadoAnterior(anterior)
                    .estadoNuevo(EstadoUnidadMovil.FUERA_DE_SERVICIO)
                    .fechaEfectiva(OffsetDateTime.now())
                    .motivo("Falla técnica crítica reportada en incidencia: " + inc.getCodigo())
                    .usuarioRegistro(usuario)
                    .build();
            historialEstadoRepository.save(hist);
        }

        return mapearIncidenciaResponse(inc);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(UUID id) {
        UnidadMovilDetailResponse u = obtenerDetalle(id);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            // Encabezado institucional
            Font fontTituloInst = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(71, 85, 105));
            Font fontDocTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(30, 58, 138));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(51, 65, 85));
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(15, 23, 42));

            Paragraph pInst = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTituloInst);
            pInst.setAlignment(Element.ALIGN_CENTER);
            doc.add(pInst);

            Paragraph pSub = new Paragraph("Subgerencia de Seguridad Ciudadana y Serenazgo\nProvincia de Huamanga — Región Ayacucho", fontSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10f);
            doc.add(pSub);

            LineSeparator sep = new LineSeparator(1.5f, 100, new Color(30, 58, 138), Element.ALIGN_CENTER, -2);
            doc.add(sep);

            Paragraph pTit = new Paragraph("FICHA TÉCNICA Y OPERATIVA DE UNIDAD MÓVIL", fontDocTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            pTit.setSpacingBefore(12f);
            pTit.setSpacingAfter(15f);
            doc.add(pTit);

            // Tabla de datos del vehículo
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{22, 28, 22, 28});

            agregarCelda(table, "Código Interno:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getCodigoInterno(), fontValor, Color.WHITE);
            agregarCelda(table, "Tipo de Unidad:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getTipoUnidadDesc(), fontValor, Color.WHITE);

            agregarCelda(table, "Placa Oficial:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getPlaca() != null ? u.getPlaca() : "NO APLICA", fontValor, Color.WHITE);
            agregarCelda(table, "Estado Operativo:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getEstadoOperativoDesc(), fontValor, Color.WHITE);

            agregarCelda(table, "Marca / Modelo:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, (u.getMarca() != null ? u.getMarca() : "-") + " / " + (u.getModelo() != null ? u.getModelo() : "-"), fontValor, Color.WHITE);
            agregarCelda(table, "Año Fabricación:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getAnio() != null ? String.valueOf(u.getAnio()) : "N/D", fontValor, Color.WHITE);

            agregarCelda(table, "Color:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getColor() != null ? u.getColor() : "N/D", fontValor, Color.WHITE);
            agregarCelda(table, "Kilometraje Actual:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getKilometrajeActual() != null ? u.getKilometrajeActual().toString() + " km" : "0 km", fontValor, Color.WHITE);

            agregarCelda(table, "N° de Motor:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getNumeroMotor() != null ? u.getNumeroMotor() : "N/D", fontValor, Color.WHITE);
            agregarCelda(table, "N° de Chasis/VIN:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getNumeroChasis() != null ? u.getNumeroChasis() : "N/D", fontValor, Color.WHITE);

            agregarCelda(table, "Cód. Patrimonial:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getCodigoPatrimonial() != null ? u.getCodigoPatrimonial() : "PENDIENTE", fontValor, Color.WHITE);
            agregarCelda(table, "Oficina Responsable:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getOficinaNombre() != null ? u.getOficinaNombre() : "BASE CENTRAL SERENAZGO", fontValor, Color.WHITE);

            agregarCelda(table, "Fecha Incorporación:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getFechaIncorporacion() != null ? u.getFechaIncorporacion().toString() : "N/D", fontValor, Color.WHITE);
            agregarCelda(table, "Asignado Actual:", fontLabel, Color.LIGHT_GRAY);
            agregarCelda(table, u.getResponsableActualNombre() != null ? u.getResponsableActualNombre() : "DISPONIBLE EN BASE", fontValor, Color.WHITE);

            doc.add(table);

            // Observaciones
            if (u.getObservaciones() != null && !u.getObservaciones().trim().isEmpty()) {
                Paragraph pObs = new Paragraph("\nObservaciones y Especificaciones Técnicas:\n" + u.getObservaciones(), fontValor);
                pObs.setSpacingAfter(15f);
                doc.add(pObs);
            }

            // Pie de página de trazabilidad
            Paragraph pPie = new Paragraph("\n\nDocumento emitido electrónicamente por el Sistema Integral de Gestión Municipal (SIGM-QUINUA).\nFecha de emisión: " + 
                    OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontSubtitulo);
            pPie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pPie);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar ficha PDF de unidad", e);
            throw new RuntimeException("Error en generación de ficha técnica PDF", e);
        }
    }

    private void agregarCelda(PdfPTable table, String texto, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(6f);
        if (bgColor != Color.WHITE) {
            cell.setBackgroundColor(new Color(241, 245, 249));
        }
        table.addCell(cell);
    }

    private String normalizarPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) return null;
        return placa.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private UnidadMovilResponse mapearResponse(UnidadMovilSerenazgo u) {
        boolean disp = (u.getEstadoOperativo() == EstadoUnidadMovil.DISPONIBLE || u.getEstadoOperativo() == EstadoUnidadMovil.OPERATIVA);

        String responsable = null;
        UUID asignacionId = null;
        Optional<AsignacionUnidadMovil> asig = asignacionRepository.findFirstByUnidadIdAndEstadoAsignacion(u.getId(), EstadoAsignacionRecurso.ACTIVA);
        if (asig.isPresent()) {
            asignacionId = asig.get().getId();
            if (asig.get().getPersonal() != null && asig.get().getPersonal().getPersona() != null) {
                responsable = asig.get().getPersonal().getPersona().getNombreCompleto();
            }
        }

        return UnidadMovilResponse.builder()
                .id(u.getId())
                .codigoInterno(u.getCodigoInterno())
                .tipoUnidad(u.getTipoUnidad())
                .tipoUnidadDesc(u.getTipoUnidad().getDescripcion())
                .placa(u.getPlaca())
                .marca(u.getMarca())
                .modelo(u.getModelo())
                .anio(u.getAnio())
                .color(u.getColor())
                .codigoPatrimonial(u.getCodigoPatrimonial())
                .estadoOperativo(u.getEstadoOperativo())
                .estadoOperativoDesc(u.getEstadoOperativo().getDescripcion())
                .oficinaId(u.getOficinaResponsable() != null ? u.getOficinaResponsable().getId() : null)
                .oficinaNombre(u.getOficinaResponsable() != null ? u.getOficinaResponsable().getNombre() : null)
                .kilometrajeActual(u.getKilometrajeActual())
                .fechaIncorporacion(u.getFechaIncorporacion())
                .observaciones(u.getObservaciones())
                .responsableActualNombre(responsable)
                .asignacionActivaId(asignacionId)
                .disponibleParaAsignacion(disp)
                .build();
    }

    private AsignacionUnidadResponse mapearAsignacionResponse(AsignacionUnidadMovil a) {
        String personalNombre = null;
        String personalCod = null;
        if (a.getPersonal() != null) {
            personalCod = a.getPersonal().getCodigoInterno();
            if (a.getPersonal().getPersona() != null) {
                personalNombre = a.getPersonal().getPersona().getNombreCompleto();
            }
        }

        return AsignacionUnidadResponse.builder()
                .id(a.getId())
                .unidadId(a.getUnidad().getId())
                .unidadCodigo(a.getUnidad().getCodigoInterno())
                .unidadPlaca(a.getUnidad().getPlaca())
                .personalId(a.getPersonal().getId())
                .personalNombreCompleto(personalNombre)
                .personalCodigo(personalCod)
                .fechaInicio(a.getFechaInicio())
                .fechaFinProgramada(a.getFechaFinProgramada())
                .fechaDevolucionReal(a.getFechaDevolucionReal())
                .kilometrajeSalida(a.getKilometrajeSalida())
                .kilometrajeRetorno(a.getKilometrajeRetorno())
                .motivoServicio(a.getMotivoServicio())
                .estadoAsignacion(a.getEstadoAsignacion())
                .estadoAsignacionDesc(a.getEstadoAsignacion().getDescripcion())
                .estadoInicialUnidad(a.getEstadoInicialUnidad())
                .estadoRetornoUnidad(a.getEstadoRetornoUnidad())
                .observacionesEntrega(a.getObservacionesEntrega())
                .observacionesDevolucion(a.getObservacionesDevolucion())
                .usuarioEntrega(a.getUsuarioEntrega() != null ? a.getUsuarioEntrega().getUsername() : null)
                .usuarioRecepcion(a.getUsuarioRecepcion() != null ? a.getUsuarioRecepcion().getUsername() : null)
                .build();
    }

    private MantenimientoResponse mapearMantenimientoResponse(MantenimientoRecurso m) {
        String infoUnidad = m.getUnidad() != null ? m.getUnidad().getCodigoInterno() + " (" + (m.getUnidad().getPlaca() != null ? m.getUnidad().getPlaca() : m.getUnidad().getTipoUnidad().getDescripcion()) + ")" : null;
        String infoEquipo = m.getEquipamiento() != null ? m.getEquipamiento().getCodigoInterno() + " (" + m.getEquipamiento().getDescripcion() + ")" : null;

        return MantenimientoResponse.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .tipoRecurso(m.getTipoRecurso())
                .tipoRecursoDesc(m.getTipoRecurso().getDescripcion())
                .unidadId(m.getUnidad() != null ? m.getUnidad().getId() : null)
                .unidadInfo(infoUnidad)
                .equipamientoId(m.getEquipamiento() != null ? m.getEquipamiento().getId() : null)
                .equipamientoInfo(infoEquipo)
                .tipoMantenimiento(m.getTipoMantenimiento())
                .tipoMantenimientoDesc(m.getTipoMantenimiento().getDescripcion())
                .fechaProgramada(m.getFechaProgramada())
                .fechaRealizada(m.getFechaRealizada())
                .kilometrajeMantenimiento(m.getKilometrajeMantenimiento())
                .descripcion(m.getDescripcion())
                .tallerProveedor(m.getTallerProveedor())
                .costo(m.getCosto())
                .estado(m.getEstado())
                .estadoDesc(m.getEstado().getDescripcion())
                .motivoCancelacion(m.getMotivoCancelacion())
                .resultadoTecnico(m.getResultadoTecnico())
                .usuarioRegistro(m.getUsuarioRegistro() != null ? m.getUsuarioRegistro().getUsername() : null)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private IncidenciaTecnicaResponse mapearIncidenciaResponse(IncidenciaTecnicaRecurso i) {
        String persNombre = null;
        if (i.getPersonalReporta() != null && i.getPersonalReporta().getPersona() != null) {
            persNombre = i.getPersonalReporta().getPersona().getNombreCompleto();
        }

        return IncidenciaTecnicaResponse.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .tipoRecurso(i.getTipoRecurso())
                .tipoRecursoDesc(i.getTipoRecurso().getDescripcion())
                .unidadId(i.getUnidad() != null ? i.getUnidad().getId() : null)
                .unidadInfo(i.getUnidad() != null ? i.getUnidad().getCodigoInterno() : null)
                .equipamientoId(i.getEquipamiento() != null ? i.getEquipamiento().getId() : null)
                .equipamientoInfo(i.getEquipamiento() != null ? i.getEquipamiento().getCodigoInterno() : null)
                .tipoIncidencia(i.getTipoIncidencia())
                .tipoIncidenciaDesc(i.getTipoIncidencia().getDescripcion())
                .fechaReporte(i.getFechaReporte())
                .descripcion(i.getDescripcion())
                .personalReportaId(i.getPersonalReporta() != null ? i.getPersonalReporta().getId() : null)
                .personalReportaNombre(persNombre)
                .nivelGravedad(i.getNivelGravedad())
                .nivelGravedadDesc(i.getNivelGravedad().getDescripcion())
                .estado(i.getEstado())
                .estadoDesc(i.getEstado().getDescripcion())
                .accionesTomadas(i.getAccionesTomadas())
                .usuarioRegistro(i.getUsuarioRegistro() != null ? i.getUsuarioRegistro().getUsername() : null)
                .createdAt(i.getCreatedAt())
                .build();
    }

    private HistorialEstadoUnidadResponse mapearHistorialEstadoResponse(HistorialEstadoUnidadMovil h) {
        return HistorialEstadoUnidadResponse.builder()
                .id(h.getId())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoAnteriorDesc(h.getEstadoAnterior().getDescripcion())
                .estadoNuevo(h.getEstadoNuevo())
                .estadoNuevoDesc(h.getEstadoNuevo().getDescripcion())
                .fechaEfectiva(h.getFechaEfectiva())
                .motivo(h.getMotivo())
                .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : null)
                .build();
    }

    private EquipamientoResponse mapearEquipamientoResponse(EquipamientoSerenazgo e) {
        String resp = null;
        if (e.getResponsable() != null && e.getResponsable().getPersona() != null) {
            resp = e.getResponsable().getPersona().getNombreCompleto();
        }

        String uni = null;
        if (e.getUnidadMovil() != null) {
            uni = e.getUnidadMovil().getCodigoInterno() + " (" + (e.getUnidadMovil().getPlaca() != null ? e.getUnidadMovil().getPlaca() : e.getUnidadMovil().getTipoUnidad().getDescripcion()) + ")";
        }

        return EquipamientoResponse.builder()
                .id(e.getId())
                .codigoInterno(e.getCodigoInterno())
                .tipoEquipo(e.getTipoEquipo())
                .tipoEquipoDesc(e.getTipoEquipo().getDescripcion())
                .descripcion(e.getDescripcion())
                .marca(e.getMarca())
                .modelo(e.getModelo())
                .numeroSerie(e.getNumeroSerie())
                .codigoPatrimonial(e.getCodigoPatrimonial())
                .oficinaResponsableId(e.getOficinaResponsable() != null ? e.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(e.getOficinaResponsable() != null ? e.getOficinaResponsable().getNombre() : null)
                .estado(e.getEstado())
                .estadoDesc(e.getEstado().getDescripcion())
                .esControladoPorCantidad(e.getEsControladoPorCantidad())
                .stockTotal(e.getStockTotal())
                .stockDisponible(e.getStockDisponible())
                .responsableId(e.getResponsable() != null ? e.getResponsable().getId() : null)
                .responsableNombre(resp)
                .unidadMovilId(e.getUnidadMovil() != null ? e.getUnidadMovil().getId() : null)
                .unidadMovilInfo(uni)
                .fechaAsignacion(e.getFechaAsignacion())
                .fechaIncorporacion(e.getFechaIncorporacion())
                .observaciones(e.getObservaciones())
                .disponibleParaAsignacion(e.getEstado() == EstadoEquipamientoSerenazgo.DISPONIBLE && (e.getStockDisponible() != null && e.getStockDisponible() > 0))
                .build();
    }
}

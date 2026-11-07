package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EquipamientoSerenazgoService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipamientoSerenazgoServiceImpl implements EquipamientoSerenazgoService {

    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final OficinaRepository oficinaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionEquipamientoRepository asignacionRepository;
    private final MantenimientoRecursoRepository mantenimientoRepository;
    private final IncidenciaTecnicaRecursoRepository incidenciaRepository;

    @Override
    @Transactional
    public EquipamientoResponse registrar(EquipamientoCreateRequest request, String username) {
        log.info("Registrando equipamiento de serenazgo por usuario: {}", username);

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina responsable no existe"));
        }

        Long seq = equipamientoRepository.obtenerSiguienteValorSecuencia();
        String codigoGen = String.format("EQP-%06d", seq);

        boolean esCantidad = Boolean.TRUE.equals(request.getEsControladoPorCantidad());
        int stock = (esCantidad && request.getStockTotal() != null && request.getStockTotal() > 0) ? request.getStockTotal() : 1;

        EquipamientoSerenazgo equip = EquipamientoSerenazgo.builder()
                .codigoInterno(codigoGen)
                .tipoEquipo(request.getTipoEquipo())
                .descripcion(request.getDescripcion().trim())
                .marca(request.getMarca() != null ? request.getMarca().trim() : null)
                .modelo(request.getModelo() != null ? request.getModelo().trim() : null)
                .numeroSerie(request.getNumeroSerie() != null ? request.getNumeroSerie().trim() : null)
                .codigoPatrimonial(request.getCodigoPatrimonial() != null ? request.getCodigoPatrimonial().trim() : null)
                .oficinaResponsable(oficina)
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .esControladoPorCantidad(esCantidad)
                .stockTotal(stock)
                .stockDisponible(stock)
                .fechaIncorporacion(LocalDate.now())
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .build();

        equip = equipamientoRepository.save(equip);
        log.info("Equipamiento registrado con éxito: {}", equip.getCodigoInterno());
        return mapearResponse(equip);
    }

    @Override
    @Transactional
    public EquipamientoResponse actualizar(UUID id, EquipamientoUpdateRequest request, String username) {
        log.info("Actualizando equipamiento ID: {} por {}", id, username);

        EquipamientoSerenazgo equip = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina responsable no existe"));
        }

        equip.setDescripcion(request.getDescripcion().trim());
        equip.setMarca(request.getMarca() != null ? request.getMarca().trim() : null);
        equip.setModelo(request.getModelo() != null ? request.getModelo().trim() : null);
        equip.setNumeroSerie(request.getNumeroSerie() != null ? request.getNumeroSerie().trim() : null);
        equip.setCodigoPatrimonial(request.getCodigoPatrimonial() != null ? request.getCodigoPatrimonial().trim() : null);
        equip.setOficinaResponsable(oficina);
        if (Boolean.TRUE.equals(equip.getEsControladoPorCantidad()) && request.getStockTotal() != null && request.getStockTotal() > 0) {
            int diferencia = request.getStockTotal() - equip.getStockTotal();
            equip.setStockTotal(request.getStockTotal());
            equip.setStockDisponible(Math.max(0, equip.getStockDisponible() + diferencia));
        }
        equip.setObservaciones(request.getObservaciones());
        equip.setUpdatedBy(username);

        equip = equipamientoRepository.save(equip);
        return mapearResponse(equip);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipamientoDetailResponse obtenerDetalle(UUID id) {
        EquipamientoSerenazgo e = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        String resp = (e.getResponsable() != null && e.getResponsable().getPersona() != null) ? 
                e.getResponsable().getPersona().getNombreCompleto() : null;
        String uniInfo = (e.getUnidadMovil() != null) ? 
                e.getUnidadMovil().getCodigoInterno() + " (" + (e.getUnidadMovil().getPlaca() != null ? e.getUnidadMovil().getPlaca() : e.getUnidadMovil().getTipoUnidad().getDescripcion()) + ")" : null;

        EquipamientoDetailResponse respDetail = EquipamientoDetailResponse.builder()
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
                .unidadMovilInfo(uniInfo)
                .fechaAsignacion(e.getFechaAsignacion())
                .fechaIncorporacion(e.getFechaIncorporacion())
                .observaciones(e.getObservaciones())
                .version(e.getVersion())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .disponibleParaAsignacion(e.getEstado() == EstadoEquipamientoSerenazgo.DISPONIBLE && (e.getStockDisponible() != null && e.getStockDisponible() > 0))
                .build();

        // Asignaciones
        List<AsignacionEquipamiento> asigs = asignacionRepository.findByEquipamientoIdOrderByFechaEntregaDesc(id);
        respDetail.setAsignaciones(asigs.stream().map(this::mapearAsignacionResponse).collect(Collectors.toList()));

        // Mantenimientos
        List<MantenimientoRecurso> mnts = mantenimientoRepository.findByEquipamientoIdOrderByFechaProgramadaDesc(id);
        respDetail.setMantenimientos(mnts.stream().map(this::mapearMantenimientoResponse).collect(Collectors.toList()));

        // Incidencias
        List<IncidenciaTecnicaRecurso> incs = incidenciaRepository.findByEquipamientoIdOrderByFechaReporteDesc(id);
        respDetail.setIncidencias(incs.stream().map(this::mapearIncidenciaResponse).collect(Collectors.toList()));

        return respDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipamientoResponse> listar(EquipamientoFilter filter, Pageable pageable) {
        List<EquipamientoSerenazgo> todos = equipamientoRepository.findAll();

        List<EquipamientoResponse> filtrados = todos.stream()
                .filter(e -> {
                    if (filter == null) return true;
                    if (filter.getQ() != null && !filter.getQ().trim().isEmpty()) {
                        String q = filter.getQ().trim().toLowerCase();
                        boolean mCod = e.getCodigoInterno() != null && e.getCodigoInterno().toLowerCase().contains(q);
                        boolean mDesc = e.getDescripcion() != null && e.getDescripcion().toLowerCase().contains(q);
                        boolean mSerie = e.getNumeroSerie() != null && e.getNumeroSerie().toLowerCase().contains(q);
                        boolean mPat = e.getCodigoPatrimonial() != null && e.getCodigoPatrimonial().toLowerCase().contains(q);
                        boolean mMarca = e.getMarca() != null && e.getMarca().toLowerCase().contains(q);
                        if (!mCod && !mDesc && !mSerie && !mPat && !mMarca) return false;
                    }
                    if (filter.getTipoEquipo() != null && e.getTipoEquipo() != filter.getTipoEquipo()) {
                        return false;
                    }
                    if (filter.getEstado() != null && e.getEstado() != filter.getEstado()) {
                        return false;
                    }
                    if (filter.getOficinaId() != null) {
                        if (e.getOficinaResponsable() == null || !e.getOficinaResponsable().getId().equals(filter.getOficinaId())) {
                            return false;
                        }
                    }
                    if (filter.getResponsableId() != null) {
                        if (e.getResponsable() == null || !e.getResponsable().getId().equals(filter.getResponsableId())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtrados, pageable, filtrados.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtrados.size());
        List<EquipamientoResponse> pageContent = (start < filtrados.size()) ? filtrados.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtrados.size());
    }

    @Override
    @Transactional
    public AsignacionEquipamientoResponse asignar(UUID id, AsignarEquipamientoRequest request, String username) {
        log.info("Asignando equipamiento ID: {} por usuario: {}", id, username);

        EquipamientoSerenazgo equip = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        if (equip.getEstado() != EstadoEquipamientoSerenazgo.DISPONIBLE) {
            throw new IllegalStateException("El bien no está disponible para asignación. Estado actual: " + equip.getEstado().getDescripcion());
        }

        int cantidadAAsignar = (request.getCantidad() != null && request.getCantidad() > 0) ? request.getCantidad() : 1;

        if (Boolean.TRUE.equals(equip.getEsControladoPorCantidad())) {
            if (equip.getStockDisponible() < cantidadAAsignar) {
                throw new IllegalStateException("Stock insuficiente. Stock disponible: " + equip.getStockDisponible() + ", solicitado: " + cantidadAAsignar);
            }
            equip.setStockDisponible(equip.getStockDisponible() - cantidadAAsignar);
            if (equip.getStockDisponible() == 0) {
                equip.setEstado(EstadoEquipamientoSerenazgo.ASIGNADO);
            }
        } else {
            equip.setStockDisponible(0);
            equip.setEstado(EstadoEquipamientoSerenazgo.ASIGNADO);
        }

        PersonalSerenazgo personal = null;
        if (request.getPersonalId() != null) {
            personal = personalRepository.findById(request.getPersonalId())
                    .orElseThrow(() -> new NoSuchElementException("El efectivo de serenazgo no existe"));
            if (personal.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
                throw new IllegalStateException("El personal sereno no está en servicio activo");
            }
            equip.setResponsable(personal);
        }

        UnidadMovilSerenazgo unidad = null;
        if (request.getUnidadId() != null) {
            unidad = unidadRepository.findById(request.getUnidadId())
                    .orElseThrow(() -> new NoSuchElementException("La unidad móvil no existe"));
            equip.setUnidadMovil(unidad);
        }

        if (personal == null && unidad == null) {
            throw new IllegalArgumentException("Debe asignar el equipamiento a un efectivo de serenazgo o a una unidad móvil");
        }

        equip.setFechaAsignacion(LocalDate.now());
        equip.setUpdatedBy(username);
        equipamientoRepository.save(equip);

        Usuario usuarioEntrega = usuarioRepository.findByUsername(username).orElse(null);

        AsignacionEquipamiento asig = AsignacionEquipamiento.builder()
                .equipamiento(equip)
                .personal(personal)
                .unidad(unidad)
                .fechaEntrega(OffsetDateTime.now())
                .cantidad(cantidadAAsignar)
                .cantidadDevuelta(0)
                .estadoAsignacion(EstadoAsignacionRecurso.ACTIVA)
                .estadoBienEntrega(request.getEstadoBienEntrega() != null ? request.getEstadoBienEntrega() : "BUENO")
                .motivo(request.getMotivo())
                .observaciones(request.getObservaciones())
                .usuarioEntrega(usuarioEntrega)
                .build();

        asig = asignacionRepository.save(asig);
        return mapearAsignacionResponse(asig);
    }

    @Override
    @Transactional
    public AsignacionEquipamientoResponse devolver(UUID id, DevolverEquipamientoRequest request, String username) {
        log.info("Devolviendo equipamiento ID: {} por {}", id, username);

        EquipamientoSerenazgo equip = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        AsignacionEquipamiento asig = asignacionRepository.findFirstByEquipamientoIdAndEstadoAsignacion(id, EstadoAsignacionRecurso.ACTIVA)
                .orElseThrow(() -> new IllegalStateException("No existe una asignación activa para este bien"));

        Usuario usuarioRecepcion = usuarioRepository.findByUsername(username).orElse(null);

        int cantDevolver = (request.getCantidadDevuelta() != null && request.getCantidadDevuelta() > 0) ? 
                request.getCantidadDevuelta() : (asig.getCantidad() - asig.getCantidadDevuelta());

        if (cantDevolver > (asig.getCantidad() - asig.getCantidadDevuelta())) {
            throw new IllegalArgumentException("La cantidad devuelta supera la cantidad pendiente de devolución");
        }

        asig.setCantidadDevuelta(asig.getCantidadDevuelta() + cantDevolver);
        if (asig.getCantidadDevuelta().equals(asig.getCantidad())) {
            asig.setEstadoAsignacion(EstadoAsignacionRecurso.DEVUELTA_TOTAL);
            asig.setFechaDevolucion(OffsetDateTime.now());
        } else {
            asig.setEstadoAsignacion(EstadoAsignacionRecurso.DEVUELTA_PARCIAL);
        }

        asig.setEstadoBienDevolucion(request.getEstadoBienDevolucion() != null ? request.getEstadoBienDevolucion() : "CONFORME");
        asig.setObservaciones(request.getObservaciones());
        asig.setUsuarioRecepcion(usuarioRecepcion);
        asig = asignacionRepository.save(asig);

        // Reintegrar stock y estado en equipamiento
        equip.setStockDisponible(Math.min(equip.getStockTotal(), equip.getStockDisponible() + cantDevolver));
        if (equip.getStockDisponible() > 0) {
            equip.setEstado(EstadoEquipamientoSerenazgo.DISPONIBLE);
        }
        if (asig.getEstadoAsignacion() == EstadoAsignacionRecurso.DEVUELTA_TOTAL) {
            equip.setResponsable(null);
            equip.setUnidadMovil(null);
            equip.setFechaAsignacion(null);
        }
        equip.setUpdatedBy(username);
        equipamientoRepository.save(equip);

        return mapearAsignacionResponse(asig);
    }

    @Override
    @Transactional
    public MantenimientoResponse registrarMantenimiento(UUID id, RegistroMantenimientoRequest request, String username) {
        EquipamientoSerenazgo equip = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        Long seq = mantenimientoRepository.obtenerSiguienteValorSecuencia();
        String cod = String.format("MNT-%06d", seq);

        Usuario usr = usuarioRepository.findByUsername(username).orElse(null);

        MantenimientoRecurso mnt = MantenimientoRecurso.builder()
                .codigo(cod)
                .tipoRecurso(TipoRecursoSeguridad.EQUIPAMIENTO)
                .equipamiento(equip)
                .tipoMantenimiento(request.getTipoMantenimiento())
                .fechaProgramada(request.getFechaProgramada())
                .descripcion(request.getDescripcion())
                .tallerProveedor(request.getTallerProveedor())
                .costo(request.getCosto())
                .estado(EstadoMantenimientoRecurso.PROGRAMADO)
                .usuarioRegistro(usr)
                .build();

        mnt = mantenimientoRepository.save(mnt);

        if (request.getFechaProgramada().isEqual(LocalDate.now())) {
            equip.setEstado(EstadoEquipamientoSerenazgo.EN_MANTENIMIENTO);
            equipamientoRepository.save(equip);
        }

        return mapearMantenimientoResponse(mnt);
    }

    @Override
    @Transactional
    public IncidenciaTecnicaResponse registrarIncidencia(UUID id, RegistroIncidenciaTecnicaRequest request, String username) {
        EquipamientoSerenazgo equip = equipamientoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El equipamiento especificado no existe"));

        Long seq = incidenciaRepository.obtenerSiguienteValorSecuencia();
        String cod = String.format("INC-%06d", seq);

        PersonalSerenazgo reporta = null;
        if (request.getPersonalReportaId() != null) {
            reporta = personalRepository.findById(request.getPersonalReportaId()).orElse(null);
        }

        Usuario usr = usuarioRepository.findByUsername(username).orElse(null);

        IncidenciaTecnicaRecurso inc = IncidenciaTecnicaRecurso.builder()
                .codigo(cod)
                .tipoRecurso(TipoRecursoSeguridad.EQUIPAMIENTO)
                .equipamiento(equip)
                .tipoIncidencia(request.getTipoIncidencia())
                .fechaReporte(OffsetDateTime.now())
                .descripcion(request.getDescripcion())
                .personalReporta(reporta)
                .nivelGravedad(request.getNivelGravedad() != null ? request.getNivelGravedad() : NivelGravedadIncidencia.MEDIA)
                .estado(EstadoIncidenciaTecnica.REGISTRADA)
                .usuarioRegistro(usr)
                .build();

        inc = incidenciaRepository.save(inc);

        if (request.getNivelGravedad() == NivelGravedadIncidencia.CRITICA) {
            equip.setEstado(EstadoEquipamientoSerenazgo.EN_REPARACION);
            equipamientoRepository.save(equip);
        }

        return mapearIncidenciaResponse(inc);
    }

    private EquipamientoResponse mapearResponse(EquipamientoSerenazgo e) {
        String resp = (e.getResponsable() != null && e.getResponsable().getPersona() != null) ? 
                e.getResponsable().getPersona().getNombreCompleto() : null;
        String uniInfo = (e.getUnidadMovil() != null) ? 
                e.getUnidadMovil().getCodigoInterno() + " (" + (e.getUnidadMovil().getPlaca() != null ? e.getUnidadMovil().getPlaca() : e.getUnidadMovil().getTipoUnidad().getDescripcion()) + ")" : null;

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
                .unidadMovilInfo(uniInfo)
                .fechaAsignacion(e.getFechaAsignacion())
                .fechaIncorporacion(e.getFechaIncorporacion())
                .observaciones(e.getObservaciones())
                .disponibleParaAsignacion(e.getEstado() == EstadoEquipamientoSerenazgo.DISPONIBLE && (e.getStockDisponible() != null && e.getStockDisponible() > 0))
                .build();
    }

    private AsignacionEquipamientoResponse mapearAsignacionResponse(AsignacionEquipamiento a) {
        String persNom = (a.getPersonal() != null && a.getPersonal().getPersona() != null) ? 
                a.getPersonal().getPersona().getNombreCompleto() : null;
        String persCod = (a.getPersonal() != null) ? a.getPersonal().getCodigoInterno() : null;
        String uniInfo = (a.getUnidad() != null) ? a.getUnidad().getCodigoInterno() + " [" + (a.getUnidad().getPlaca() != null ? a.getUnidad().getPlaca() : a.getUnidad().getTipoUnidad().getDescripcion()) + "]" : null;

        return AsignacionEquipamientoResponse.builder()
                .id(a.getId())
                .equipamientoId(a.getEquipamiento().getId())
                .equipamientoCodigo(a.getEquipamiento().getCodigoInterno())
                .equipamientoDescripcion(a.getEquipamiento().getDescripcion())
                .personalId(a.getPersonal() != null ? a.getPersonal().getId() : null)
                .personalNombre(persNom)
                .personalCodigo(persCod)
                .unidadId(a.getUnidad() != null ? a.getUnidad().getId() : null)
                .unidadInfo(uniInfo)
                .fechaEntrega(a.getFechaEntrega())
                .fechaDevolucion(a.getFechaDevolucion())
                .cantidad(a.getCantidad())
                .cantidadDevuelta(a.getCantidadDevuelta())
                .estadoAsignacion(a.getEstadoAsignacion())
                .estadoAsignacionDesc(a.getEstadoAsignacion().getDescripcion())
                .estadoBienEntrega(a.getEstadoBienEntrega())
                .estadoBienDevolucion(a.getEstadoBienDevolucion())
                .motivo(a.getMotivo())
                .observaciones(a.getObservaciones())
                .usuarioEntrega(a.getUsuarioEntrega() != null ? a.getUsuarioEntrega().getUsername() : null)
                .usuarioRecepcion(a.getUsuarioRecepcion() != null ? a.getUsuarioRecepcion().getUsername() : null)
                .build();
    }

    private MantenimientoResponse mapearMantenimientoResponse(MantenimientoRecurso m) {
        String infoUnidad = m.getUnidad() != null ? m.getUnidad().getCodigoInterno() : null;
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
}

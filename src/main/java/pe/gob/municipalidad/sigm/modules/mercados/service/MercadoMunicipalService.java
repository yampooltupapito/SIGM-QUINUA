package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MercadoMunicipalService {

    private static final Set<String> ORDENAMIENTO_PERMITIDO = Set.of(
            "nombre", "codigo", "fechaRegistro", "estado", "capacidadReferencial", "createdAt"
    );

    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentoRepository documentoRepository;
    private final MercadoDocumentoRepository mercadoDocumentoRepository;
    private final MercadoCambioEstadoRepository cambioEstadoRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public MercadoMunicipalService(
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            OficinaRepository oficinaRepository,
            DocumentoRepository documentoRepository,
            MercadoDocumentoRepository mercadoDocumentoRepository,
            MercadoCambioEstadoRepository cambioEstadoRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.oficinaRepository = oficinaRepository;
        this.documentoRepository = documentoRepository;
        this.mercadoDocumentoRepository = mercadoDocumentoRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MercadoResponse crearMercado(MercadoRequest request) {
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código del mercado es obligatorio.");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre del mercado es obligatorio.");
        }
        if (request.getDireccion() == null || request.getDireccion().isBlank()) {
            throw new InvalidRequestException("La dirección del mercado es obligatoria.");
        }
        if (request.getCapacidadReferencial() != null && request.getCapacidadReferencial() < 0) {
            throw new InvalidRequestException("La capacidad referencial no puede ser negativa.");
        }
        if (request.getLatitud() != null && (request.getLatitud().compareTo(BigDecimal.valueOf(-90)) < 0 || request.getLatitud().compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new InvalidRequestException("La latitud debe estar comprendida entre -90 y 90 grados.");
        }
        if (request.getLongitud() != null && (request.getLongitud().compareTo(BigDecimal.valueOf(-180)) < 0 || request.getLongitud().compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new InvalidRequestException("La longitud debe estar comprendida entre -180 y 180 grados.");
        }

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (mercadoRepository.existsByCodigo(codigoNormalizado)) {
            throw new DuplicateResourceException("El código de mercado '" + codigoNormalizado + "' ya se encuentra registrado.");
        }

        Oficina oficina = null;
        if (request.getOficinaAdministradoraId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaAdministradoraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina administradora no encontrada con ID: " + request.getOficinaAdministradoraId()));
            if (!"ACTIVO".equalsIgnoreCase(oficina.getEstado())) {
                throw new InvalidRequestException("La oficina seleccionada no se encuentra activa para administrar mercados.");
            }
        }

        MercadoMunicipal mercado = MercadoMunicipal.builder()
                .codigo(codigoNormalizado)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .direccion(request.getDireccion().trim())
                .referenciaUbicacion(request.getReferenciaUbicacion() != null ? request.getReferenciaUbicacion().trim() : null)
                .distrito(request.getDistrito() != null ? request.getDistrito().trim() : "Quinua")
                .provincia(request.getProvincia() != null ? request.getProvincia().trim() : "Huamanga")
                .departamento(request.getDepartamento() != null ? request.getDepartamento().trim() : "Ayacucho")
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .capacidadReferencial(request.getCapacidadReferencial() != null ? request.getCapacidadReferencial() : 0)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoMercado.ACTIVO)
                .oficinaAdministradora(oficina)
                .fechaRegistro(LocalDate.now())
                .build();

        MercadoMunicipal guardado = mercadoRepository.save(mercado);
        log.info("Mercado municipal creado exitosamente: {} ({})", guardado.getNombre(), guardado.getCodigo());

        auditoriaService.registrar("CREAR_MERCADO", "mercados_mercados", null, "EXITO",
                "Creado mercado municipal: " + guardado.getCodigo() + " - " + guardado.getNombre() + " [UUID: " + guardado.getId() + "]");

        return toResponse(guardado);
    }

    @Transactional
    public MercadoResponse actualizarMercado(UUID id, MercadoActualizacionRequest request) {
        MercadoMunicipal mercado = mercadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + id));

        // Control de concurrencia optimista
        if (request.getVersion() != null && !request.getVersion().equals(mercado.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "El mercado ha sido modificado por otro usuario en simultáneo. Por favor, recargue la página para ver los cambios recientes."
            );
        }

        Oficina oficina = null;
        if (request.getOficinaAdministradoraId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaAdministradoraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina administradora no encontrada con ID: " + request.getOficinaAdministradoraId()));
            if (!"ACTIVO".equalsIgnoreCase(oficina.getEstado())) {
                throw new InvalidRequestException("La oficina seleccionada no se encuentra activa para administrar mercados.");
            }
        }

        mercado.setNombre(request.getNombre().trim());
        mercado.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        mercado.setDireccion(request.getDireccion().trim());
        mercado.setReferenciaUbicacion(request.getReferenciaUbicacion() != null ? request.getReferenciaUbicacion().trim() : null);
        mercado.setDistrito(request.getDistrito() != null ? request.getDistrito().trim() : "Quinua");
        mercado.setProvincia(request.getProvincia() != null ? request.getProvincia().trim() : "Huamanga");
        mercado.setDepartamento(request.getDepartamento() != null ? request.getDepartamento().trim() : "Ayacucho");
        mercado.setLatitud(request.getLatitud());
        mercado.setLongitud(request.getLongitud());
        if (request.getCapacidadReferencial() != null) {
            mercado.setCapacidadReferencial(request.getCapacidadReferencial());
        }
        if (request.getEstado() != null) {
            mercado.setEstado(request.getEstado());
        }
        mercado.setOficinaAdministradora(oficina);

        MercadoMunicipal actualizado = mercadoRepository.save(mercado);
        log.info("Mercado municipal actualizado: {} ({})", actualizado.getNombre(), actualizado.getCodigo());

        auditoriaService.registrar("ACTUALIZAR_MERCADO", "mercados_mercados", null, "EXITO",
                "Actualizado mercado: " + actualizado.getCodigo() + " [UUID: " + actualizado.getId() + ", Versión: " + actualizado.getVersion() + "]");

        return toResponse(actualizado);
    }

    @Transactional(readOnly = true)
    public MercadoResponse obtenerPorId(UUID id) {
        MercadoMunicipal mercado = mercadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + id));
        return toResponse(mercado);
    }

    @Transactional(readOnly = true)
    public MercadoResponse obtenerPorCodigo(String codigo) {
        MercadoMunicipal mercado = mercadoRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con código: " + codigo));
        return toResponse(mercado);
    }

    @Transactional(readOnly = true)
    public MercadoDetalleResponse obtenerDetalle(UUID id) {
        MercadoMunicipal mercado = mercadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + id));

        MercadoResponse mercadoResp = toResponse(mercado);
        ResumenPuestosMercadoResponse resumenPuestos = obtenerResumenPuestos(id);

        List<SectorResponse> sectores = sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(id)
                .stream()
                .map(s -> SectorResponse.builder()
                        .id(s.getId())
                        .mercadoId(mercado.getId())
                        .mercadoNombre(mercado.getNombre())
                        .codigo(s.getCodigo())
                        .nombre(s.getNombre())
                        .descripcion(s.getDescripcion())
                        .tipoSector(s.getTipoSector())
                        .estado(s.getEstado())
                        .ordenVisual(s.getOrdenVisual())
                        .build())
                .collect(Collectors.toList());

        List<PabellonResponse> pabellones = pabellonRepository.findByMercadoIdOrderByOrdenVisualAsc(id)
                .stream()
                .map(p -> PabellonResponse.builder()
                        .id(p.getId())
                        .mercadoId(mercado.getId())
                        .mercadoNombre(mercado.getNombre())
                        .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                        .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                        .codigo(p.getCodigo())
                        .nombre(p.getNombre())
                        .descripcion(p.getDescripcion())
                        .nivel(p.getNivel())
                        .estado(p.getEstado())
                        .ordenVisual(p.getOrdenVisual())
                        .build())
                .collect(Collectors.toList());

        Pageable puestoPageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "numero"));
        List<PuestoResponse> puestos = puestoRepository.findByMercadoId(id, puestoPageable)
                .getContent()
                .stream()
                .map(p -> PuestoResponse.builder()
                        .id(p.getId())
                        .mercadoId(mercado.getId())
                        .mercadoNombre(mercado.getNombre())
                        .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                        .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                        .pabellonId(p.getPabellon() != null ? p.getPabellon().getId() : null)
                        .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : null)
                        .codigo(p.getCodigo())
                        .numero(p.getNumero())
                        .descripcion(p.getDescripcion())
                        .areaMetrosCuadrados(p.getAreaMetrosCuadrados())
                        .ubicacionReferencia(p.getUbicacionReferencia())
                        .estado(p.getEstado())
                        .tipoPuestoId(p.getTipoPuesto() != null ? p.getTipoPuesto().getId() : null)
                        .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : null)
                        .version(p.getVersion())
                        .build())
                .collect(Collectors.toList());

        List<MercadoDocumentoResponse> documentos = listarDocumentos(id);
        List<MercadoHistorialItemResponse> historial = obtenerHistorial(id);
        long asignacionesVigentes = asignacionRepository.countByPuestoMercadoIdAndEstado(id, EstadoAsignacion.VIGENTE);

        return MercadoDetalleResponse.builder()
                .mercado(mercadoResp)
                .resumenPuestos(resumenPuestos)
                .sectores(sectores)
                .pabellones(pabellones)
                .puestos(puestos)
                .documentos(documentos)
                .historialEstados(historial)
                .totalAsignacionesVigentes(asignacionesVigentes)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<MercadoResponse> listarMercados(String termino, EstadoMercado estado, Pageable pageable) {
        MercadoFilter filter = MercadoFilter.builder()
                .termino(termino)
                .estado(estado)
                .page(pageable != null ? pageable.getPageNumber() : 0)
                .size(pageable != null ? pageable.getPageSize() : 10)
                .build();
        return listarMercados(filter);
    }

    @Transactional(readOnly = true)
    public Page<MercadoResponse> listarMercados(MercadoFilter filter) {
        // Validación de ordenamiento seguro por lista blanca
        String sortBy = filter.getSortBy();
        if (sortBy == null || !ORDENAMIENTO_PERMITIDO.contains(sortBy)) {
            sortBy = "nombre";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.max(1, filter.getSize()), Sort.by(direction, sortBy));

        String term = (filter.getTermino() != null && !filter.getTermino().isBlank()) ? filter.getTermino().trim() : null;
        String dist = (filter.getDistrito() != null && !filter.getDistrito().isBlank()) ? filter.getDistrito().trim().toLowerCase() : null;

        Page<MercadoMunicipal> pagina;
        if (term != null) {
            pagina = mercadoRepository.filtrarMercadosConTermino(
                    term, filter.getEstado(), filter.getOficinaId(), dist,
                    filter.getFechaDesde(), filter.getFechaHasta(), pageable
            );
        } else {
            pagina = mercadoRepository.filtrarMercadosSinTermino(
                    filter.getEstado(), filter.getOficinaId(), dist,
                    filter.getFechaDesde(), filter.getFechaHasta(), pageable
            );
        }

        return pagina.map(this::toResponse);
    }

    @Transactional
    public MercadoResponse cambiarEstado(UUID id, CambioEstadoMercadoRequest request) {
        MercadoMunicipal mercado = mercadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + id));

        EstadoMercado estadoActual = mercado.getEstado();
        EstadoMercado nuevoEstado = request.getNuevoEstado();

        if (estadoActual == nuevoEstado) {
            throw new ConflictException("El mercado ya se encuentra en el estado " + nuevoEstado);
        }

        // Reglas de transición de estado
        validarTransicionEstado(estadoActual, nuevoEstado);

        // Restricción informativa: si se desactiva o suspende, verificar asignaciones vigentes
        if (nuevoEstado == EstadoMercado.INACTIVO || nuevoEstado == EstadoMercado.SUSPENDIDO) {
            long asignacionesVigentes = asignacionRepository.countByPuestoMercadoIdAndEstado(mercado.getId(), EstadoAsignacion.VIGENTE);
            if (asignacionesVigentes > 0) {
                log.warn("Mercado municipal {} pasa a estado {} contando con {} asignaciones comerciales vigentes. Se conserva la integridad sin cancelación masiva.",
                        mercado.getCodigo(), nuevoEstado, asignacionesVigentes);
            }
        }

        String usuario = "SISTEMA";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            usuario = auth.getName();
        }

        // Registrar en historial de cambios de estado
        MercadoCambioEstado cambio = MercadoCambioEstado.builder()
                .mercado(mercado)
                .estadoAnterior(estadoActual)
                .estadoNuevo(nuevoEstado)
                .motivo(request.getMotivo().trim())
                .usuarioResponsable(usuario)
                .fechaCambio(OffsetDateTime.now())
                .build();
        cambioEstadoRepository.save(cambio);

        mercado.setEstado(nuevoEstado);
        MercadoMunicipal actualizado = mercadoRepository.save(mercado);

        auditoriaService.registrar("CAMBIO_ESTADO_MERCADO", "mercados_mercados", null, "EXITO",
                "Mercado " + mercado.getCodigo() + " cambió de estado: " + estadoActual + " -> " + nuevoEstado + ". Motivo: " + request.getMotivo());

        return toResponse(actualizado);
    }

    private void validarTransicionEstado(EstadoMercado actual, EstadoMercado nuevo) {
        boolean valida = switch (actual) {
            case ACTIVO -> nuevo == EstadoMercado.EN_MANTENIMIENTO || nuevo == EstadoMercado.SUSPENDIDO || nuevo == EstadoMercado.INACTIVO;
            case EN_MANTENIMIENTO -> nuevo == EstadoMercado.ACTIVO || nuevo == EstadoMercado.SUSPENDIDO || nuevo == EstadoMercado.INACTIVO;
            case SUSPENDIDO -> nuevo == EstadoMercado.ACTIVO || nuevo == EstadoMercado.INACTIVO;
            case INACTIVO -> nuevo == EstadoMercado.ACTIVO;
        };

        if (!valida) {
            throw new InvalidRequestException("Transición de estado no permitida de " + actual + " a " + nuevo);
        }
    }

    @Transactional(readOnly = true)
    public MercadoEstadisticasResponse obtenerEstadisticas() {
        long total = mercadoRepository.count();
        long activos = mercadoRepository.countByEstado(EstadoMercado.ACTIVO);
        long inactivos = mercadoRepository.countByEstado(EstadoMercado.INACTIVO);
        long enMantenimiento = mercadoRepository.countByEstado(EstadoMercado.EN_MANTENIMIENTO);
        long suspendidos = mercadoRepository.countByEstado(EstadoMercado.SUSPENDIDO);
        long totalPuestos = puestoRepository.count();
        long asignaciones = asignacionRepository.countByEstado(EstadoAsignacion.VIGENTE);

        return MercadoEstadisticasResponse.builder()
                .totalMercados(total)
                .mercadosActivos(activos)
                .mercadosInactivos(inactivos)
                .mercadosEnMantenimiento(enMantenimiento)
                .mercadosSuspendidos(suspendidos)
                .totalPuestosAsociados(totalPuestos)
                .totalAsignacionesVigentes(asignaciones)
                .build();
    }

    @Transactional(readOnly = true)
    public ResumenPuestosMercadoResponse obtenerResumenPuestos(UUID mercadoId) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado no encontrado con ID: " + mercadoId));

        long total = puestoRepository.countByMercadoId(mercadoId);
        long disponibles = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.DISPONIBLE);
        long ocupados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.OCUPADO);
        long reservados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.RESERVADO);
        long enMantenimiento = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.EN_MANTENIMIENTO);
        long inhabilitados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.INHABILITADO);

        return ResumenPuestosMercadoResponse.builder()
                .mercadoId(mercadoId)
                .mercadoNombre(mercado.getNombre())
                .total(total)
                .disponibles(disponibles)
                .ocupados(ocupados)
                .reservados(reservados)
                .enMantenimiento(enMantenimiento)
                .inhabilitados(inhabilitados)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MercadoHistorialItemResponse> obtenerHistorial(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado no encontrado con ID: " + mercadoId);
        }
        return cambioEstadoRepository.findByMercadoIdOrderByFechaCambioDesc(mercadoId)
                .stream()
                .map(c -> MercadoHistorialItemResponse.builder()
                        .id(c.getId())
                        .estadoAnterior(c.getEstadoAnterior())
                        .estadoNuevo(c.getEstadoNuevo())
                        .motivo(c.getMotivo())
                        .usuarioResponsable(c.getUsuarioResponsable())
                        .fechaCambio(c.getFechaCambio())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public MercadoDocumentoResponse asociarDocumento(UUID mercadoId, MercadoDocumentoRequest request) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado no encontrado con ID: " + mercadoId));

        Documento documento = documentoRepository.findById(request.getDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Documento institucional no encontrado con ID: " + request.getDocumentoId()));

        String usuario = "SISTEMA";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            usuario = auth.getName();
        }

        MercadoDocumento md = MercadoDocumento.builder()
                .mercado(mercado)
                .documento(documento)
                .tipoDocumento(request.getTipoDocumento())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .createdAt(OffsetDateTime.now())
                .createdBy(usuario)
                .build();

        MercadoDocumento guardado = mercadoDocumentoRepository.save(md);
        log.info("Documento {} asociado al mercado {}", documento.getNombreOriginal(), mercado.getCodigo());

        auditoriaService.registrar("ASOCIAR_DOCUMENTO_MERCADO", "mercados_documentos", null, "EXITO",
                "Asociado documento '" + documento.getNombreOriginal() + "' [ID: " + documento.getId() + "] al mercado " + mercado.getCodigo());

        return toDocumentoResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<MercadoDocumentoResponse> listarDocumentos(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado no encontrado con ID: " + mercadoId);
        }
        return mercadoDocumentoRepository.findByMercadoIdOrderByCreatedAtDesc(mercadoId)
                .stream()
                .map(this::toDocumentoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OficinaSimpleResponse> listarOficinasDisponibles() {
        return oficinaRepository.findByEstado("ACTIVO")
                .stream()
                .map(o -> OficinaSimpleResponse.builder()
                        .id(o.getId())
                        .codigo(o.getCodigo())
                        .nombre(o.getNombre())
                        .sigla(o.getSigla())
                        .build())
                .sorted(Comparator.comparing(OficinaSimpleResponse::getNombre))
                .collect(Collectors.toList());
    }

    public MercadoResponse toResponse(MercadoMunicipal m) {
        long totalSectores = sectorRepository.countByMercadoId(m.getId());
        long totalPuestos = puestoRepository.countByMercadoId(m.getId());
        long disponibles = puestoRepository.countByMercadoIdAndEstado(m.getId(), EstadoPuesto.DISPONIBLE);
        long ocupados = puestoRepository.countByMercadoIdAndEstado(m.getId(), EstadoPuesto.OCUPADO);

        return MercadoResponse.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .descripcion(m.getDescripcion())
                .direccion(m.getDireccion())
                .referenciaUbicacion(m.getReferenciaUbicacion())
                .distrito(m.getDistrito())
                .provincia(m.getProvincia())
                .departamento(m.getDepartamento())
                .latitud(m.getLatitud())
                .longitud(m.getLongitud())
                .capacidadReferencial(m.getCapacidadReferencial())
                .estado(m.getEstado())
                .oficinaAdministradoraId(m.getOficinaAdministradora() != null ? m.getOficinaAdministradora().getId() : null)
                .oficinaAdministradoraNombre(m.getOficinaAdministradora() != null ? m.getOficinaAdministradora().getNombre() : null)
                .fechaRegistro(m.getFechaRegistro())
                .totalSectores(totalSectores)
                .totalPuestos(totalPuestos)
                .puestosDisponibles(disponibles)
                .puestosOcupados(ocupados)
                .version(m.getVersion())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private MercadoDocumentoResponse toDocumentoResponse(MercadoDocumento md) {
        Documento d = md.getDocumento();
        return MercadoDocumentoResponse.builder()
                .id(md.getId())
                .documentoId(d.getId())
                .nombreOriginal(d.getNombreOriginal())
                .extension(d.getExtension())
                .mimeType(d.getMimeType())
                .tamanoBytes(d.getTamanoBytes())
                .tipoDocumento(md.getTipoDocumento())
                .descripcion(md.getDescripcion())
                .createdAt(md.getCreatedAt())
                .createdBy(md.getCreatedBy())
                .build();
    }
}

package pe.gob.municipalidad.sigm.modules.mercados.service;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.PuestoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.PuestoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio integral para el registro, administración, reubicación y trazabilidad de puestos comerciales.
 */
@Slf4j
@Service
public class PuestoComercialService {

    private final PuestoComercialRepository puestoRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final ZonaComercialRepository zonaRepository;
    private final TipoPuestoComercialRepository tipoPuestoRepository;
    private final PuestoCambioEstadoRepository cambioEstadoRepository;
    private final PuestoCambioUbicacionRepository cambioUbicacionRepository;
    private final PuestoDocumentoRepository puestoDocumentoRepository;
    private final DocumentoRepository documentoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public PuestoComercialService(
            PuestoComercialRepository puestoRepository,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            ZonaComercialRepository zonaRepository,
            TipoPuestoComercialRepository tipoPuestoRepository,
            PuestoCambioEstadoRepository cambioEstadoRepository,
            PuestoCambioUbicacionRepository cambioUbicacionRepository,
            PuestoDocumentoRepository puestoDocumentoRepository,
            DocumentoRepository documentoRepository,
            AsignacionPuestoRepository asignacionRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.puestoRepository = puestoRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.zonaRepository = zonaRepository;
        this.tipoPuestoRepository = tipoPuestoRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.cambioUbicacionRepository = cambioUbicacionRepository;
        this.puestoDocumentoRepository = puestoDocumentoRepository;
        this.documentoRepository = documentoRepository;
        this.asignacionRepository = asignacionRepository;
        this.auditoriaService = auditoriaService;
    }

    // ==========================================
    // CREACIÓN Y REGISTRO
    // ==========================================

    @Transactional
    public PuestoComercialResponse registrarPuesto(PuestoComercialCreateRequest request) {
        if (request.getMercadoId() == null) {
            throw new InvalidRequestException("El ID del mercado es obligatorio.");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código del puesto es obligatorio.");
        }
        if (request.getNumero() == null || request.getNumero().isBlank()) {
            throw new InvalidRequestException("El número de puesto es obligatorio.");
        }
        if (request.getTipoPuestoId() == null) {
            throw new InvalidRequestException("El tipo de puesto comercial es obligatorio.");
        }

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + request.getMercadoId()));

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (puestoRepository.existsByMercadoIdAndCodigoIgnoreCase(mercado.getId(), codigoNormalizado)) {
            throw new DuplicateResourceException("El código de puesto '" + codigoNormalizado + "' ya existe en el mercado " + mercado.getNombre());
        }

        // Validación de relaciones físicas
        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + request.getSectorId()));
            if (!sector.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El sector especificado no pertenece al mercado indicado.");
            }
        }

        PabellonMercado pabellon = null;
        if (request.getPabellonId() != null) {
            pabellon = pabellonRepository.findById(request.getPabellonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + request.getPabellonId()));
            if (!pabellon.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El pabellón especificado no pertenece al mercado indicado.");
            }
            if (sector != null && pabellon.getSector() != null && !pabellon.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El pabellón seleccionado pertenece a un sector distinto al indicado.");
            }
        }

        ZonaComercial zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + request.getZonaId()));
            if (!zona.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("La zona especificada no pertenece al mercado indicado.");
            }
        }

        TipoPuestoComercial tipoPuesto = tipoPuestoRepository.findById(request.getTipoPuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de puesto no encontrado con ID: " + request.getTipoPuestoId()));

        // Validación y cálculo de dimensiones
        BigDecimal area = request.getAreaMetrosCuadrados();
        BigDecimal ancho = request.getAnchoMetros();
        BigDecimal largo = request.getLargoMetros();

        if (area != null && area.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El área en metros cuadrados no puede ser negativa (no puede ser un valor negativo).");
        }
        if (ancho != null && ancho.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El ancho en metros no puede ser negativo.");
        }
        if (largo != null && largo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El largo en metros no puede ser negativo.");
        }

        // Si área no se indicó pero ancho y largo sí (> 0), calculamos automáticamente
        if ((area == null || area.compareTo(BigDecimal.ZERO) == 0) && ancho != null && largo != null && ancho.compareTo(BigDecimal.ZERO) > 0 && largo.compareTo(BigDecimal.ZERO) > 0) {
            area = ancho.multiply(largo).setScale(2, RoundingMode.HALF_UP);
        }

        PuestoComercial puesto = PuestoComercial.builder()
                .mercado(mercado)
                .sector(sector)
                .pabellon(pabellon)
                .zona(zona)
                .codigo(codigoNormalizado)
                .numero(request.getNumero().trim())
                .nombreReferencial(request.getNombreReferencial() != null ? request.getNombreReferencial().trim() : null)
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .areaMetrosCuadrados(area)
                .anchoMetros(ancho)
                .largoMetros(largo)
                .nivel(request.getNivel() != null ? request.getNivel() : 1)
                .ubicacionReferencia(request.getUbicacionReferencia() != null ? request.getUbicacionReferencia().trim() : null)
                .estadoFisico(request.getEstadoFisico() != null ? request.getEstadoFisico() : EstadoFisicoPuesto.OPERATIVO)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoPuesto.DISPONIBLE)
                .tipoPuesto(tipoPuesto)
                .build();

        PuestoComercial guardado = puestoRepository.save(puesto);
        log.info("Puesto comercial creado: {} en mercado {}", guardado.getCodigo(), mercado.getCodigo());

        auditoriaService.registrar("CREAR_PUESTO_COMERCIAL", "mercados_puestos", null, "EXITO",
                "Creado puesto " + guardado.getCodigo() + " en mercado " + mercado.getCodigo());

        return toDetalleResponse(guardado);
    }

    // ==========================================
    // ACTUALIZACIÓN DE DATOS TÉCNICOS
    // ==========================================

    @Transactional
    public PuestoComercialResponse actualizarPuesto(UUID puestoId, UUID mercadoId, PuestoComercialUpdateRequest request) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        // Control de concurrencia optimista
        if (request.getVersion() != null && !request.getVersion().equals(puesto.getVersion())) {
            throw new ConflictException("El puesto ha sido modificado concurrentemente por otro usuario. Recargue la página para ver los cambios.");
        }

        if (request.getTipoPuestoId() != null) {
            TipoPuestoComercial tipo = tipoPuestoRepository.findById(request.getTipoPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de puesto no encontrado con ID: " + request.getTipoPuestoId()));
            puesto.setTipoPuesto(tipo);
        }

        BigDecimal area = request.getAreaMetrosCuadrados();
        BigDecimal ancho = request.getAnchoMetros();
        BigDecimal largo = request.getLargoMetros();

        if (area != null && area.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El área en metros cuadrados no puede ser negativa (no puede ser un valor negativo).");
        }
        if (ancho != null && ancho.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El ancho en metros no puede ser negativo.");
        }
        if (largo != null && largo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El largo en metros no puede ser negativo.");
        }

        if ((area == null || area.compareTo(BigDecimal.ZERO) == 0) && ancho != null && largo != null && ancho.compareTo(BigDecimal.ZERO) > 0 && largo.compareTo(BigDecimal.ZERO) > 0) {
            area = ancho.multiply(largo).setScale(2, RoundingMode.HALF_UP);
        }

        puesto.setNombreReferencial(request.getNombreReferencial() != null ? request.getNombreReferencial().trim() : null);
        puesto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        puesto.setAreaMetrosCuadrados(area);
        puesto.setAnchoMetros(ancho);
        puesto.setLargoMetros(largo);
        if (request.getNivel() != null) {
            puesto.setNivel(request.getNivel());
        }
        puesto.setUbicacionReferencia(request.getUbicacionReferencia() != null ? request.getUbicacionReferencia().trim() : null);

        try {
            PuestoComercial actualizado = puestoRepository.saveAndFlush(puesto);
            auditoriaService.registrar("EDITAR_PUESTO_COMERCIAL", "mercados_puestos", null, "EXITO",
                    "Actualizados datos del puesto " + actualizado.getCodigo() + " en mercado " + puesto.getMercado().getCodigo());
            return toDetalleResponse(actualizado);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Conflicto de concurrencia al actualizar el puesto comercial.");
        }
    }

    // ==========================================
    // CAMBIO DE ESTADO FÍSICO Y OPERATIVO
    // ==========================================

    @Transactional
    public PuestoComercialResponse cambiarEstado(UUID puestoId, UUID mercadoId, PuestoEstadoRequest request) {
        if (request.getMotivo() == null || request.getMotivo().trim().length() < 5) {
            throw new InvalidRequestException("El motivo del cambio de estado es obligatorio y debe tener al menos 5 caracteres.");
        }

        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        EstadoFisicoPuesto estadoFisicoAnterior = puesto.getEstadoFisico();
        EstadoPuesto estadoOperativoAnterior = puesto.getEstado();

        boolean huboCambioFisico = request.getNuevoEstadoFisico() != null && request.getNuevoEstadoFisico() != estadoFisicoAnterior;
        boolean huboCambioOperativo = request.getNuevoEstadoOperativo() != null && request.getNuevoEstadoOperativo() != estadoOperativoAnterior;

        if (!huboCambioFisico && !huboCambioOperativo) {
            throw new InvalidRequestException("El puesto ya se encuentra en el estado indicado. Debe especificar un cambio válido.");
        }

        String username = getAuthenticatedUsername();

        if (huboCambioFisico) {
            EstadoFisicoPuesto nuevoFisico = request.getNuevoEstadoFisico();
            // Advertencia/Verificación: Si se inhabilita o pasa a mantenimiento, revisar asignaciones vigentes
            if (nuevoFisico == EstadoFisicoPuesto.INHABILITADO || nuevoFisico == EstadoFisicoPuesto.EN_MANTENIMIENTO) {
                boolean tieneAsignacion = asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.VIGENTE);
                if (tieneAsignacion) {
                    log.warn("El puesto {} tiene asignaciones vigentes activas. Se registra cambio a {}", puesto.getCodigo(), nuevoFisico);
                }
                // Si estaba disponible, se ajusta automáticamente para evitar asignaciones indebidas
                if (puesto.getEstado() == EstadoPuesto.DISPONIBLE) {
                    puesto.setEstado(nuevoFisico == EstadoFisicoPuesto.EN_MANTENIMIENTO ? EstadoPuesto.EN_MANTENIMIENTO : EstadoPuesto.INHABILITADO);
                }
            } else if (nuevoFisico == EstadoFisicoPuesto.OPERATIVO && (puesto.getEstado() == EstadoPuesto.EN_MANTENIMIENTO || puesto.getEstado() == EstadoPuesto.INHABILITADO)) {
                puesto.setEstado(EstadoPuesto.DISPONIBLE);
            }

            puesto.setEstadoFisico(nuevoFisico);

            PuestoCambioEstado histFisico = PuestoCambioEstado.builder()
                    .puesto(puesto)
                    .tipoCambio("ESTADO_FISICO")
                    .estadoAnterior(estadoFisicoAnterior.name())
                    .estadoNuevo(nuevoFisico.name())
                    .motivo(request.getMotivo().trim())
                    .usuarioResponsable(username)
                    .usuarioUsername(username)
                    .build();
            cambioEstadoRepository.save(histFisico);
        }

        if (huboCambioOperativo) {
            EstadoPuesto nuevoOperativo = request.getNuevoEstadoOperativo();
            puesto.setEstado(nuevoOperativo);

            PuestoCambioEstado histOperativo = PuestoCambioEstado.builder()
                    .puesto(puesto)
                    .tipoCambio("ESTADO_OPERATIVO")
                    .estadoAnterior(estadoOperativoAnterior.name())
                    .estadoNuevo(nuevoOperativo.name())
                    .motivo(request.getMotivo().trim())
                    .usuarioResponsable(username)
                    .usuarioUsername(username)
                    .build();
            cambioEstadoRepository.save(histOperativo);
        }

        PuestoComercial actualizado = puestoRepository.save(puesto);
        auditoriaService.registrar("CAMBIO_ESTADO_PUESTO", "mercados_puestos", null, "EXITO",
                "Cambio de estado del puesto " + actualizado.getCodigo() + ". Motivo: " + request.getMotivo());

        return toDetalleResponse(actualizado);
    }

    // ==========================================
    // CAMBIO DE UBICACIÓN FÍSICA
    // ==========================================

    @Transactional
    public PuestoComercialResponse cambiarUbicacion(UUID puestoId, UUID mercadoId, PuestoUbicacionRequest request) {
        if (request.getMotivo() == null || request.getMotivo().trim().length() < 5) {
            throw new InvalidRequestException("El motivo de la reubicación es obligatorio y debe tener al menos 5 caracteres.");
        }

        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        SectorMercado sectorAnterior = puesto.getSector();
        PabellonMercado pabellonAnterior = puesto.getPabellon();
        ZonaComercial zonaAnterior = puesto.getZona();
        String ubicacionRefAnterior = puesto.getUbicacionReferencia();

        SectorMercado sectorNuevo = null;
        if (request.getNuevoSectorId() != null) {
            sectorNuevo = sectorRepository.findById(request.getNuevoSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector de destino no encontrado con ID: " + request.getNuevoSectorId()));
            if (!sectorNuevo.getMercado().getId().equals(mercadoId)) {
                throw new BusinessException("El sector de destino no pertenece al mismo mercado municipal.");
            }
        }

        PabellonMercado pabellonNuevo = null;
        if (request.getNuevoPabellonId() != null) {
            pabellonNuevo = pabellonRepository.findById(request.getNuevoPabellonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón de destino no encontrado con ID: " + request.getNuevoPabellonId()));
            if (!pabellonNuevo.getMercado().getId().equals(mercadoId)) {
                throw new BusinessException("El pabellón de destino no pertenece al mismo mercado municipal.");
            }
            if (sectorNuevo != null && pabellonNuevo.getSector() != null && !pabellonNuevo.getSector().getId().equals(sectorNuevo.getId())) {
                throw new BusinessException("El pabellón seleccionado pertenece a un sector diferente.");
            }
        }

        ZonaComercial zonaNueva = null;
        if (request.getNuevaZonaId() != null) {
            zonaNueva = zonaRepository.findById(request.getNuevaZonaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona de destino no encontrada con ID: " + request.getNuevaZonaId()));
            if (!zonaNueva.getMercado().getId().equals(mercadoId)) {
                throw new BusinessException("La zona de destino no pertenece al mismo mercado municipal.");
            }
        }

        puesto.setSector(sectorNuevo);
        puesto.setPabellon(pabellonNuevo);
        puesto.setZona(zonaNueva);
        if (request.getNuevaUbicacionReferencia() != null) {
            puesto.setUbicacionReferencia(request.getNuevaUbicacionReferencia().trim());
        }

        String username = getAuthenticatedUsername();

        PuestoCambioUbicacion hist = PuestoCambioUbicacion.builder()
                .puesto(puesto)
                .sectorAnterior(sectorAnterior)
                .sectorNuevo(sectorNuevo)
                .pabellonAnterior(pabellonAnterior)
                .pabellonNuevo(pabellonNuevo)
                .zonaAnterior(zonaAnterior)
                .zonaNueva(zonaNueva)
                .ubicacionRefAnterior(ubicacionRefAnterior)
                .ubicacionRefNueva(puesto.getUbicacionReferencia())
                .motivo(request.getMotivo().trim())
                .usuarioResponsable(username)
                .usuarioUsername(username)
                .build();
        cambioUbicacionRepository.save(hist);

        PuestoComercial actualizado = puestoRepository.save(puesto);
        auditoriaService.registrar("CAMBIO_UBICACION_PUESTO", "mercados_puestos", null, "EXITO",
                "Reubicado puesto " + actualizado.getCodigo() + ". Motivo: " + request.getMotivo());

        return toDetalleResponse(actualizado);
    }

    // ==========================================
    // CONSULTAS, DISPONIBILIDAD Y DETALLES
    // ==========================================

    @Transactional(readOnly = true)
    public PuestoComercialResponse obtenerPorIdYMercado(UUID puestoId, UUID mercadoId) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));
        return toDetalleResponse(puesto);
    }

    @Transactional(readOnly = true)
    public PuestoComercialDetailResponse obtenerDetalleCompleto(UUID puestoId, UUID mercadoId) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        PuestoComercialResponse basico = toDetalleResponse(puesto);

        List<PuestoCambioEstadoResponse> historialEstados = cambioEstadoRepository.findByPuestoIdOrderByFechaCambioDesc(puestoId)
                .stream()
                .map(h -> PuestoCambioEstadoResponse.builder()
                        .id(h.getId())
                        .tipoCambio(h.getTipoCambio())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .motivo(h.getMotivo() + " (" + h.getTipoCambio() + ")")
                        .usuarioResponsable(h.getUsuarioResponsable())
                        .usuarioUsername(h.getUsuarioUsername())
                        .fechaCambio(h.getFechaCambio() != null ? h.getFechaCambio().toLocalDateTime() : null)
                        .build())
                .collect(Collectors.toList());

        List<HistorialUbicacionResponse> historialUbicacion = cambioUbicacionRepository.findByPuestoIdOrderByFechaCambioDesc(puestoId)
                .stream()
                .map(u -> HistorialUbicacionResponse.builder()
                        .id(u.getId())
                        .sectorAnteriorNombre(u.getSectorAnterior() != null ? u.getSectorAnterior().getNombre() : "Sin Sector")
                        .sectorNuevoNombre(u.getSectorNuevo() != null ? u.getSectorNuevo().getNombre() : "Sin Sector")
                        .pabellonAnteriorNombre(u.getPabellonAnterior() != null ? u.getPabellonAnterior().getNombre() : "Sin Pabellón")
                        .pabellonNuevoNombre(u.getPabellonNuevo() != null ? u.getPabellonNuevo().getNombre() : "Sin Pabellón")
                        .zonaAnteriorNombre(u.getZonaAnterior() != null ? u.getZonaAnterior().getNombre() : "Sin Zona")
                        .zonaNuevaNombre(u.getZonaNueva() != null ? u.getZonaNueva().getNombre() : "Sin Zona")
                        .ubicacionRefAnterior(u.getUbicacionRefAnterior())
                        .ubicacionRefNueva(u.getUbicacionRefNueva())
                        .motivo(u.getMotivo())
                        .usuarioResponsable(u.getUsuarioResponsable())
                        .fechaCambio(u.getFechaCambio() != null ? u.getFechaCambio().toLocalDateTime() : null)
                        .build())
                .collect(Collectors.toList());

        List<PuestoDocumentoResponse> documentos = puestoDocumentoRepository.findByPuestoIdOrderByCreatedAtDesc(puestoId)
                .stream()
                .map(d -> PuestoDocumentoResponse.builder()
                        .id(d.getId())
                        .documentoId(d.getDocumento().getIdentificadorPublico())
                        .tipoDocumento(d.getTipoDocumento())
                        .nombreArchivo(d.getDocumento().getNombreOriginal())
                        .tamanoBytes(d.getDocumento().getTamanoBytes())
                        .mimeType(d.getDocumento().getMimeType())
                        .descripcion(d.getDescripcion())
                        .createdBy(d.getCreatedBy())
                        .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toLocalDateTime() : null)
                        .build())
                .collect(Collectors.toList());

        // Consultar ocupante actual si existe
        String ocupanteNombre = null;
        String ocupanteDni = null;
        String ocupanteContrato = null;

        Optional<AsignacionPuesto> asignacionOpt = asignacionRepository.findFirstByPuestoIdAndEstado(puestoId, EstadoAsignacion.VIGENTE);
        if (asignacionOpt.isPresent()) {
            AsignacionPuesto asig = asignacionOpt.get();
            if (asig.getComerciante() != null && asig.getComerciante().getPersona() != null) {
                ocupanteNombre = asig.getComerciante().getPersona().getNombres() + " " + asig.getComerciante().getPersona().getApellidoPaterno();
                ocupanteDni = asig.getComerciante().getPersona().getNumeroDocumento();
            } else if (asig.getComerciante() != null && asig.getComerciante().getPersonaJuridica() != null) {
                ocupanteNombre = asig.getComerciante().getPersonaJuridica().getRazonSocial();
                ocupanteDni = asig.getComerciante().getPersonaJuridica().getRuc();
            }
            ocupanteContrato = asig.getTipoAsignacion() != null ? asig.getTipoAsignacion().name() : "ASIGNACIÓN VIGENTE";
        }

        List<AsignacionResponse> asignaciones = asignacionRepository.findByPuestoIdOrderByFechaInicioDesc(puestoId).stream()
                .map(a -> {
                    String titular = "-";
                    String doc = "-";
                    if (a.getComerciante() != null) {
                        if (a.getComerciante().getPersona() != null) {
                            titular = a.getComerciante().getPersona().getNombreCompleto();
                            doc = a.getComerciante().getPersona().getNumeroDocumento();
                        } else if (a.getComerciante().getPersonaJuridica() != null) {
                            titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                            doc = a.getComerciante().getPersonaJuridica().getRuc();
                        }
                    }
                    return AsignacionResponse.builder()
                            .id(a.getId())
                            .codigoAsignacion(a.getCodigoAsignacion())
                            .puestoId(a.getPuesto().getId())
                            .puestoCodigo(a.getPuesto().getCodigo())
                            .puestoNumero(a.getPuesto().getNumero())
                            .mercadoNombre(a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-")
                            .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                            .comercianteCodigo(a.getComerciante() != null ? a.getComerciante().getCodigoComerciante() : "-")
                            .titularNombre(titular)
                            .documentoEnmascarado(doc)
                            .fechaSolicitud(a.getFechaSolicitud())
                            .fechaInicio(a.getFechaInicio())
                            .fechaFin(a.getFechaFin())
                            .estado(a.getEstado())
                            .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : "-")
                            .badgeClase(a.getEstado() != null ? a.getEstado().getBadgeClase() : "")
                            .tipoAsignacion(a.getTipoAsignacion())
                            .motivo(a.getMotivo())
                            .build();
                })
                .collect(Collectors.toList());

        return PuestoComercialDetailResponse.builder()
                .puesto(basico)
                .ocupanteActualNombre(ocupanteNombre)
                .ocupanteActualDni(ocupanteDni)
                .ocupanteContratoNumero(ocupanteContrato)
                .historialEstados(historialEstados)
                .historialUbicacion(historialUbicacion)
                .documentos(documentos)
                .asignaciones(asignaciones)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(UUID puestoId, UUID mercadoId) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        boolean mercadoHabilitado = puesto.getMercado().getEstado() == EstadoMercado.ACTIVO;
        boolean fisicoHabilitado = puesto.getEstadoFisico() == EstadoFisicoPuesto.OPERATIVO;
        boolean estadoDisponible = puesto.getEstado() == EstadoPuesto.DISPONIBLE;
        boolean tieneAsignacionVigente = asignacionRepository.existsByPuestoIdAndEstado(puestoId, EstadoAsignacion.VIGENTE);

        boolean completamenteDisponible = mercadoHabilitado && fisicoHabilitado && estadoDisponible && !tieneAsignacionVigente;

        Map<String, Object> res = new HashMap<>();
        res.put("puestoId", puesto.getId());
        res.put("codigo", puesto.getCodigo());
        res.put("disponible", completamenteDisponible);
        res.put("mercadoHabilitado", mercadoHabilitado);
        res.put("estadoFisico", puesto.getEstadoFisico());
        res.put("estadoOperativo", puesto.getEstado());
        res.put("tieneAsignacionVigente", tieneAsignacionVigente);
        if (!completamenteDisponible) {
            if (!mercadoHabilitado) res.put("motivoNoDisponible", "El mercado municipal se encuentra inactivo o cerrado.");
            else if (!fisicoHabilitado) res.put("motivoNoDisponible", "El puesto se encuentra en " + puesto.getEstadoFisico().getDescripcion().toLowerCase() + ".");
            else if (tieneAsignacionVigente) res.put("motivoNoDisponible", "El puesto cuenta con una asignación contractual vigente.");
            else res.put("motivoNoDisponible", "El estado operativo actual es " + puesto.getEstado().getDescripcion() + ".");
        } else {
            res.put("motivoNoDisponible", null);
        }
        return res;
    }

    // ==========================================
    // LISTADOS FILTRADOS Y PAGINADOS
    // ==========================================

    @Transactional(readOnly = true)
    public Page<PuestoComercialResponse> listarPuestosPaginado(
            UUID mercadoId, String termino, UUID sectorId, UUID pabellonId, UUID zonaId,
            Long tipoPuestoId, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado,
            Integer nivel, BigDecimal minArea, BigDecimal maxArea, Pageable pageable) {

        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }

        Specification<PuestoComercial> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("mercado").get("id"), mercadoId));

            if (termino != null && !termino.isBlank()) {
                String likePattern = "%" + termino.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigo")), likePattern);
                Predicate porNumero = cb.like(cb.lower(root.get("numero")), likePattern);
                Predicate porNombre = cb.like(cb.lower(root.get("nombreReferencial")), likePattern);
                Predicate porUbicacion = cb.like(cb.lower(root.get("ubicacionReferencia")), likePattern);
                predicates.add(cb.or(porCodigo, porNumero, porNombre, porUbicacion));
            }

            if (sectorId != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), sectorId));
            }
            if (pabellonId != null) {
                predicates.add(cb.equal(root.get("pabellon").get("id"), pabellonId));
            }
            if (zonaId != null) {
                predicates.add(cb.equal(root.get("zona").get("id"), zonaId));
            }
            if (tipoPuestoId != null) {
                predicates.add(cb.equal(root.get("tipoPuesto").get("id"), tipoPuestoId));
            }
            if (estadoFisico != null) {
                predicates.add(cb.equal(root.get("estadoFisico"), estadoFisico));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (nivel != null) {
                predicates.add(cb.equal(root.get("nivel"), nivel));
            }
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("areaMetrosCuadrados"), minArea));
            }
            if (maxArea != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("areaMetrosCuadrados"), maxArea));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return puestoRepository.findAll(spec, pageable).map(this::toDetalleResponse);
    }

    // ==========================================
    // ESTADÍSTICAS DEL MERCADO
    // ==========================================

    @Transactional(readOnly = true)
    public PuestoEstadisticasResponse obtenerEstadisticas(UUID mercadoId) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId));

        long total = puestoRepository.countByMercadoId(mercadoId);
        long disponibles = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.DISPONIBLE);
        long ocupados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.OCUPADO);
        long reservados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.RESERVADO);
        long mantenimiento = puestoRepository.countByMercadoIdAndEstadoFisico(mercadoId, EstadoFisicoPuesto.EN_MANTENIMIENTO);
        long inhabilitados = puestoRepository.countByMercadoIdAndEstadoFisico(mercadoId, EstadoFisicoPuesto.INHABILITADO);
        long noAsignables = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.NO_ASIGNABLE);

        // Desglose por sectores
        List<SectorMercado> sectores = sectorRepository.findByMercadoIdOrderByOrdenVisualAscCodigoAsc(mercadoId);
        List<PuestoEstadisticasResponse.SectorEstadisticaItem> itemsSector = new ArrayList<>();
        for (SectorMercado s : sectores) {
            long totalSec = puestoRepository.countBySectorId(s.getId());
            long dispSec = puestoRepository.countBySectorIdAndEstado(s.getId(), EstadoPuesto.DISPONIBLE);
            long ocupSec = puestoRepository.countBySectorIdAndEstado(s.getId(), EstadoPuesto.OCUPADO);
            itemsSector.add(new PuestoEstadisticasResponse.SectorEstadisticaItem(s.getId(), s.getNombre(), totalSec, dispSec, ocupSec));
        }

        // Desglose por tipos de puesto
        List<TipoPuestoComercial> tipos = tipoPuestoRepository.findAll();
        List<PuestoEstadisticasResponse.TipoPuestoEstadisticaItem> itemsTipo = new ArrayList<>();
        for (TipoPuestoComercial t : tipos) {
            long cantTipo = puestoRepository.countByMercadoIdAndTipoPuestoId(mercadoId, t.getId());
            double pct = total > 0 ? ((double) cantTipo / total) * 100.0 : 0.0;
            itemsTipo.add(new PuestoEstadisticasResponse.TipoPuestoEstadisticaItem(t.getId(), t.getNombre(), cantTipo, Math.round(pct * 100.0) / 100.0));
        }

        return PuestoEstadisticasResponse.builder()
                .mercadoId(mercado.getId())
                .mercadoNombre(mercado.getNombre())
                .totalPuestos(total)
                .puestosDisponibles(disponibles)
                .puestosOcupados(ocupados)
                .puestosReservados(reservados)
                .puestosMantenimiento(mantenimiento)
                .puestosInhabilitados(inhabilitados)
                .puestosNoAsignables(noAsignables)
                .porSector(itemsSector)
                .porTipo(itemsTipo)
                .build();
    }

    // ==========================================
    // ASOCIACIÓN DOCUMENTAL
    // ==========================================

    @Transactional
    public PuestoDocumentoResponse asociarDocumento(UUID puestoId, UUID mercadoId, UUID documentoId, String tipoDoc, String descripcion) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        Documento documento = documentoRepository.findByIdentificadorPublico(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento institucional no encontrado con identificador: " + documentoId));

        if (puestoDocumentoRepository.existsByPuestoIdAndDocumentoId(puestoId, documento.getId())) {
            throw new DuplicateResourceException("El documento ya se encuentra asociado a este puesto comercial.");
        }

        String username = getAuthenticatedUsername();

        PuestoDocumento pd = PuestoDocumento.builder()
                .puesto(puesto)
                .documento(documento)
                .tipoDocumento(tipoDoc != null ? tipoDoc : "PLANO_PUESTO")
                .descripcion(descripcion)
                .createdBy(username)
                .build();

        PuestoDocumento guardado = puestoDocumentoRepository.save(pd);
        auditoriaService.registrar("ASOCIAR_DOC_PUESTO", "mercados_puestos_documentos", null, "EXITO",
                "Asociado documento " + documento.getNombreOriginal() + " al puesto " + puesto.getCodigo());

        return PuestoDocumentoResponse.builder()
                .id(guardado.getId())
                .documentoId(documento.getIdentificadorPublico())
                .tipoDocumento(guardado.getTipoDocumento())
                .nombreArchivo(documento.getNombreOriginal())
                .tamanoBytes(documento.getTamanoBytes())
                .mimeType(documento.getMimeType())
                .descripcion(guardado.getDescripcion())
                .createdBy(guardado.getCreatedBy())
                .createdAt(guardado.getCreatedAt() != null ? guardado.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    @Transactional
    public PuestoDocumentoResponse asociarDocumento(UUID puestoId, UUID mercadoId, Long documentoId, String tipoDoc, String descripcion) {
        PuestoComercial puesto = puestoRepository.findByIdAndMercadoId(puestoId, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId + " en el mercado especificado"));

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento institucional no encontrado con ID: " + documentoId));

        if (puestoDocumentoRepository.existsByPuestoIdAndDocumentoId(puestoId, documento.getId())) {
            throw new DuplicateResourceException("El documento ya se encuentra asociado a este puesto comercial.");
        }

        String username = getAuthenticatedUsername();

        PuestoDocumento pd = PuestoDocumento.builder()
                .puesto(puesto)
                .documento(documento)
                .tipoDocumento(tipoDoc != null ? tipoDoc : "PLANO_PUESTO")
                .descripcion(descripcion)
                .createdBy(username)
                .build();

        PuestoDocumento guardado = puestoDocumentoRepository.save(pd);
        auditoriaService.registrar("ASOCIAR_DOC_PUESTO", "mercados_puestos_documentos", null, "EXITO",
                "Asociado documento " + documento.getNombreOriginal() + " al puesto " + puesto.getCodigo());

        return PuestoDocumentoResponse.builder()
                .id(guardado.getId())
                .documentoId(documento.getIdentificadorPublico())
                .tipoDocumento(guardado.getTipoDocumento())
                .nombreArchivo(documento.getNombreOriginal())
                .tamanoBytes(documento.getTamanoBytes())
                .mimeType(documento.getMimeType())
                .descripcion(guardado.getDescripcion())
                .createdBy(guardado.getCreatedBy())
                .createdAt(guardado.getCreatedAt() != null ? guardado.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    // ==========================================
    // COMPATIBILIDAD CON PROMPTS PREVIOS
    // ==========================================

    @Transactional
    public PuestoResponse crearPuesto(PuestoRequest request) {
        Long tipoPuestoId = request.getTipoPuestoId();
        if (tipoPuestoId == null) {
            tipoPuestoId = tipoPuestoRepository.findAll().stream()
                    .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                    .map(TipoPuestoComercial::getId)
                    .findFirst()
                    .orElseGet(() -> {
                        TipoPuestoComercial def = tipoPuestoRepository.save(TipoPuestoComercial.builder()
                                .codigo("FIJO")
                                .nombre("Puesto Fijo")
                                .permiteInstalacionFija(true)
                                .estado("ACTIVO")
                                .build());
                        return def.getId();
                    });
        }
        PuestoComercialCreateRequest modern = PuestoComercialCreateRequest.builder()
                .mercadoId(request.getMercadoId())
                .sectorId(request.getSectorId())
                .pabellonId(request.getPabellonId())
                .tipoPuestoId(tipoPuestoId)
                .codigo(request.getCodigo())
                .numero(request.getNumero())
                .descripcion(request.getDescripcion())
                .areaMetrosCuadrados(request.getAreaMetrosCuadrados())
                .ubicacionReferencia(request.getUbicacionReferencia())
                .estado(request.getEstado())
                .build();
        PuestoComercialResponse resp = registrarPuesto(modern);
        return toLegacyResponse(resp);
    }

    @Transactional
    public PuestoResponse actualizarPuesto(UUID id, PuestoRequest request) {
        PuestoComercial puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + id));

        PuestoComercialUpdateRequest modern = PuestoComercialUpdateRequest.builder()
                .tipoPuestoId(request.getTipoPuestoId())
                .descripcion(request.getDescripcion())
                .areaMetrosCuadrados(request.getAreaMetrosCuadrados())
                .ubicacionReferencia(request.getUbicacionReferencia())
                .version(puesto.getVersion())
                .build();

        PuestoComercialResponse resp = actualizarPuesto(id, puesto.getMercado().getId(), modern);
        return toLegacyResponse(resp);
    }

    @Transactional
    public PuestoResponse cambiarEstadoPuesto(UUID id, EstadoPuesto nuevoEstado, String motivo) {
        PuestoComercial puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + id));
        PuestoEstadoRequest modern = PuestoEstadoRequest.builder()
                .nuevoEstadoOperativo(nuevoEstado)
                .motivo(motivo != null ? motivo : "Cambio de estado administrativo")
                .build();
        PuestoComercialResponse resp = cambiarEstado(id, puesto.getMercado().getId(), modern);
        return toLegacyResponse(resp);
    }

    @Transactional(readOnly = true)
    public PuestoResponse obtenerPorId(UUID id) {
        PuestoComercial puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + id));
        return toLegacyResponse(toDetalleResponse(puesto));
    }

    @Transactional(readOnly = true)
    public Page<PuestoResponse> listarPuestos(UUID mercadoId, UUID sectorId, UUID pabellonId,
                                             EstadoPuesto estado, String termino, Pageable pageable) {
        return listarPuestosPaginado(mercadoId, termino, sectorId, pabellonId, null, null, null, estado, null, null, null, pageable)
                .map(this::toLegacyResponse);
    }

    @Transactional(readOnly = true)
    public List<PuestoResponse> listarPuestosDisponibles(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        return puestoRepository.findByMercadoIdAndEstado(mercadoId, EstadoPuesto.DISPONIBLE)
                .stream()
                .map(p -> toLegacyResponse(toDetalleResponse(p)))
                .collect(Collectors.toList());
    }

    // ==========================================
    // MAPPERS Y HELPERS
    // ==========================================

    public PuestoComercialResponse toDetalleResponse(PuestoComercial p) {
        return PuestoComercialResponse.builder()
                .id(p.getId())
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                .pabellonId(p.getPabellon() != null ? p.getPabellon().getId() : null)
                .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : null)
                .zonaId(p.getZona() != null ? p.getZona().getId() : null)
                .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                .codigo(p.getCodigo())
                .numero(p.getNumero())
                .nombreReferencial(p.getNombreReferencial())
                .descripcion(p.getDescripcion())
                .tipoPuestoId(p.getTipoPuesto() != null ? p.getTipoPuesto().getId() : null)
                .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : null)
                .areaMetrosCuadrados(p.getAreaMetrosCuadrados())
                .anchoMetros(p.getAnchoMetros())
                .largoMetros(p.getLargoMetros())
                .nivel(p.getNivel())
                .ubicacionReferencia(p.getUbicacionReferencia())
                .estadoFisico(p.getEstadoFisico())
                .estadoFisicoDescripcion(p.getEstadoFisico() != null ? p.getEstadoFisico().getDescripcion() : null)
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().getDescripcion() : null)
                .disponible(p.isDisponible())
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PuestoResponse toLegacyResponse(PuestoComercialResponse resp) {
        return PuestoResponse.builder()
                .id(resp.getId())
                .mercadoId(resp.getMercadoId())
                .mercadoNombre(resp.getMercadoNombre())
                .sectorId(resp.getSectorId())
                .sectorNombre(resp.getSectorNombre())
                .pabellonId(resp.getPabellonId())
                .pabellonNombre(resp.getPabellonNombre())
                .codigo(resp.getCodigo())
                .numero(resp.getNumero())
                .descripcion(resp.getDescripcion())
                .areaMetrosCuadrados(resp.getAreaMetrosCuadrados())
                .ubicacionReferencia(resp.getUbicacionReferencia())
                .estado(resp.getEstado())
                .tipoPuestoId(resp.getTipoPuestoId())
                .tipoPuestoNombre(resp.getTipoPuestoNombre())
                .version(resp.getVersion())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }
}

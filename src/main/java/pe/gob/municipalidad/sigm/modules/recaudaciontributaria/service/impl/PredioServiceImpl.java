package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.PredioSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.PredioValidator;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredioServiceImpl implements PredioService {

    private final PredioTributarioRepository predioRepository;
    private final ConstruccionPredialRepository construccionRepository;
    private final RelacionContribuyentePredioRepository relacionRepository;
    private final DocumentoPredioRepository documentoRepository;
    private final HistorialEstadoPredioRepository historialRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final PredioValidator predioValidator;

    @Override
    @Transactional
    public PredioDetailResponse registrarPredio(PredioCreateRequest request, String usuario) {
        log.info("Iniciando registro de predio por usuario '{}': tipo={}, direccion={}",
                usuario, request.getTipoPredio(), request.getDireccion());

        predioValidator.validarCreacion(request);

        // Generar código único y estable
        Long siguienteSeq = predioRepository.obtenerSiguienteValorSecuencia();
        String codigoGenerado = String.format("PR-%06d", siguienteSeq);

        while (predioRepository.existsByCodigoPredio(codigoGenerado)) {
            siguienteSeq++;
            codigoGenerado = String.format("PR-%06d", siguienteSeq);
        }

        // Calcular superficie normalizada en metros cuadrados (m2)
        BigDecimal areaM2 = request.getAreaTerreno();
        String unidad = request.getUnidadMedidaArea() != null ? request.getUnidadMedidaArea().trim().toUpperCase() : "M2";
        if ("HA".equalsIgnoreCase(unidad)) {
            areaM2 = request.getAreaTerreno().multiply(BigDecimal.valueOf(10000));
        }

        // Formatear dirección integral
        String direccionFinal = request.getDireccion();
        if (direccionFinal == null || direccionFinal.isBlank()) {
            StringBuilder sb = new StringBuilder();
            if (request.getComunidadCentroPoblado() != null && !request.getComunidadCentroPoblado().isBlank()) {
                sb.append("C.P. / Comunidad: ").append(request.getComunidadCentroPoblado().trim()).append(", ");
            }
            if (request.getTipoVia() != null && !request.getTipoVia().isBlank()) {
                sb.append(request.getTipoVia().trim()).append(" ");
            }
            if (request.getNombreVia() != null && !request.getNombreVia().isBlank()) {
                sb.append(request.getNombreVia().trim()).append(" ");
            }
            if (request.getNumeroMunicipal() != null && !request.getNumeroMunicipal().isBlank()) {
                sb.append("N° ").append(request.getNumeroMunicipal().trim()).append(" ");
            }
            if (request.getManzana() != null && !request.getManzana().isBlank()) {
                sb.append("Mz. ").append(request.getManzana().trim()).append(" ");
            }
            if (request.getLote() != null && !request.getLote().isBlank()) {
                sb.append("Lt. ").append(request.getLote().trim()).append(" ");
            }
            sb.append("- Quinua");
            direccionFinal = sb.toString();
        }

        SectorPatrullaje sector = null;
        String sectorNombre = request.getSectorNombre();
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(null);
            if (sector != null && (sectorNombre == null || sectorNombre.isBlank())) {
                sectorNombre = sector.getNombre();
            }
        }

        PredioTributario predio = PredioTributario.builder()
                .codigoPredio(codigoGenerado)
                .codigoCatastral(request.getCodigoCatastral() != null ? request.getCodigoCatastral().trim() : null)
                .tipoPredio(request.getTipoPredio())
                .clasificacionPredio(request.getClasificacionPredio() != null ? request.getClasificacionPredio() : ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(request.getUsoDeclarado() != null ? request.getUsoDeclarado() : UsoDeclaradoPredio.VIVIENDA)
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .ubigeo("050108")
                .comunidadCentroPoblado(request.getComunidadCentroPoblado())
                .tipoVia(request.getTipoVia())
                .nombreVia(request.getNombreVia())
                .numeroMunicipal(request.getNumeroMunicipal())
                .manzana(request.getManzana())
                .lote(request.getLote())
                .interior(request.getInterior())
                .zona(request.getZona())
                .direccion(direccionFinal)
                .sector(sector)
                .sectorNombre(sectorNombre)
                .referenciaUbicacion(request.getReferenciaUbicacion())
                .areaTerreno(request.getAreaTerreno())
                .areaTerrenoM2(areaM2)
                .areaConstruida(request.getAreaConstruida() != null ? request.getAreaConstruida() : BigDecimal.ZERO)
                .unidadMedidaArea(unidad)
                .tieneAgua(request.isTieneAgua())
                .tieneDesague(request.isTieneDesague())
                .tieneLuz(request.isTieneLuz())
                .tieneAlumbradoPublico(request.isTieneAlumbradoPublico())
                .tienePistasVeredas(request.isTienePistasVeredas())
                .tieneLimpiezaPublica(request.isTieneLimpiezaPublica())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .precisionMetros(request.getPrecisionMetros())
                .fuenteCoordenadas(request.getFuenteCoordenadas() != null ? request.getFuenteCoordenadas() : FuenteCoordenadas.NO_DISPONIBLE)
                .estado(EstadoPredio.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .observaciones(request.getObservaciones())
                .build();

        PredioTributario guardado = predioRepository.save(predio);

        // Si se indicó un contribuyente inicial, vincularlo
        if (request.getContribuyenteIdInicial() != null) {
            ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteIdInicial())
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente inicial"));

            TipoRelacionContribuyentePredio relTipo = request.getTipoRelacionInicial() != null ?
                    request.getTipoRelacionInicial() : TipoRelacionContribuyentePredio.PROPIETARIO_UNICO;
            BigDecimal pct = request.getPorcentajeParticipacionInicial() != null ?
                    request.getPorcentajeParticipacionInicial() : new BigDecimal("100.00");

            RelacionContribuyentePredio rel = RelacionContribuyentePredio.builder()
                    .contribuyente(contribuyente)
                    .predio(guardado)
                    .tipoRelacion(relTipo)
                    .porcentajeParticipacion(pct)
                    .fechaInicio(LocalDate.now())
                    .esPrincipal(true)
                    .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                    .observaciones("Asignación de titularidad en el registro inicial del predio")
                    .build();

            relacionRepository.save(rel);
            guardado.getRelacionesContribuyente().add(rel);
        }

        // Registrar auditoría de estado inicial
        HistorialEstadoPredio hist = HistorialEstadoPredio.builder()
                .predio(guardado)
                .estadoAnterior(EstadoPredio.ACTIVO)
                .nuevoEstado(EstadoPredio.ACTIVO)
                .motivo("Alta y registro inicial del predio en el catastro tributario municipal")
                .usuarioRegistro(usuario != null ? usuario : "SYSTEM")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);
        guardado.getHistorialEstados().add(hist);

        log.info("Predio registrado con éxito: ID={}, Código={}", guardado.getId(), guardado.getCodigoPredio());
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public PredioDetailResponse actualizarPredio(UUID id, PredioUpdateRequest request, String usuario) {
        PredioTributario predio = predioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio con ID: " + id));

        predioValidator.validarActualizacion(id, request);

        BigDecimal areaM2 = request.getAreaTerreno();
        String unidad = request.getUnidadMedidaArea() != null ? request.getUnidadMedidaArea().trim().toUpperCase() : "M2";
        if ("HA".equalsIgnoreCase(unidad)) {
            areaM2 = request.getAreaTerreno().multiply(BigDecimal.valueOf(10000));
        }

        SectorPatrullaje sector = null;
        String sectorNombre = request.getSectorNombre();
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(null);
            if (sector != null && (sectorNombre == null || sectorNombre.isBlank())) {
                sectorNombre = sector.getNombre();
            }
        }

        predio.setCodigoCatastral(request.getCodigoCatastral() != null ? request.getCodigoCatastral().trim() : null);
        predio.setTipoPredio(request.getTipoPredio());
        predio.setClasificacionPredio(request.getClasificacionPredio());
        predio.setUsoDeclarado(request.getUsoDeclarado());
        predio.setComunidadCentroPoblado(request.getComunidadCentroPoblado());
        predio.setTipoVia(request.getTipoVia());
        predio.setNombreVia(request.getNombreVia());
        predio.setNumeroMunicipal(request.getNumeroMunicipal());
        predio.setManzana(request.getManzana());
        predio.setLote(request.getLote());
        predio.setInterior(request.getInterior());
        predio.setZona(request.getZona());
        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            predio.setDireccion(request.getDireccion().trim());
        }
        predio.setSector(sector);
        predio.setSectorNombre(sectorNombre);
        predio.setReferenciaUbicacion(request.getReferenciaUbicacion());
        predio.setAreaTerreno(request.getAreaTerreno());
        predio.setAreaTerrenoM2(areaM2);
        predio.setUnidadMedidaArea(unidad);

        predio.setTieneAgua(request.isTieneAgua());
        predio.setTieneDesague(request.isTieneDesague());
        predio.setTieneLuz(request.isTieneLuz());
        predio.setTieneAlumbradoPublico(request.isTieneAlumbradoPublico());
        predio.setTienePistasVeredas(request.isTienePistasVeredas());
        predio.setTieneLimpiezaPublica(request.isTieneLimpiezaPublica());

        predio.setLatitud(request.getLatitud());
        predio.setLongitud(request.getLongitud());
        predio.setPrecisionMetros(request.getPrecisionMetros());
        if (request.getFuenteCoordenadas() != null) {
            predio.setFuenteCoordenadas(request.getFuenteCoordenadas());
        }
        predio.setObservaciones(request.getObservaciones());

        PredioTributario actualizado = predioRepository.save(predio);
        log.info("Predio {} actualizado por usuario '{}'", id, usuario);
        return mapToDetailResponse(actualizado);
    }

    @Override
    @Transactional
    public PredioDetailResponse cambiarEstado(UUID id, CambioEstadoPredioRequest request, String usuario) {
        PredioTributario predio = predioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio con ID: " + id));

        if (request.getNuevoEstado() == predio.getEstado()) {
            throw new BusinessException("El predio ya se encuentra en estado " + request.getNuevoEstado().getDescripcion());
        }

        EstadoPredio estadoAnterior = predio.getEstado();
        predio.setEstado(request.getNuevoEstado());
        predio.setMotivoCambioEstado(request.getMotivo());
        predio.setFechaCambioEstado(OffsetDateTime.now());

        HistorialEstadoPredio hist = HistorialEstadoPredio.builder()
                .predio(predio)
                .estadoAnterior(estadoAnterior)
                .nuevoEstado(request.getNuevoEstado())
                .motivo(request.getMotivo())
                .resolucionSustento(request.getResolucionSustento())
                .usuarioRegistro(usuario != null ? usuario : "SYSTEM")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        PredioTributario guardado = predioRepository.save(predio);
        log.info("Estado del predio {} cambiado de {} a {} por {}", id, estadoAnterior, request.getNuevoEstado(), usuario);
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PredioDetailResponse obtenerDetalle(UUID id) {
        PredioTributario predio = predioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio con ID: " + id));
        return mapToDetailResponse(predio);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PredioSummaryDto> listarConFiltros(PredioFilterDto filtro, Pageable pageable) {
        Specification<PredioTributario> spec = PredioSpecification.conFiltros(
                filtro != null ? filtro.getQuery() : null,
                filtro != null ? filtro.getTipo() : null,
                filtro != null ? filtro.getEstado() : null,
                filtro != null ? filtro.getSectorId() : null,
                filtro != null ? filtro.getContribuyenteId() : null);

        return predioRepository.findAll(spec, pageable).map(this::mapToSummaryDto);
    }

    @Override
    @Transactional
    public ConstruccionResponse agregarConstruccion(UUID predioId, ConstruccionPredialRequest request, String usuario) {
        PredioTributario predio = predioRepository.findById(predioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio con ID: " + predioId));

        if (request.getAreaConstruida() == null || request.getAreaConstruida().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El área construida debe ser mayor a cero.");
        }

        String cod = request.getCodigoConstruccion();
        if (cod == null || cod.isBlank()) {
            cod = "BLQ-" + (predio.getConstrucciones().size() + 1) + "-P" + request.getNivelPiso();
        }

        ConstruccionPredial construccion = ConstruccionPredial.builder()
                .predio(predio)
                .codigoConstruccion(cod)
                .seccionBloque(request.getSeccionBloque())
                .nivelPiso(request.getNivelPiso())
                .areaConstruida(request.getAreaConstruida())
                .materialPredominante(request.getMaterialPredominante())
                .estadoConservacion(request.getEstadoConservacion())
                .anioConstruccion(request.getAnioConstruccion())
                .clasificacionEdificacion(request.getClasificacionEdificacion() != null ? request.getClasificacionEdificacion() : "PREDOMINANTE")
                .descripcion(request.getDescripcion())
                .build();

        ConstruccionPredial guardada = construccionRepository.save(construccion);
        predio.getConstrucciones().add(guardada);
        predio.recalcularAreaConstruidaTotal();
        predioRepository.save(predio);

        log.info("Construcción agregada al predio {}: ID={}", predioId, guardada.getId());
        return mapConstruccionToDto(guardada);
    }

    @Override
    @Transactional
    public void eliminarConstruccion(UUID predioId, UUID construccionId, String usuario) {
        ConstruccionPredial constr = construccionRepository.findById(construccionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la construcción"));
        if (!constr.getPredio().getId().equals(predioId)) {
            throw new BusinessException("La construcción no pertenece al predio especificado.");
        }
        PredioTributario predio = constr.getPredio();
        construccionRepository.delete(constr);
        predio.getConstrucciones().remove(constr);
        predio.recalcularAreaConstruidaTotal();
        predioRepository.save(predio);
    }

    @Override
    @Transactional
    public RelacionContribuyenteResponse asociarContribuyente(UUID predioId, RelacionContribuyentePredioRequest request, String usuario) {
        predioValidator.validarRelacion(request);

        PredioTributario predio = predioRepository.findById(predioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio"));

        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente"));

        // Verificar si ya existe relación activa
        boolean yaExiste = relacionRepository.existsByContribuyenteIdAndPredioIdAndEstado(
                contribuyente.getId(), predioId, EstadoRelacionContribuyentePredio.ACTIVO);
        if (yaExiste) {
            throw new BusinessException("El contribuyente ya se encuentra vinculado activamente a este predio.");
        }

        RelacionContribuyentePredio relacion = RelacionContribuyentePredio.builder()
                .contribuyente(contribuyente)
                .predio(predio)
                .tipoRelacion(request.getTipoRelacion())
                .porcentajeParticipacion(request.getPorcentajeParticipacion())
                .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now())
                .fechaFin(request.getFechaFin())
                .esPrincipal(request.getEsPrincipal() != null ? request.getEsPrincipal() : true)
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .observaciones(request.getObservaciones())
                .build();

        RelacionContribuyentePredio guardada = relacionRepository.save(relacion);
        log.info("Contribuyente {} asociado al predio {}", contribuyente.getId(), predioId);
        return mapRelacionToDto(guardada);
    }

    @Override
    @Transactional
    public void desvincularContribuyente(UUID predioId, UUID relacionId, String usuario) {
        RelacionContribuyentePredio rel = relacionRepository.findById(relacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la relación titular-predio"));
        if (!rel.getPredio().getId().equals(predioId)) {
            throw new BusinessException("La relación no pertenece al predio especificado.");
        }
        rel.setEstado(EstadoRelacionContribuyentePredio.HISTORICO);
        rel.setFechaFin(LocalDate.now());
        relacionRepository.save(rel);
        log.info("Relación titular-predio {} archivada por {}", relacionId, usuario);
    }

    @Override
    @Transactional
    public DocumentoPredioResponse agregarDocumento(UUID predioId, DocumentoPredioRequest request, String usuario) {
        PredioTributario predio = predioRepository.findById(predioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el predio"));

        DocumentoPredio doc = DocumentoPredio.builder()
                .predio(predio)
                .tipoDocumento(request.getTipoDocumento())
                .titulo(request.getTitulo().trim())
                .archivoUrl(request.getArchivoUrl())
                .numeroFolio(request.getNumeroFolio())
                .fechaEmision(request.getFechaEmision())
                .entidadEmisora(request.getEntidadEmisora())
                .observaciones(request.getObservaciones())
                .build();

        DocumentoPredio guardado = documentoRepository.save(doc);
        return mapDocumentoToDto(guardado);
    }

    @Override
    @Transactional
    public void eliminarDocumento(UUID predioId, UUID documentoId, String usuario) {
        DocumentoPredio doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el documento"));
        if (!doc.getPredio().getId().equals(predioId)) {
            throw new BusinessException("El documento no pertenece al predio especificado.");
        }
        documentoRepository.delete(doc);
    }

    @Override
    @Transactional
    public PredioDetailResponse registrarPredio(PredioCreateRequest request) {
        return registrarPredio(request, "SISTEMA");
    }

    @Override
    @Transactional
    public PredioDetailResponse actualizarPredio(UUID id, PredioUpdateRequest request) {
        return actualizarPredio(id, request, "SISTEMA");
    }

    @Override
    @Transactional
    public PredioDetailResponse cambiarEstado(UUID id, CambioEstadoPredioRequest request) {
        return cambiarEstado(id, request, "SISTEMA");
    }

    @Override
    @Transactional(readOnly = true)
    public PredioDetailResponse obtenerPredioPorId(UUID id) {
        return obtenerDetalle(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PredioSummaryDto> listarPredios(PredioFilterDto filtro, Pageable pageable) {
        return listarConFiltros(filtro, pageable);
    }

    @Override
    @Transactional
    public ConstruccionResponse agregarConstruccion(UUID predioId, ConstruccionPredialRequest request) {
        return agregarConstruccion(predioId, request, "SISTEMA");
    }

    @Override
    @Transactional
    public void eliminarConstruccion(UUID predioId, UUID construccionId) {
        eliminarConstruccion(predioId, construccionId, "SISTEMA");
    }

    @Override
    @Transactional
    public RelacionContribuyenteResponse asociarContribuyente(UUID predioId, RelacionContribuyentePredioRequest request) {
        return asociarContribuyente(predioId, request, "SISTEMA");
    }

    @Override
    @Transactional
    public RelacionContribuyenteResponse vincularContribuyente(UUID predioId, RelacionContribuyentePredioRequest request) {
        return asociarContribuyente(predioId, request, "SISTEMA");
    }

    @Override
    @Transactional
    public RelacionContribuyenteResponse vincularContribuyente(UUID predioId, RelacionContribuyentePredioRequest request, String usuario) {
        return asociarContribuyente(predioId, request, usuario);
    }

    @Override
    @Transactional
    public void desvincularContribuyente(UUID predioId, UUID relacionId) {
        desvincularContribuyente(predioId, relacionId, "SISTEMA");
    }

    @Override
    @Transactional
    public DocumentoPredioResponse agregarDocumento(UUID predioId, DocumentoPredioRequest request) {
        return agregarDocumento(predioId, request, "SISTEMA");
    }

    @Override
    @Transactional
    public void eliminarDocumento(UUID predioId, UUID documentoId) {
        eliminarDocumento(predioId, documentoId, "SISTEMA");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        long total = predioRepository.count();
        long urbanos = predioRepository.countByTipoPredio(TipoPredio.URBANO);
        long rurales = predioRepository.countByTipoPredio(TipoPredio.RURAL);
        long activos = predioRepository.countByEstado(EstadoPredio.ACTIVO);
        long observados = predioRepository.countByEstado(EstadoPredio.OBSERVADO);

        stats.put("total", total);
        stats.put("urbanos", urbanos);
        stats.put("rurales", rurales);
        stats.put("activos", activos);
        stats.put("observados", observados);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelacionContribuyenteResponse> listarPrediosPorContribuyente(UUID contribuyenteId) {
        return relacionRepository.findByContribuyenteIdAndEstado(contribuyenteId, EstadoRelacionContribuyentePredio.ACTIVO)
                .stream().map(this::mapRelacionToDto).toList();
    }

    private PredioSummaryDto mapToSummaryDto(PredioTributario p) {
        String primerTitular = null;
        if (!p.getRelacionesContribuyente().isEmpty()) {
            primerTitular = p.getRelacionesContribuyente().get(0).getContribuyente().getNombreRazonSocial();
        }

        return PredioSummaryDto.builder()
                .id(p.getId())
                .codigoPredio(p.getCodigoPredio())
                .codigoCatastral(p.getCodigoCatastral())
                .tipoPredio(p.getTipoPredio() != null ? p.getTipoPredio().name() : "")
                .tipoPredioDesc(p.getTipoPredio() != null ? p.getTipoPredio().getDescripcion() : "")
                .clasificacionPredioDesc(p.getClasificacionPredio() != null ? p.getClasificacionPredio().getDescripcion() : "")
                .direccion(p.getDireccionCompleta())
                .sectorNombre(p.getSectorNombre())
                .areaTerreno(p.getAreaTerreno())
                .areaTerrenoM2(p.getAreaTerrenoM2())
                .areaConstruida(p.getAreaConstruida())
                .unidadMedidaArea(p.getUnidadMedidaArea())
                .primerContribuyenteNombre(primerTitular)
                .referencia(p.getReferenciaUbicacion())
                .estado(p.getEstado() != null ? p.getEstado().name() : "ACTIVO")
                .estadoDesc(p.getEstado() != null ? p.getEstado().getDescripcion() : "Activo")
                .badgeClass(p.getEstado() != null ? p.getEstado().getBadgeClass() : "")
                .fechaRegistro(p.getFechaRegistro() != null ? p.getFechaRegistro().toString() : "")
                .build();
    }

    private PredioDetailResponse mapToDetailResponse(PredioTributario p) {
        List<ConstruccionResponse> constrs = construccionRepository.findByPredioIdOrderByNivelPisoAsc(p.getId())
                .stream().map(this::mapConstruccionToDto).toList();

        List<RelacionContribuyenteResponse> rels = relacionRepository.findByPredioId(p.getId())
                .stream().map(this::mapRelacionToDto).toList();

        List<DocumentoPredioResponse> docs = documentoRepository.findByPredioIdOrderByCreatedAtDesc(p.getId())
                .stream().map(this::mapDocumentoToDto).toList();

        List<HistorialEstadoPredioResponse> hist = historialRepository.findByPredioIdOrderByFechaRegistroDesc(p.getId())
                .stream().map(this::mapHistorialToDto).toList();

        return PredioDetailResponse.builder()
                .id(p.getId())
                .codigoPredio(p.getCodigoPredio())
                .codigoCatastral(p.getCodigoCatastral())
                .tipoPredio(p.getTipoPredio())
                .tipoPredioDesc(p.getTipoPredio() != null ? p.getTipoPredio().getDescripcion() : "")
                .clasificacionPredio(p.getClasificacionPredio())
                .clasificacionPredioDesc(p.getClasificacionPredio() != null ? p.getClasificacionPredio().getDescripcion() : "")
                .usoDeclarado(p.getUsoDeclarado())
                .usoDeclaradoDesc(p.getUsoDeclarado() != null ? p.getUsoDeclarado().getDescripcion() : "")
                .departamento(p.getDepartamento())
                .provincia(p.getProvincia())
                .distrito(p.getDistrito())
                .ubigeo(p.getUbigeo())
                .comunidadCentroPoblado(p.getComunidadCentroPoblado())
                .tipoVia(p.getTipoVia())
                .nombreVia(p.getNombreVia())
                .numeroMunicipal(p.getNumeroMunicipal())
                .manzana(p.getManzana())
                .lote(p.getLote())
                .interior(p.getInterior())
                .direccion(p.getDireccion())
                .direccionCompleta(p.getDireccionCompleta())
                .referenciaUbicacion(p.getReferenciaUbicacion())
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorNombre(p.getSectorNombre())
                .zona(p.getZona())
                .areaTerreno(p.getAreaTerreno())
                .areaTerrenoM2(p.getAreaTerrenoM2())
                .areaConstruida(p.getAreaConstruida())
                .unidadMedidaArea(p.getUnidadMedidaArea())
                .valorArancelarioReferencial(p.getValorArancelarioReferencial())
                .valorAutovaluoReferencial(p.getValorAutovaluoReferencial())
                .tieneAgua(p.isTieneAgua())
                .tieneDesague(p.isTieneDesague())
                .tieneLuz(p.isTieneLuz())
                .tieneAlumbradoPublico(p.isTieneAlumbradoPublico())
                .tienePistasVeredas(p.isTienePistasVeredas())
                .tieneLimpiezaPublica(p.isTieneLimpiezaPublica())
                .latitud(p.getLatitud())
                .longitud(p.getLongitud())
                .precisionMetros(p.getPrecisionMetros())
                .fuenteCoordenadas(p.getFuenteCoordenadas())
                .fuenteCoordenadasDesc(p.getFuenteCoordenadas() != null ? p.getFuenteCoordenadas().getDescripcion() : "")
                .tieneCoordenadas(p.tieneCoordenadasValidas())
                .estado(p.getEstado())
                .estadoDesc(p.getEstado() != null ? p.getEstado().getDescripcion() : "")
                .badgeClass(p.getEstado() != null ? p.getEstado().getBadgeClass() : "")
                .fechaRegistro(p.getFechaRegistro())
                .observaciones(p.getObservaciones())
                .version(p.getVersion())
                .construcciones(constrs)
                .contribuyentesVinculados(rels)
                .documentos(docs)
                .historialEstados(hist)
                .build();
    }

    private ConstruccionResponse mapConstruccionToDto(ConstruccionPredial c) {
        return ConstruccionResponse.builder()
                .id(c.getId())
                .codigoConstruccion(c.getCodigoConstruccion())
                .seccionBloque(c.getSeccionBloque())
                .nivelPiso(c.getNivelPiso())
                .areaConstruida(c.getAreaConstruida())
                .materialPredominante(c.getMaterialPredominante())
                .materialPredominanteDesc(c.getMaterialPredominante() != null ? c.getMaterialPredominante().getDescripcion() : "")
                .estadoConservacion(c.getEstadoConservacion())
                .estadoConservacionDesc(c.getEstadoConservacion() != null ? c.getEstadoConservacion().getDescripcion() : "")
                .estadoConservacionBadgeClass(c.getEstadoConservacion() != null ? c.getEstadoConservacion().getBadgeClass() : "")
                .anioConstruccion(c.getAnioConstruccion())
                .clasificacionEdificacion(c.getClasificacionEdificacion())
                .descripcion(c.getDescripcion())
                .build();
    }

    private RelacionContribuyenteResponse mapRelacionToDto(RelacionContribuyentePredio r) {
        return RelacionContribuyenteResponse.builder()
                .id(r.getId())
                .contribuyenteId(r.getContribuyente().getId())
                .codigoContribuyente(r.getContribuyente().getCodigoContribuyente())
                .tipoDocumento(r.getContribuyente().getTipoDocumento())
                .numeroDocumentoEnmascarado(r.getContribuyente().getNumeroDocumentoEnmascarado())
                .nombreRazonSocial(r.getContribuyente().getNombreRazonSocial())
                .tipoRelacion(r.getTipoRelacion())
                .tipoRelacionDesc(r.getTipoRelacion() != null ? r.getTipoRelacion().getDescripcion() : "")
                .porcentajeParticipacion(r.getPorcentajeParticipacion())
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .esPrincipal(r.getEsPrincipal())
                .estado(r.getEstado())
                .estadoDesc(r.getEstado() != null ? r.getEstado().name() : "ACTIVO")
                .observaciones(r.getObservaciones())
                .predioId(r.getPredio() != null ? r.getPredio().getId() : null)
                .codigoPredio(r.getPredio() != null ? r.getPredio().getCodigoPredio() : null)
                .codigoCatastral(r.getPredio() != null ? r.getPredio().getCodigoCatastral() : null)
                .direccionPredio(r.getPredio() != null ? (r.getPredio().getDireccionCompleta() != null ? r.getPredio().getDireccionCompleta() : r.getPredio().getDireccion()) : null)
                .build();
    }

    private DocumentoPredioResponse mapDocumentoToDto(DocumentoPredio d) {
        return DocumentoPredioResponse.builder()
                .id(d.getId())
                .tipoDocumento(d.getTipoDocumento())
                .tipoDocumentoDesc(d.getTipoDocumento() != null ? d.getTipoDocumento().getDescripcion() : "")
                .titulo(d.getTitulo())
                .archivoUrl(d.getArchivoUrl())
                .numeroFolio(d.getNumeroFolio())
                .fechaEmision(d.getFechaEmision())
                .entidadEmisora(d.getEntidadEmisora())
                .observaciones(d.getObservaciones())
                .build();
    }

    private HistorialEstadoPredioResponse mapHistorialToDto(HistorialEstadoPredio h) {
        return HistorialEstadoPredioResponse.builder()
                .id(h.getId())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoAnteriorDesc(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getDescripcion() : "")
                .nuevoEstado(h.getNuevoEstado())
                .nuevoEstadoDesc(h.getNuevoEstado() != null ? h.getNuevoEstado().getDescripcion() : "")
                .nuevoEstadoBadgeClass(h.getNuevoEstado() != null ? h.getNuevoEstado().getBadgeClass() : "")
                .motivo(h.getMotivo())
                .resolucionSustento(h.getResolucionSustento())
                .usuarioRegistro(h.getUsuarioRegistro())
                .fechaRegistro(h.getFechaRegistro())
                .build();
    }
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConfiguracionArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DistribucionArbitrioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ArbitrioSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ConfiguracionArbitrioSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ArbitrioValidator;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArbitrioMunicipalServiceImpl implements ArbitrioMunicipalService {

    private final ServicioArbitrioMunicipalRepository servicioRepository;
    private final ConfiguracionArbitrioMunicipalRepository configuracionRepository;
    private final CostoServicioArbitrioRepository costoRepository;
    private final CriterioDistribucionArbitrioRepository criterioRepository;
    private final CalculoArbitrioMunicipalRepository calculoRepository;
    private final DetalleCalculoArbitrioRepository detalleRepository;
    private final HistorialArbitrioMunicipalRepository historialRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final NormaTributariaConfiguradaRepository normaRepository;
    private final RelacionContribuyentePredioRepository relacionRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final DistribucionArbitrioService distribucionService;
    private final ArbitrioValidator validator;
    private final ObjectMapper objectMapper;

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal DOCE = new BigDecimal("12.00");

    // ==========================================
    // CATÁLOGO DE SERVICIOS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ServicioArbitrioMunicipal> listarServiciosActivos() {
        return servicioRepository.findByEstadoOrderByNombreAsc(EstadoServicioArbitrio.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioArbitrioMunicipal obtenerServicioPorId(UUID id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el servicio de arbitrio con ID: " + id));
    }

    // ==========================================
    // CONFIGURACIÓN DE ARBITRIOS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<ConfiguracionArbitrioSummaryDto> listarConfiguraciones(
            String query, Integer anio, UUID servicioId, TipoServicioArbitrio tipoServicio, EstadoConfiguracionArbitrio estado, Pageable pageable) {
        Specification<ConfiguracionArbitrioMunicipal> spec = ConfiguracionArbitrioSpecification.conFiltro(query, anio, servicioId, tipoServicio, estado);
        return configuracionRepository.findAll(spec, pageable).map(this::mapConfiguracionToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionArbitrioDetailResponse obtenerConfiguracionPorId(UUID id) {
        ConfiguracionArbitrioMunicipal config = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración de arbitrio con ID: " + id));
        return mapConfiguracionToDetail(config);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse registrarConfiguracion(ConfiguracionArbitrioCreateRequest request, String usuario) {
        validator.validarConfiguracion(request);

        ServicioArbitrioMunicipal servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el servicio de arbitrio con ID: " + request.getServicioId()));

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el período fiscal con ID: " + request.getPeriodoId()));

        NormaTributariaConfigurada norma = null;
        if (request.getNormaId() != null) {
            norma = normaRepository.findById(request.getNormaId()).orElse(null);
        }

        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(null);
        }

        // Calcular totales de costos
        BigDecimal totalAnual = BigDecimal.ZERO;
        if (request.getCostos() != null) {
            totalAnual = request.getCostos().stream()
                    .map(c -> c.getImporte() != null ? c.getImporte() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalMensual = totalAnual.divide(DOCE, 2, RoundingMode.HALF_UP);

        long countExistentes = configuracionRepository.count();
        String codigoConf = String.format("CONF-ARB-%d-%s-%03d",
                periodo.getAnio(), servicio.getTipoServicio().name().substring(0, Math.min(3, servicio.getTipoServicio().name().length())), countExistentes + 1);

        ConfiguracionArbitrioMunicipal config = ConfiguracionArbitrioMunicipal.builder()
                .codigoConfiguracion(codigoConf)
                .servicio(servicio)
                .periodo(periodo)
                .norma(norma)
                .sector(sector)
                .zona(request.getZona())
                .versionConfiguracion(1)
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .estado(EstadoConfiguracionArbitrio.BORRADOR)
                .costoTotalAnual(totalAnual)
                .costoTotalMensual(totalMensual)
                .metodologiaDistribucion(request.getMetodologiaDistribucion() != null ? request.getMetodologiaDistribucion() : MetodoDistribucionArbitrio.USO_PREDIO_ACTIVIDAD)
                .cantidadPrediosAfectos(150) // Base referencial de contribuyentes en casco distrital
                .descripcionJustificacion(request.getDescripcionJustificacion())
                .build();

        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(config);

        // Guardar costos
        if (request.getCostos() != null) {
            for (var cReq : request.getCostos()) {
                CostoServicioArbitrio costo = CostoServicioArbitrio.builder()
                        .configuracion(guardada)
                        .categoriaCosto(cReq.getCategoriaCosto())
                        .descripcion(cReq.getDescripcion())
                        .importe(cReq.getImporte())
                        .moneda("PEN")
                        .periodoCosto(cReq.getPeriodoCosto() != null ? cReq.getPeriodoCosto() : "ANUAL")
                        .documentoSustentoRef(cReq.getDocumentoSustentoRef())
                        .fuenteSustento(cReq.getFuenteSustento())
                        .estadoValidacion(EstadoValidacionCosto.DECLARADO)
                        .build();
                costoRepository.save(costo);
            }
        }

        // Guardar criterios
        if (request.getCriterios() != null) {
            for (var critReq : request.getCriterios()) {
                CriterioDistribucionArbitrio crit = CriterioDistribucionArbitrio.builder()
                        .configuracion(guardada)
                        .codigo(critReq.getCodigo())
                        .nombre(critReq.getNombre())
                        .tipoDato(critReq.getTipoDato())
                        .unidad(critReq.getUnidad())
                        .metodoDistribucion(critReq.getMetodoDistribucion())
                        .ponderacionPorcentaje(critReq.getPonderacionPorcentaje() != null ? critReq.getPonderacionPorcentaje() : CIEN)
                        .factorMinimo(critReq.getFactorMinimo())
                        .factorMaximo(critReq.getFactorMaximo())
                        .fuenteNormativa(critReq.getFuenteNormativa())
                        .estado("ACTIVO")
                        .build();
                criterioRepository.save(crit);
            }
        }

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.CREACION, null, EstadoConfiguracionArbitrio.BORRADOR.name(),
                "Formulación inicial de configuración operativa de arbitrios.", usuario);

        log.info("Configuración de arbitrios registrada: ID={}, Código={}", guardada.getId(), guardada.getCodigoConfiguracion());
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse enviarConfiguracionARevision(UUID id, String motivo, String usuario) {
        ConfiguracionArbitrioMunicipal config = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + id));

        if (config.getEstado() != EstadoConfiguracionArbitrio.BORRADOR && config.getEstado() != EstadoConfiguracionArbitrio.OBSERVADO) {
            throw new BusinessException("Solo configuraciones en BORRADOR u OBSERVADO pueden remitirse a revisión.");
        }

        String anterior = config.getEstado().name();
        config.setEstado(EstadoConfiguracionArbitrio.EN_REVISION);
        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(config);

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.ENVIO_REVISION, anterior, EstadoConfiguracionArbitrio.EN_REVISION.name(),
                motivo != null ? motivo : "Remisión para visación y control de legalidad.", usuario);

        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse aprobarConfiguracion(UUID id, String resolucionAprobacion, String usuario) {
        ConfiguracionArbitrioMunicipal config = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + id));

        validator.validarParaAprobacionConfiguracion(config);

        String anterior = config.getEstado().name();
        config.setEstado(EstadoConfiguracionArbitrio.APROBADO);
        config.setUsuarioAprobador(usuario != null ? usuario : "AUTORIDAD");
        config.setFechaAprobacion(OffsetDateTime.now());
        config.setResolucionAprobacion(resolucionAprobacion);

        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(config);

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.APROBACION, anterior, EstadoConfiguracionArbitrio.APROBADO.name(),
                "Aprobación formal con Ordenanza / Resolución: " + resolucionAprobacion, usuario);

        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse activarConfiguracion(UUID id, String usuario) {
        ConfiguracionArbitrioMunicipal config = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + id));

        if (config.getEstado() != EstadoConfiguracionArbitrio.APROBADO) {
            throw new BusinessException("Solo configuraciones formalmente APROBADAS pueden ser activadas para cobro.");
        }

        String anterior = config.getEstado().name();
        config.setEstado(EstadoConfiguracionArbitrio.ACTIVO);
        config.setUsuarioActivador(usuario != null ? usuario : "SISTEMA");
        config.setFechaActivacion(OffsetDateTime.now());

        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(config);

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.ACTIVACION, anterior, EstadoConfiguracionArbitrio.ACTIVO.name(),
                "Activación operativa para emisión y cálculo.", usuario);

        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse desactivarConfiguracion(UUID id, String motivo, String usuario) {
        ConfiguracionArbitrioMunicipal config = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + id));

        String anterior = config.getEstado().name();
        config.setEstado(EstadoConfiguracionArbitrio.INACTIVO);
        config.setMotivoDesactivacion(motivo);

        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(config);

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.DESACTIVACION, anterior, EstadoConfiguracionArbitrio.INACTIVO.name(),
                motivo != null ? motivo : "Desactivación de configuración.", usuario);

        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionArbitrioDetailResponse crearNuevaVersionConfiguracion(UUID id, ConfiguracionArbitrioCreateRequest request, String motivo, String usuario) {
        ConfiguracionArbitrioMunicipal anterior = configuracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración original con ID: " + id));

        int nuevaVer = anterior.getVersionConfiguracion() + 1;
        String nuevoCod = String.format("CONF-ARB-%d-%s-%03d-v%d",
                anterior.getPeriodo().getAnio(), anterior.getServicio().getTipoServicio().name().substring(0, Math.min(3, anterior.getServicio().getTipoServicio().name().length())),
                configuracionRepository.count() + 1, nuevaVer);

        ConfiguracionArbitrioMunicipal nueva = ConfiguracionArbitrioMunicipal.builder()
                .codigoConfiguracion(nuevoCod)
                .servicio(anterior.getServicio())
                .periodo(anterior.getPeriodo())
                .norma(request.getNormaId() != null ? normaRepository.findById(request.getNormaId()).orElse(anterior.getNorma()) : anterior.getNorma())
                .sector(anterior.getSector())
                .zona(request.getZona() != null ? request.getZona() : anterior.getZona())
                .versionConfiguracion(nuevaVer)
                .configuracionPredecesora(anterior)
                .fechaInicioVigencia(request.getFechaInicioVigencia() != null ? request.getFechaInicioVigencia() : LocalDate.now())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .estado(EstadoConfiguracionArbitrio.BORRADOR)
                .costoTotalAnual(anterior.getCostoTotalAnual())
                .costoTotalMensual(anterior.getCostoTotalMensual())
                .metodologiaDistribucion(request.getMetodologiaDistribucion() != null ? request.getMetodologiaDistribucion() : anterior.getMetodologiaDistribucion())
                .cantidadPrediosAfectos(anterior.getCantidadPrediosAfectos())
                .descripcionJustificacion("Nueva versión derivada de " + anterior.getCodigoConfiguracion() + ": " + (motivo != null ? motivo : ""))
                .build();

        ConfiguracionArbitrioMunicipal guardada = configuracionRepository.save(nueva);

        registrarHistorial("CONFIGURACION", guardada.getId(), guardada.getCodigoConfiguracion(),
                AccionHistorialArbitrio.RECALCULO, anterior.getEstado().name(), EstadoConfiguracionArbitrio.BORRADOR.name(),
                "Creación de nueva versión v" + nuevaVer + " por motivo: " + motivo, usuario);

        return mapConfiguracionToDetail(guardada);
    }

    // ==========================================
    // SIMULACIÓN Y CÁLCULO DE ARBITRIOS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ArbitrioDetailResponse simularCalculo(SimulacionArbitrioRequest request, String usuario) {
        List<String> advertencias = new ArrayList<>();
        List<String> explicaciones = new ArrayList<>();

        validator.validarSimulacion(request, advertencias);

        ContribuyenteTributario cont = contribuyenteRepository.findById(request.getContribuyenteId()).orElseThrow();
        PredioTributario predio = predioRepository.findById(request.getPredioId()).orElseThrow();
        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId()).orElseThrow();

        // Determinar participación en copropiedad
        BigDecimal participacion = CIEN;
        var relOpt = relacionRepository.findByContribuyenteIdAndPredioId(cont.getId(), predio.getId());
        if (relOpt.isPresent() && relOpt.get().getPorcentajeParticipacion() != null) {
            participacion = relOpt.get().getPorcentajeParticipacion();
        }

        // Obtener configuraciones aplicables
        List<ConfiguracionArbitrioMunicipal> configs = resolverConfiguracionesAplicables(periodo, request.getServiciosIds());

        if (configs.isEmpty()) {
            advertencias.add("No se encontraron configuraciones vigentes de arbitrios para el ejercicio " + periodo.getAnio() + ". Se utilizarán tarifas paramétricas de referencia.");
        }

        List<DetalleCalculoArbitrioResponse> detallesResp = new ArrayList<>();
        BigDecimal importeAnualTotal = BigDecimal.ZERO;
        BigDecimal importePeriodoTotal = BigDecimal.ZERO;
        BigDecimal montoDescuentoTotal = BigDecimal.ZERO;

        for (ConfiguracionArbitrioMunicipal cfg : configs) {
            DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                    predio, cfg, request.getBeneficio(), participacion, advertencias, explicaciones
            );
            detallesResp.add(mapDetalleToResponse(det));
            importeAnualTotal = importeAnualTotal.add(det.getImporteFinalAnual());
            importePeriodoTotal = importePeriodoTotal.add(det.getImporteFinalMensual());
            montoDescuentoTotal = montoDescuentoTotal.add(det.getDescuentoAplicado());
        }

        advertencias.add("Simulación referencial — no constituye deuda tributaria exigible.");

        return ArbitrioDetailResponse.builder()
                .id(UUID.randomUUID())
                .codigoCalculo("SIM-ARB-" + periodo.getAnio() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .contribuyenteId(cont.getId())
                .contribuyenteCodigo(cont.getCodigoContribuyente())
                .contribuyenteNombre(cont.getNombreRazonSocial())
                .contribuyenteDocumento(cont.getTipoDocumento() + ": " + cont.getNumeroDocumento())
                .predioId(predio.getId())
                .predioCodigo(predio.getCodigoPredio())
                .predioDireccion(predio.getDireccion())
                .tipoPredio(predio.getTipoPredio() != null ? predio.getTipoPredio().name() : "URBANO")
                .clasificacionPredio(predio.getClasificacionPredio() != null ? predio.getClasificacionPredio().name() : "CASA_HABITACION")
                .areaTerreno(predio.getAreaTerrenoM2() != null ? predio.getAreaTerrenoM2() : predio.getAreaTerreno())
                .areaConstruida(predio.getAreaConstruida())
                .porcentajeParticipacion(participacion)
                .periodoAnio(periodo.getAnio())
                .periodoId(periodo.getId())
                .tipoCalculo(TipoCalculoArbitrio.SIMULACION)
                .estado(EstadoCalculoArbitrio.SIMULACION)
                .badgeClass(EstadoCalculoArbitrio.SIMULACION.getBadgeClass())
                .fechaCalculo(OffsetDateTime.now())
                .fechaReferencia(request.getFechaReferencia())
                .subperiodo(request.getSubperiodo() != null ? request.getSubperiodo() : "ANUAL")
                .totalServicios(detallesResp.size())
                .importeAnualTotal(importeAnualTotal.setScale(2, RoundingMode.HALF_UP))
                .importePeriodoTotal(importePeriodoTotal.setScale(2, RoundingMode.HALF_UP))
                .moneda("PEN")
                .beneficioAplicado(request.getBeneficio())
                .porcentajeDescuentoBeneficio(request.getBeneficio() != null ? BigDecimal.valueOf(request.getBeneficio().getPorcentajeDescuento()) : BigDecimal.ZERO)
                .montoDescuentoBeneficio(montoDescuentoTotal.setScale(2, RoundingMode.HALF_UP))
                .resolucionBeneficio(request.getResolucionBeneficio())
                .observaciones(request.getObservaciones())
                .usuarioCalculo(usuario != null ? usuario : "SISTEMA")
                .versionCalculo(1)
                .detalles(detallesResp)
                .advertencias(advertencias)
                .explicaciones(explicaciones)
                .build();
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse registrarCalculoPreliminar(CalculoArbitrioCreateRequest request, String usuario) {
        validator.validarCalculoPreliminar(request);

        ContribuyenteTributario cont = contribuyenteRepository.findById(request.getContribuyenteId()).orElseThrow();
        PredioTributario predio = predioRepository.findById(request.getPredioId()).orElseThrow();
        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId()).orElseThrow();

        BigDecimal participacion = CIEN;
        var relOpt = relacionRepository.findByContribuyenteIdAndPredioId(cont.getId(), predio.getId());
        if (relOpt.isPresent() && relOpt.get().getPorcentajeParticipacion() != null) {
            participacion = relOpt.get().getPorcentajeParticipacion();
        }

        List<String> advertencias = new ArrayList<>();
        List<String> explicaciones = new ArrayList<>();
        List<ConfiguracionArbitrioMunicipal> configs = resolverConfiguracionesAplicables(periodo, request.getServiciosIds());

        List<DetalleCalculoArbitrio> detallesEntities = new ArrayList<>();
        BigDecimal importeAnualTotal = BigDecimal.ZERO;
        BigDecimal importePeriodoTotal = BigDecimal.ZERO;
        BigDecimal montoDescuentoTotal = BigDecimal.ZERO;

        for (ConfiguracionArbitrioMunicipal cfg : configs) {
            DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                    predio, cfg, request.getBeneficio(), participacion, advertencias, explicaciones
            );
            detallesEntities.add(det);
            importeAnualTotal = importeAnualTotal.add(det.getImporteFinalAnual());
            importePeriodoTotal = importePeriodoTotal.add(det.getImporteFinalMensual());
            montoDescuentoTotal = montoDescuentoTotal.add(det.getDescuentoAplicado());
        }

        long count = calculoRepository.count();
        String codigoCalc = String.format("DET-ARB-%d-%06d", periodo.getAnio(), count + 1);

        CalculoArbitrioMunicipal calculo = CalculoArbitrioMunicipal.builder()
                .codigoCalculo(codigoCalc)
                .contribuyente(cont)
                .predio(predio)
                .periodo(periodo)
                .configuracion(configs.isEmpty() ? null : configs.get(0))
                .tipoCalculo(request.getTipoCalculo() != null ? request.getTipoCalculo() : TipoCalculoArbitrio.DETERMINACION_INDIVIDUAL)
                .estado(EstadoCalculoArbitrio.PRELIMINAR)
                .fechaCalculo(OffsetDateTime.now())
                .fechaReferencia(request.getFechaReferencia() != null ? request.getFechaReferencia() : LocalDate.now())
                .subperiodo(request.getSubperiodo() != null ? request.getSubperiodo() : "ANUAL")
                .totalServicios(detallesEntities.size())
                .importeAnualTotal(importeAnualTotal.setScale(2, RoundingMode.HALF_UP))
                .importePeriodoTotal(importePeriodoTotal.setScale(2, RoundingMode.HALF_UP))
                .moneda("PEN")
                .beneficioAplicado(request.getBeneficio() != null ? request.getBeneficio() : TipoBeneficioArbitrio.NINGUNO)
                .porcentajeDescuentoBeneficio(request.getBeneficio() != null ? BigDecimal.valueOf(request.getBeneficio().getPorcentajeDescuento()) : BigDecimal.ZERO)
                .montoDescuentoBeneficio(montoDescuentoTotal.setScale(2, RoundingMode.HALF_UP))
                .resolucionBeneficio(request.getResolucionBeneficio())
                .observaciones(request.getObservaciones())
                .usuarioCalculo(usuario != null ? usuario : "SISTEMA")
                .versionCalculo(1)
                .build();

        // Serializar snapshot
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ejercicio", periodo.getAnio());
        snapshot.put("predioCodigo", predio.getCodigoPredio());
        snapshot.put("participacion", participacion);
        snapshot.put("importeAnualTotal", importeAnualTotal);
        snapshot.put("explicaciones", explicaciones);
        try {
            calculo.setSnapshotCalculo(objectMapper.writeValueAsString(snapshot));
        } catch (JsonProcessingException e) {
            log.warn("Error al serializar snapshot de arbitrio: {}", e.getMessage());
        }

        CalculoArbitrioMunicipal guardado = calculoRepository.save(calculo);

        for (DetalleCalculoArbitrio det : detallesEntities) {
            det.setCalculo(guardado);
            detalleRepository.save(det);
        }
        guardado.setDetalles(detallesEntities);

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.CREACION, null, EstadoCalculoArbitrio.PRELIMINAR.name(),
                "Determinación preliminar de arbitrios registrada para revisión técnica.", usuario);

        log.info("Cálculo preliminar de arbitrios registrado: ID={}, Código={}", guardado.getId(), guardado.getCodigoCalculo());
        ArbitrioDetailResponse resp = mapCalculoToDetail(guardado);
        resp.setAdvertencias(advertencias);
        resp.setExplicaciones(explicaciones);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public ArbitrioDetailResponse obtenerCalculoPorId(UUID id) {
        CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo de arbitrio con ID: " + id));
        return mapCalculoToDetail(c);
    }

    @Override
    @Transactional(readOnly = true)
    public ArbitrioDetailResponse obtenerCalculoPorCodigo(String codigo) {
        CalculoArbitrioMunicipal c = calculoRepository.findByCodigoCalculoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo de arbitrio con código: " + codigo));
        return mapCalculoToDetail(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArbitrioSummaryDto> listarCalculos(ArbitrioFilterDto filtro, Pageable pageable) {
        Specification<CalculoArbitrioMunicipal> spec = ArbitrioSpecification.conFiltro(filtro);
        return calculoRepository.findAll(spec, pageable).map(this::mapCalculoToSummary);
    }

    // ==========================================
    // CICLO DE VIDA DEL CÁLCULO
    // ==========================================

    @Override
    @Transactional
    public ArbitrioDetailResponse enviarCalculoARevision(UUID id, String motivo, String usuario) {
        CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarTransicionEstado(c, EstadoCalculoArbitrio.EN_REVISION);

        String ant = c.getEstado().name();
        c.setEstado(EstadoCalculoArbitrio.EN_REVISION);
        CalculoArbitrioMunicipal guardado = calculoRepository.save(c);

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.ENVIO_REVISION, ant, EstadoCalculoArbitrio.EN_REVISION.name(),
                motivo != null ? motivo : "Remisión a fiscalización tributaria para visación.", usuario);

        return mapCalculoToDetail(guardado);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse registrarObservacionesCalculo(UUID id, String motivo, String usuario) {
        CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarTransicionEstado(c, EstadoCalculoArbitrio.OBSERVADO);

        String ant = c.getEstado().name();
        c.setEstado(EstadoCalculoArbitrio.OBSERVADO);
        CalculoArbitrioMunicipal guardado = calculoRepository.save(c);

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.OBSERVADO, ant, EstadoCalculoArbitrio.OBSERVADO.name(),
                motivo != null ? motivo : "Observaciones técnicas registradas sobre la determinación.", usuario);

        return mapCalculoToDetail(guardado);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse aprobarCalculo(UUID id, String resolucionAprobacion, String motivo, String usuario) {
        CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarTransicionEstado(c, EstadoCalculoArbitrio.APROBADO);

        String ant = c.getEstado().name();
        c.setEstado(EstadoCalculoArbitrio.APROBADO);
        c.setUsuarioAprobador(usuario != null ? usuario : "AUTORIDAD");
        c.setFechaAprobacion(OffsetDateTime.now());
        c.setResolucionAprobacion(resolucionAprobacion != null ? resolucionAprobacion : "AUTO-APROBACION-TRIBUTARIA");

        CalculoArbitrioMunicipal guardado = calculoRepository.save(c);

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.APROBACION, ant, EstadoCalculoArbitrio.APROBADO.name(),
                "Aprobación técnica y legal de arbitrios. Acto: " + c.getResolucionAprobacion(), usuario);

        return mapCalculoToDetail(guardado);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse anularCalculo(UUID id, String motivo, String usuario) {
        CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe ingresar un motivo justificado para anular la determinación de arbitrios.");
        }

        validator.validarTransicionEstado(c, EstadoCalculoArbitrio.ANULADO);

        String ant = c.getEstado().name();
        c.setEstado(EstadoCalculoArbitrio.ANULADO);
        c.setMotivoAnulacion(motivo);
        c.setUsuarioAnulador(usuario != null ? usuario : "SISTEMA");
        c.setFechaAnulacion(OffsetDateTime.now());

        CalculoArbitrioMunicipal guardado = calculoRepository.save(c);

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.ANULACION, ant, EstadoCalculoArbitrio.ANULADO.name(),
                "Anulación formal: " + motivo, usuario);

        return mapCalculoToDetail(guardado);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse recalcularNuevaVersion(UUID id, String motivo, String usuario) {
        CalculoArbitrioMunicipal anterior = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo de arbitrio con ID: " + id));

        if (anterior.getEstado() == EstadoCalculoArbitrio.ANULADO) {
            throw new BusinessException("No se puede recalcular sobre una liquidación de arbitrios ANULADA.");
        }

        int nuevaVer = anterior.getVersionCalculo() + 1;
        String nuevoCod = String.format("%s-v%d", anterior.getCodigoCalculo().split("-v")[0], nuevaVer);

        CalculoArbitrioMunicipal nuevo = CalculoArbitrioMunicipal.builder()
                .codigoCalculo(nuevoCod)
                .contribuyente(anterior.getContribuyente())
                .predio(anterior.getPredio())
                .periodo(anterior.getPeriodo())
                .configuracion(anterior.getConfiguracion())
                .tipoCalculo(anterior.getTipoCalculo())
                .estado(EstadoCalculoArbitrio.PRELIMINAR)
                .fechaCalculo(OffsetDateTime.now())
                .fechaReferencia(LocalDate.now())
                .subperiodo(anterior.getSubperiodo())
                .totalServicios(anterior.getTotalServicios())
                .importeAnualTotal(anterior.getImporteAnualTotal())
                .importePeriodoTotal(anterior.getImportePeriodoTotal())
                .moneda(anterior.getMoneda())
                .beneficioAplicado(anterior.getBeneficioAplicado())
                .porcentajeDescuentoBeneficio(anterior.getPorcentajeDescuentoBeneficio())
                .montoDescuentoBeneficio(anterior.getMontoDescuentoBeneficio())
                .resolucionBeneficio(anterior.getResolucionBeneficio())
                .observaciones("Recálculo técnico derivado de " + anterior.getCodigoCalculo() + ". Motivo: " + (motivo != null ? motivo : ""))
                .usuarioCalculo(usuario != null ? usuario : "SISTEMA")
                .versionCalculo(nuevaVer)
                .calculoPredecesor(anterior)
                .build();

        CalculoArbitrioMunicipal guardado = calculoRepository.save(nuevo);

        // Copiar detalles
        if (anterior.getDetalles() != null) {
            for (DetalleCalculoArbitrio dAnt : anterior.getDetalles()) {
                DetalleCalculoArbitrio dNuevo = DetalleCalculoArbitrio.builder()
                        .calculo(guardado)
                        .servicio(dAnt.getServicio())
                        .configuracion(dAnt.getConfiguracion())
                        .codigoServicio(dAnt.getCodigoServicio())
                        .nombreServicio(dAnt.getNombreServicio())
                        .tipoServicio(dAnt.getTipoServicio())
                        .criterioAplicado(dAnt.getCriterioAplicado())
                        .parametroBaseValor(dAnt.getParametroBaseValor())
                        .unidadBase(dAnt.getUnidadBase())
                        .tasaUnitarioMensual(dAnt.getTasaUnitarioMensual())
                        .costoMensualServicio(dAnt.getCostoMensualServicio())
                        .costoAnualServicio(dAnt.getCostoAnualServicio())
                        .descuentoAplicado(dAnt.getDescuentoAplicado())
                        .importeFinalMensual(dAnt.getImporteFinalMensual())
                        .importeFinalAnual(dAnt.getImporteFinalAnual())
                        .explicacionDetalle(dAnt.getExplicacionDetalle())
                        .build();
                detalleRepository.save(dNuevo);
            }
        }

        registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                AccionHistorialArbitrio.RECALCULO, anterior.getEstado().name(), EstadoCalculoArbitrio.PRELIMINAR.name(),
                "Recálculo generado en versión v" + nuevaVer + ". Motivo: " + motivo, usuario);

        return mapCalculoToDetail(guardado);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse aprobarCalculo(UUID id, String usuario) {
        return aprobarCalculo(id, "RESOLUCION-ALCALDIA-ARBITRIOS", "Aprobación técnica y legal de arbitrios.", usuario);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse recalcular(UUID id, String motivo, String usuario) {
        return recalcularNuevaVersion(id, motivo, usuario);
    }

    @Override
    @Transactional
    public ArbitrioDetailResponse cambiarEstado(UUID id, CambioEstadoArbitrioRequest request, String usuario) {
        if (request == null || request.getNuevoEstado() == null) {
            throw new BusinessException("Debe especificar el nuevo estado del cálculo de arbitrios.");
        }
        return switch (request.getNuevoEstado()) {
            case EN_REVISION -> enviarCalculoARevision(id, request.getMotivo(), usuario);
            case OBSERVADO -> registrarObservacionesCalculo(id, request.getMotivo(), usuario);
            case APROBADO -> aprobarCalculo(id, request.getResolucion(), request.getMotivo(), usuario);
            case ANULADO -> anularCalculo(id, request.getMotivo(), usuario);
            default -> {
                CalculoArbitrioMunicipal c = calculoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));
                validator.validarTransicionEstado(c, request.getNuevoEstado());
                String ant = c.getEstado().name();
                c.setEstado(request.getNuevoEstado());
                CalculoArbitrioMunicipal guardado = calculoRepository.save(c);
                registrarHistorial("CALCULO", guardado.getId(), guardado.getCodigoCalculo(),
                        AccionHistorialArbitrio.MODIFICACION, ant, request.getNuevoEstado().name(),
                        request.getMotivo(), usuario);
                yield mapCalculoToDetail(guardado);
            }
        };
    }

    // ==========================================
    // HISTORIAL Y ESTADÍSTICAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<HistorialArbitrioResponse> obtenerHistorial(UUID calculoId) {
        return historialRepository.findByEntidadIdOrderByFechaRegistroDesc(calculoId)
                .stream().map(this::mapHistorialToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalServicios", servicioRepository.count());
        stats.put("configuracionesActivas", configuracionRepository.countByEstado(EstadoConfiguracionArbitrio.ACTIVO));
        stats.put("configuracionesPendientes", configuracionRepository.countByEstado(EstadoConfiguracionArbitrio.BORRADOR)
                + configuracionRepository.countByEstado(EstadoConfiguracionArbitrio.EN_REVISION));
        stats.put("calculosPreliminares", calculoRepository.countByEstado(EstadoCalculoArbitrio.PRELIMINAR));
        stats.put("calculosEnRevision", calculoRepository.countByEstado(EstadoCalculoArbitrio.EN_REVISION));
        stats.put("calculosAprobados", calculoRepository.countByEstado(EstadoCalculoArbitrio.APROBADO));
        stats.put("totalCalculos", calculoRepository.count());
        stats.put("aprobados", stats.get("calculosAprobados"));
        stats.put("preliminares", stats.get("calculosPreliminares"));
        return stats;
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y MAPEOS
    // ==========================================

    private List<ConfiguracionArbitrioMunicipal> resolverConfiguracionesAplicables(PeriodoTributario periodo, List<UUID> serviciosIds) {
        List<ConfiguracionArbitrioMunicipal> configs = configuracionRepository.findActivasPorAnio(periodo.getAnio());

        if (configs.isEmpty()) {
            configs = configuracionRepository.findByPeriodoId(periodo.getId());
        }

        // Si todavía está vacío, sintetizar configuraciones a partir del catálogo base de servicios
        if (configs.isEmpty()) {
            List<ServicioArbitrioMunicipal> servicios = servicioRepository.findByEstadoOrderByNombreAsc(EstadoServicioArbitrio.ACTIVO);
            for (ServicioArbitrioMunicipal s : servicios) {
                ConfiguracionArbitrioMunicipal dummy = ConfiguracionArbitrioMunicipal.builder()
                        .servicio(s)
                        .periodo(periodo)
                        .versionConfiguracion(1)
                        .metodologiaDistribucion(switch (s.getTipoServicio()) {
                            case BARRIDO_CALLES -> MetodoDistribucionArbitrio.FRENTE_LINEAL;
                            case PARQUES_JARDINES -> MetodoDistribucionArbitrio.UBICACION_ZONAL;
                            case SERENAZGO -> MetodoDistribucionArbitrio.RIESGO_SECTOR;
                            default -> MetodoDistribucionArbitrio.USO_PREDIO_ACTIVIDAD;
                        })
                        .costoTotalAnual(new BigDecimal("12000.00"))
                        .costoTotalMensual(new BigDecimal("1000.00"))
                        .cantidadPrediosAfectos(150)
                        .estado(EstadoConfiguracionArbitrio.ACTIVO)
                        .fechaInicioVigencia(LocalDate.of(periodo.getAnio(), 1, 1))
                        .build();
                configs.add(dummy);
            }
        }

        if (serviciosIds != null && !serviciosIds.isEmpty()) {
            configs = configs.stream().filter(c -> c.getServicio() != null && serviciosIds.contains(c.getServicio().getId())).collect(Collectors.toList());
        }

        return configs;
    }

    private void registrarHistorial(String tipo, UUID entidadId, String codigo, AccionHistorialArbitrio accion,
                                    String ant, String nuevo, String motivo, String usuario) {
        HistorialArbitrioMunicipal h = HistorialArbitrioMunicipal.builder()
                .entidadTipo(tipo)
                .entidadId(entidadId)
                .codigoReferencia(codigo)
                .accion(accion)
                .estadoAnterior(ant)
                .nuevoEstado(nuevo)
                .motivo(motivo)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(h);
    }

    private ConfiguracionArbitrioSummaryDto mapConfiguracionToSummary(ConfiguracionArbitrioMunicipal c) {
        return ConfiguracionArbitrioSummaryDto.builder()
                .id(c.getId())
                .codigoConfiguracion(c.getCodigoConfiguracion())
                .servicioId(c.getServicio().getId())
                .servicioNombre(c.getServicio().getNombre())
                .servicioCodigo(c.getServicio().getCodigo())
                .tipoServicio(c.getServicio().getTipoServicio())
                .periodoAnio(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 2026)
                .normaNumero(c.getNorma() != null ? c.getNorma().getNumeroNorma() : "-")
                .versionConfiguracion(c.getVersionConfiguracion())
                .fechaInicioVigencia(c.getFechaInicioVigencia())
                .fechaFinVigencia(c.getFechaFinVigencia())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .costoTotalAnual(c.getCostoTotalAnual())
                .costoTotalMensual(c.getCostoTotalMensual())
                .metodologiaDistribucion(c.getMetodologiaDistribucion())
                .cantidadPrediosAfectos(c.getCantidadPrediosAfectos())
                .build();
    }

    private ConfiguracionArbitrioDetailResponse mapConfiguracionToDetail(ConfiguracionArbitrioMunicipal c) {
        List<CostoArbitrioResponse> costos = c.getCostos() != null ? c.getCostos().stream().map(cost -> CostoArbitrioResponse.builder()
                .id(cost.getId())
                .categoriaCosto(cost.getCategoriaCosto())
                .categoriaDescripcion(cost.getCategoriaCosto().getDescripcion())
                .descripcion(cost.getDescripcion())
                .importe(cost.getImporte())
                .moneda(cost.getMoneda())
                .periodoCosto(cost.getPeriodoCosto())
                .documentoSustentoRef(cost.getDocumentoSustentoRef())
                .fuenteSustento(cost.getFuenteSustento())
                .estadoValidacion(cost.getEstadoValidacion())
                .estadoBadgeClass(cost.getEstadoValidacion() != null ? cost.getEstadoValidacion().getBadgeClass() : "")
                .observaciones(cost.getObservaciones())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        List<CriterioArbitrioResponse> criterios = c.getCriterios() != null ? c.getCriterios().stream().map(crit -> CriterioArbitrioResponse.builder()
                .id(crit.getId())
                .codigo(crit.getCodigo())
                .nombre(crit.getNombre())
                .tipoDato(crit.getTipoDato())
                .unidad(crit.getUnidad())
                .metodoDistribucion(crit.getMetodoDistribucion())
                .metodoDescripcion(crit.getMetodoDistribucion() != null ? crit.getMetodoDistribucion().getDescripcion() : "-")
                .ponderacionPorcentaje(crit.getPonderacionPorcentaje())
                .factorMinimo(crit.getFactorMinimo())
                .factorMaximo(crit.getFactorMaximo())
                .fuenteNormativa(crit.getFuenteNormativa())
                .estado(crit.getEstado())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        List<HistorialArbitrioResponse> historial = historialRepository.findByEntidadIdOrderByFechaRegistroDesc(c.getId())
                .stream().map(this::mapHistorialToResponse).collect(Collectors.toList());

        return ConfiguracionArbitrioDetailResponse.builder()
                .id(c.getId())
                .codigoConfiguracion(c.getCodigoConfiguracion())
                .servicioId(c.getServicio().getId())
                .servicioCodigo(c.getServicio().getCodigo())
                .servicioNombre(c.getServicio().getNombre())
                .tipoServicio(c.getServicio().getTipoServicio())
                .periodoId(c.getPeriodo().getId())
                .periodoAnio(c.getPeriodo().getAnio())
                .normaId(c.getNorma() != null ? c.getNorma().getId() : null)
                .normaNumero(c.getNorma() != null ? c.getNorma().getNumeroNorma() : "-")
                .normaDescripcion(c.getNorma() != null ? c.getNorma().getDescripcion() : null)
                .sectorId(c.getSector() != null ? c.getSector().getId() : null)
                .sectorNombre(c.getSector() != null ? c.getSector().getNombre() : null)
                .zona(c.getZona())
                .versionConfiguracion(c.getVersionConfiguracion())
                .configuracionPredecesoraId(c.getConfiguracionPredecesora() != null ? c.getConfiguracionPredecesora().getId() : null)
                .fechaInicioVigencia(c.getFechaInicioVigencia())
                .fechaFinVigencia(c.getFechaFinVigencia())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .costoTotalAnual(c.getCostoTotalAnual())
                .costoTotalMensual(c.getCostoTotalMensual())
                .metodologiaDistribucion(c.getMetodologiaDistribucion())
                .metodologiaDescripcion(c.getMetodologiaDistribucion() != null ? c.getMetodologiaDistribucion().getDescripcion() : "-")
                .cantidadPrediosAfectos(c.getCantidadPrediosAfectos())
                .descripcionJustificacion(c.getDescripcionJustificacion())
                .usuarioAprobador(c.getUsuarioAprobador())
                .fechaAprobacion(c.getFechaAprobacion())
                .resolucionAprobacion(c.getResolucionAprobacion())
                .usuarioActivador(c.getUsuarioActivador())
                .fechaActivacion(c.getFechaActivacion())
                .motivoDesactivacion(c.getMotivoDesactivacion())
                .costos(costos)
                .criterios(criterios)
                .historial(historial)
                .build();
    }

    private ArbitrioSummaryDto mapCalculoToSummary(CalculoArbitrioMunicipal c) {
        return ArbitrioSummaryDto.builder()
                .id(c.getId())
                .codigoCalculo(c.getCodigoCalculo())
                .contribuyenteId(c.getContribuyente().getId())
                .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                .predioId(c.getPredio().getId())
                .predioCodigo(c.getPredio().getCodigoPredio())
                .predioDireccion(c.getPredio().getDireccion())
                .periodoAnio(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 2026)
                .tipoCalculo(c.getTipoCalculo())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .totalServicios(c.getTotalServicios())
                .importeAnualTotal(c.getImporteAnualTotal())
                .importePeriodoTotal(c.getImportePeriodoTotal())
                .moneda(c.getMoneda())
                .fechaCalculo(c.getFechaCalculo())
                .versionCalculo(c.getVersionCalculo())
                .build();
    }

    private ArbitrioDetailResponse mapCalculoToDetail(CalculoArbitrioMunicipal c) {
        List<DetalleCalculoArbitrio> detallesList = (c.getDetalles() != null && !c.getDetalles().isEmpty())
                ? c.getDetalles()
                : detalleRepository.findByCalculoId(c.getId());

        List<DetalleCalculoArbitrioResponse> detalles = detallesList.stream()
                .map(this::mapDetalleToResponse).collect(Collectors.toList());

        List<HistorialArbitrioResponse> historial = historialRepository.findByEntidadIdOrderByFechaRegistroDesc(c.getId())
                .stream().map(this::mapHistorialToResponse).collect(Collectors.toList());

        return ArbitrioDetailResponse.builder()
                .id(c.getId())
                .codigoCalculo(c.getCodigoCalculo())
                .contribuyenteId(c.getContribuyente().getId())
                .contribuyenteCodigo(c.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                .predioId(c.getPredio().getId())
                .predioCodigo(c.getPredio().getCodigoPredio())
                .predioDireccion(c.getPredio().getDireccion())
                .tipoPredio(c.getPredio().getTipoPredio() != null ? c.getPredio().getTipoPredio().name() : "URBANO")
                .clasificacionPredio(c.getPredio().getClasificacionPredio() != null ? c.getPredio().getClasificacionPredio().name() : "CASA_HABITACION")
                .areaTerreno(c.getPredio().getAreaTerrenoM2() != null ? c.getPredio().getAreaTerrenoM2() : c.getPredio().getAreaTerreno())
                .areaConstruida(c.getPredio().getAreaConstruida())
                .porcentajeParticipacion(CIEN)
                .periodoAnio(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 2026)
                .periodoId(c.getPeriodo() != null ? c.getPeriodo().getId() : null)
                .tipoCalculo(c.getTipoCalculo())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .fechaCalculo(c.getFechaCalculo())
                .fechaReferencia(c.getFechaReferencia())
                .subperiodo(c.getSubperiodo())
                .totalServicios(c.getTotalServicios())
                .importeAnualTotal(c.getImporteAnualTotal())
                .importePeriodoTotal(c.getImportePeriodoTotal())
                .moneda(c.getMoneda())
                .beneficioAplicado(c.getBeneficioAplicado())
                .porcentajeDescuentoBeneficio(c.getPorcentajeDescuentoBeneficio())
                .montoDescuentoBeneficio(c.getMontoDescuentoBeneficio())
                .resolucionBeneficio(c.getResolucionBeneficio())
                .observaciones(c.getObservaciones())
                .resolucionAprobacion(c.getResolucionAprobacion())
                .motivoAnulacion(c.getMotivoAnulacion())
                .usuarioCalculo(c.getUsuarioCalculo())
                .usuarioAprobador(c.getUsuarioAprobador())
                .fechaAprobacion(c.getFechaAprobacion())
                .versionCalculo(c.getVersionCalculo())
                .calculoPredecesorId(c.getCalculoPredecesor() != null ? c.getCalculoPredecesor().getId() : null)
                .detalles(detalles)
                .historial(historial)
                .build();
    }

    private DetalleCalculoArbitrioResponse mapDetalleToResponse(DetalleCalculoArbitrio d) {
        return DetalleCalculoArbitrioResponse.builder()
                .id(d.getId())
                .servicioId(d.getServicio() != null ? d.getServicio().getId() : null)
                .codigoServicio(d.getCodigoServicio())
                .nombreServicio(d.getNombreServicio())
                .tipoServicio(d.getTipoServicio())
                .criterioAplicado(d.getCriterioAplicado())
                .parametroBaseValor(d.getParametroBaseValor())
                .unidadBase(d.getUnidadBase())
                .tasaUnitarioMensual(d.getTasaUnitarioMensual())
                .costoMensualServicio(d.getCostoMensualServicio())
                .costoAnualServicio(d.getCostoAnualServicio())
                .descuentoAplicado(d.getDescuentoAplicado())
                .importeFinalMensual(d.getImporteFinalMensual())
                .importeFinalAnual(d.getImporteFinalAnual())
                .explicacionDetalle(d.getExplicacionDetalle())
                .build();
    }

    private HistorialArbitrioResponse mapHistorialToResponse(HistorialArbitrioMunicipal h) {
        return HistorialArbitrioResponse.builder()
                .id(h.getId())
                .entidadTipo(h.getEntidadTipo())
                .entidadId(h.getEntidadId())
                .codigoReferencia(h.getCodigoReferencia())
                .accion(h.getAccion())
                .accionDescripcion(h.getAccion() != null ? h.getAccion().getDescripcion() : "-")
                .motivo(h.getMotivo())
                .usuarioRegistro(h.getUsuarioRegistro())
                .fechaRegistro(h.getFechaRegistro())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionArbitrioSummaryDto> listarConfiguracionesActivas() {
        return configuracionRepository.findActivasPorAnio(LocalDate.now().getYear())
                .stream().map(this::mapConfiguracionToSummary).toList();
    }
}

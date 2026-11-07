package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CalculoPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ValorizacionPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.CalculoPredialSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.CalculoPredialValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculoPredialServiceImpl implements CalculoPredialService {

    private final CalculoImpuestoPredialRepository calculoRepository;
    private final CalculoPredialDetalleRepository detalleRepository;
    private final CalculoPredialTramoRepository tramoRepository;
    private final HistorialCalculoPredialRepository historialRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final ConfiguracionConceptoTributarioRepository configuracionRepository;
    private final ConceptoTributarioRepository conceptoRepository;
    private final RelacionContribuyentePredioRepository relacionRepository;
    private final ValorizacionPredialService valorizacionService;
    private final CalculoPredialValidator validator;
    private final ObjectMapper objectMapper;

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal TASA_TRAMO_1 = new BigDecimal("0.20"); // 0.2%
    private static final BigDecimal TASA_TRAMO_2 = new BigDecimal("0.60"); // 0.6%
    private static final BigDecimal TASA_TRAMO_3 = new BigDecimal("1.00"); // 1.0%
    private static final BigDecimal FACTOR_MINIMO_LEGAL = new BigDecimal("0.006"); // 0.6% de la UIT

    @Override
    @Transactional(readOnly = true)
    public CalculoPredialDetailResponse simularCalculo(SimulacionPredialRequest request, String usuario) {
        log.info("Ejecutando simulación de impuesto predial para contribuyente ID: {}, período ID: {}",
                request.getContribuyenteId(), request.getPeriodoId());

        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + request.getContribuyenteId()));

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el período fiscal con ID: " + request.getPeriodoId()));

        List<RelacionContribuyentePredio> relaciones = obtenerRelacionesPredialesValidas(contribuyente.getId(), request.getPrediosSeleccionadosIds());
        validator.validarSimulacion(request, contribuyente, periodo, relaciones);

        ConfiguracionConceptoTributario config = resolverConfiguracionPredial(request.getConfiguracionId(), periodo.getId());

        List<String> advertencias = new ArrayList<>();
        List<String> explicaciones = new ArrayList<>();

        LocalDate fechaRef = request.getFechaReferencia() != null ? request.getFechaReferencia() : LocalDate.of(periodo.getAnio(), 1, 1);
        explicaciones.add(String.format("Fecha de referencia legal para la determinación: %s (Ejercicio Fiscal %d)", fechaRef, periodo.getAnio()));
        explicaciones.add(String.format("UIT oficial aplicable para el ejercicio: S/ %s (D.S. publicado por el MEF)", periodo.getUitVigente()));

        // 1. Valorización predial de cada predio
        List<CalculoPredialDetalle> detalles = new ArrayList<>();
        BigDecimal totalAutovaluoBruto = BigDecimal.ZERO;
        BigDecimal totalAutovaluoImputable = BigDecimal.ZERO;

        for (RelacionContribuyentePredio rel : relaciones) {
            CalculoPredialDetalle det = valorizacionService.valorizarPredio(
                    rel.getPredio(),
                    rel.getPorcentajeParticipacion(),
                    periodo.getAnio(),
                    periodo.getUitVigente(),
                    config,
                    advertencias,
                    explicaciones
            );
            detalles.add(det);
            totalAutovaluoBruto = totalAutovaluoBruto.add(det.getValorAutovaluoTotal());
            totalAutovaluoImputable = totalAutovaluoImputable.add(det.getValorAutovaluoImputable());
        }

        // 2. Beneficios / Deducciones
        TipoBeneficioPredial beneficio = request.getBeneficio() != null ? request.getBeneficio() : TipoBeneficioPredial.NINGUNO;
        BigDecimal totalDeducciones = BigDecimal.ZERO;

        if (beneficio == TipoBeneficioPredial.PENSIONISTA_ADULTO_MAYOR_50_UIT) {
            BigDecimal topeDeduccion50Uit = periodo.getUitVigente().multiply(new BigDecimal("50.00")).setScale(2, RoundingMode.HALF_UP);
            totalDeducciones = totalAutovaluoImputable.min(topeDeduccion50Uit);
            explicaciones.add(String.format("Beneficio Art. 19 LTM (Pensionista/Adulto Mayor): Deducción de hasta 50 UIT (S/ %s). Deducción aplicada: S/ %s",
                    topeDeduccion50Uit, totalDeducciones));
        } else if (beneficio == TipoBeneficioPredial.INAFECTACION_ESTATAL || beneficio == TipoBeneficioPredial.EXONERACION_ESPECIAL) {
            totalDeducciones = totalAutovaluoImputable;
            explicaciones.add(String.format("Inafectación / Exoneración aplicada al 100%%: Deducción de S/ %s", totalDeducciones));
        }

        BigDecimal baseImponibleNeta = totalAutovaluoImputable.subtract(totalDeducciones).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        explicaciones.add(String.format("Base Imponible Neta Afecta: S/ %s", baseImponibleNeta));

        // 3. Tramos progresivos acumulativos (Art. 13 LTM)
        List<CalculoPredialTramo> tramos = calcularTramosProgresivos(baseImponibleNeta, periodo.getUitVigente(), explicaciones);
        BigDecimal impuestoTramos = tramos.stream()
                .map(CalculoPredialTramo::getImpuestoTramo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Impuesto mínimo legal (0.6% UIT)
        BigDecimal impuestoMinimoLegal = periodo.getUitVigente().multiply(FACTOR_MINIMO_LEGAL).setScale(2, RoundingMode.HALF_UP);
        boolean aplicaMinimo = false;
        BigDecimal impuestoFinal;

        if (baseImponibleNeta.compareTo(BigDecimal.ZERO) == 0) {
            impuestoFinal = BigDecimal.ZERO;
            explicaciones.add("Base imponible es S/ 0.00 debido a inafectación o deducción total. Impuesto anual determinado: S/ 0.00.");
        } else if (impuestoTramos.compareTo(impuestoMinimoLegal) < 0) {
            aplicaMinimo = true;
            impuestoFinal = impuestoMinimoLegal;
            explicaciones.add(String.format("Impuesto por tramos (S/ %s) inferior al mínimo legal del 0.6%% de la UIT (S/ %s). Aplica impuesto mínimo legal.",
                    impuestoTramos, impuestoMinimoLegal));
        } else {
            impuestoFinal = impuestoTramos;
            explicaciones.add(String.format("Impuesto anual determinado por tramos progresivos: S/ %s", impuestoFinal));
        }

        // Construir objeto transitorio
        CalculoImpuestoPredial calculoTransient = CalculoImpuestoPredial.builder()
                .id(UUID.randomUUID())
                .codigoCalculo("SIM-" + periodo.getAnio() + "-" + System.currentTimeMillis() % 1000000)
                .contribuyente(contribuyente)
                .periodo(periodo)
                .configuracion(config)
                .norma(config != null ? config.getNorma() : null)
                .tipoCalculo(TipoCalculoPredial.SIMULACION)
                .estado(EstadoCalculoPredial.SIMULACION)
                .fechaCalculo(OffsetDateTime.now())
                .fechaReferencia(fechaRef)
                .uitAplicada(periodo.getUitVigente())
                .cantidadPredios(detalles.size())
                .totalAutovaluoBruto(totalAutovaluoBruto)
                .totalDeduccionesBeneficios(totalDeducciones)
                .baseImponibleNeta(baseImponibleNeta)
                .impuestoCalculadoTramos(impuestoTramos)
                .impuestoMinimoLegal(impuestoMinimoLegal)
                .aplicaImpuestoMinimo(aplicaMinimo)
                .impuestoAnualDeterminado(impuestoFinal)
                .moneda("PEN")
                .beneficioAplicado(beneficio)
                .resolucionBeneficio(request.getResolucionBeneficio())
                .observaciones(request.getObservaciones())
                .usuarioCalculo(usuario != null ? usuario : "SISTEMA")
                .versionCalculo(1)
                .prediosDetalle(detalles)
                .tramosDetalle(tramos)
                .build();

        CalculoPredialDetailResponse resp = mapToDetailResponse(calculoTransient);
        resp.setAdvertencias(advertencias);
        resp.setExplicacionPasoAPaso(explicaciones);
        return resp;
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse registrarCalculo(CalculoPredialCreateRequest request, String usuario) {
        log.info("Registrando determinación formal de impuesto predial para contribuyente: {}", request.getContribuyenteId());

        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + request.getContribuyenteId()));

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el período fiscal con ID: " + request.getPeriodoId()));

        List<RelacionContribuyentePredio> relaciones = obtenerRelacionesPredialesValidas(contribuyente.getId(), request.getPrediosSeleccionadosIds());
        validator.validarRegistroCalculo(request, contribuyente, periodo, relaciones);

        ConfiguracionConceptoTributario config = resolverConfiguracionPredial(request.getConfiguracionId(), periodo.getId());

        List<String> advertencias = new ArrayList<>();
        List<String> explicaciones = new ArrayList<>();

        LocalDate fechaRef = request.getFechaReferencia() != null ? request.getFechaReferencia() : LocalDate.of(periodo.getAnio(), 1, 1);

        // 1. Valorización
        List<CalculoPredialDetalle> detalles = new ArrayList<>();
        BigDecimal totalAutovaluoBruto = BigDecimal.ZERO;
        BigDecimal totalAutovaluoImputable = BigDecimal.ZERO;

        for (RelacionContribuyentePredio rel : relaciones) {
            CalculoPredialDetalle det = valorizacionService.valorizarPredio(
                    rel.getPredio(),
                    rel.getPorcentajeParticipacion(),
                    periodo.getAnio(),
                    periodo.getUitVigente(),
                    config,
                    advertencias,
                    explicaciones
            );
            detalles.add(det);
            totalAutovaluoBruto = totalAutovaluoBruto.add(det.getValorAutovaluoTotal());
            totalAutovaluoImputable = totalAutovaluoImputable.add(det.getValorAutovaluoImputable());
        }

        // 2. Beneficios
        TipoBeneficioPredial beneficio = request.getBeneficio() != null ? request.getBeneficio() : TipoBeneficioPredial.NINGUNO;
        BigDecimal totalDeducciones = BigDecimal.ZERO;

        if (beneficio == TipoBeneficioPredial.PENSIONISTA_ADULTO_MAYOR_50_UIT) {
            BigDecimal topeDeduccion50Uit = periodo.getUitVigente().multiply(new BigDecimal("50.00")).setScale(2, RoundingMode.HALF_UP);
            totalDeducciones = totalAutovaluoImputable.min(topeDeduccion50Uit);
        } else if (beneficio == TipoBeneficioPredial.INAFECTACION_ESTATAL || beneficio == TipoBeneficioPredial.EXONERACION_ESPECIAL) {
            totalDeducciones = totalAutovaluoImputable;
        }

        BigDecimal baseImponibleNeta = totalAutovaluoImputable.subtract(totalDeducciones).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // 3. Tramos
        List<CalculoPredialTramo> tramos = calcularTramosProgresivos(baseImponibleNeta, periodo.getUitVigente(), explicaciones);
        BigDecimal impuestoTramos = tramos.stream()
                .map(CalculoPredialTramo::getImpuestoTramo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Mínimo legal
        BigDecimal impuestoMinimoLegal = periodo.getUitVigente().multiply(FACTOR_MINIMO_LEGAL).setScale(2, RoundingMode.HALF_UP);
        boolean aplicaMinimo = false;
        BigDecimal impuestoFinal;

        if (baseImponibleNeta.compareTo(BigDecimal.ZERO) == 0) {
            impuestoFinal = BigDecimal.ZERO;
        } else if (impuestoTramos.compareTo(impuestoMinimoLegal) < 0) {
            aplicaMinimo = true;
            impuestoFinal = impuestoMinimoLegal;
        } else {
            impuestoFinal = impuestoTramos;
        }

        // Código correlativo
        String codigoCalculo = generarCodigoCalculo(periodo.getAnio());

        CalculoImpuestoPredial calculo = CalculoImpuestoPredial.builder()
                .codigoCalculo(codigoCalculo)
                .contribuyente(contribuyente)
                .periodo(periodo)
                .configuracion(config)
                .norma(config != null ? config.getNorma() : null)
                .tipoCalculo(request.getTipoCalculo() != null ? request.getTipoCalculo() : TipoCalculoPredial.DETERMINACION_INDIVIDUAL)
                .estado(EstadoCalculoPredial.PRELIMINAR)
                .fechaCalculo(OffsetDateTime.now())
                .fechaReferencia(fechaRef)
                .uitAplicada(periodo.getUitVigente())
                .cantidadPredios(detalles.size())
                .totalAutovaluoBruto(totalAutovaluoBruto)
                .totalDeduccionesBeneficios(totalDeducciones)
                .baseImponibleNeta(baseImponibleNeta)
                .impuestoCalculadoTramos(impuestoTramos)
                .impuestoMinimoLegal(impuestoMinimoLegal)
                .aplicaImpuestoMinimo(aplicaMinimo)
                .impuestoAnualDeterminado(impuestoFinal)
                .moneda("PEN")
                .beneficioAplicado(beneficio)
                .resolucionBeneficio(request.getResolucionBeneficio())
                .observaciones(request.getObservaciones())
                .usuarioCalculo(usuario != null ? usuario : "SISTEMA")
                .versionCalculo(1)
                .build();

        // Snapshot JSON
        Map<String, Object> snapshotMap = new LinkedHashMap<>();
        snapshotMap.put("uit", periodo.getUitVigente());
        snapshotMap.put("cantidadPredios", detalles.size());
        snapshotMap.put("totalAutovaluoBruto", totalAutovaluoBruto);
        snapshotMap.put("baseImponibleNeta", baseImponibleNeta);
        snapshotMap.put("impuestoDeterminado", impuestoFinal);
        snapshotMap.put("explicaciones", explicaciones);
        try {
            calculo.setSnapshotCalculo(objectMapper.writeValueAsString(snapshotMap));
        } catch (JsonProcessingException e) {
            log.warn("No se pudo serializar snapshot del cálculo predial: {}", e.getMessage());
        }

        CalculoImpuestoPredial guardado = calculoRepository.save(calculo);

        // Asociar y guardar detalles
        for (CalculoPredialDetalle det : detalles) {
            det.setCalculo(guardado);
            detalleRepository.save(det);
        }

        // Asociar y guardar tramos
        for (CalculoPredialTramo tramo : tramos) {
            tramo.setCalculo(guardado);
            tramoRepository.save(tramo);
        }

        // Historial
        HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                .calculo(guardado)
                .accion(AccionHistorialCalculoPredial.GUARDADO_PRELIMINAR)
                .estadoAnterior(null)
                .nuevoEstado(EstadoCalculoPredial.PRELIMINAR)
                .motivo("Determinación técnica preliminar registrada en el sistema.")
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        log.info("Cálculo predial guardado exitosamente con ID: {}, Código: {}", guardado.getId(), guardado.getCodigoCalculo());

        CalculoPredialDetailResponse resp = mapToDetailResponse(guardado);
        resp.setAdvertencias(advertencias);
        resp.setExplicacionPasoAPaso(explicaciones);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public CalculoPredialDetailResponse obtenerCalculoPorId(UUID id) {
        CalculoImpuestoPredial calculo = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo predial con ID: " + id));
        return mapToDetailResponse(calculo);
    }

    @Override
    @Transactional(readOnly = true)
    public CalculoPredialDetailResponse obtenerCalculoPorCodigo(String codigo) {
        CalculoImpuestoPredial calculo = calculoRepository.findByCodigoCalculoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo predial con código: " + codigo));
        return mapToDetailResponse(calculo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CalculoPredialSummaryDto> listarCalculos(CalculoPredialFilterDto filtro, Pageable pageable) {
        return calculoRepository.findAll(CalculoPredialSpecification.conFiltro(filtro), pageable)
                .map(this::mapToSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalculoPredialSummaryDto> listarTodosCalculos(CalculoPredialFilterDto filtro) {
        return calculoRepository.findAll(CalculoPredialSpecification.conFiltro(filtro))
                .stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse enviarARevision(UUID id, String motivo, String usuario) {
        CalculoImpuestoPredial calculo = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarParaRevision(calculo);

        EstadoCalculoPredial anterior = calculo.getEstado();
        calculo.setEstado(EstadoCalculoPredial.EN_REVISION);
        CalculoImpuestoPredial guardado = calculoRepository.save(calculo);

        HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                .calculo(guardado)
                .accion(AccionHistorialCalculoPredial.ENVIO_A_REVISION)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoCalculoPredial.EN_REVISION)
                .motivo(motivo != null && !motivo.trim().isEmpty() ? motivo : "Enviado a revisión técnica y fiscalización municipal.")
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse registrarObservaciones(UUID id, String observaciones, String usuario) {
        CalculoImpuestoPredial calculo = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        if (calculo.getEstado() != EstadoCalculoPredial.EN_REVISION) {
            throw new BusinessException("Solo se pueden asentar observaciones a cálculos que se encuentren en estado EN_REVISION.");
        }

        EstadoCalculoPredial anterior = calculo.getEstado();
        calculo.setEstado(EstadoCalculoPredial.OBSERVADO);
        calculo.setObservaciones(observaciones);
        CalculoImpuestoPredial guardado = calculoRepository.save(calculo);

        HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                .calculo(guardado)
                .accion(AccionHistorialCalculoPredial.REGISTRO_OBSERVACIONES)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoCalculoPredial.OBSERVADO)
                .motivo(observaciones)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse aprobarCalculo(UUID id, String resolucion, String motivo, String usuario) {
        CalculoImpuestoPredial calculo = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarParaAprobacion(calculo, resolucion);

        EstadoCalculoPredial anterior = calculo.getEstado();
        calculo.setEstado(EstadoCalculoPredial.APROBADO);
        calculo.setResolucionAprobacion(resolucion.trim());
        calculo.setUsuarioAprobador(usuario != null ? usuario : "SISTEMA");
        calculo.setFechaAprobacion(OffsetDateTime.now());

        CalculoImpuestoPredial guardado = calculoRepository.save(calculo);

        HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                .calculo(guardado)
                .accion(AccionHistorialCalculoPredial.APROBACION_CALCULO)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoCalculoPredial.APROBADO)
                .motivo(motivo != null && !motivo.trim().isEmpty() ? motivo : "Aprobación formal de la determinación del impuesto predial.")
                .resolucionSustento(resolucion.trim())
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        log.info("Cálculo predial ID {} formalmente APROBADO según {}", id, resolucion);
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse anularCalculo(UUID id, String motivo, String usuario) {
        CalculoImpuestoPredial calculo = calculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo con ID: " + id));

        validator.validarParaAnulacion(calculo, motivo);

        EstadoCalculoPredial anterior = calculo.getEstado();
        calculo.setEstado(EstadoCalculoPredial.ANULADO);
        calculo.setMotivoAnulacion(motivo.trim());
        calculo.setUsuarioAnulador(usuario != null ? usuario : "SISTEMA");
        calculo.setFechaAnulacion(OffsetDateTime.now());

        CalculoImpuestoPredial guardado = calculoRepository.save(calculo);

        HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                .calculo(guardado)
                .accion(AccionHistorialCalculoPredial.ANULACION_CALCULO)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoCalculoPredial.ANULADO)
                .motivo(motivo.trim())
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public CalculoPredialDetailResponse recalcularNuevaVersion(UUID calculoOriginalId, String motivo, String usuario) {
        CalculoImpuestoPredial original = calculoRepository.findById(calculoOriginalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cálculo original con ID: " + calculoOriginalId));

        CalculoPredialCreateRequest request = CalculoPredialCreateRequest.builder()
                .contribuyenteId(original.getContribuyente().getId())
                .periodoId(original.getPeriodo().getId())
                .configuracionId(original.getConfiguracion() != null ? original.getConfiguracion().getId() : null)
                .tipoCalculo(TipoCalculoPredial.RECTIFICATORIA)
                .fechaReferencia(original.getFechaReferencia())
                .beneficio(original.getBeneficioAplicado())
                .resolucionBeneficio(original.getResolucionBeneficio())
                .observaciones("Nueva versión por recálculo de " + original.getCodigoCalculo() + ". Motivo: " + motivo)
                .build();

        CalculoPredialDetailResponse nuevoResp = registrarCalculo(request, usuario);

        // Enlazar versión y predecesor
        CalculoImpuestoPredial nuevo = calculoRepository.findById(nuevoResp.getId()).orElse(null);
        if (nuevo != null) {
            nuevo.setCalculoPredecesor(original);
            nuevo.setVersionCalculo(original.getVersionCalculo() + 1);
            calculoRepository.save(nuevo);

            HistorialCalculoPredial hist = HistorialCalculoPredial.builder()
                    .calculo(nuevo)
                    .accion(AccionHistorialCalculoPredial.RECALCULO_NUEVA_VERSION)
                    .estadoAnterior(null)
                    .nuevoEstado(nuevo.getEstado())
                    .motivo("Recálculo a versión v" + nuevo.getVersionCalculo() + " fundamentado: " + motivo)
                    .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                    .fechaRegistro(OffsetDateTime.now())
                    .build();
            historialRepository.save(hist);
            return mapToDetailResponse(nuevo);
        }

        return nuevoResp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCalculoResponse> obtenerHistorial(UUID calculoId) {
        return historialRepository.findByCalculoIdOrderByFechaRegistroDesc(calculoId)
                .stream()
                .map(this::mapHistorialToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCalculos", calculoRepository.count());
        stats.put("totalSimulaciones", calculoRepository.countByTipoCalculo(TipoCalculoPredial.SIMULACION));
        stats.put("calculosPreliminares", calculoRepository.countByEstado(EstadoCalculoPredial.PRELIMINAR));
        stats.put("calculosEnRevision", calculoRepository.countByEstado(EstadoCalculoPredial.EN_REVISION));
        stats.put("calculosAprobados", calculoRepository.countByEstado(EstadoCalculoPredial.APROBADO));
        stats.put("totalMontoAprobado", calculoRepository.sumTotalImpuestoPorEstado(EstadoCalculoPredial.APROBADO));
        stats.put("totalBaseAprobada", calculoRepository.sumTotalBaseImponiblePorEstado(EstadoCalculoPredial.APROBADO));
        return stats;
    }

    private List<CalculoPredialTramo> calcularTramosProgresivos(BigDecimal baseImponible, BigDecimal uit, List<String> explicaciones) {
        List<CalculoPredialTramo> tramos = new ArrayList<>();

        BigDecimal limTramo1Monto = uit.multiply(new BigDecimal("15.00")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal limTramo2Monto = uit.multiply(new BigDecimal("60.00")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal baseRestante = baseImponible;

        // Tramo 1: Hasta 15 UIT (0.2%)
        BigDecimal baseTramo1 = baseRestante.min(limTramo1Monto);
        BigDecimal impTramo1 = baseTramo1.multiply(TASA_TRAMO_1).divide(CIEN, 2, RoundingMode.HALF_UP);
        tramos.add(CalculoPredialTramo.builder()
                .numeroTramo(1)
                .descripcionTramo("Tramo 1: Hasta 15 UIT (0.2%)")
                .limiteInferiorUit(BigDecimal.ZERO)
                .limiteSuperiorUit(new BigDecimal("15.00"))
                .limiteInferiorMonto(BigDecimal.ZERO)
                .limiteSuperiorMonto(limTramo1Monto)
                .alicuotaPorcentaje(TASA_TRAMO_1)
                .baseImponibleTramo(baseTramo1)
                .impuestoTramo(impTramo1)
                .build());

        explicaciones.add(String.format("  - Tramo 1 [0 a S/ %s] (0.2%%): Base S/ %s = S/ %s",
                limTramo1Monto, baseTramo1, impTramo1));

        baseRestante = baseRestante.subtract(baseTramo1);

        // Tramo 2: Más de 15 UIT hasta 60 UIT (0.6%)
        BigDecimal anchoTramo2 = limTramo2Monto.subtract(limTramo1Monto);
        BigDecimal baseTramo2 = baseRestante.max(BigDecimal.ZERO).min(anchoTramo2);
        BigDecimal impTramo2 = baseTramo2.multiply(TASA_TRAMO_2).divide(CIEN, 2, RoundingMode.HALF_UP);
        tramos.add(CalculoPredialTramo.builder()
                .numeroTramo(2)
                .descripcionTramo("Tramo 2: Más de 15 UIT hasta 60 UIT (0.6%)")
                .limiteInferiorUit(new BigDecimal("15.00"))
                .limiteSuperiorUit(new BigDecimal("60.00"))
                .limiteInferiorMonto(limTramo1Monto)
                .limiteSuperiorMonto(limTramo2Monto)
                .alicuotaPorcentaje(TASA_TRAMO_2)
                .baseImponibleTramo(baseTramo2)
                .impuestoTramo(impTramo2)
                .build());

        if (baseTramo2.compareTo(BigDecimal.ZERO) > 0) {
            explicaciones.add(String.format("  - Tramo 2 [S/ %s a S/ %s] (0.6%%): Base S/ %s = S/ %s",
                    limTramo1Monto, limTramo2Monto, baseTramo2, impTramo2));
        }

        baseRestante = baseRestante.subtract(baseTramo2);

        // Tramo 3: Más de 60 UIT (1.0%)
        BigDecimal baseTramo3 = baseRestante.max(BigDecimal.ZERO);
        BigDecimal impTramo3 = baseTramo3.multiply(TASA_TRAMO_3).divide(CIEN, 2, RoundingMode.HALF_UP);
        tramos.add(CalculoPredialTramo.builder()
                .numeroTramo(3)
                .descripcionTramo("Tramo 3: Más de 60 UIT (1.0%)")
                .limiteInferiorUit(new BigDecimal("60.00"))
                .limiteSuperiorUit(null)
                .limiteInferiorMonto(limTramo2Monto)
                .limiteSuperiorMonto(null)
                .alicuotaPorcentaje(TASA_TRAMO_3)
                .baseImponibleTramo(baseTramo3)
                .impuestoTramo(impTramo3)
                .build());

        if (baseTramo3.compareTo(BigDecimal.ZERO) > 0) {
            explicaciones.add(String.format("  - Tramo 3 [Más de S/ %s] (1.0%%): Base S/ %s = S/ %s",
                    limTramo2Monto, baseTramo3, impTramo3));
        }

        return tramos;
    }

    private List<RelacionContribuyentePredio> obtenerRelacionesPredialesValidas(UUID contribuyenteId, List<UUID> prediosSeleccionadosIds) {
        List<RelacionContribuyentePredio> activas = relacionRepository.findByContribuyenteIdAndEstado(
                contribuyenteId, EstadoRelacionContribuyentePredio.ACTIVO
        );

        if (prediosSeleccionadosIds != null && !prediosSeleccionadosIds.isEmpty()) {
            return activas.stream()
                    .filter(r -> prediosSeleccionadosIds.contains(r.getPredio().getId()))
                    .collect(Collectors.toList());
        }

        return activas;
    }

    private ConfiguracionConceptoTributario resolverConfiguracionPredial(UUID configuracionId, UUID periodoId) {
        if (configuracionId != null) {
            return configuracionRepository.findById(configuracionId).orElse(null);
        }

        // Buscar concepto oficial IMP_PREDIAL
        Optional<ConceptoTributario> conceptoOpt = conceptoRepository.findByCodigoIgnoreCase("IMP_PREDIAL");
        if (conceptoOpt.isPresent()) {
            List<ConfiguracionConceptoTributario> configs = configuracionRepository.findByConceptoIdOrderByNumeroVersionDesc(conceptoOpt.get().getId());
            for (ConfiguracionConceptoTributario c : configs) {
                if (c.getPeriodo().getId().equals(periodoId) && (c.getEstado() == EstadoConfiguracionConcepto.ACTIVO || c.getEstado() == EstadoConfiguracionConcepto.APROBADO)) {
                    return c;
                }
            }
            if (!configs.isEmpty()) {
                return configs.get(0);
            }
        }
        return null;
    }

    private String generarCodigoCalculo(Integer anio) {
        long count = calculoRepository.count();
        return String.format("DET-PRED-%d-%06d", anio != null ? anio : 2026, count + 1);
    }

    private CalculoPredialSummaryDto mapToSummaryDto(CalculoImpuestoPredial c) {
        return CalculoPredialSummaryDto.builder()
                .id(c.getId())
                .codigoCalculo(c.getCodigoCalculo())
                .contribuyenteId(c.getContribuyente().getId())
                .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                .periodoAnio(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 2026)
                .tipoCalculo(c.getTipoCalculo())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .cantidadPredios(c.getCantidadPredios())
                .baseImponibleNeta(c.getBaseImponibleNeta())
                .impuestoAnualDeterminado(c.getImpuestoAnualDeterminado())
                .moneda(c.getMoneda())
                .fechaCalculo(c.getFechaCalculo())
                .versionCalculo(c.getVersionCalculo())
                .build();
    }

    private CalculoPredialDetailResponse mapToDetailResponse(CalculoImpuestoPredial c) {
        List<CalculoPredialDetalleResponse> detalles = c.getPrediosDetalle() != null ? c.getPrediosDetalle().stream().map(d -> CalculoPredialDetalleResponse.builder()
                .id(d.getId())
                .predioId(d.getPredio() != null ? d.getPredio().getId() : null)
                .codigoPredio(d.getCodigoPredio())
                .direccionPredio(d.getDireccionPredio())
                .tipoPredio(d.getTipoPredio())
                .clasificacionPredio(d.getClasificacionPredio())
                .porcentajeParticipacion(d.getPorcentajeParticipacion())
                .areaTerreno(d.getAreaTerreno())
                .unidadMedidaTerreno(d.getUnidadMedidaTerreno())
                .arancelUnitarioM2(d.getArancelUnitarioM2())
                .valorTerreno(d.getValorTerreno())
                .cantidadConstrucciones(d.getCantidadConstrucciones())
                .areaTotalConstruida(d.getAreaTotalConstruida())
                .valorConstrucciones(d.getValorConstrucciones())
                .valorInstalaciones(d.getValorInstalaciones())
                .valorAutovaluoTotal(d.getValorAutovaluoTotal())
                .valorAutovaluoImputable(d.getValorAutovaluoImputable())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        List<CalculoPredialTramoResponse> tramos = c.getTramosDetalle() != null ? c.getTramosDetalle().stream().map(t -> CalculoPredialTramoResponse.builder()
                .id(t.getId())
                .numeroTramo(t.getNumeroTramo())
                .descripcionTramo(t.getDescripcionTramo())
                .limiteInferiorUit(t.getLimiteInferiorUit())
                .limiteSuperiorUit(t.getLimiteSuperiorUit())
                .limiteInferiorMonto(t.getLimiteInferiorMonto())
                .limiteSuperiorMonto(t.getLimiteSuperiorMonto())
                .alicuotaPorcentaje(t.getAlicuotaPorcentaje())
                .baseImponibleTramo(t.getBaseImponibleTramo())
                .impuestoTramo(t.getImpuestoTramo())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        List<HistorialCalculoResponse> historial = c.getHistorial() != null ? c.getHistorial().stream().map(this::mapHistorialToResponse).collect(Collectors.toList()) : new ArrayList<>();

        return CalculoPredialDetailResponse.builder()
                .id(c.getId())
                .codigoCalculo(c.getCodigoCalculo())
                .contribuyenteId(c.getContribuyente().getId())
                .contribuyenteCodigo(c.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                .periodoId(c.getPeriodo() != null ? c.getPeriodo().getId() : null)
                .periodoAnio(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 2026)
                .uitAplicada(c.getUitAplicada())
                .configuracionId(c.getConfiguracion() != null ? c.getConfiguracion().getId() : null)
                .configuracionVersion(c.getConfiguracion() != null ? c.getConfiguracion().getNumeroVersion() : null)
                .normaId(c.getNorma() != null ? c.getNorma().getId() : null)
                .normaReferencia(c.getNorma() != null ? c.getNorma().getTipoNorma() + " N° " + c.getNorma().getNumeroNorma() : null)
                .tipoCalculo(c.getTipoCalculo())
                .estado(c.getEstado())
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .fechaCalculo(c.getFechaCalculo())
                .fechaReferencia(c.getFechaReferencia())
                .cantidadPredios(c.getCantidadPredios())
                .totalAutovaluoBruto(c.getTotalAutovaluoBruto())
                .totalDeduccionesBeneficios(c.getTotalDeduccionesBeneficios())
                .baseImponibleNeta(c.getBaseImponibleNeta())
                .impuestoCalculadoTramos(c.getImpuestoCalculadoTramos())
                .impuestoMinimoLegal(c.getImpuestoMinimoLegal())
                .aplicaImpuestoMinimo(c.isAplicaImpuestoMinimo())
                .impuestoAnualDeterminado(c.getImpuestoAnualDeterminado())
                .moneda(c.getMoneda())
                .beneficioAplicado(c.getBeneficioAplicado())
                .beneficioNombre(c.getBeneficioAplicado() != null ? c.getBeneficioAplicado().getNombre() : "Ninguno")
                .resolucionBeneficio(c.getResolucionBeneficio())
                .observaciones(c.getObservaciones())
                .resolucionAprobacion(c.getResolucionAprobacion())
                .motivoAnulacion(c.getMotivoAnulacion())
                .usuarioCalculo(c.getUsuarioCalculo())
                .usuarioAprobador(c.getUsuarioAprobador())
                .fechaAprobacion(c.getFechaAprobacion())
                .usuarioAnulador(c.getUsuarioAnulador())
                .fechaAnulacion(c.getFechaAnulacion())
                .versionCalculo(c.getVersionCalculo())
                .calculoPredecesorId(c.getCalculoPredecesor() != null ? c.getCalculoPredecesor().getId() : null)
                .predios(detalles)
                .tramos(tramos)
                .historial(historial)
                .build();
    }

    private HistorialCalculoResponse mapHistorialToResponse(HistorialCalculoPredial h) {
        return HistorialCalculoResponse.builder()
                .id(h.getId())
                .accion(h.getAccion())
                .accionDescripcion(h.getAccion() != null ? h.getAccion().getDescripcion() : "")
                .estadoAnterior(h.getEstadoAnterior())
                .nuevoEstado(h.getNuevoEstado())
                .motivo(h.getMotivo())
                .resolucionSustento(h.getResolucionSustento())
                .usuarioRegistro(h.getUsuarioRegistro())
                .fechaRegistro(h.getFechaRegistro())
                .detallesCambios(h.getDetallesCambios())
                .build();
    }
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ObligacionSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ObligacionValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObligacionTributariaServiceImpl implements ObligacionTributariaService {

    private final ObligacionTributariaRepository obligacionRepository;
    private final CuotaObligacionTributariaRepository cuotaRepository;
    private final HistorialObligacionTributariaRepository historialRepository;
    private final MovimientoCuentaCorrienteTributariaRepository movimientoRepository;
    private final LoteGeneracionObligacionesRepository loteRepository;
    private final DetalleLoteGeneracionRepository detalleLoteRepository;

    private final CalculoImpuestoPredialRepository calculoPredialRepository;
    private final CalculoArbitrioMunicipalRepository calculoArbitrioRepository;
    private final ConceptoTributarioRepository conceptoRepository;
    private final PeriodoTributarioRepository periodoRepository;

    private final CuentaCorrienteTributariaService cuentaCorrienteService;
    private final ObligacionValidator validator;

    @Override
    @Transactional(readOnly = true)
    public Page<ObligacionSummaryDto> listarObligaciones(ObligacionFilterDto filter, Pageable pageable) {
        Page<ObligacionTributaria> page = obligacionRepository.findAll(ObligacionSpecification.conFiltros(filter), pageable);
        List<ObligacionSummaryDto> dtoList = page.getContent().stream().map(this::mapearASummary).collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ObligacionDetailResponse obtenerPorId(UUID id) {
        ObligacionTributaria o = obligacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + id));
        return mapearADetalle(o);
    }

    @Override
    @Transactional
    public ObligacionDetailResponse generarDesdeDeterminacion(GeneracionObligacionRequest request, String usuario) {
        validator.validarGeneracionIndividual(request);

        if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.IMPUESTO_PREDIAL) {
            return generarObligacionPredial(request.getDeterminacionId(), request.getCuotas(), request.getObservaciones(), usuario);
        } else if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES) {
            return generarObligacionArbitrio(request.getDeterminacionId(), request.getCuotas(), request.getObservaciones(), usuario);
        } else {
            throw new BusinessException("Tipo de determinación aún no soportado para generación directa: " + request.getTipoDeterminacion());
        }
    }

    private ObligacionDetailResponse generarObligacionPredial(UUID determinacionId, int cuotasNum, String observaciones, String usuario) {
        CalculoImpuestoPredial calculo = calculoPredialRepository.findById(determinacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Determinación predial no encontrada: " + determinacionId));

        boolean yaExiste = obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                calculo.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA)
        );
        validator.validarElegibilidadPredial(calculo, yaExiste);

        ConceptoTributario concepto = conceptoRepository.findByCodigo("IMP_PREDIAL")
                .or(() -> calculo.getConfiguracion() != null ? Optional.ofNullable(calculo.getConfiguracion().getConcepto()) : Optional.empty())
                .orElseThrow(() -> new BusinessException("Concepto tributario 'IMP_PREDIAL' no configurado en el sistema."));

        PeriodoTributario periodo = calculo.getPeriodo();
        int anio = periodo.getAnio();
        Long seq = obligacionRepository.obtenerSiguienteValorSecuencia();
        String codigoObligacion = String.format("OBL-PRED-%d-%06d", anio, seq);

        BigDecimal importeTotal = calculo.getImpuestoAnualDeterminado().setScale(2, RoundingMode.HALF_UP);
        LocalDate fechaVencimientoGeneral = LocalDate.of(anio, 2, 28);

        ObligacionTributaria obligacion = ObligacionTributaria.builder()
                .codigoObligacion(codigoObligacion)
                .contribuyente(calculo.getContribuyente())
                .concepto(concepto)
                .periodo(periodo)
                .predio(null) // El Impuesto Predial grava la totalidad del patrimonio predial del contribuyente en el distrito
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .determinacionOrigenId(calculo.getId())
                .numeroVersionDeterminacion(calculo.getVersionCalculo())
                .cuotaNumero(1)
                .totalCuotas(cuotasNum)
                .importeOriginal(importeTotal)
                .importeVigente(importeTotal)
                .importeDeterminado(importeTotal)
                .importeAjustes(BigDecimal.ZERO)
                .importePagado(BigDecimal.ZERO)
                .saldo(importeTotal)
                .moneda("PEN")
                .fechaDeterminacion(calculo.getFechaReferencia() != null ? calculo.getFechaReferencia() : LocalDate.now())
                .fechaVencimiento(fechaVencimientoGeneral)
                .fechaRegistro(OffsetDateTime.now())
                .estado(EstadoObligacionTributaria.REGISTRADA)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .estadoExigibilidad(EstadoExigibilidadObligacion.NO_EXIGIBLE)
                .versionCalculo(String.valueOf(calculo.getVersionCalculo()))
                .normaAplicable(calculo.getNorma())
                .snapshotDeterminacion(calculo.getSnapshotCalculo())
                .resolucionSustento(calculo.getResolucionAprobacion())
                .observaciones(observaciones)
                .build();

        obligacion.setCreatedBy(usuario != null ? usuario : "SISTEMA");
        ObligacionTributaria guardada = obligacionRepository.save(obligacion);

        // Generar Cuotas Individuales con consistencia exacta de redondeo
        generarCuotasPredial(guardada, cuotasNum, importeTotal, anio, usuario);

        // Asegurar Cuenta Corriente y registrar CARGO contable
        CuentaCorrienteTributaria cta = cuentaCorrienteService.asegurarCuentaContribuyente(calculo.getContribuyente().getId(), usuario);
        cuentaCorrienteService.registrarMovimiento(
                cta.getId(),
                guardada,
                null,
                TipoMovimientoCuentaCorriente.CARGO_OBLIGACION,
                importeTotal,
                guardada.getCodigoObligacion(),
                calculo.getResolucionAprobacion(),
                "Cargo inicial por determinación del Impuesto Predial " + anio + " (Cálculo " + calculo.getCodigoCalculo() + ")",
                usuario
        );

        // Registrar Historial
        registrarHistorial(guardada, AccionHistorialObligacion.GENERACION, null, EstadoObligacionTributaria.REGISTRADA.name(),
                BigDecimal.ZERO, importeTotal, "Generación formal de obligación tributaria desde determinación predial aprobada.",
                calculo.getResolucionAprobacion(), usuario);

        log.info("Obligación tributaria predial generada con éxito: ID={}, Código={}, Total={}", guardada.getId(), guardada.getCodigoObligacion(), importeTotal);
        return mapearADetalle(guardada);
    }

    private ObligacionDetailResponse generarObligacionArbitrio(UUID determinacionId, int cuotasNum, String observaciones, String usuario) {
        CalculoArbitrioMunicipal calculo = calculoArbitrioRepository.findById(determinacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Determinación de arbitrios no encontrada: " + determinacionId));

        boolean yaExiste = obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                calculo.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA)
        );
        validator.validarElegibilidadArbitrio(calculo, yaExiste);

        ConceptoTributario concepto = null;
        if (calculo.getConfiguracion() != null && calculo.getConfiguracion().getServicio() != null) {
            concepto = calculo.getConfiguracion().getServicio().getConceptoTributario();
        }
        if (concepto == null) {
            concepto = conceptoRepository.findByCodigo("ARB_LIMPIEZA")
                    .orElseGet(() -> conceptoRepository.findAll().stream().filter(c -> c.getCodigo().startsWith("ARB")).findFirst()
                            .orElseThrow(() -> new BusinessException("Concepto tributario de arbitrios no encontrado.")));
        }

        PeriodoTributario periodo = calculo.getPeriodo();
        int anio = periodo.getAnio();
        Long seq = obligacionRepository.obtenerSiguienteValorSecuencia();
        String codigoObligacion = String.format("OBL-ARB-%d-%06d", anio, seq);

        BigDecimal importeTotal = (calculo.getImportePeriodoTotal() != null && calculo.getImportePeriodoTotal().compareTo(BigDecimal.ZERO) > 0
                ? calculo.getImportePeriodoTotal() : calculo.getImporteAnualTotal()).setScale(2, RoundingMode.HALF_UP);

        LocalDate fechaVencimientoGeneral = LocalDate.of(anio, 3, 31);

        ObligacionTributaria obligacion = ObligacionTributaria.builder()
                .codigoObligacion(codigoObligacion)
                .contribuyente(calculo.getContribuyente())
                .concepto(concepto)
                .periodo(periodo)
                .predio(calculo.getPredio())
                .tipoDeterminacion(TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES)
                .determinacionOrigenId(calculo.getId())
                .numeroVersionDeterminacion(calculo.getVersionCalculo())
                .cuotaNumero(1)
                .totalCuotas(cuotasNum)
                .importeOriginal(importeTotal)
                .importeVigente(importeTotal)
                .importeDeterminado(importeTotal)
                .importeAjustes(BigDecimal.ZERO)
                .importePagado(BigDecimal.ZERO)
                .saldo(importeTotal)
                .moneda("PEN")
                .fechaDeterminacion(calculo.getFechaReferencia() != null ? calculo.getFechaReferencia() : LocalDate.now())
                .fechaVencimiento(fechaVencimientoGeneral)
                .fechaRegistro(OffsetDateTime.now())
                .estado(EstadoObligacionTributaria.REGISTRADA)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .estadoExigibilidad(EstadoExigibilidadObligacion.NO_EXIGIBLE)
                .versionCalculo(String.valueOf(calculo.getVersionCalculo()))
                .snapshotDeterminacion(calculo.getSnapshotCalculo())
                .resolucionSustento(calculo.getResolucionAprobacion())
                .observaciones(observaciones)
                .build();

        obligacion.setCreatedBy(usuario != null ? usuario : "SISTEMA");
        ObligacionTributaria guardada = obligacionRepository.save(obligacion);

        generarCuotasArbitrios(guardada, cuotasNum, importeTotal, anio, usuario);

        CuentaCorrienteTributaria cta = cuentaCorrienteService.asegurarCuentaContribuyente(calculo.getContribuyente().getId(), usuario);
        cuentaCorrienteService.registrarMovimiento(
                cta.getId(),
                guardada,
                null,
                TipoMovimientoCuentaCorriente.CARGO_OBLIGACION,
                importeTotal,
                guardada.getCodigoObligacion(),
                calculo.getResolucionAprobacion(),
                "Cargo inicial por determinación de Arbitrios Municipales " + anio + " (Predio " + (calculo.getPredio() != null ? calculo.getPredio().getCodigoPredio() : "N/A") + ")",
                usuario
        );

        registrarHistorial(guardada, AccionHistorialObligacion.GENERACION, null, EstadoObligacionTributaria.REGISTRADA.name(),
                BigDecimal.ZERO, importeTotal, "Generación formal de obligación tributaria desde liquidación de arbitrios aprobada.",
                calculo.getResolucionAprobacion(), usuario);

        log.info("Obligación tributaria de arbitrios generada con éxito: ID={}, Código={}, Total={}", guardada.getId(), guardada.getCodigoObligacion(), importeTotal);
        return mapearADetalle(guardada);
    }

    private void generarCuotasPredial(ObligacionTributaria obligacion, int cuotasNum, BigDecimal importeTotal, int anio, String usuario) {
        if (cuotasNum <= 1) {
            CuotaObligacionTributaria c1 = CuotaObligacionTributaria.builder()
                    .obligacion(obligacion)
                    .numeroCuota(1)
                    .importe(importeTotal)
                    .saldo(importeTotal)
                    .importePagado(BigDecimal.ZERO)
                    .fechaVencimiento(LocalDate.of(anio, 2, 28))
                    .estado(EstadoCuotaTributaria.PENDIENTE)
                    .build();
            c1.setCreatedBy(usuario != null ? usuario : "SISTEMA");
            cuotaRepository.save(c1);
            return;
        }

        // Caso estándar Ley de Tributación Municipal: 4 cuotas trimestrales
        BigDecimal divisor = new BigDecimal(cuotasNum);
        BigDecimal cuotaBase = importeTotal.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal sumaPrimeras = cuotaBase.multiply(new BigDecimal(cuotasNum - 1));
        BigDecimal cuotaUltima = importeTotal.subtract(sumaPrimeras);

        LocalDate[] vencimientos = new LocalDate[] {
                LocalDate.of(anio, 2, 28),
                LocalDate.of(anio, 5, 31),
                LocalDate.of(anio, 8, 31),
                LocalDate.of(anio, 11, 30)
        };

        for (int i = 1; i <= cuotasNum; i++) {
            BigDecimal montoCuota = (i == cuotasNum) ? cuotaUltima : cuotaBase;
            LocalDate fechaVenc = (i <= vencimientos.length) ? vencimientos[i - 1] : LocalDate.of(anio, 12, 31);

            CuotaObligacionTributaria cuota = CuotaObligacionTributaria.builder()
                    .obligacion(obligacion)
                    .numeroCuota(i)
                    .importe(montoCuota)
                    .saldo(montoCuota)
                    .importePagado(BigDecimal.ZERO)
                    .fechaVencimiento(fechaVenc)
                    .estado(EstadoCuotaTributaria.PENDIENTE)
                    .build();
            cuota.setCreatedBy(usuario != null ? usuario : "SISTEMA");
            cuotaRepository.save(cuota);
        }
    }

    private void generarCuotasArbitrios(ObligacionTributaria obligacion, int cuotasNum, BigDecimal importeTotal, int anio, String usuario) {
        if (cuotasNum <= 1) {
            CuotaObligacionTributaria c1 = CuotaObligacionTributaria.builder()
                    .obligacion(obligacion)
                    .numeroCuota(1)
                    .importe(importeTotal)
                    .saldo(importeTotal)
                    .importePagado(BigDecimal.ZERO)
                    .fechaVencimiento(LocalDate.of(anio, 3, 31))
                    .estado(EstadoCuotaTributaria.PENDIENTE)
                    .build();
            c1.setCreatedBy(usuario != null ? usuario : "SISTEMA");
            cuotaRepository.save(c1);
            return;
        }

        BigDecimal divisor = new BigDecimal(cuotasNum);
        BigDecimal cuotaBase = importeTotal.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal sumaPrimeras = cuotaBase.multiply(new BigDecimal(cuotasNum - 1));
        BigDecimal cuotaUltima = importeTotal.subtract(sumaPrimeras);

        for (int i = 1; i <= cuotasNum; i++) {
            BigDecimal montoCuota = (i == cuotasNum) ? cuotaUltima : cuotaBase;
            // Trimestres por defecto
            LocalDate fechaVenc = LocalDate.of(anio, Math.min(i * 3, 12), 28);

            CuotaObligacionTributaria cuota = CuotaObligacionTributaria.builder()
                    .obligacion(obligacion)
                    .numeroCuota(i)
                    .importe(montoCuota)
                    .saldo(montoCuota)
                    .importePagado(BigDecimal.ZERO)
                    .fechaVencimiento(fechaVenc)
                    .estado(EstadoCuotaTributaria.PENDIENTE)
                    .build();
            cuota.setCreatedBy(usuario != null ? usuario : "SISTEMA");
            cuotaRepository.save(cuota);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PrevisualizacionLoteDto previsualizarLote(GeneracionLoteRequest request) {
        if (request == null || request.getTipoDeterminacion() == null || request.getPeriodoId() == null) {
            throw new BusinessException("Parámetros de previsualización de lote incompletos.");
        }

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado: " + request.getPeriodoId()));

        int evaluadas = 0;
        int elegibles = 0;
        int duplicadas = 0;
        int noElegibles = 0;
        BigDecimal totalEstimado = BigDecimal.ZERO;

        if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.IMPUESTO_PREDIAL) {
            List<CalculoImpuestoPredial> calculos = calculoPredialRepository.findByPeriodoId(periodo.getId());
            evaluadas = calculos.size();
            for (CalculoImpuestoPredial c : calculos) {
                if (c.getEstado() != EstadoCalculoPredial.APROBADO) {
                    noElegibles++;
                } else if (obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                        c.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA))) {
                    duplicadas++;
                } else {
                    elegibles++;
                    totalEstimado = totalEstimado.add(c.getImpuestoAnualDeterminado());
                }
            }
        } else if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES) {
            List<CalculoArbitrioMunicipal> calculos = calculoArbitrioRepository.findByPeriodoId(periodo.getId());
            evaluadas = calculos.size();
            for (CalculoArbitrioMunicipal c : calculos) {
                if (c.getEstado() != EstadoCalculoArbitrio.APROBADO) {
                    noElegibles++;
                } else if (obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                        c.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA))) {
                    duplicadas++;
                } else {
                    elegibles++;
                    BigDecimal imp = c.getImportePeriodoTotal() != null && c.getImportePeriodoTotal().compareTo(BigDecimal.ZERO) > 0
                            ? c.getImportePeriodoTotal() : c.getImporteAnualTotal();
                    totalEstimado = totalEstimado.add(imp);
                }
            }
        }

        return PrevisualizacionLoteDto.builder()
                .tipoDeterminacion(request.getTipoDeterminacion())
                .periodoId(periodo.getId())
                .periodoAnio(periodo.getAnio())
                .totalEvaluadas(evaluadas)
                .totalElegibles(elegibles)
                .totalDuplicadas(duplicadas)
                .totalNoElegibles(noElegibles)
                .importeTotalEstimado(totalEstimado.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    @Transactional
    public LoteGeneracionResponse generarLote(GeneracionLoteRequest request, String usuario) {
        PrevisualizacionLoteDto prev = previsualizarLote(request);
        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        Long seqLote = loteRepository.obtenerSiguienteValorSecuencia();
        String codigoLote = String.format("LOTE-OBL-%d-%06d", periodo.getAnio(), seqLote);

        LoteGeneracionObligaciones lote = LoteGeneracionObligaciones.builder()
                .codigoLote(codigoLote)
                .tipoDeterminacion(request.getTipoDeterminacion())
                .periodo(periodo)
                .totalEvaluadas(prev.getTotalEvaluadas())
                .totalElegibles(prev.getTotalElegibles())
                .totalDuplicadas(prev.getTotalDuplicadas())
                .totalGeneradas(0)
                .totalErrores(0)
                .importeTotalGenerado(BigDecimal.ZERO)
                .estado(EstadoLoteGeneracion.EN_PROCESO)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .observaciones(request.getObservaciones())
                .build();

        lote.setCreatedBy(usuario != null ? usuario : "SISTEMA");
        LoteGeneracionObligaciones guardado = loteRepository.save(lote);

        int generadas = 0;
        int errores = 0;
        BigDecimal importeAcumulado = BigDecimal.ZERO;
        List<DetalleLoteGeneracion> detalles = new ArrayList<>();

        if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.IMPUESTO_PREDIAL) {
            List<CalculoImpuestoPredial> calculos = calculoPredialRepository.findByPeriodoId(periodo.getId());
            for (CalculoImpuestoPredial c : calculos) {
                if (c.getEstado() != EstadoCalculoPredial.APROBADO) {
                    detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                            ResultadoGeneracionItem.NO_ELEGIBLE, BigDecimal.ZERO, "Determinación no se encuentra en estado APROBADO"));
                } else if (obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                        c.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA))) {
                    detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                            ResultadoGeneracionItem.DUPLICADO, BigDecimal.ZERO, "Ya cuenta con obligación tributaria activa registrada"));
                } else {
                    try {
                        GeneracionObligacionRequest genReq = GeneracionObligacionRequest.builder()
                                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                                .determinacionId(c.getId())
                                .cuotas(4)
                                .observaciones("Generado automáticamente mediante lote " + codigoLote)
                                .build();
                        ObligacionDetailResponse resp = generarDesdeDeterminacion(genReq, usuario);
                        generadas++;
                        importeAcumulado = importeAcumulado.add(resp.getImporteDeterminado());

                        ObligacionTributaria oblEntity = obligacionRepository.findById(resp.getId()).orElse(null);
                        detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), oblEntity,
                                ResultadoGeneracionItem.EXITO, resp.getImporteDeterminado(), "Obligación generada: " + resp.getCodigoObligacion()));
                    } catch (Exception e) {
                        log.error("Error al generar obligación en lote para cálculo {}: {}", c.getCodigoCalculo(), e.getMessage());
                        errores++;
                        detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                                ResultadoGeneracionItem.ERROR, BigDecimal.ZERO, "Error técnico: " + e.getMessage()));
                    }
                }
            }
        } else if (request.getTipoDeterminacion() == TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES) {
            List<CalculoArbitrioMunicipal> calculos = calculoArbitrioRepository.findByPeriodoId(periodo.getId());
            for (CalculoArbitrioMunicipal c : calculos) {
                if (c.getEstado() != EstadoCalculoArbitrio.APROBADO) {
                    detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                            ResultadoGeneracionItem.NO_ELEGIBLE, BigDecimal.ZERO, "Liquidación de arbitrios no está en estado APROBADO"));
                } else if (obligacionRepository.existsByDeterminacionOrigenIdAndEstadoNotIn(
                        c.getId(), List.of(EstadoObligacionTributaria.ANULADA, EstadoObligacionTributaria.RECTIFICADA))) {
                    detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                            ResultadoGeneracionItem.DUPLICADO, BigDecimal.ZERO, "Ya cuenta con obligación tributaria activa registrada"));
                } else {
                    try {
                        GeneracionObligacionRequest genReq = GeneracionObligacionRequest.builder()
                                .tipoDeterminacion(TipoDeterminacionTributaria.ARBITRIOS_MUNICIPALES)
                                .determinacionId(c.getId())
                                .cuotas(4)
                                .observaciones("Generado automáticamente mediante lote " + codigoLote)
                                .build();
                        ObligacionDetailResponse resp = generarDesdeDeterminacion(genReq, usuario);
                        generadas++;
                        importeAcumulado = importeAcumulado.add(resp.getImporteDeterminado());

                        ObligacionTributaria oblEntity = obligacionRepository.findById(resp.getId()).orElse(null);
                        detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), oblEntity,
                                ResultadoGeneracionItem.EXITO, resp.getImporteDeterminado(), "Obligación generada: " + resp.getCodigoObligacion()));
                    } catch (Exception e) {
                        log.error("Error al generar obligación en lote para arbitrios {}: {}", c.getCodigoCalculo(), e.getMessage());
                        errores++;
                        detalles.add(crearDetalleLote(guardado, c.getId(), c.getCodigoCalculo(), c.getContribuyente(), null,
                                ResultadoGeneracionItem.ERROR, BigDecimal.ZERO, "Error técnico: " + e.getMessage()));
                    }
                }
            }
        }

        detalleLoteRepository.saveAll(detalles);
        guardado.setTotalGeneradas(generadas);
        guardado.setTotalErrores(errores);
        guardado.setImporteTotalGenerado(importeAcumulado);
        guardado.setEstado(errores > 0 ? EstadoLoteGeneracion.COMPLETADO_CON_ERRORES : EstadoLoteGeneracion.COMPLETADO);
        LoteGeneracionObligaciones loteActualizado = loteRepository.save(guardado);

        return mapearALoteResponse(loteActualizado, detalles);
    }

    private DetalleLoteGeneracion crearDetalleLote(
            LoteGeneracionObligaciones lote,
            UUID detId,
            String codDet,
            ContribuyenteTributario contrib,
            ObligacionTributaria obl,
            ResultadoGeneracionItem res,
            BigDecimal imp,
            String msg
    ) {
        return DetalleLoteGeneracion.builder()
                .lote(lote)
                .determinacionId(detId)
                .codigoDeterminacion(codDet)
                .contribuyente(contrib)
                .obligacion(obl)
                .resultado(res)
                .importeGenerado(imp)
                .mensajeDetalle(msg)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public ObligacionDetailResponse validarObligacion(UUID id, String resolucionSustento, String usuario) {
        ObligacionTributaria o = obligacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        if (o.getEstado() == EstadoObligacionTributaria.ANULADA) {
            throw new BusinessException("No se puede validar una obligación que ha sido anulada.");
        }

        String estadoAnt = o.getEstado().name();
        o.setEstado(EstadoObligacionTributaria.VALIDADA);
        o.setEstadoExigibilidad(EstadoExigibilidadObligacion.EXIGIBLE);
        o.setFechaValidacion(OffsetDateTime.now());
        o.setUsuarioValidador(usuario != null ? usuario : "SISTEMA");
        if (resolucionSustento != null && !resolucionSustento.trim().isEmpty()) {
            o.setResolucionSustento(resolucionSustento.trim());
        }

        ObligacionTributaria guardada = obligacionRepository.save(o);
        registrarHistorial(guardada, AccionHistorialObligacion.VALIDACION, estadoAnt, EstadoObligacionTributaria.VALIDADA.name(),
                guardada.getImporteDeterminado(), guardada.getImporteDeterminado(),
                "Validación administrativa formal de la obligación y habilitación de exigibilidad.",
                resolucionSustento, usuario);

        return mapearADetalle(guardada);
    }

    @Override
    @Transactional
    public ObligacionDetailResponse suspenderObligacion(UUID id, AccionObligacionRequest request, String usuario) {
        ObligacionTributaria o = obligacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        validator.validarSuspension(o, request);
        String estadoAnt = o.getEstado().name();

        o.setEstado(EstadoObligacionTributaria.SUSPENDIDA);
        o.setEstadoExigibilidad(EstadoExigibilidadObligacion.SUSPENDIDA_LEGALMENTE);
        o.setMotivoAjuste(request.getMotivo());
        if (request.getResolucionSustento() != null) {
            o.setResolucionSustento(request.getResolucionSustento());
        }

        ObligacionTributaria guardada = obligacionRepository.save(o);
        registrarHistorial(guardada, AccionHistorialObligacion.SUSPENSION, estadoAnt, EstadoObligacionTributaria.SUSPENDIDA.name(),
                guardada.getImporteDeterminado(), guardada.getImporteDeterminado(),
                "Suspensión legal de la exigibilidad: " + request.getMotivo(),
                request.getResolucionSustento(), usuario);

        return mapearADetalle(guardada);
    }

    @Override
    @Transactional
    public ObligacionDetailResponse rectificarObligacion(UUID id, AccionObligacionRequest request, String usuario) {
        ObligacionTributaria antigua = obligacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        validator.validarRectificacion(antigua, request);

        // Rectificar marca la obligación anterior como RECTIFICADA
        String estadoAnt = antigua.getEstado().name();
        antigua.setEstado(EstadoObligacionTributaria.RECTIFICADA);
        antigua.setMotivoAjuste("Rectificada por sustitución técnica: " + request.getMotivo());
        obligacionRepository.save(antigua);

        registrarHistorial(antigua, AccionHistorialObligacion.RECTIFICACION, estadoAnt, EstadoObligacionTributaria.RECTIFICADA.name(),
                antigua.getImporteDeterminado(), antigua.getImporteDeterminado(),
                "Obligación cerrada por rectificación formal: " + request.getMotivo(),
                request.getResolucionSustento(), usuario);

        // Generar la nueva obligación desde la nueva determinación aprobada
        GeneracionObligacionRequest nuevaReq = GeneracionObligacionRequest.builder()
                .tipoDeterminacion(antigua.getTipoDeterminacion())
                .determinacionId(request.getNuevaDeterminacionId())
                .cuotas(antigua.getTotalCuotas())
                .observaciones("Generada por rectificación de " + antigua.getCodigoObligacion() + ": " + request.getMotivo())
                .build();

        ObligacionDetailResponse nueva = generarDesdeDeterminacion(nuevaReq, usuario);

        // Ajuste en cuenta corriente por la diferencia
        BigDecimal saldoAntiguoAReversar = antigua.getSaldo();
        if (saldoAntiguoAReversar.compareTo(BigDecimal.ZERO) > 0) {
            CuentaCorrienteTributaria cta = cuentaCorrienteService.asegurarCuentaContribuyente(antigua.getContribuyente().getId(), usuario);
            cuentaCorrienteService.registrarMovimiento(
                    cta.getId(),
                    antigua,
                    null,
                    TipoMovimientoCuentaCorriente.ANULACION_AUTORIZADA,
                    saldoAntiguoAReversar,
                    antigua.getCodigoObligacion(),
                    request.getResolucionSustento(),
                    "Descargo del saldo de obligación rectificada " + antigua.getCodigoObligacion(),
                    usuario
            );
            antigua.setSaldo(BigDecimal.ZERO);
            antigua.setEstadoFinanciero(EstadoFinancieroObligacion.CANCELADA);
            obligacionRepository.save(antigua);
        }

        return nueva;
    }

    @Override
    @Transactional
    public ObligacionDetailResponse anularObligacion(UUID id, AccionObligacionRequest request, String usuario) {
        ObligacionTributaria o = obligacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        validator.validarAnulacion(o, request);
        String estadoAnt = o.getEstado().name();
        BigDecimal saldoPendiente = o.getSaldo();

        // 1. Revertir saldo pendiente en cuenta corriente si existía
        if (saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
            CuentaCorrienteTributaria cta = cuentaCorrienteService.asegurarCuentaContribuyente(o.getContribuyente().getId(), usuario);
            cuentaCorrienteService.registrarMovimiento(
                    cta.getId(),
                    o,
                    null,
                    TipoMovimientoCuentaCorriente.ANULACION_AUTORIZADA,
                    saldoPendiente,
                    o.getCodigoObligacion(),
                    request.getResolucionSustento(),
                    "Anulación formal autorizada: " + request.getMotivo(),
                    usuario
            );
        }

        // 2. Anular cuotas pendientes
        for (CuotaObligacionTributaria cuota : o.getCuotas()) {
            if (cuota.getEstado() != EstadoCuotaTributaria.CANCELADA) {
                cuota.setEstado(EstadoCuotaTributaria.ANULADA);
                cuota.setSaldo(BigDecimal.ZERO);
                cuotaRepository.save(cuota);
            }
        }

        // 3. Actualizar obligación
        o.setEstado(EstadoObligacionTributaria.ANULADA);
        o.setEstadoFinanciero(EstadoFinancieroObligacion.CANCELADA);
        o.setEstadoExigibilidad(EstadoExigibilidadObligacion.NO_EXIGIBLE);
        o.setSaldo(BigDecimal.ZERO);
        o.setFechaAnulacion(OffsetDateTime.now());
        o.setUsuarioAnulador(usuario != null ? usuario : "SISTEMA");
        o.setMotivoAnulacion(request.getMotivo());
        o.setResolucionSustento(request.getResolucionSustento());

        ObligacionTributaria guardada = obligacionRepository.save(o);

        registrarHistorial(guardada, AccionHistorialObligacion.ANULACION, estadoAnt, EstadoObligacionTributaria.ANULADA.name(),
                saldoPendiente, BigDecimal.ZERO,
                "Anulación formal autorizada de la obligación: " + request.getMotivo(),
                request.getResolucionSustento(), usuario);

        log.info("Obligación {} anulada formalmente por {}", guardada.getCodigoObligacion(), usuario);
        return mapearADetalle(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricasGenerales() {
        Map<String, Object> m = new HashMap<>();
        long totalObligaciones = obligacionRepository.count();
        long registradas = obligacionRepository.countByEstado(EstadoObligacionTributaria.REGISTRADA);
        long validadas = obligacionRepository.countByEstado(EstadoObligacionTributaria.VALIDADA);
        long suspendidas = obligacionRepository.countByEstado(EstadoObligacionTributaria.SUSPENDIDA);
        long anuladas = obligacionRepository.countByEstado(EstadoObligacionTributaria.ANULADA);

        BigDecimal importeDeterminado = obligacionRepository.sumarTotalImporteDeterminado();
        BigDecimal saldoPendiente = obligacionRepository.sumarTotalSaldoPendiente();

        m.put("totalObligaciones", totalObligaciones);
        m.put("registradas", registradas);
        m.put("validadas", validadas);
        m.put("suspendidas", suspendidas);
        m.put("anuladas", anuladas);
        m.put("importeDeterminado", importeDeterminado != null ? importeDeterminado : BigDecimal.ZERO);
        m.put("saldoPendiente", saldoPendiente != null ? saldoPendiente : BigDecimal.ZERO);

        return m;
    }

    private void registrarHistorial(
            ObligacionTributaria obligacion,
            AccionHistorialObligacion accion,
            String estAnt,
            String estNuevo,
            BigDecimal impAnt,
            BigDecimal impNuevo,
            String motivo,
            String resolucion,
            String usuario
    ) {
        HistorialObligacionTributaria h = HistorialObligacionTributaria.builder()
                .obligacion(obligacion)
                .accion(accion)
                .estadoAnterior(estAnt)
                .estadoNuevo(estNuevo)
                .importeAnterior(impAnt)
                .importeNuevo(impNuevo)
                .motivo(motivo)
                .resolucionSustento(resolucion)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(h);
    }

    private ObligacionSummaryDto mapearASummary(ObligacionTributaria o) {
        return ObligacionSummaryDto.builder()
                .id(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .contribuyenteId(o.getContribuyente().getId())
                .contribuyenteCodigo(o.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(o.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(o.getContribuyente().getTipoDocumento() + ": " + o.getContribuyente().getNumeroDocumento())
                .conceptoCodigo(o.getConcepto().getCodigo())
                .conceptoNombre(o.getConcepto().getNombre())
                .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : "N/A")
                .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccionCompleta() : "N/A")
                .periodoAnio(o.getPeriodo().getAnio())
                .tipoDeterminacion(o.getTipoDeterminacion())
                .cuotaNumero(o.getCuotaNumero())
                .totalCuotas(o.getTotalCuotas())
                .importeDeterminado(o.getImporteDeterminado())
                .importePagado(o.getImportePagado())
                .saldo(o.getSaldo())
                .moneda(o.getMoneda())
                .fechaVencimiento(o.getFechaVencimiento())
                .fechaRegistro(o.getFechaRegistro())
                .estado(o.getEstado())
                .estadoFinanciero(o.getEstadoFinanciero())
                .estadoExigibilidad(o.getEstadoExigibilidad())
                .build();
    }

    private ObligacionDetailResponse mapearADetalle(ObligacionTributaria o) {
        List<CuotaResponse> cuotas = cuotaRepository.findByObligacionIdOrderByNumeroCuotaAsc(o.getId())
                .stream().map(c -> CuotaResponse.builder()
                        .id(c.getId())
                        .numeroCuota(c.getNumeroCuota())
                        .importe(c.getImporte())
                        .importePagado(c.getImportePagado())
                        .saldo(c.getSaldo())
                        .fechaVencimiento(c.getFechaVencimiento())
                        .estado(c.getEstado())
                        .build()
                ).collect(Collectors.toList());

        List<MovimientoResponse> movimientos = movimientoRepository.findByObligacionIdOrderByNumeroSecuenciaAsc(o.getId())
                .stream().map(m -> MovimientoResponse.builder()
                        .id(m.getId())
                        .numeroSecuencia(m.getNumeroSecuencia())
                        .tipoMovimiento(m.getTipoMovimiento())
                        .naturaleza(m.getNaturaleza())
                        .importe(m.getImporte())
                        .saldoAnterior(m.getSaldoAnterior())
                        .saldoPosterior(m.getSaldoPosterior())
                        .fechaOperacion(m.getFechaOperacion())
                        .fechaRegistro(m.getFechaRegistro())
                        .referenciaOrigen(m.getReferenciaOrigen())
                        .documentoSustento(m.getDocumentoSustento())
                        .descripcion(m.getDescripcion())
                        .estado(m.getEstado())
                        .usuarioResponsable(m.getUsuarioResponsable())
                        .obligacionId(o.getId())
                        .obligacionCodigo(o.getCodigoObligacion())
                        .build()
                ).collect(Collectors.toList());

        List<HistorialObligacionResponse> historial = historialRepository.findByObligacionIdOrderByFechaRegistroDesc(o.getId())
                .stream().map(h -> HistorialObligacionResponse.builder()
                        .id(h.getId())
                        .accion(h.getAccion())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .importeAnterior(h.getImporteAnterior())
                        .importeNuevo(h.getImporteNuevo())
                        .motivo(h.getMotivo())
                        .resolucionSustento(h.getResolucionSustento())
                        .usuarioRegistro(h.getUsuarioRegistro())
                        .fechaRegistro(h.getFechaRegistro())
                        .build()
                ).collect(Collectors.toList());

        return ObligacionDetailResponse.builder()
                .id(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .contribuyenteId(o.getContribuyente().getId())
                .contribuyenteCodigo(o.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(o.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(o.getContribuyente().getTipoDocumento() + ": " + o.getContribuyente().getNumeroDocumento())
                .contribuyenteDireccion(o.getContribuyente().getDomicilioFiscal() != null ? o.getContribuyente().getDomicilioFiscal() : "Quinua, Huamanga")
                .conceptoId(o.getConcepto().getId())
                .conceptoCodigo(o.getConcepto().getCodigo())
                .conceptoNombre(o.getConcepto().getNombre())
                .predioId(o.getPredio() != null ? o.getPredio().getId() : null)
                .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : "N/A")
                .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccionCompleta() : "N/A")
                .predioClasificacion(o.getPredio() != null && o.getPredio().getClasificacionPredio() != null ? o.getPredio().getClasificacionPredio().name() : "N/A")
                .periodoId(o.getPeriodo().getId())
                .periodoAnio(o.getPeriodo().getAnio())
                .tipoDeterminacion(o.getTipoDeterminacion())
                .determinacionOrigenId(o.getDeterminacionOrigenId())
                .numeroVersionDeterminacion(o.getNumeroVersionDeterminacion())
                .cuotaNumero(o.getCuotaNumero())
                .totalCuotas(o.getTotalCuotas())
                .importeOriginal(o.getImporteOriginal())
                .importeVigente(o.getImporteVigente())
                .importeDeterminado(o.getImporteDeterminado())
                .importeAjustes(o.getImporteAjustes())
                .importePagado(o.getImportePagado())
                .saldo(o.getSaldo())
                .moneda(o.getMoneda())
                .fechaDeterminacion(o.getFechaDeterminacion())
                .fechaVencimiento(o.getFechaVencimiento())
                .fechaRegistro(o.getFechaRegistro())
                .fechaValidacion(o.getFechaValidacion())
                .usuarioValidador(o.getUsuarioValidador())
                .fechaAnulacion(o.getFechaAnulacion())
                .usuarioAnulador(o.getUsuarioAnulador())
                .estado(o.getEstado())
                .estadoFinanciero(o.getEstadoFinanciero())
                .estadoExigibilidad(o.getEstadoExigibilidad())
                .versionCalculo(o.getVersionCalculo())
                .normaNombre(o.getNormaAplicable() != null ? o.getNormaAplicable().getNumeroNorma() : null)
                .snapshotDeterminacion(o.getSnapshotDeterminacion())
                .observaciones(o.getObservaciones())
                .motivoAjuste(o.getMotivoAjuste())
                .motivoAnulacion(o.getMotivoAnulacion())
                .resolucionSustento(o.getResolucionSustento())
                .cuotas(cuotas)
                .movimientos(movimientos)
                .historial(historial)
                .build();
    }

    private LoteGeneracionResponse mapearALoteResponse(LoteGeneracionObligaciones lote, List<DetalleLoteGeneracion> detalles) {
        List<DetalleLoteResponse> detResp = detalles.stream().map(d -> DetalleLoteResponse.builder()
                .id(d.getId())
                .determinacionId(d.getDeterminacionId())
                .codigoDeterminacion(d.getCodigoDeterminacion())
                .contribuyenteId(d.getContribuyente().getId())
                .contribuyenteNombre(d.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(d.getContribuyente().getTipoDocumento() + ": " + d.getContribuyente().getNumeroDocumento())
                .obligacionId(d.getObligacion() != null ? d.getObligacion().getId() : null)
                .obligacionCodigo(d.getObligacion() != null ? d.getObligacion().getCodigoObligacion() : null)
                .resultado(d.getResultado())
                .importeGenerado(d.getImporteGenerado())
                .mensajeDetalle(d.getMensajeDetalle())
                .createdAt(d.getCreatedAt())
                .build()
        ).toList();

        return LoteGeneracionResponse.builder()
                .id(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .tipoDeterminacion(lote.getTipoDeterminacion())
                .periodoId(lote.getPeriodo().getId())
                .periodoAnio(lote.getPeriodo().getAnio())
                .totalEvaluadas(lote.getTotalEvaluadas())
                .totalElegibles(lote.getTotalElegibles())
                .totalGeneradas(lote.getTotalGeneradas())
                .totalDuplicadas(lote.getTotalDuplicadas())
                .totalErrores(lote.getTotalErrores())
                .importeTotalGenerado(lote.getImporteTotalGenerado())
                .estado(lote.getEstado())
                .usuarioRegistro(lote.getUsuarioRegistro())
                .observaciones(lote.getObservaciones())
                .createdAt(lote.getCreatedAt())
                .detalles(detResp)
                .build();
    }
}

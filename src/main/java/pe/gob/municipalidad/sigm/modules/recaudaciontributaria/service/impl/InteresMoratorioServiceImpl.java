package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.ParametroInteresRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.ParametroInteresResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.SimulacionInteresResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NormaTributariaConfigurada;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadTemporalInteres;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NormaTributariaConfiguradaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ParametroInteresTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.InteresMoratorioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteresMoratorioServiceImpl implements InteresMoratorioService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal TREINTA = new BigDecimal("30");
    private static final String ADVERTENCIA_LEGAL_ESTANDAR =
            "Cálculo informativo referencial conforme al Art. 33 del TUO del Código Tributario (D.S. 133-2013-EF). " +
            "No capitaliza intereses moratorios ni constituye cargo definitivo en la cuenta corriente tributaria " +
            "sin la debida liquidación u orden de pago notificada formalmente.";

    private final ParametroInteresTributarioRepository parametroRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final ConceptoTributarioRepository conceptoRepository;
    private final NormaTributariaConfiguradaRepository normaRepository;

    @Override
    @Transactional(readOnly = true)
    public SimulacionInteresResponse simularInteresObligacion(UUID obligacionId, LocalDate fechaCorte) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        return calcularInteresParaObligacion(obligacion, fechaCorte);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulacionInteresResponse simularInteresGenerico(SimulacionInteresRequest request) {
        if (request.getObligacionId() != null) {
            return simularInteresObligacion(request.getObligacionId(), request.getFechaCorte());
        }

        BigDecimal capital = request.getMontoCapital() != null ? request.getMontoCapital() : BigDecimal.ZERO;
        LocalDate vencimiento = request.getFechaVencimiento() != null ? request.getFechaVencimiento() : LocalDate.now();
        LocalDate corte = request.getFechaCorte() != null ? request.getFechaCorte() : LocalDate.now();

        return ejecutarCalculo(null, null, capital, vencimiento, corte, request.getParametroId(), request.getConceptoId(), "PEN");
    }

    @Override
    @Transactional(readOnly = true)
    public SimulacionInteresResponse calcularInteresParaObligacion(ObligacionTributaria obligacion, LocalDate fechaCorte) {
        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        BigDecimal capital = obligacion.getSaldo() != null ? obligacion.getSaldo() : BigDecimal.ZERO;
        LocalDate vencimiento = obligacion.getFechaVencimiento();

        UUID conceptoId = obligacion.getConcepto() != null ? obligacion.getConcepto().getId() : null;

        return ejecutarCalculo(
                obligacion.getId(),
                obligacion.getCodigoObligacion(),
                capital,
                vencimiento,
                corte,
                null,
                conceptoId,
                obligacion.getMoneda() != null ? obligacion.getMoneda() : "PEN"
        );
    }

    private SimulacionInteresResponse ejecutarCalculo(
            UUID obligacionId,
            String codigoObligacion,
            BigDecimal capital,
            LocalDate vencimiento,
            LocalDate corte,
            UUID parametroIdFijo,
            UUID conceptoId,
            String moneda) {

        long diasMorosidad = 0;
        if (vencimiento != null && corte.isAfter(vencimiento)) {
            diasMorosidad = ChronoUnit.DAYS.between(vencimiento, corte);
        }

        // Si no hay días de mora o el saldo es cero
        if (diasMorosidad <= 0 || capital.compareTo(BigDecimal.ZERO) <= 0) {
            return SimulacionInteresResponse.builder()
                    .obligacionId(obligacionId)
                    .codigoObligacion(codigoObligacion)
                    .montoCapital(capital)
                    .fechaVencimiento(vencimiento)
                    .fechaCorte(corte)
                    .diasMorosidad(0)
                    .tasaNominal(BigDecimal.ZERO)
                    .unidadTemporal(UnidadTemporalInteres.MENSUAL)
                    .factorDiario(BigDecimal.ZERO)
                    .montoInteresCalculado(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .montoTotalSimulado(capital.setScale(2, RoundingMode.HALF_UP))
                    .moneda(moneda)
                    .baseLegal("No aplica morosidad (obligación al día o cancelada)")
                    .advertenciaLegal(ADVERTENCIA_LEGAL_ESTANDAR)
                    .esAptoParaCalculo(true)
                    .build();
        }

        // Buscar parámetro legal aplicable
        ParametroInteresTributario param;
        if (parametroIdFijo != null) {
            param = parametroRepository.findById(parametroIdFijo).orElse(null);
        } else {
            List<ParametroInteresTributario> candidatos = parametroRepository.findVigenteParaFechaYConcepto(corte, conceptoId);
            param = candidatos.isEmpty() ? null : candidatos.get(0);
        }

        if (param == null) {
            return SimulacionInteresResponse.builder()
                    .obligacionId(obligacionId)
                    .codigoObligacion(codigoObligacion)
                    .montoCapital(capital)
                    .fechaVencimiento(vencimiento)
                    .fechaCorte(corte)
                    .diasMorosidad(diasMorosidad)
                    .tasaNominal(BigDecimal.ZERO)
                    .unidadTemporal(UnidadTemporalInteres.MENSUAL)
                    .factorDiario(BigDecimal.ZERO)
                    .montoInteresCalculado(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .montoTotalSimulado(capital.setScale(2, RoundingMode.HALF_UP))
                    .moneda(moneda)
                    .baseLegal("Sin parámetro normativo vigente para la fecha de corte")
                    .advertenciaLegal(ADVERTENCIA_LEGAL_ESTANDAR)
                    .esAptoParaCalculo(false)
                    .mensajeError("No existe una tasa de interés moratorio formalmente aprobada y vigente para este concepto y período. Por seguridad jurídica, no se inventan tasas.")
                    .build();
        }

        // Cálculo del factor diario conforme a la normativa tributaria
        BigDecimal factorDiario;
        if (param.getUnidadTemporal() == UnidadTemporalInteres.MENSUAL) {
            // Tasa expresada en porcentaje mensual (ej: 0.90%). Factor diario = (0.90 / 100) / 30
            factorDiario = param.getTasa().divide(CIEN, 8, RoundingMode.HALF_UP).divide(TREINTA, 8, RoundingMode.HALF_UP);
        } else if (param.getUnidadTemporal() == UnidadTemporalInteres.DIARIA) {
            factorDiario = param.getTasa().divide(CIEN, 8, RoundingMode.HALF_UP);
        } else { // ANUAL
            factorDiario = param.getTasa().divide(CIEN, 8, RoundingMode.HALF_UP).divide(new BigDecimal("360"), 8, RoundingMode.HALF_UP);
        }

        // Interés = Capital * factorDiario * días
        BigDecimal interes = capital
                .multiply(factorDiario)
                .multiply(BigDecimal.valueOf(diasMorosidad))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = capital.add(interes).setScale(2, RoundingMode.HALF_UP);

        return SimulacionInteresResponse.builder()
                .obligacionId(obligacionId)
                .codigoObligacion(codigoObligacion)
                .montoCapital(capital)
                .fechaVencimiento(vencimiento)
                .fechaCorte(corte)
                .diasMorosidad(diasMorosidad)
                .parametroId(param.getId())
                .codigoParametro(param.getCodigo())
                .nombreParametro(param.getNombre())
                .tipoInteres(param.getTipoInteres())
                .tasaNominal(param.getTasa())
                .unidadTemporal(param.getUnidadTemporal())
                .factorDiario(factorDiario)
                .montoInteresCalculado(interes)
                .montoTotalSimulado(total)
                .moneda(moneda)
                .baseLegal(param.getBaseLegal())
                .advertenciaLegal(ADVERTENCIA_LEGAL_ESTANDAR)
                .esAptoParaCalculo(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametroInteresResponse> listarParametros() {
        LocalDate hoy = LocalDate.now();
        return parametroRepository.findAll().stream()
                .map(p -> mapToResponse(p, hoy))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParametroInteresResponse obtenerParametro(UUID id) {
        ParametroInteresTributario p = parametroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro de interés no encontrado: " + id));
        return mapToResponse(p, LocalDate.now());
    }

    @Override
    @Transactional
    public ParametroInteresResponse crearParametro(ParametroInteresRequest request, String username) {
        if (parametroRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new BusinessException("Ya existe un parámetro de interés con el código: " + request.getCodigo());
        }

        ConceptoTributario concepto = null;
        if (request.getConceptoId() != null) {
            concepto = conceptoRepository.findById(request.getConceptoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concepto tributario no encontrado: " + request.getConceptoId()));
        }

        NormaTributariaConfigurada norma = null;
        if (request.getNormaId() != null) {
            norma = normaRepository.findById(request.getNormaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Norma tributaria no encontrada: " + request.getNormaId()));
        }

        ParametroInteresTributario entity = ParametroInteresTributario.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .concepto(concepto)
                .tipoInteres(request.getTipoInteres())
                .tasa(request.getTasa())
                .unidadTemporal(request.getUnidadTemporal())
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .norma(norma)
                .baseLegal(request.getBaseLegal().trim())
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .build();
        entity.setCreatedBy(username);

        entity = parametroRepository.save(entity);
        log.info("Parámetro de interés {} creado por {}", entity.getCodigo(), username);
        return mapToResponse(entity, LocalDate.now());
    }

    @Override
    @Transactional
    public ParametroInteresResponse actualizarParametro(UUID id, ParametroInteresRequest request, String username) {
        ParametroInteresTributario entity = parametroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro de interés no encontrado: " + id));

        ConceptoTributario concepto = null;
        if (request.getConceptoId() != null) {
            concepto = conceptoRepository.findById(request.getConceptoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concepto tributario no encontrado: " + request.getConceptoId()));
        }

        NormaTributariaConfigurada norma = null;
        if (request.getNormaId() != null) {
            norma = normaRepository.findById(request.getNormaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Norma tributaria no encontrada: " + request.getNormaId()));
        }

        entity.setNombre(request.getNombre().trim());
        entity.setConcepto(concepto);
        entity.setTipoInteres(request.getTipoInteres());
        entity.setTasa(request.getTasa());
        entity.setUnidadTemporal(request.getUnidadTemporal());
        entity.setFechaInicioVigencia(request.getFechaInicioVigencia());
        entity.setFechaFinVigencia(request.getFechaFinVigencia());
        entity.setNorma(norma);
        entity.setBaseLegal(request.getBaseLegal().trim());
        if (request.getEstado() != null) {
            entity.setEstado(request.getEstado());
        }
        entity.setUpdatedBy(username);

        entity = parametroRepository.save(entity);
        log.info("Parámetro de interés {} actualizado por {}", entity.getCodigo(), username);
        return mapToResponse(entity, LocalDate.now());
    }

    private ParametroInteresResponse mapToResponse(ParametroInteresTributario p, LocalDate hoy) {
        boolean esVigente = Boolean.TRUE.equals(p.getEstado())
                && !hoy.isBefore(p.getFechaInicioVigencia())
                && (p.getFechaFinVigencia() == null || !hoy.isAfter(p.getFechaFinVigencia()));

        return ParametroInteresResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .conceptoId(p.getConcepto() != null ? p.getConcepto().getId() : null)
                .conceptoNombre(p.getConcepto() != null ? p.getConcepto().getNombre() : "TODOS LOS CONCEPTOS TRIBUTARIOS")
                .tipoInteres(p.getTipoInteres())
                .tasa(p.getTasa())
                .unidadTemporal(p.getUnidadTemporal())
                .fechaInicioVigencia(p.getFechaInicioVigencia())
                .fechaFinVigencia(p.getFechaFinVigencia())
                .normaId(p.getNorma() != null ? p.getNorma().getId() : null)
                .normaNombre(p.getNorma() != null ? p.getNorma().getNumeroNorma() : null)
                .baseLegal(p.getBaseLegal())
                .estado(p.getEstado())
                .esVigente(esVigente)
                .build();
    }
}

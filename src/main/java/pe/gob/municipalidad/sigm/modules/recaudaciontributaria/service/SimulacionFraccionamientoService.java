package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.CuotaSimuladaDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.SimulacionFraccionamientoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.SimulacionFraccionamientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ProgramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoProgramaFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ParametroFraccionamientoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ProgramaFraccionamientoTributarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de simulación financiera de fraccionamiento tributario municipal.
 * Opera exclusivamente como simulación matemática sin efecto vinculante ni generación de cargos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulacionFraccionamientoService {

    private final ProgramaFraccionamientoTributarioRepository programaRepository;
    private final ParametroFraccionamientoTributarioRepository parametroRepository;
    private final ObligacionTributariaRepository obligacionRepository;

    public SimulacionFraccionamientoResponse simular(SimulacionFraccionamientoRequest request) {
        if (request == null || request.getProgramaId() == null) {
            throw new BusinessException("El programa de fraccionamiento es obligatorio para la simulación");
        }

        LocalDate fechaCorte = request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now();

        ProgramaFraccionamientoTributario programa = programaRepository.findById(request.getProgramaId())
                .orElseThrow(() -> new BusinessException("Programa de fraccionamiento no encontrado"));

        if (programa.getEstado() != EstadoProgramaFraccionamiento.ACTIVO) {
            throw new BusinessException("El programa de fraccionamiento seleccionado no se encuentra activo");
        }

        // 1. Determinar importe base
        BigDecimal importeBase = BigDecimal.ZERO;
        if (request.getObligacionIds() != null && !request.getObligacionIds().isEmpty()) {
            for (UUID oblId : request.getObligacionIds()) {
                ObligacionTributaria obl = obligacionRepository.findById(oblId)
                        .orElseThrow(() -> new BusinessException("Obligación tributaria no encontrada: " + oblId));
                if (obl.getSaldo() == null || obl.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("La obligación " + obl.getCodigoObligacion() + " no tiene saldo pendiente");
                }
                importeBase = importeBase.add(obl.getSaldo());
            }
        } else if (request.getImporteBase() != null && request.getImporteBase().compareTo(BigDecimal.ZERO) > 0) {
            importeBase = request.getImporteBase();
        } else {
            throw new BusinessException("Debe especificar obligaciones válidas o un importe base positivo a simular");
        }

        // 2. Parámetros normativos
        BigDecimal montoMinimo = obtenerParametroNumerico(programa.getId(), "MONTO_MINIMO_ELEGIBLE", fechaCorte, new BigDecimal("150.00"));
        if (importeBase.compareTo(montoMinimo) < 0) {
            throw new BusinessException(String.format("El importe a fraccionar (S/ %s) no alcanza el monto mínimo normativo (S/ %s)",
                    importeBase, montoMinimo));
        }

        int cuotasMax = obtenerParametroNumerico(programa.getId(), "CUOTAS_MAXIMAS", fechaCorte, new BigDecimal("24")).intValue();
        int cuotasMin = obtenerParametroNumerico(programa.getId(), "CUOTAS_MINIMAS", fechaCorte, new BigDecimal("2")).intValue();
        int cuotasSolicitadas = request.getNumeroCuotas() != null ? request.getNumeroCuotas() : cuotasMin;

        if (cuotasSolicitadas < cuotasMin || cuotasSolicitadas > cuotasMax) {
            throw new BusinessException(String.format("El número de cuotas (%d) debe estar comprendido entre %d y %d cuotas",
                    cuotasSolicitadas, cuotasMin, cuotasMax));
        }

        BigDecimal pctCuotaInicialMin = obtenerParametroNumerico(programa.getId(), "PORCENTAJE_CUOTA_INICIAL_MIN", fechaCorte, BigDecimal.ZERO);
        BigDecimal cuotaInicialMinima = importeBase.multiply(pctCuotaInicialMin).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal cuotaInicial = request.getImporteCuotaInicial() != null ? request.getImporteCuotaInicial() : cuotaInicialMinima;
        if (cuotaInicial.compareTo(cuotaInicialMinima) < 0) {
            throw new BusinessException(String.format("La cuota inicial (S/ %s) no puede ser menor al %s%% normativo (S/ %s)",
                    cuotaInicial, pctCuotaInicialMin, cuotaInicialMinima));
        }

        if (cuotaInicial.compareTo(importeBase) >= 0) {
            throw new BusinessException("La cuota inicial no puede ser igual o mayor al total de la deuda base");
        }

        BigDecimal importeFinanciado = importeBase.subtract(cuotaInicial);
        BigDecimal tasaMensual = obtenerParametroNumerico(programa.getId(), "TASA_INTERES_MENSUAL", fechaCorte, new BigDecimal("0.7200"));
        int periodicidadDias = obtenerParametroNumerico(programa.getId(), "PERIODICIDAD_DIAS", fechaCorte, new BigDecimal("30")).intValue();

        // 3. Generación matemática del cronograma
        BigDecimal capitalPorCuota = importeFinanciado.divide(BigDecimal.valueOf(cuotasSolicitadas), 2, RoundingMode.HALF_UP);
        BigDecimal factorTasa = tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);

        List<CuotaSimuladaDto> cuotas = new ArrayList<>();
        BigDecimal saldoCapitalRestante = importeFinanciado;
        BigDecimal sumaCapital = BigDecimal.ZERO;
        BigDecimal sumaIntereses = BigDecimal.ZERO;

        LocalDate fechaVenc = fechaCorte.plusDays(periodicidadDias);
        LocalDate fechaPrimeraCuota = fechaVenc;

        for (int i = 1; i <= cuotasSolicitadas; i++) {
            BigDecimal capitalCuota;
            if (i == cuotasSolicitadas) {
                // Ajuste de redondeo final para cuadrar exactamente el importe financiado
                capitalCuota = importeFinanciado.subtract(sumaCapital);
            } else {
                capitalCuota = capitalPorCuota;
            }

            // Interés simple sobre saldo de capital según Art. 36 Código Tributario
            BigDecimal interesCuota = saldoCapitalRestante.multiply(factorTasa).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalCuota = capitalCuota.add(interesCuota);

            saldoCapitalRestante = saldoCapitalRestante.subtract(capitalCuota);
            if (saldoCapitalRestante.compareTo(BigDecimal.ZERO) < 0) {
                saldoCapitalRestante = BigDecimal.ZERO;
            }

            sumaCapital = sumaCapital.add(capitalCuota);
            sumaIntereses = sumaIntereses.add(interesCuota);

            cuotas.add(CuotaSimuladaDto.builder()
                    .numeroCuota(i)
                    .fechaVencimiento(fechaVenc)
                    .capital(capitalCuota)
                    .interes(interesCuota)
                    .totalCuota(totalCuota)
                    .saldoRestante(saldoCapitalRestante)
                    .build());

            fechaVenc = fechaVenc.plusDays(periodicidadDias);
        }

        BigDecimal totalFinanciadoConIntereses = importeFinanciado.add(sumaIntereses);

        return SimulacionFraccionamientoResponse.builder()
                .programaCodigo(programa.getCodigo())
                .programaNombre(programa.getNombre())
                .importeBase(importeBase)
                .cuotaInicial(cuotaInicial)
                .importeFinanciado(importeFinanciado)
                .tasaInteresMensual(tasaMensual)
                .totalIntereses(sumaIntereses)
                .importeTotalFinanciado(totalFinanciadoConIntereses)
                .numeroCuotas(cuotasSolicitadas)
                .fechaPrimeraCuota(fechaPrimeraCuota)
                .cuotas(cuotas)
                .notaLegal("Simulación de carácter estrictamente informativo. No genera compromiso financiero ni altera vencimientos hasta la aprobación formal del convenio.")
                .build();
    }

    private BigDecimal obtenerParametroNumerico(UUID programaId, String codigo, LocalDate fecha, BigDecimal valorDefecto) {
        return parametroRepository.findPrimerVigente(programaId, codigo, fecha)
                .map(ParametroFraccionamientoTributario::getValorNumerico)
                .orElse(valorDefecto);
    }
}

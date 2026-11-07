package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConservacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MaterialPredominante;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ValorizacionPredialService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValorizacionPredialServiceImpl implements ValorizacionPredialService {

    private static final BigDecimal MAX_DEPRECIACION = new BigDecimal("70.00");
    private static final BigDecimal CIEN = new BigDecimal("100.00");

    @Override
    public CalculoPredialDetalle valorizarPredio(
            PredioTributario predio,
            BigDecimal porcentajeParticipacion,
            Integer anioFiscal,
            BigDecimal uit,
            ConfiguracionConceptoTributario configuracion,
            List<String> advertencias,
            List<String> explicaciones) {

        log.debug("Iniciando valorización de predio: {}", predio.getCodigoPredio());

        if (porcentajeParticipacion == null || porcentajeParticipacion.compareTo(BigDecimal.ZERO) <= 0) {
            porcentajeParticipacion = new BigDecimal("100.00");
        }

        // 1. Superficie de Terreno
        BigDecimal areaTerreno = predio.getAreaTerrenoM2() != null && predio.getAreaTerrenoM2().compareTo(BigDecimal.ZERO) > 0
                ? predio.getAreaTerrenoM2()
                : (predio.getAreaTerreno() != null ? predio.getAreaTerreno() : BigDecimal.ZERO);

        if (areaTerreno.compareTo(BigDecimal.ZERO) <= 0) {
            advertencias.add("El predio " + predio.getCodigoPredio() + " no registra superficie de terreno válida (0.00 m2).");
        }

        // 2. Arancel Unitario
        BigDecimal arancelUnitario = determinarArancelUnitario(predio, configuracion);
        BigDecimal valorTerreno = areaTerreno.multiply(arancelUnitario).setScale(2, RoundingMode.HALF_UP);

        explicaciones.add(String.format("Predio %s: Terreno %s m2 a S/ %s/m2 = S/ %s",
                predio.getCodigoPredio(), areaTerreno, arancelUnitario, valorTerreno));

        // 3. Edificaciones / Construcciones
        BigDecimal totalAreaConstruida = BigDecimal.ZERO;
        BigDecimal valorTotalConstrucciones = BigDecimal.ZERO;
        int cantidadConstrucciones = 0;

        if (predio.getConstrucciones() != null && !predio.getConstrucciones().isEmpty()) {
            for (ConstruccionPredial constr : predio.getConstrucciones()) {
                cantidadConstrucciones++;
                BigDecimal areaC = constr.getAreaConstruida() != null ? constr.getAreaConstruida() : BigDecimal.ZERO;
                totalAreaConstruida = totalAreaConstruida.add(areaC);

                BigDecimal valorUnitarioEdif = determinarValorUnitarioEdificacion(constr.getMaterialPredominante(), configuracion);
                BigDecimal porcentajeDepreciacion = calcularPorcentajeDepreciacion(constr, anioFiscal);

                // Factor depreciado = 1 - (depreciacion / 100)
                BigDecimal factorDepreciado = BigDecimal.ONE.subtract(
                        porcentajeDepreciacion.divide(CIEN, 4, RoundingMode.HALF_UP)
                );

                BigDecimal valorConstruccionItem = areaC.multiply(valorUnitarioEdif)
                        .multiply(factorDepreciado)
                        .setScale(2, RoundingMode.HALF_UP);

                valorTotalConstrucciones = valorTotalConstrucciones.add(valorConstruccionItem);

                explicaciones.add(String.format("  - Piso/Bloque %s (%s, %s): %s m2 a S/ %s/m2 (Deprec: %s%%) = S/ %s",
                        constr.getCodigoConstruccion() != null ? constr.getCodigoConstruccion() : String.valueOf(constr.getNivelPiso()),
                        constr.getMaterialPredominante(), constr.getEstadoConservacion(),
                        areaC, valorUnitarioEdif, porcentajeDepreciacion, valorConstruccionItem));
            }
        } else if (predio.getAreaConstruida() != null && predio.getAreaConstruida().compareTo(BigDecimal.ZERO) > 0) {
            // Predio con área construida declarada sin desglose pormenorizado
            cantidadConstrucciones = 1;
            totalAreaConstruida = predio.getAreaConstruida();
            BigDecimal valorUnitarioEdif = determinarValorUnitarioEdificacion(MaterialPredominante.ADOBE_TAPIA, configuracion);
            BigDecimal porcentajeDepreciacion = new BigDecimal("10.00"); // Depreciación regular por defecto
            BigDecimal factorDepreciado = BigDecimal.ONE.subtract(porcentajeDepreciacion.divide(CIEN, 4, RoundingMode.HALF_UP));

            valorTotalConstrucciones = totalAreaConstruida.multiply(valorUnitarioEdif)
                    .multiply(factorDepreciado)
                    .setScale(2, RoundingMode.HALF_UP);

            advertencias.add("El predio " + predio.getCodigoPredio() + " no tiene construcciones detalladas por bloque. Se valorizó sobre área general de " + totalAreaConstruida + " m2.");
        }

        // 4. Instalaciones Fijas y Permanentes (referencial o nulo si no existe)
        BigDecimal valorInstalaciones = BigDecimal.ZERO;

        // 5. Autovalúo Total y Porcentaje Atribuible (Copropiedad)
        BigDecimal autovaluoTotal = valorTerreno.add(valorTotalConstrucciones).add(valorInstalaciones).setScale(2, RoundingMode.HALF_UP);
        BigDecimal factorParticipacion = porcentajeParticipacion.divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal autovaluoImputable = autovaluoTotal.multiply(factorParticipacion).setScale(2, RoundingMode.HALF_UP);

        if (porcentajeParticipacion.compareTo(CIEN) < 0) {
            explicaciones.add(String.format("Predio %s: Autovalúo Total S/ %s x %s%% participación = S/ %s imputable",
                    predio.getCodigoPredio(), autovaluoTotal, porcentajeParticipacion, autovaluoImputable));
        }

        return CalculoPredialDetalle.builder()
                .predio(predio)
                .codigoPredio(predio.getCodigoPredio())
                .direccionPredio(predio.getDireccion() != null ? predio.getDireccion() : "Sin dirección declarada")
                .tipoPredio(predio.getTipoPredio() != null ? predio.getTipoPredio().name() : "URBANO")
                .clasificacionPredio(predio.getClasificacionPredio() != null ? predio.getClasificacionPredio().name() : "CASA_HABITACION")
                .porcentajeParticipacion(porcentajeParticipacion)
                .areaTerreno(areaTerreno)
                .unidadMedidaTerreno(predio.getUnidadMedidaArea() != null ? predio.getUnidadMedidaArea() : "M2")
                .arancelUnitarioM2(arancelUnitario)
                .valorTerreno(valorTerreno)
                .cantidadConstrucciones(cantidadConstrucciones)
                .areaTotalConstruida(totalAreaConstruida)
                .valorConstrucciones(valorTotalConstrucciones)
                .valorInstalaciones(valorInstalaciones)
                .valorAutovaluoTotal(autovaluoTotal)
                .valorAutovaluoImputable(autovaluoImputable)
                .build();
    }

    private BigDecimal determinarArancelUnitario(PredioTributario predio, ConfiguracionConceptoTributario configuracion) {
        if (predio.getValorArancelarioReferencial() != null && predio.getValorArancelarioReferencial().compareTo(BigDecimal.ZERO) > 0) {
            return predio.getValorArancelarioReferencial().setScale(2, RoundingMode.HALF_UP);
        }

        if (configuracion != null && configuracion.getParametros() != null) {
            for (ParametroTributario p : configuracion.getParametros()) {
                if ("ARANCEL_BASE_M2".equalsIgnoreCase(p.getCodigo()) && p.getValorDecimal() != null) {
                    return p.getValorDecimal().setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        // Aranceles oficiales referenciales aprobados para la zona de Quinua
        if ("RUSTICO".equalsIgnoreCase(predio.getTipoPredio() != null ? predio.getTipoPredio().name() : "")) {
            return new BigDecimal("6.50");
        }
        return new BigDecimal("35.00"); // Base urbana referencial
    }

    private BigDecimal determinarValorUnitarioEdificacion(MaterialPredominante material, ConfiguracionConceptoTributario configuracion) {
        if (configuracion != null && configuracion.getParametros() != null && material != null) {
            String claveParam = "VALOR_EDIF_" + material.name();
            for (ParametroTributario p : configuracion.getParametros()) {
                if (claveParam.equalsIgnoreCase(p.getCodigo()) && p.getValorDecimal() != null) {
                    return p.getValorDecimal().setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        if (material == null) {
            return new BigDecimal("220.00");
        }

        return switch (material) {
            case CONCRETO, LADRILLO_CONCRETO -> new BigDecimal("850.00");
            case LADRILLO -> new BigDecimal("520.00");
            case ADOBE_TAPIA -> new BigDecimal("220.00");
            case MADERA -> new BigDecimal("280.00");
            case QUINCHA -> new BigDecimal("150.00");
            default -> new BigDecimal("180.00");
        };
    }

    private BigDecimal calcularPorcentajeDepreciacion(ConstruccionPredial constr, Integer anioFiscal) {
        int anioConstruccion = constr.getAnioConstruccion() != null ? constr.getAnioConstruccion() : (anioFiscal != null ? anioFiscal : 2026);
        int antiguedad = (anioFiscal != null ? anioFiscal : 2026) - anioConstruccion;
        if (antiguedad < 0) {
            antiguedad = 0;
        }

        MaterialPredominante mat = constr.getMaterialPredominante() != null ? constr.getMaterialPredominante() : MaterialPredominante.ADOBE_TAPIA;
        EstadoConservacion estado = constr.getEstadoConservacion() != null ? constr.getEstadoConservacion() : EstadoConservacion.REGULAR;

        // Tasa anual de depreciación por material
        BigDecimal tasaAnual = switch (mat) {
            case CONCRETO, LADRILLO_CONCRETO -> new BigDecimal("1.00");
            case LADRILLO -> new BigDecimal("1.30");
            case ADOBE_TAPIA -> new BigDecimal("2.00");
            case MADERA -> new BigDecimal("2.50");
            case QUINCHA -> new BigDecimal("3.00");
            default -> new BigDecimal("1.80");
        };

        // Factor adicional por estado de conservación
        BigDecimal factorConservacion = switch (estado) {
            case MUY_BUENO -> new BigDecimal("0.00");
            case BUENO -> new BigDecimal("2.00");
            case REGULAR -> new BigDecimal("5.00");
            case MALO -> new BigDecimal("15.00");
            default -> new BigDecimal("5.00");
        };

        BigDecimal depCalculada = tasaAnual.multiply(new BigDecimal(antiguedad)).add(factorConservacion);

        if (depCalculada.compareTo(MAX_DEPRECIACION) > 0) {
            return MAX_DEPRECIACION;
        }
        if (depCalculada.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return depCalculada.setScale(2, RoundingMode.HALF_UP);
    }
}

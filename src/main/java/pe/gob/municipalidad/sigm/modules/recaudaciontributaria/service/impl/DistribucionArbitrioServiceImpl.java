package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UsoDeclaradoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DistribucionArbitrioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class DistribucionArbitrioServiceImpl implements DistribucionArbitrioService {

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal DOCE = new BigDecimal("12.00");

    @Override
    public DetalleCalculoArbitrio calcularServicioArbitrio(
            PredioTributario predio,
            ConfiguracionArbitrioMunicipal config,
            TipoBeneficioArbitrio beneficio,
            BigDecimal porcentajeParticipacion,
            List<String> advertencias,
            List<String> explicaciones) {

        TipoServicioArbitrio tipo = config.getServicio().getTipoServicio();
        String codigoServicio = config.getServicio().getCodigo();
        String nombreServicio = config.getServicio().getNombre();

        BigDecimal part = porcentajeParticipacion != null && porcentajeParticipacion.compareTo(BigDecimal.ZERO) > 0
                ? porcentajeParticipacion
                : CIEN;

        BigDecimal parametroBase;
        String unidadBase;
        BigDecimal tasaUnitarioMensual;
        BigDecimal costoMensualBruto;
        String explicacionFormula;

        switch (tipo) {
            case RECOLECCION_RESIDUOS -> {
                // Distribución por uso/actividad y área techada
                BigDecimal factorUso = obtenerFactorUsoRecoleccion(predio.getUsoDeclarado());
                BigDecimal areaConstruida = predio.getAreaConstruida() != null ? predio.getAreaConstruida() : BigDecimal.ZERO;
                BigDecimal factorArea = BigDecimal.ONE;
                if (areaConstruida.compareTo(new BigDecimal("200.00")) > 0) {
                    BigDecimal exceso100m2 = areaConstruida.subtract(new BigDecimal("200.00"))
                            .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
                    factorArea = factorArea.add(exceso100m2.multiply(new BigDecimal("0.10")));
                }
                parametroBase = factorUso.multiply(factorArea).setScale(4, RoundingMode.HALF_UP);
                unidadBase = "FACTOR_USO_VOL";

                // Tasa unitaria mensual referencial según costo de configuración
                tasaUnitarioMensual = determinarTasaBaseMensual(config, new BigDecimal("6.50"));
                costoMensualBruto = tasaUnitarioMensual.multiply(parametroBase).setScale(2, RoundingMode.HALF_UP);

                explicacionFormula = String.format("Recolección Residuos: Factor Uso %s (%s) x Factor Área %s x Tasa S/ %s/mes = S/ %s/mes",
                        factorUso, predio.getUsoDeclarado(), factorArea, tasaUnitarioMensual, costoMensualBruto);
            }

            case BARRIDO_CALLES -> {
                // Distribución por frente lineal de fachada a vía pública
                BigDecimal frenteLineal = estimarFrenteLinealMetros(predio);
                parametroBase = frenteLineal;
                unidadBase = "METROS_LINEALES";

                tasaUnitarioMensual = determinarTasaBaseMensual(config, new BigDecimal("0.45"));
                costoMensualBruto = tasaUnitarioMensual.multiply(frenteLineal).setScale(2, RoundingMode.HALF_UP);

                explicacionFormula = String.format("Barrido de Calles: Frente lineal %s m x Tasa S/ %s/m.l. = S/ %s/mes",
                        frenteLineal, tasaUnitarioMensual, costoMensualBruto);
            }

            case PARQUES_JARDINES -> {
                // Criterio de ubicación y zonificación verde urbana
                if (predio.getTipoPredio() == TipoPredio.RURAL) {
                    parametroBase = BigDecimal.ZERO;
                    unidadBase = "COEF_URBANO";
                    tasaUnitarioMensual = BigDecimal.ZERO;
                    costoMensualBruto = BigDecimal.ZERO;
                    advertencias.add("El predio es RURAL. No corresponde cobro de Parques y Jardines por carecer de servicio público de áreas verdes distritales en su radio.");
                    explicacionFormula = "Parques y Jardines: Predio RURAL inafecto de la prestación directa (S/ 0.00/mes).";
                } else {
                    BigDecimal factorZonal = new BigDecimal("1.00");
                    if (predio.getClasificacionPredio() != null) {
                        factorZonal = switch (predio.getClasificacionPredio()) {
                            case COMERCIAL, INDUSTRIAL -> new BigDecimal("1.25");
                            case TERRENO_SIN_CONSTRUIR -> new BigDecimal("0.50");
                            default -> new BigDecimal("1.00");
                        };
                    }
                    parametroBase = factorZonal;
                    unidadBase = "FACTOR_ZONAL";
                    tasaUnitarioMensual = determinarTasaBaseMensual(config, new BigDecimal("3.20"));
                    costoMensualBruto = tasaUnitarioMensual.multiply(factorZonal).setScale(2, RoundingMode.HALF_UP);

                    explicacionFormula = String.format("Parques y Jardines: Coeficiente Zonal %s x Tasa S/ %s/mes = S/ %s/mes",
                            factorZonal, tasaUnitarioMensual, costoMensualBruto);
                }
            }

            case SERENAZGO -> {
                // Distribución por nivel de riesgo del sector y uso del predio
                BigDecimal factorSector = new BigDecimal("1.00");
                if (predio.getSector() != null && predio.getSector().getNombre() != null) {
                    String secNom = predio.getSector().getNombre().toUpperCase();
                    if (secNom.contains("PLAZA") || secNom.contains("CENTRO") || secNom.contains("HISTORICO")) {
                        factorSector = new BigDecimal("1.20"); // Mayor frecuencia de patrullaje
                    } else if (secNom.contains("PERIFER") || secNom.contains("ALTO")) {
                        factorSector = new BigDecimal("0.80");
                    }
                }

                UsoDeclaradoPredio usoDeclarado = predio.getUsoDeclarado();
                BigDecimal factorActividad = BigDecimal.ONE;
                if (usoDeclarado != null) {
                    factorActividad = switch (usoDeclarado) {
                        case COMERCIO, ARTESANAL, MIXTO -> new BigDecimal("1.35");
                        case DESOCUPADO, AGRICOLA, AGROPECUARIO -> new BigDecimal("0.40");
                        case GOBIERNO, INSTITUCIONAL, EDUCATIVO_CULTURAL, SALUD, CULTO_EDUCACION -> new BigDecimal("1.20");
                        default -> new BigDecimal("1.00");
                    };
                }

                parametroBase = factorSector.multiply(factorActividad).setScale(4, RoundingMode.HALF_UP);
                unidadBase = "INDICE_VULNERABILIDAD";

                tasaUnitarioMensual = determinarTasaBaseMensual(config, new BigDecimal("5.00"));
                costoMensualBruto = tasaUnitarioMensual.multiply(parametroBase).setScale(2, RoundingMode.HALF_UP);

                explicacionFormula = String.format("Serenazgo: Factor Sector %s x Factor Actividad %s x Tasa S/ %s = S/ %s/mes",
                        factorSector, factorActividad, tasaUnitarioMensual, costoMensualBruto);
            }

            default -> {
                parametroBase = BigDecimal.ONE;
                unidadBase = "CUOTA_FIJA";
                tasaUnitarioMensual = determinarTasaBaseMensual(config, new BigDecimal("4.00"));
                costoMensualBruto = tasaUnitarioMensual;
                explicacionFormula = String.format("Otros Servicios: Cuota fija referencial S/ %s/mes", costoMensualBruto);
            }
        }

        // Beneficio y Exoneración
        BigDecimal porcentajeDesc = BigDecimal.ZERO;
        if (beneficio != null && beneficio != TipoBeneficioArbitrio.NINGUNO) {
            porcentajeDesc = BigDecimal.valueOf(beneficio.getPorcentajeDescuento());
        }

        BigDecimal factorBeneficio = BigDecimal.ONE.subtract(porcentajeDesc.divide(CIEN, 4, RoundingMode.HALF_UP));
        BigDecimal factorCopropiedad = part.divide(CIEN, 4, RoundingMode.HALF_UP);

        BigDecimal costoAnualBruto = costoMensualBruto.multiply(DOCE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuentoMensual = costoMensualBruto.multiply(porcentajeDesc.divide(CIEN, 4, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal importeFinalMensual = costoMensualBruto.subtract(descuentoMensual)
                .multiply(factorCopropiedad)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal importeFinalAnual = importeFinalMensual.multiply(DOCE).setScale(2, RoundingMode.HALF_UP);

        explicaciones.add(explicacionFormula);
        if (porcentajeDesc.compareTo(BigDecimal.ZERO) > 0) {
            explicaciones.add(String.format("  - Beneficio aplicable: %s (%s%% de descuento).", beneficio.getDescripcion(), porcentajeDesc));
        }
        if (part.compareTo(CIEN) < 0) {
            explicaciones.add(String.format("  - Participación de copropiedad: %s%% sobre la cuota imputada.", part));
        }

        return DetalleCalculoArbitrio.builder()
                .servicio(config.getServicio())
                .configuracion(config)
                .codigoServicio(codigoServicio)
                .nombreServicio(nombreServicio)
                .tipoServicio(tipo)
                .criterioAplicado(config.getMetodologiaDistribucion().getDescripcion())
                .parametroBaseValor(parametroBase)
                .unidadBase(unidadBase)
                .tasaUnitarioMensual(tasaUnitarioMensual)
                .costoMensualServicio(costoMensualBruto)
                .costoAnualServicio(costoAnualBruto)
                .descuentoAplicado(descuentoMensual.multiply(DOCE).setScale(2, RoundingMode.HALF_UP))
                .importeFinalMensual(importeFinalMensual)
                .importeFinalAnual(importeFinalAnual)
                .explicacionDetalle(explicacionFormula)
                .build();
    }

    @Override
    public BigDecimal estimarFrenteLinealMetros(PredioTributario predio) {
        // Si el predio tiene un área definida de terreno, calcular estimación geométrica típica: sqrt(area) * 0.6
        BigDecimal area = predio.getAreaTerrenoM2() != null && predio.getAreaTerrenoM2().compareTo(BigDecimal.ZERO) > 0
                ? predio.getAreaTerrenoM2()
                : (predio.getAreaTerreno() != null ? predio.getAreaTerreno() : BigDecimal.ZERO);

        if (area.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("6.00"); // Frente mínimo urbano por defecto
        }

        double areaDouble = area.doubleValue();
        double estimacion = Math.sqrt(areaDouble) * 0.60;
        BigDecimal frente = BigDecimal.valueOf(estimacion).setScale(2, RoundingMode.HALF_UP);
        return frente.max(new BigDecimal("6.00")).min(new BigDecimal("50.00"));
    }

    private BigDecimal obtenerFactorUsoRecoleccion(UsoDeclaradoPredio uso) {
        if (uso == null) return BigDecimal.ONE;
        return switch (uso) {
            case COMERCIO -> new BigDecimal("1.50");
            case ARTESANAL, MIXTO -> new BigDecimal("1.25");
            case EDUCATIVO_CULTURAL, SALUD, INSTITUCIONAL, GOBIERNO, CULTO_EDUCACION -> new BigDecimal("1.30");
            case DESOCUPADO, AGRICOLA, AGROPECUARIO -> new BigDecimal("0.30");
            default -> new BigDecimal("1.00");
        };
    }

    private BigDecimal determinarTasaBaseMensual(ConfiguracionArbitrioMunicipal config, BigDecimal fallbackDefault) {
        if (config.getCostoTotalMensual() != null && config.getCostoTotalMensual().compareTo(BigDecimal.ZERO) > 0
                && config.getCantidadPrediosAfectos() != null && config.getCantidadPrediosAfectos() > 0) {
            return config.getCostoTotalMensual().divide(
                    BigDecimal.valueOf(config.getCantidadPrediosAfectos()), 4, RoundingMode.HALF_UP
            );
        }
        return fallbackDefault;
    }
}

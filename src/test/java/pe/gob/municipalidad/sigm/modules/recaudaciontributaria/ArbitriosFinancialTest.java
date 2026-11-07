package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ServicioArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DistribucionArbitrioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl.DistribucionArbitrioServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArbitriosFinancialTest {

    private final DistribucionArbitrioService distribucionService = new DistribucionArbitrioServiceImpl();

    private ConfiguracionArbitrioMunicipal crearConfiguracion(TipoServicioArbitrio tipo, BigDecimal costoTotalMensual, Integer prediosAfectos) {
        ServicioArbitrioMunicipal srv = ServicioArbitrioMunicipal.builder()
                .codigo("SRV-" + tipo.name())
                .nombre(tipo.getDescripcion())
                .tipoServicio(tipo)
                .build();

        return ConfiguracionArbitrioMunicipal.builder()
                .servicio(srv)
                .costoTotalMensual(costoTotalMensual)
                .costoTotalAnual(costoTotalMensual.multiply(new BigDecimal("12.00")))
                .cantidadPrediosAfectos(prediosAfectos)
                .build();
    }

    @Test
    @DisplayName("1. Precisión Financiera: Distribución de Limpieza Pública Residuos Sólidos según uso y área")
    void testDistribucionResiduosSolidos() {
        PredioTributario predio = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .areaConstruida(new BigDecimal("120.00"))
                .areaTerrenoM2(new BigDecimal("150.00"))
                .build();

        // 4000 mensual / 800 predios = 5.00 soles tasa base mensual
        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.RECOLECCION_RESIDUOS, new BigDecimal("4000.00"), 800);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predio,
                config,
                TipoBeneficioArbitrio.NINGUNO,
                new BigDecimal("100.00"),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertNotNull(det);
        assertEquals(TipoServicioArbitrio.RECOLECCION_RESIDUOS, det.getTipoServicio());
        assertTrue(det.getParametroBaseValor().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(det.getCostoAnualServicio().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), det.getDescuentoAplicado());
        assertEquals(det.getCostoAnualServicio(), det.getImporteFinalAnual());
    }

    @Test
    @DisplayName("2. Precisión Financiera: Distribución de Barrido de Calles según frontis lineal")
    void testDistribucionBarridoCalles() {
        PredioTributario predio = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .areaTerrenoM2(new BigDecimal("100.00"))
                .build();

        // 2000 mensual / 1000 predios = 2.00 tasa base
        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.BARRIDO_CALLES, new BigDecimal("2000.00"), 1000);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predio,
                config,
                TipoBeneficioArbitrio.NINGUNO,
                new BigDecimal("100.00"),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertNotNull(det);
        assertEquals(new BigDecimal("6.00"), det.getParametroBaseValor());
        assertTrue(det.getImporteFinalAnual().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("3. Control Constitucional: Predios Rurales están Exonerados de Parques y Jardines")
    void testExclusionPredioRuralParquesJardines() {
        PredioTributario predioRural = PredioTributario.builder()
                .tipoPredio(TipoPredio.RURAL)
                .areaTerrenoM2(new BigDecimal("5000.00"))
                .build();

        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.PARQUES_JARDINES, new BigDecimal("1500.00"), 500);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predioRural,
                config,
                TipoBeneficioArbitrio.NINGUNO,
                new BigDecimal("100.00"),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertNotNull(det);
        assertEquals(0, BigDecimal.ZERO.compareTo(det.getParametroBaseValor()));
        assertEquals(0, BigDecimal.ZERO.compareTo(det.getCostoAnualServicio()));
        assertEquals(0, BigDecimal.ZERO.compareTo(det.getImporteFinalAnual()));
    }

    @Test
    @DisplayName("4. Precisión Financiera: Parques y Jardines en Zona Urbana")
    void testParquesJardinesZonaUrbana() {
        PredioTributario predioUrbano = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .areaTerrenoM2(new BigDecimal("150.00"))
                .build();

        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.PARQUES_JARDINES, new BigDecimal("1500.00"), 500);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predioUrbano,
                config,
                TipoBeneficioArbitrio.NINGUNO,
                new BigDecimal("100.00"),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertNotNull(det);
        assertTrue(det.getImporteFinalAnual().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("5. Copropiedad: Participación al 50% divide proporcionalmente el importe anual")
    void testFactorCopropiedad() {
        PredioTributario predio = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .areaTerrenoM2(new BigDecimal("100.00"))
                .build();

        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.BARRIDO_CALLES, new BigDecimal("2000.00"), 1000);

        DetalleCalculoArbitrio det100 = distribucionService.calcularServicioArbitrio(
                predio, config, TipoBeneficioArbitrio.NINGUNO, new BigDecimal("100.00"), new ArrayList<>(), new ArrayList<>()
        );

        DetalleCalculoArbitrio det50 = distribucionService.calcularServicioArbitrio(
                predio, config, TipoBeneficioArbitrio.NINGUNO, new BigDecimal("50.00"), new ArrayList<>(), new ArrayList<>()
        );

        assertNotNull(det100);
        assertNotNull(det50);

        BigDecimal esperado50 = det100.getImporteFinalAnual().divide(new BigDecimal("2.00"), 2, RoundingMode.HALF_UP);
        assertEquals(esperado50, det50.getImporteFinalAnual());
    }

    @Test
    @DisplayName("6. Beneficio Legal: Exoneración al 50% para Adulto Mayor")
    void testBeneficioAdultoMayor() {
        PredioTributario predio = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.CASA_HABITACION)
                .usoDeclarado(UsoDeclaradoPredio.VIVIENDA)
                .areaConstruida(new BigDecimal("100.00"))
                .areaTerrenoM2(new BigDecimal("120.00"))
                .build();

        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.RECOLECCION_RESIDUOS, new BigDecimal("4000.00"), 800);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predio, config, TipoBeneficioArbitrio.BENEFICIO_SOCIAL_ADULTO_MAYOR, new BigDecimal("100.00"), new ArrayList<>(), new ArrayList<>()
        );

        assertNotNull(det);
        BigDecimal subtotal = det.getCostoAnualServicio();
        BigDecimal descuento = det.getDescuentoAplicado();
        BigDecimal finalAnual = det.getImporteFinalAnual();

        assertEquals(subtotal.subtract(descuento), finalAnual);
        assertTrue(descuento.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("7. Beneficio Legal: Inafectación Legal Total al 100%")
    void testInafectacionTotal() {
        PredioTributario predio = PredioTributario.builder()
                .tipoPredio(TipoPredio.URBANO)
                .clasificacionPredio(ClasificacionPredio.INSTITUCIONAL)
                .usoDeclarado(UsoDeclaradoPredio.EDUCATIVO_CULTURAL)
                .areaTerrenoM2(new BigDecimal("500.00"))
                .build();

        ConfiguracionArbitrioMunicipal config = crearConfiguracion(TipoServicioArbitrio.SERENAZGO, new BigDecimal("3000.00"), 600);

        DetalleCalculoArbitrio det = distribucionService.calcularServicioArbitrio(
                predio, config, TipoBeneficioArbitrio.INAFECTACION_LEGAL, new BigDecimal("100.00"), new ArrayList<>(), new ArrayList<>()
        );

        assertNotNull(det);
        assertEquals(det.getCostoAnualServicio(), det.getDescuentoAplicado());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), det.getImporteFinalAnual());
    }

    @Test
    @DisplayName("8. División Trimestral y Redondeo Half-Up en Cuotas")
    void testDivisionCuotasTrimestrales() {
        BigDecimal importeAnual = new BigDecimal("123.45");
        BigDecimal cuotaTrimestral = importeAnual.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("30.86"), cuotaTrimestral);
    }

    @Test
    @DisplayName("9. Estimación de frente lineal cuando no está registrado en catastro")
    void testEstimacionFrenteLineal() {
        PredioTributario predio = PredioTributario.builder()
                .areaTerrenoM2(new BigDecimal("100.00"))
                .build();

        BigDecimal frente = distribucionService.estimarFrenteLinealMetros(predio);
        assertNotNull(frente);
        assertTrue(frente.compareTo(new BigDecimal("6.00")) >= 0);
    }
}

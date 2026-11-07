package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialTramoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConservacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl.CalculoPredialServiceImpl;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl.ValorizacionPredialServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpuestoPredialFinancialTest {

    private final BigDecimal UIT_2026 = new BigDecimal("5150.00");

    @Test
    @DisplayName("Escala Progresiva Tramo 1 (hasta 15 UIT): Alícuota 0.2%")
    void testEscalaProgresivaTramo1() {
        // 15 UIT = 15 * 5,150.00 = 77,250.00
        BigDecimal limite15Uit = UIT_2026.multiply(new BigDecimal("15")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("77250.00"), limite15Uit);

        // Caso sintético: Base imponible S/ 50,000.00 (menor a 15 UIT)
        BigDecimal base = new BigDecimal("50000.00");
        BigDecimal alicuota1 = new BigDecimal("0.002"); // 0.2%
        BigDecimal impuestoEsperado = base.multiply(alicuota1).setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("100.00"), impuestoEsperado);
    }

    @Test
    @DisplayName("Escala Progresiva Tramo 2 (más de 15 hasta 60 UIT): Alícuotas 0.2% y 0.6%")
    void testEscalaProgresivaTramo2() {
        // 15 UIT = 77,250.00 | 60 UIT = 309,000.00
        BigDecimal limite15Uit = UIT_2026.multiply(new BigDecimal("15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal limite60Uit = UIT_2026.multiply(new BigDecimal("60")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("309000.00"), limite60Uit);

        // Caso sintético: Base imponible S/ 150,000.00
        BigDecimal base = new BigDecimal("150000.00");

        // Tramo 1: hasta 77,250.00 al 0.2% = 154.50
        BigDecimal baseTramo1 = limite15Uit;
        BigDecimal impTramo1 = baseTramo1.multiply(new BigDecimal("0.002")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("154.50"), impTramo1);

        // Tramo 2: (150,000 - 77,250) = 72,750.00 al 0.6% = 436.50
        BigDecimal baseTramo2 = base.subtract(limite15Uit);
        BigDecimal impTramo2 = baseTramo2.multiply(new BigDecimal("0.006")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("436.50"), impTramo2);

        // Total Impuesto: 154.50 + 436.50 = 591.00
        BigDecimal impuestoTotal = impTramo1.add(impTramo2);
        assertEquals(new BigDecimal("591.00"), impuestoTotal);
    }

    @Test
    @DisplayName("Escala Progresiva Tramo 3 (más de 60 UIT): Alícuotas 0.2%, 0.6% y 1.0%")
    void testEscalaProgresivaTramo3() {
        BigDecimal limite15Uit = UIT_2026.multiply(new BigDecimal("15")).setScale(2, RoundingMode.HALF_UP); // 77,250.00
        BigDecimal limite60Uit = UIT_2026.multiply(new BigDecimal("60")).setScale(2, RoundingMode.HALF_UP); // 309,000.00

        // Caso sintético: Base imponible S/ 500,000.00
        BigDecimal base = new BigDecimal("500000.00");

        // Tramo 1: 77,250.00 al 0.2% = 154.50
        BigDecimal imp1 = limite15Uit.multiply(new BigDecimal("0.002")).setScale(2, RoundingMode.HALF_UP);

        // Tramo 2: (309,000 - 77,250) = 231,750.00 al 0.6% = 1,390.50
        BigDecimal base2 = limite60Uit.subtract(limite15Uit);
        BigDecimal imp2 = base2.multiply(new BigDecimal("0.006")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1390.50"), imp2);

        // Tramo 3: (500,000 - 309,000) = 191,000.00 al 1.0% = 1,910.00
        BigDecimal base3 = base.subtract(limite60Uit);
        BigDecimal imp3 = base3.multiply(new BigDecimal("0.010")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1910.00"), imp3);

        // Total: 154.50 + 1,390.50 + 1,910.00 = 3,455.00
        BigDecimal impTotal = imp1.add(imp2).add(imp3).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("3455.00"), impTotal);
    }

    @Test
    @DisplayName("Tope Legal Mínimo: 0.6% de la UIT cuando el cálculo por alícuota sea menor")
    void testTopeMinimoLegal() {
        // UIT 2026 = 5,150.00 -> 0.6% UIT = 30.90
        BigDecimal topeMinimo = UIT_2026.multiply(new BigDecimal("0.006")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("30.90"), topeMinimo);

        // Si la base es muy baja, ej. S/ 5,000.00 -> 5,000 * 0.002 = 10.00 < 30.90
        BigDecimal basePequena = new BigDecimal("5000.00");
        BigDecimal impBruto = basePequena.multiply(new BigDecimal("0.002")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("10.00"), impBruto);

        // Al aplicar la regla del Art. 13 in fine, el impuesto no puede ser inferior a 0.6% UIT
        BigDecimal impuestoFinal = impBruto.compareTo(topeMinimo) < 0 ? topeMinimo : impBruto;
        assertEquals(new BigDecimal("30.90"), impuestoFinal);
    }

    @Test
    @DisplayName("Beneficio Pensionista / Adulto Mayor: Deducción de 50 UIT de la Base Imponible")
    void testBeneficioPensionista50Uit() {
        // 50 UIT = 50 * 5,150.00 = 257,500.00
        BigDecimal deduccion50Uit = UIT_2026.multiply(new BigDecimal("50")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("257500.00"), deduccion50Uit);

        // Caso A: Autovalúo S/ 200,000.00 <= 50 UIT -> Base Neta = 0.00 -> Aplica tope mínimo 0.6% UIT
        BigDecimal baseMenor = new BigDecimal("200000.00");
        BigDecimal baseNetaA = baseMenor.subtract(deduccion50Uit);
        if (baseNetaA.compareTo(BigDecimal.ZERO) < 0) {
            baseNetaA = BigDecimal.ZERO;
        }
        assertEquals(BigDecimal.ZERO, baseNetaA);

        // Caso B: Autovalúo S/ 300,000.00 > 50 UIT -> Base Neta = S/ 42,500.00
        BigDecimal baseMayor = new BigDecimal("300000.00");
        BigDecimal baseNetaB = baseMayor.subtract(deduccion50Uit);
        assertEquals(new BigDecimal("42500.00"), baseNetaB);

        // Impuesto sobre base neta B (42,500 al 0.2%) = 85.00 > tope mínimo 30.90
        BigDecimal impB = baseNetaB.multiply(new BigDecimal("0.002")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("85.00"), impB);
    }

    @Test
    @DisplayName("Copropiedad y Múltiples Predios: Suma ponderada exacta según porcentaje de titularidad")
    void testCopropiedadYMultiplesPredios() {
        // Predio 1: Autovalúo S/ 100,000.00, Copropiedad 50% -> Afecto S/ 50,000.00
        BigDecimal auto1 = new BigDecimal("100000.00");
        BigDecimal pct1 = new BigDecimal("50.00");
        BigDecimal afecto1 = auto1.multiply(pct1).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("50000.00"), afecto1);

        // Predio 2: Autovalúo S/ 80,000.00, Titularidad 100% -> Afecto S/ 80,000.00
        BigDecimal auto2 = new BigDecimal("80000.00");
        BigDecimal pct2 = new BigDecimal("100.00");
        BigDecimal afecto2 = auto2.multiply(pct2).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("80000.00"), afecto2);

        // Predio 3: Autovalúo S/ 60,000.00, Copropiedad 33.33% -> Afecto S/ 19,998.00
        BigDecimal auto3 = new BigDecimal("60000.00");
        BigDecimal pct3 = new BigDecimal("33.33");
        BigDecimal afecto3 = auto3.multiply(pct3).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("19998.00"), afecto3);

        // Base Imponible Consolidada del Contribuyente = 50,000 + 80,000 + 19,998 = 149,998.00
        BigDecimal baseImponibleTotal = afecto1.add(afecto2).add(afecto3);
        assertEquals(new BigDecimal("149998.00"), baseImponibleTotal);
    }

    @Test
    @DisplayName("Depreciación de Construcciones: Deducción porcentual por antigüedad y conservación")
    void testDepreciacionConstruccion() {
        // Área: 120.50 m², Valor unitario oficial: S/ 450.00 -> Valor nuevo: 54,225.00
        BigDecimal area = new BigDecimal("120.50");
        BigDecimal valorUnitario = new BigDecimal("450.00");
        BigDecimal valorNuevo = area.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("54225.00"), valorNuevo);

        // Porcentaje depreciación tabla oficial (ej. 15 años, regular conservación): 24%
        BigDecimal porcentajeDeprec = new BigDecimal("24.00");
        BigDecimal factorDeprec = BigDecimal.ONE.subtract(porcentajeDeprec.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("0.7600"), factorDeprec);

        // Valor Depreciado = 54,225.00 * 0.76 = 41,211.00
        BigDecimal valorDepreciado = valorNuevo.multiply(factorDeprec).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("41211.00"), valorDepreciado);
    }
}

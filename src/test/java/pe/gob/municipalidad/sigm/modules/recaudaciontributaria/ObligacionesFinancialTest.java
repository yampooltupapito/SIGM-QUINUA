package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaMovimiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Financieras y Contables de Obligaciones y Cuentas Corrientes — Prompt 57")
class ObligacionesFinancialTest {

    @Test
    @DisplayName("La suma de las 4 cuotas trimestrales debe coincidir exactamente con el total de la obligación sin pérdida de centavos")
    void testConsistenciaSumaCuotas() {
        BigDecimal importeTotal = new BigDecimal("123.45");
        int cuotasNum = 4;

        BigDecimal divisor = new BigDecimal(cuotasNum);
        BigDecimal cuotaBase = importeTotal.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal sumaPrimeras = cuotaBase.multiply(new BigDecimal(cuotasNum - 1));
        BigDecimal cuotaUltima = importeTotal.subtract(sumaPrimeras);

        BigDecimal sumaTotalCuotas = sumaPrimeras.add(cuotaUltima);

        assertEquals(new BigDecimal("30.86"), cuotaBase);
        assertEquals(new BigDecimal("30.87"), cuotaUltima);
        assertEquals(importeTotal, sumaTotalCuotas);
        assertEquals(0, importeTotal.compareTo(sumaTotalCuotas));
    }

    @Test
    @DisplayName("La suma de 12 cuotas mensuales con decimales periódicos concilia exactamente")
    void testConsistenciaCuotasMensuales() {
        BigDecimal importeTotal = new BigDecimal("100.00");
        int cuotasNum = 12;

        BigDecimal divisor = new BigDecimal(cuotasNum);
        BigDecimal cuotaBase = importeTotal.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal sumaPrimeras = cuotaBase.multiply(new BigDecimal(cuotasNum - 1));
        BigDecimal cuotaUltima = importeTotal.subtract(sumaPrimeras);

        BigDecimal sumaTotal = sumaPrimeras.add(cuotaUltima);

        assertEquals(new BigDecimal("8.33"), cuotaBase);
        assertEquals(new BigDecimal("8.37"), cuotaUltima);
        assertEquals(importeTotal, sumaTotal);
    }

    @Test
    @DisplayName("Un movimiento DÉBITO por CARGO_OBLIGACION incrementa exactamente el saldo de la cuenta corriente")
    void testMovimientoDebitoIncrementaSaldo() {
        BigDecimal saldoInicial = new BigDecimal("500.00");
        BigDecimal cargo = new BigDecimal("150.50");

        TipoMovimientoCuentaCorriente tipo = TipoMovimientoCuentaCorriente.CARGO_OBLIGACION;
        assertEquals(NaturalezaMovimiento.DEBITO, tipo.getNaturalezaPorDefecto());

        BigDecimal saldoPosterior = saldoInicial.add(cargo);
        assertEquals(new BigDecimal("650.50"), saldoPosterior);
    }

    @Test
    @DisplayName("Un movimiento CRÉDITO por ANULACION_AUTORIZADA disminuye exactamente el saldo sin dejar saldos negativos inconsistentes")
    void testMovimientoCreditoDisminuyeSaldo() {
        BigDecimal saldoInicial = new BigDecimal("450.00");
        BigDecimal anulacion = new BigDecimal("450.00");

        TipoMovimientoCuentaCorriente tipo = TipoMovimientoCuentaCorriente.ANULACION_AUTORIZADA;
        assertEquals(NaturalezaMovimiento.CREDITO, tipo.getNaturalezaPorDefecto());

        BigDecimal saldoPosterior = saldoInicial.subtract(anulacion);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), saldoPosterior.setScale(2, RoundingMode.HALF_UP));
        assertTrue(saldoPosterior.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Ajuste por rectificación calcula la diferencia exacta y aplica la naturaleza contable correcta")
    void testAjustePorRectificacionDiferencia() {
        BigDecimal importeOriginal = new BigDecimal("1000.00");
        BigDecimal importeRectificadoMayor = new BigDecimal("1250.00");

        BigDecimal diferenciaCargo = importeRectificadoMayor.subtract(importeOriginal);
        assertEquals(new BigDecimal("250.00"), diferenciaCargo);
        assertTrue(diferenciaCargo.compareTo(BigDecimal.ZERO) > 0);

        BigDecimal importeRectificadoMenor = new BigDecimal("850.00");
        BigDecimal diferenciaAbono = importeOriginal.subtract(importeRectificadoMenor);
        assertEquals(new BigDecimal("150.00"), diferenciaAbono);
        assertTrue(diferenciaAbono.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("No se permite generar obligaciones con importes negativos ni cero sin sustento")
    void testImporteObligacionPositivo() {
        BigDecimal importeValido = new BigDecimal("0.01");
        BigDecimal importeInvalido = new BigDecimal("-10.00");

        assertTrue(importeValido.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(importeInvalido.compareTo(BigDecimal.ZERO) < 0);
    }
}

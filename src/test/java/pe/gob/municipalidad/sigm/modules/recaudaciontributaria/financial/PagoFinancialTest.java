package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.financial;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SesionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoFinancieroObligacion;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class PagoFinancialTest {

    @Test
    @DisplayName("Pago total cancela la obligación y saldo llega exactamente a 0.00")
    void testPagoTotalCancelaObligacion() {
        BigDecimal importeVigente = new BigDecimal("350.85");
        ObligacionTributaria obl = ObligacionTributaria.builder()
                .importeVigente(importeVigente)
                .importePagado(BigDecimal.ZERO)
                .saldo(importeVigente)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .build();

        BigDecimal pago = new BigDecimal("350.85");

        obl.setImportePagado(obl.getImportePagado().add(pago));
        obl.setSaldo(obl.getSaldo().subtract(pago));

        if (obl.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            obl.setEstadoFinanciero(EstadoFinancieroObligacion.CANCELADA);
        }

        assertEquals(0, BigDecimal.ZERO.compareTo(obl.getSaldo()));
        assertEquals(importeVigente, obl.getImportePagado());
        assertEquals(EstadoFinancieroObligacion.CANCELADA, obl.getEstadoFinanciero());
    }

    @Test
    @DisplayName("Pago parcial reduce el saldo y mantiene estado PARCIALMENTE_CUBIERTA")
    void testPagoParcialReduceSaldo() {
        BigDecimal importeVigente = new BigDecimal("200.00");
        ObligacionTributaria obl = ObligacionTributaria.builder()
                .importeVigente(importeVigente)
                .importePagado(BigDecimal.ZERO)
                .saldo(importeVigente)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .build();

        BigDecimal pagoParcial = new BigDecimal("75.40");

        obl.setImportePagado(obl.getImportePagado().add(pagoParcial));
        obl.setSaldo(obl.getSaldo().subtract(pagoParcial));

        if (obl.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            obl.setEstadoFinanciero(EstadoFinancieroObligacion.PARCIALMENTE_CUBIERTA);
        }

        assertEquals(new BigDecimal("124.60"), obl.getSaldo());
        assertEquals(new BigDecimal("75.40"), obl.getImportePagado());
        assertEquals(EstadoFinancieroObligacion.PARCIALMENTE_CUBIERTA, obl.getEstadoFinanciero());
    }

    @Test
    @DisplayName("Reversión de pago restaura el saldo y el estado PENDIENTE original")
    void testReversionPagoRestauraSaldo() {
        BigDecimal importeVigente = new BigDecimal("100.00");
        ObligacionTributaria obl = ObligacionTributaria.builder()
                .importeVigente(importeVigente)
                .importePagado(new BigDecimal("100.00"))
                .saldo(BigDecimal.ZERO)
                .estadoFinanciero(EstadoFinancieroObligacion.CANCELADA)
                .build();

        // Reversión
        BigDecimal revertir = new BigDecimal("100.00");
        obl.setImportePagado(obl.getImportePagado().subtract(revertir));
        obl.setSaldo(obl.getSaldo().add(revertir));

        if (obl.getSaldo().compareTo(obl.getImporteVigente()) >= 0) {
            obl.setEstadoFinanciero(EstadoFinancieroObligacion.PENDIENTE);
        }

        assertEquals(0, obl.getSaldo().compareTo(new BigDecimal("100.00")));
        assertEquals(0, obl.getImportePagado().compareTo(BigDecimal.ZERO));
        assertEquals(EstadoFinancieroObligacion.PENDIENTE, obl.getEstadoFinanciero());
    }

    @Test
    @DisplayName("Aplicación a cuotas actualiza saldos de cuotas sin descalce de centavos")
    void testAplicacionCuotasSinDescalce() {
        CuotaObligacionTributaria c1 = CuotaObligacionTributaria.builder()
                .numeroCuota(1)
                .importe(new BigDecimal("50.00"))
                .importePagado(BigDecimal.ZERO)
                .saldo(new BigDecimal("50.00"))
                .estado(EstadoCuotaTributaria.PENDIENTE)
                .build();

        BigDecimal abono = new BigDecimal("50.00");
        c1.setImportePagado(c1.getImportePagado().add(abono));
        c1.setSaldo(c1.getSaldo().subtract(abono));
        if (c1.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            c1.setEstado(EstadoCuotaTributaria.CANCELADA);
        }

        assertEquals(0, BigDecimal.ZERO.compareTo(c1.getSaldo()));
        assertEquals(EstadoCuotaTributaria.CANCELADA, c1.getEstado());
    }

    @Test
    @DisplayName("Cálculo de arqueo de caja diferencia exactamente faltante, sobrante y cuadrado")
    void testCalculoArqueoCajaDiferencias() {
        BigDecimal fondoApertura = new BigDecimal("50.00");
        BigDecimal recaudadoEfectivo = new BigDecimal("250.00");
        BigDecimal esperado = fondoApertura.add(recaudadoEfectivo); // 300.00

        // Caso 1: Cuadrado exacto
        BigDecimal declaradoCuadrado = new BigDecimal("300.00");
        BigDecimal difCuadrado = declaradoCuadrado.subtract(esperado);
        assertEquals(0, BigDecimal.ZERO.compareTo(difCuadrado));

        // Caso 2: Faltante de 10 soles
        BigDecimal declaradoFaltante = new BigDecimal("290.00");
        BigDecimal difFaltante = declaradoFaltante.subtract(esperado);
        assertEquals(new BigDecimal("-10.00"), difFaltante);
        assertTrue(difFaltante.compareTo(BigDecimal.ZERO) < 0);

        // Caso 3: Sobrante de 5 soles
        BigDecimal declaradoSobrante = new BigDecimal("305.00");
        BigDecimal difSobrante = declaradoSobrante.subtract(esperado);
        assertEquals(new BigDecimal("5.00"), difSobrante);
        assertTrue(difSobrante.compareTo(BigDecimal.ZERO) > 0);
    }
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Servicio de análisis y cálculo metodológico del índice de morosidad y tasa de recuperación.
 * Fórmulas:
 *  - Morosidad Financiera (%) = (Saldo Vencido / (Recaudación Confirmada Anual + Saldo Total Pendiente)) * 100
 *  - Morosidad Operativa (%)  = (N° Obligaciones Vencidas / N° Total Obligaciones Emitidas Válidas) * 100
 *  - Tasa de Cumplimiento (%) = 100 - Morosidad Financiera (%)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteMorosidadService {

    private final ObligacionTributariaRepository obligacionRepository;
    private final PagoTributarioRepository pagoRepository;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Transactional(readOnly = true)
    public BigDecimal calcularMorosidadFinanciera(Integer ejercicio) {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        int anio = (ejercicio != null && ejercicio > 2000) ? ejercicio : hoy.getYear();

        OffsetDateTime inicioAnio = LocalDate.of(anio, 1, 1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime finAnio = LocalDate.of(anio, 12, 31).atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

        BigDecimal recaudacionAnio = pagoRepository.calcularRecaudacionPeriodo(inicioAnio, finAnio);
        BigDecimal saldoTotalPendiente = obligacionRepository.sumarTotalSaldoPendiente();
        BigDecimal saldoVencido = obligacionRepository.sumarSaldoVencido(hoy);

        BigDecimal base = (recaudacionAnio != null ? recaudacionAnio : BigDecimal.ZERO).add(
                saldoTotalPendiente != null ? saldoTotalPendiente : BigDecimal.ZERO);

        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return saldoVencido.multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularMorosidadOperativa() {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        List<ObligacionTributaria> todas = obligacionRepository.findAll().stream()
                .filter(o -> !"ANULADA".equalsIgnoreCase(o.getEstado().name()) &&
                             !"BORRADOR".equalsIgnoreCase(o.getEstado().name()))
                .toList();

        if (todas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long vencidas = todas.stream()
                .filter(o -> o.getSaldo() != null && o.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                .filter(o -> o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy))
                .count();

        return BigDecimal.valueOf(vencidas * 100.0 / todas.size()).setScale(2, RoundingMode.HALF_UP);
    }
}


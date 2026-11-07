package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.CarteraTramoItemDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servicio de análisis de cartera tributaria pendiente, vencida, exigible y tramos de antigüedad.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteCarteraTributariaService {

    private final ObligacionTributariaRepository obligacionRepository;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    /**
     * Calcula la distribución de la cartera vencida en 5 tramos de antigüedad oficiales.
     */
    @Transactional(readOnly = true)
    public List<CarteraTramoItemDto> obtenerDistribucionPorTramosAntiguedad() {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        List<ObligacionTributaria> todas = obligacionRepository.findAll();

        // Filtrar obligaciones vigentes con saldo > 0 y fecha vencimiento < hoy
        List<ObligacionTributaria> vencidas = todas.stream()
                .filter(o -> o.getSaldo() != null && o.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                .filter(o -> o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy))
                .filter(o -> !"ANULADA".equalsIgnoreCase(o.getEstado().name()) &&
                             !"BORRADOR".equalsIgnoreCase(o.getEstado().name()) &&
                             !"RECTIFICADA".equalsIgnoreCase(o.getEstado().name()))
                .toList();

        BigDecimal saldoTotalVencido = vencidas.stream()
                .map(ObligacionTributaria::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Definición de tramos
        Map<String, List<ObligacionTributaria>> tramosMap = new LinkedHashMap<>();
        tramosMap.put("0 a 30 días", new ArrayList<>());
        tramosMap.put("31 a 90 días", new ArrayList<>());
        tramosMap.put("91 a 180 días", new ArrayList<>());
        tramosMap.put("181 a 365 días", new ArrayList<>());
        tramosMap.put("Más de 1 año", new ArrayList<>());

        for (ObligacionTributaria o : vencidas) {
            long dias = ChronoUnit.DAYS.between(o.getFechaVencimiento(), hoy);
            if (dias <= 30) {
                tramosMap.get("0 a 30 días").add(o);
            } else if (dias <= 90) {
                tramosMap.get("31 a 90 días").add(o);
            } else if (dias <= 180) {
                tramosMap.get("91 a 180 días").add(o);
            } else if (dias <= 365) {
                tramosMap.get("181 a 365 días").add(o);
            } else {
                tramosMap.get("Más de 1 año").add(o);
            }
        }

        List<CarteraTramoItemDto> resultado = new ArrayList<>();
        for (Map.Entry<String, List<ObligacionTributaria>> entry : tramosMap.entrySet()) {
            BigDecimal saldoTramo = entry.getValue().stream()
                    .map(ObligacionTributaria::getSaldo)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal porcentaje = BigDecimal.ZERO;
            if (saldoTotalVencido.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = saldoTramo.multiply(BigDecimal.valueOf(100))
                        .divide(saldoTotalVencido, 2, RoundingMode.HALF_UP);
            }

            resultado.add(CarteraTramoItemDto.builder()
                    .tramo(entry.getKey())
                    .saldo(saldoTramo)
                    .cantidadObligaciones(entry.getValue().size())
                    .porcentaje(porcentaje)
                    .build());
        }

        return resultado;
    }
}


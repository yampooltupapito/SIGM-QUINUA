package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AplicacionPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAplicacionPago;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.AplicacionPagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CajaMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de análisis y reportes de recaudación tributaria confirmada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteRecaudacionService {

    private final PagoTributarioRepository pagoRepository;
    private final AplicacionPagoTributarioRepository aplicacionRepository;
    private final CajaMunicipalRepository cajaRepository;
    private final pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SesionCajaMunicipalRepository sesionCajaRepository;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final List<EstadoPagoTributario> ESTADOS_CONFIRMADOS = List.of(
            EstadoPagoTributario.CONFIRMADO, EstadoPagoTributario.PROCESADO);

    /**
     * Obtiene recaudación agrupada por meses dentro del ejercicio fiscal.
     */
    @Transactional(readOnly = true)
    public List<RecaudacionPeriodoItemDto> obtenerRecaudacionMensualEjercicio(Integer ejercicio) {
        int anio = (ejercicio != null && ejercicio > 2000) ? ejercicio : LocalDate.now(ZONA_LIMA).getYear();
        List<RecaudacionPeriodoItemDto> resultado = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            LocalDate primerDia = LocalDate.of(anio, m, 1);
            LocalDate ultimoDia = primerDia.plusMonths(1).minusDays(1);

            OffsetDateTime desde = primerDia.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            OffsetDateTime hasta = ultimoDia.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

            BigDecimal monto = pagoRepository.calcularRecaudacionPeriodo(desde, hasta);
            String nombreMes = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")).toUpperCase();

            resultado.add(RecaudacionPeriodoItemDto.builder()
                    .etiqueta(nombreMes + " " + anio)
                    .monto(monto != null ? monto : BigDecimal.ZERO)
                    .operaciones(0) // Agregación de alto rendimiento
                    .build());
        }

        return resultado;
    }

    /**
     * Obtiene recaudación agrupada por concepto tributario mediante las aplicaciones efectivas.
     */
    @Transactional(readOnly = true)
    public List<RecaudacionConceptoItemDto> obtenerRecaudacionPorConcepto(ReporteFilterRequest filtro) {
        List<AplicacionPagoTributario> aplicaciones;

        if (filtro != null && filtro.getFechaInicio() != null && filtro.getFechaFin() != null) {
            OffsetDateTime desde = filtro.getFechaInicio().atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            OffsetDateTime hasta = filtro.getFechaFin().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
            aplicaciones = aplicacionRepository.findByEstadoAndPagoFechaHoraPagoBetween(
                    EstadoAplicacionPago.ACTIVA, desde, hasta);
        } else {
            aplicaciones = aplicacionRepository.findByEstado(EstadoAplicacionPago.ACTIVA);
        }

        // Filtrar solo aplicaciones de pagos efectivamente confirmados (excluye reversados/anulados)
        List<AplicacionPagoTributario> validas = aplicaciones.stream()
                .filter(a -> a.getPago() != null && ESTADOS_CONFIRMADOS.contains(a.getPago().getEstado()))
                .filter(a -> a.getObligacion() != null && a.getObligacion().getConcepto() != null)
                .collect(Collectors.toList());

        BigDecimal granTotal = validas.stream()
                .map(AplicacionPagoTributario::getImporteAplicado)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<AplicacionPagoTributario>> porConcepto = validas.stream()
                .collect(Collectors.groupingBy(a -> a.getObligacion().getConcepto().getId()));

        List<RecaudacionConceptoItemDto> lista = new ArrayList<>();
        for (Map.Entry<UUID, List<AplicacionPagoTributario>> entry : porConcepto.entrySet()) {
            AplicacionPagoTributario primer = entry.getValue().get(0);
            BigDecimal suma = entry.getValue().stream()
                    .map(AplicacionPagoTributario::getImporteAplicado)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal porcentaje = BigDecimal.ZERO;
            if (granTotal.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = suma.multiply(BigDecimal.valueOf(100)).divide(granTotal, 2, RoundingMode.HALF_UP);
            }

            lista.add(RecaudacionConceptoItemDto.builder()
                    .conceptoId(entry.getKey())
                    .conceptoCodigo(primer.getObligacion().getConcepto().getCodigo())
                    .conceptoNombre(primer.getObligacion().getConcepto().getNombre())
                    .tipoConcepto(primer.getObligacion().getConcepto().getTipoConcepto() != null ?
                            primer.getObligacion().getConcepto().getTipoConcepto().name() : "OTRO")
                    .montoRecaudado(suma)
                    .porcentaje(porcentaje)
                    .build());
        }

        lista.sort((a, b) -> b.getMontoRecaudado().compareTo(a.getMontoRecaudado()));
        return lista;
    }

    /**
     * Obtiene recaudación agrupada por medio de pago institucional (Efectivo, Tarjeta, etc.).
     */
    @Transactional(readOnly = true)
    public List<RecaudacionMedioItemDto> obtenerRecaudacionPorMedioPago(ReporteFilterRequest filtro) {
        List<PagoTributario> pagos;

        if (filtro != null && filtro.getFechaInicio() != null && filtro.getFechaFin() != null) {
            OffsetDateTime desde = filtro.getFechaInicio().atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            OffsetDateTime hasta = filtro.getFechaFin().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
            pagos = pagoRepository.findByEstadoInAndFechaHoraPagoBetween(ESTADOS_CONFIRMADOS, desde, hasta);
        } else {
            pagos = pagoRepository.findByEstadoIn(ESTADOS_CONFIRMADOS);
        }

        BigDecimal granTotal = pagos.stream()
                .map(PagoTributario::getImporte)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<MedioPagoTributario, List<PagoTributario>> porMedio = pagos.stream()
                .filter(p -> p.getMedioPago() != null)
                .collect(Collectors.groupingBy(PagoTributario::getMedioPago));

        List<RecaudacionMedioItemDto> resultado = new ArrayList<>();
        for (Map.Entry<MedioPagoTributario, List<PagoTributario>> entry : porMedio.entrySet()) {
            BigDecimal suma = entry.getValue().stream()
                    .map(PagoTributario::getImporte)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal porcentaje = BigDecimal.ZERO;
            if (granTotal.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = suma.multiply(BigDecimal.valueOf(100)).divide(granTotal, 2, RoundingMode.HALF_UP);
            }

            resultado.add(RecaudacionMedioItemDto.builder()
                    .medioPago(entry.getKey())
                    .nombreMedio(entry.getKey() != null ? entry.getKey().getDescripcion() : "OTRO")
                    .montoRecaudado(suma)
                    .cantidadPagos(entry.getValue().size())
                    .porcentaje(porcentaje)
                    .build());
        }

        resultado.sort((a, b) -> b.getMontoRecaudado().compareTo(a.getMontoRecaudado()));
        return resultado;
    }

    /**
     * Obtiene consolidado de cajas municipales y sesiones activas/cerradas.
     */
    @Transactional(readOnly = true)
    public List<CajaResumenItemDto> obtenerResumenCajasMunicipales() {
        List<CajaMunicipal> cajas = cajaRepository.findAll();
        List<CajaResumenItemDto> resultado = new ArrayList<>();

        for (CajaMunicipal c : cajas) {
            // Pagos confirmados de las sesiones asociadas a la caja
            List<PagoTributario> pagosCaja = pagoRepository.findAll().stream()
                    .filter(p -> ESTADOS_CONFIRMADOS.contains(p.getEstado()))
                    .filter(p -> p.getSesionCaja() != null && p.getSesionCaja().getCaja() != null)
                    .filter(p -> p.getSesionCaja().getCaja().getId().equals(c.getId()))
                    .collect(Collectors.toList());

            BigDecimal totalEfectivo = pagosCaja.stream()
                    .filter(p -> p.getMedioPago() == MedioPagoTributario.EFECTIVO_CAJA)
                    .map(PagoTributario::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOtros = pagosCaja.stream()
                    .filter(p -> p.getMedioPago() != MedioPagoTributario.EFECTIVO_CAJA)
                    .map(PagoTributario::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resultado.add(CajaResumenItemDto.builder()
                    .cajaId(c.getId())
                    .nombreCaja(c.getNombre())
                    .numeroSesiones(sesionCajaRepository.countByCajaId(c.getId()))
                    .totalEfectivo(totalEfectivo)
                    .totalOtrosMedios(totalOtros)
                    .totalRecaudado(totalEfectivo.add(totalOtros))
                    .build());
        }

        return resultado;
    }
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MetaRecaudacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Servicio especializado en el cálculo de Indicadores Clave (KPIs) y métricas financieras de recaudación.
 * Opera exclusivamente con datos transaccionales confirmados y vigentes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndicadoresRecaudacionService {

    private final PagoTributarioRepository pagoRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final CuentaCorrienteTributariaRepository cuentaCorrienteRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final MetaRecaudacionTributariaRepository metaRepository;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Transactional(readOnly = true)
    public KpiDashboardTributarioDto obtenerKpisDashboard() {
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        LocalDate hoy = ahora.toLocalDate();

        // 1. Fechas de corte mensual y anual
        OffsetDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime finMes = hoy.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime().minusNanos(1);

        OffsetDateTime inicioAnio = hoy.withDayOfYear(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime finAnio = hoy.plusYears(1).withDayOfYear(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime().minusNanos(1);

        // 2. Recaudación Efectivamente Confirmada
        BigDecimal recaudacionMes = pagoRepository.calcularRecaudacionPeriodo(inicioMes, finMes);
        BigDecimal recaudacionAnio = pagoRepository.calcularRecaudacionPeriodo(inicioAnio, finAnio);
        long totalPagosConfirmados = pagoRepository.countPagosConfirmados();

        // 3. Cartera Pendiente y Saldos desde Fuentes Únicas
        BigDecimal saldoTotalPendiente = obligacionRepository.sumarTotalSaldoPendiente();
        BigDecimal saldoVencido = obligacionRepository.sumarSaldoVencido(hoy);
        BigDecimal saldoNoVencido = obligacionRepository.sumarSaldoNoVencido(hoy);
        BigDecimal saldoExigible = obligacionRepository.sumarSaldoExigible();

        // 4. Morosidad Financiera: (Saldo Vencido / (Recaudación Anio + Saldo Pendiente Total)) * 100
        BigDecimal baseMorosidad = recaudacionAnio.add(saldoTotalPendiente);
        BigDecimal tasaMorosidadFinanciera = BigDecimal.ZERO;
        if (baseMorosidad.compareTo(BigDecimal.ZERO) > 0) {
            tasaMorosidadFinanciera = saldoVencido.multiply(BigDecimal.valueOf(100))
                    .divide(baseMorosidad, 2, RoundingMode.HALF_UP);
        }

        // 5. Metas Institucionales
        int ejercicioActual = hoy.getYear();
        Optional<MetaRecaudacionTributaria> metaOpt = metaRepository
                .findFirstByEjercicioAndPeriodoAndConceptoIsNullAndEstado(ejercicioActual, 0, EstadoMetaRecaudacion.VIGENTE);

        BigDecimal metaAnual = null;
        BigDecimal cumplimientoMeta = null;
        String metaDescripcion = "Meta no configurada";

        if (metaOpt.isPresent()) {
            metaAnual = metaOpt.get().getImporteMeta();
            if (metaAnual.compareTo(BigDecimal.ZERO) > 0) {
                cumplimientoMeta = recaudacionAnio.multiply(BigDecimal.valueOf(100))
                        .divide(metaAnual, 2, RoundingMode.HALF_UP);
                metaDescripcion = cumplimientoMeta.toPlainString() + "%";
            }
        }

        // 6. Contribuyentes y Predios
        long totalContribuyentes = contribuyenteRepository.countByEstado(EstadoContribuyente.ACTIVO);
        long contribuyentesConDeuda = obligacionRepository.countContribuyentesConDeuda();
        long totalPredios = predioRepository.countByEstado(EstadoPredio.ACTIVO);

        // 7. Convenios y Notificaciones
        long conveniosVigentes = convenioRepository.countByEstado(EstadoConvenioFraccionamiento.VIGENTE);
        long notificacionesPendientes = notificacionRepository.countByEstadoAdministrativo(
                pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion.EN_GESTION);

        return KpiDashboardTributarioDto.builder()
                .recaudacionConfirmadaTotal(recaudacionAnio)
                .recaudacionMesActual(recaudacionMes)
                .recaudacionAnioActual(recaudacionAnio)
                .metaAnualImporte(metaAnual)
                .porcentajeCumplimientoMeta(cumplimientoMeta)
                .metaDescripcion(metaDescripcion)
                .saldoPendienteTotal(saldoTotalPendiente)
                .saldoVencido(saldoVencido)
                .saldoNoVencido(saldoNoVencido)
                .saldoExigible(saldoExigible)
                .tasaMorosidadFinanciera(tasaMorosidadFinanciera)
                .totalPagosConfirmados(totalPagosConfirmados)
                .totalContribuyentesRegistrados(totalContribuyentes)
                .totalContribuyentesConDeuda(contribuyentesConDeuda)
                .totalPrediosRegistrados(totalPredios)
                .totalConveniosVigentes(conveniosVigentes)
                .notificacionesPendientesRevisar(notificacionesPendientes)
                .fechaCorte(ahora)
                .build();
    }
}


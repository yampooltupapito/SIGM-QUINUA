package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio integrador del Centro de Reportes, Inteligencia Tributaria y Estadísticas de Quinua.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentroReportesTributariosService {

    private final IndicadoresRecaudacionService indicadoresService;
    private final ReporteRecaudacionService recaudacionService;
    private final ReporteCarteraTributariaService carteraService;
    private final ReporteMorosidadService morosidadService;
    private final DefinicionReporteTributarioRepository definicionRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final CuentaCorrienteTributariaRepository cuentaCorrienteRepository;
    private final PreferenciaReporteTributarioRepository preferenciaRepository;
    private final HistorialExportacionReporteTributarioRepository historialRepository;

    @Transactional(readOnly = true)
    public KpiDashboardTributarioDto obtenerResumenEjecutivo() {
        return indicadoresService.obtenerKpisDashboard();
    }

    @Transactional(readOnly = true)
    public List<DefinicionReporteTributario> obtenerCatalogoReportes() {
        return definicionRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<ContribuyenteCumplimientoDto> obtenerEstadisticasContribuyentes() {
        List<ContribuyenteTributario> todos = contribuyenteRepository.findAll().stream()
                .filter(c -> c.getEstado() == EstadoContribuyente.ACTIVO)
                .toList();

        Map<UUID, BigDecimal> saldoPorContribuyente = cuentaCorrienteRepository.findAll().stream()
                .filter(cc -> cc.getContribuyente() != null)
                .collect(Collectors.toMap(
                        cc -> cc.getContribuyente().getId(),
                        cc -> cc.getSaldoActual() != null ? cc.getSaldoActual() : BigDecimal.ZERO,
                        (s1, s2) -> s1
                ));

        Map<TipoContribuyente, List<ContribuyenteTributario>> porTipo = todos.stream()
                .collect(Collectors.groupingBy(ContribuyenteTributario::getTipoContribuyente));

        List<ContribuyenteCumplimientoDto> resultado = new ArrayList<>();
        for (Map.Entry<TipoContribuyente, List<ContribuyenteTributario>> entry : porTipo.entrySet()) {
            long total = entry.getValue().size();
            long conDeuda = entry.getValue().stream()
                    .filter(c -> {
                        BigDecimal saldo = saldoPorContribuyente.get(c.getId());
                        return saldo != null && saldo.compareTo(BigDecimal.ZERO) > 0;
                    })
                    .count();
            long alDia = total - conDeuda;
            BigDecimal cumplimiento = total > 0 ?
                    BigDecimal.valueOf(alDia * 100.0 / total).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            resultado.add(ContribuyenteCumplimientoDto.builder()
                    .tipoContribuyente(entry.getKey() != null ? entry.getKey().getDescripcion() : "GENERAL")
                    .totalRegistrados(total)
                    .conDeuda(conDeuda)
                    .alDia(alDia)
                    .porcentajeCumplimiento(cumplimiento)
                    .build());
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<PredioResumenDto> obtenerEstadisticasPredios() {
        List<PredioTributario> todos = predioRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPredio.ACTIVO)
                .toList();

        BigDecimal granAutovaluo = todos.stream()
                .map(PredioTributario::getValorAutovaluoReferencial)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<TipoPredio, List<PredioTributario>> porTipo = todos.stream()
                .collect(Collectors.groupingBy(PredioTributario::getTipoPredio));

        List<PredioResumenDto> resultado = new ArrayList<>();
        for (Map.Entry<TipoPredio, List<PredioTributario>> entry : porTipo.entrySet()) {
            long total = entry.getValue().size();
            BigDecimal autovaluo = entry.getValue().stream()
                    .map(PredioTributario::getValorAutovaluoReferencial)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal porcentaje = granAutovaluo.compareTo(BigDecimal.ZERO) > 0 ?
                    autovaluo.multiply(BigDecimal.valueOf(100)).divide(granAutovaluo, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            resultado.add(PredioResumenDto.builder()
                    .tipoPredio(entry.getKey() != null ? entry.getKey().getDescripcion() : "URBANO")
                    .totalPredios(total)
                    .totalAutovaluoReferencial(autovaluo)
                    .porcentaje(porcentaje)
                    .build());
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public FraccionamientoResumenDto obtenerEstadisticasFraccionamientos() {
        List<ConvenioFraccionamientoTributario> convenios = convenioRepository.findAll();

        long total = convenios.size();
        BigDecimal montoTotal = convenios.stream()
                .filter(c -> c.getCronograma() != null && c.getCronograma().getImporteTotal() != null)
                .map(c -> c.getCronograma().getImporteTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoPendiente = convenios.stream()
                .filter(c -> c.getCronograma() != null && c.getCronograma().getCuotas() != null)
                .flatMap(c -> c.getCronograma().getCuotas().stream())
                .filter(cuota -> cuota.getSaldo() != null)
                .map(CuotaFraccionamientoTributario::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amortizado = montoTotal.subtract(saldoPendiente);
        if (amortizado.compareTo(BigDecimal.ZERO) < 0) amortizado = BigDecimal.ZERO;

        BigDecimal recuperacion = montoTotal.compareTo(BigDecimal.ZERO) > 0 ?
                amortizado.multiply(BigDecimal.valueOf(100)).divide(montoTotal, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return FraccionamientoResumenDto.builder()
                .totalConvenios(total)
                .montoTotalConvenios(montoTotal)
                .montoAmortizado(amortizado)
                .saldoPendiente(saldoPendiente)
                .porcentajeRecuperacion(recuperacion)
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResumenDto> obtenerEstadisticasNotificaciones() {
        List<NotificacionTributaria> lista = notificacionRepository.findAll();

        Map<TipoNotificacionTributaria, List<NotificacionTributaria>> porTipo = lista.stream()
                .collect(Collectors.groupingBy(NotificacionTributaria::getTipoNotificacion));

        List<NotificacionResumenDto> resultado = new ArrayList<>();
        for (Map.Entry<TipoNotificacionTributaria, List<NotificacionTributaria>> entry : porTipo.entrySet()) {
            long total = entry.getValue().size();
            long entregadas = entry.getValue().stream()
                    .filter(n -> n.getEstadoEntrega() == EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA)
                    .count();
            long validas = entry.getValue().stream()
                    .filter(n -> n.getEstadoValidezLegal() == EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE)
                    .count();

            BigDecimal efectividad = total > 0 ?
                    BigDecimal.valueOf(validas * 100.0 / total).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            resultado.add(NotificacionResumenDto.builder()
                    .tipoNotificacion(entry.getKey() != null ? entry.getKey().getNombre() : "ACTO ADMINISTRATIVO")
                    .totalEmitidas(total)
                    .totalEntregadas(entregadas)
                    .totalValidezLegal(validas)
                    .porcentajeEfectividad(efectividad)
                    .build());
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<HistorialExportacionItemDto> obtenerUltimasExportaciones() {
        return historialRepository.findTop10ByOrderByFechaSolicitudDesc().stream()
                .map(h -> HistorialExportacionItemDto.builder()
                        .id(h.getId())
                        .reporteCodigo(h.getReporteCodigo())
                        .reporteNombre(h.getNombreArchivo() != null ? h.getNombreArchivo() : h.getReporteCodigo())
                        .fechaSolicitud(h.getFechaSolicitud())
                        .formato(h.getFormato())
                        .estado(h.getEstado() != null ? h.getEstado().name() : "COMPLETADO")
                        .usuarioUsername(h.getUsuario() != null ? h.getUsuario().getUsername() : "SISTEMA")
                        .nombreArchivo(h.getNombreArchivo())
                        .tamanioBytes(h.getTamanioBytes())
                        .checksumSha256(h.getChecksumSha256())
                        .build())
                .collect(Collectors.toList());
    }
}


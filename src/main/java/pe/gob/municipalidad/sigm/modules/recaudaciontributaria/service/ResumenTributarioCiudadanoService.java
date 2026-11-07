package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de consolidación de indicadores y resumen para el Dashboard de Mi Portal Tributario.
 * Operación estrictamente de solo lectura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumenTributarioCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final RelacionContribuyentePredioRepository relacionPredioRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final PagoTributarioRepository pagoRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final NotificacionTributariaRepository notificacionRepository;

    @Transactional(readOnly = true)
    public PortalTributarioDashboardDto obtenerDashboardCiudadano() {
        Usuario usuario = contextService.obtenerUsuarioAutenticado();
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();

        if (contribOpt.isEmpty()) {
            return PortalTributarioDashboardDto.builder()
                    .nombreCiudadano(usuario.getNombreCompleto())
                    .contribuyenteActivo(null)
                    .totalPredios(0)
                    .totalObligacionesPendientes(0)
                    .saldoPendienteTotal(BigDecimal.ZERO)
                    .saldoVencido(BigDecimal.ZERO)
                    .totalPagadoUltimoAnio(BigDecimal.ZERO)
                    .totalConveniosVigentes(0)
                    .totalNotificacionesSinLeer(0)
                    .fechaCorte(OffsetDateTime.now())
                    .ultimosPagos(Collections.emptyList())
                    .proximosVencimientos(Collections.emptyList())
                    .comunicacionesRecientes(Collections.emptyList())
                    .build();
        }

        ContribuyenteTributario c = contribOpt.get();
        UUID contribId = c.getId();

        // 1. Predios asociados activos
        long totalPredios = relacionPredioRepository.findByContribuyenteIdAndEstado(
                contribId, EstadoRelacionContribuyentePredio.ACTIVO).size();

        // 2. Obligaciones tributarias
        List<ObligacionTributaria> obligaciones = obligacionRepository.findByContribuyenteId(contribId);
        LocalDate hoy = LocalDate.now();

        long pendientesCount = 0;
        BigDecimal saldoPendiente = BigDecimal.ZERO;
        BigDecimal saldoVencido = BigDecimal.ZERO;

        for (ObligacionTributaria o : obligaciones) {
            if (o.getEstado() != EstadoObligacionTributaria.PAGADA &&
                o.getEstado() != EstadoObligacionTributaria.ANULADA) {

                BigDecimal s = o.getSaldo() != null ? o.getSaldo() : BigDecimal.ZERO;
                if (s.compareTo(BigDecimal.ZERO) > 0) {
                    pendientesCount++;
                    saldoPendiente = saldoPendiente.add(s);
                    if (o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy)) {
                        saldoVencido = saldoVencido.add(s);
                    }
                }
            }
        }

        // 3. Pagos confirmados
        List<PagoTributario> pagos = pagoRepository.findByContribuyenteId(
                contribId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fechaHoraPago"))).getContent();

        List<PagoCiudadanoDto> ultimosPagos = pagos.stream().map(p -> PagoCiudadanoDto.builder()
                .id(p.getId())
                .codigoPago(p.getCodigoPago())
                .fechaPago(p.getFechaHoraPago())
                .montoTotal(p.getImporte())
                .medioPago(p.getMedioPago() != null ? p.getMedioPago().getDescripcion() : "VENTANILLA")
                .estado(p.getEstado() != null ? p.getEstado().name() : "CONFIRMADO")
                .observaciones(p.getObservaciones())
                .build()).collect(Collectors.toList());

        BigDecimal totalPagadoAnio = pagos.stream()
                .map(p -> p.getImporte() != null ? p.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Convenios vigentes
        long conveniosVigentes = convenioRepository.findByContribuyenteIdAndEstadoIn(
                contribId, List.of(EstadoConvenioFraccionamiento.VIGENTE, EstadoConvenioFraccionamiento.APROBADO)).size();

        // 5. Notificaciones recibidas
        List<NotificacionTributaria> notificaciones = notificacionRepository
                .findByContribuyenteIdOrderByFechaCreacionDesc(contribId);

        List<NotificacionCiudadanoDto> recientes = notificaciones.stream().limit(5).map(n -> NotificacionCiudadanoDto.builder()
                .id(n.getId())
                .codigoNotificacion(n.getCodigoNotificacion())
                .tipoNombre(n.getTipoNotificacion().getNombre())
                .categoria(n.getTipoNotificacion().getCategoria().getNombre())
                .asunto(n.getObservaciones())
                .medioEntrega(n.getMedioNotificacion().getNombre())
                .fechaEmision(n.getFechaCreacion())
                .estadoEntrega(n.getEstadoEntrega().getEtiqueta())
                .estadoValidezLegal(n.getEstadoValidezLegal().getEtiqueta())
                .hashDocumento(n.getHashDocumento())
                .formal(n.getTipoNotificacion().getRequiereDocumento())
                .tieneDocumentoAdjunto(n.getDocumentoNotificacion() != null || n.getHashDocumento() != null)
                .observaciones(n.getObservaciones())
                .build()).collect(Collectors.toList());

        // 6. Próximos vencimientos
        List<ObligacionCiudadanoDto> proximosVenc = obligaciones.stream()
                .filter(o -> o.getSaldo() != null && o.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                .filter(o -> o.getEstado() != EstadoObligacionTributaria.PAGADA && o.getEstado() != EstadoObligacionTributaria.ANULADA)
                .sorted((a, b) -> {
                    if (a.getFechaVencimiento() == null) return 1;
                    if (b.getFechaVencimiento() == null) return -1;
                    return a.getFechaVencimiento().compareTo(b.getFechaVencimiento());
                })
                .limit(5)
                .map(o -> ObligacionCiudadanoDto.builder()
                        .id(o.getId())
                        .codigoObligacion(o.getCodigoObligacion())
                        .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "Impuesto Predial")
                        .conceptoCodigo(o.getConcepto() != null ? o.getConcepto().getCodigo() : "PRED")
                        .tipoDeterminacion(o.getTipoDeterminacion() != null ? o.getTipoDeterminacion().name() : "IMPUESTO_PREDIAL")
                        .anioPeriodo(o.getPeriodo() != null ? o.getPeriodo().getAnio() : hoy.getYear())
                        .cuotaNumero(o.getCuotaNumero())
                        .totalCuotas(o.getTotalCuotas())
                        .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : null)
                        .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccionCompleta() : "Quinua")
                        .importeOriginal(o.getImporteDeterminado())
                        .importeVigente(o.getImporteVigente())
                        .importePagado(o.getImportePagado())
                        .saldo(o.getSaldo())
                        .fechaVencimiento(o.getFechaVencimiento())
                        .estado(o.getEstado().name())
                        .estadoFinanciero(o.getEstadoFinanciero() != null ? o.getEstadoFinanciero().name() : "PENDIENTE")
                        .estadoExigibilidad(o.getEstadoExigibilidad() != null ? o.getEstadoExigibilidad().name() : "NO_EXIGIBLE")
                        .vencida(o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy))
                        .build())
                .collect(Collectors.toList());

        PortalTributarioContextoDto ctxDto = PortalTributarioContextoDto.builder()
                .contribuyenteId(c.getId())
                .codigoContribuyente(c.getCodigoContribuyente())
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumento(c.getNumeroDocumento())
                .nombreRazonSocial(c.getNombreRazonSocial())
                .esActivo(true)
                .build();

        return PortalTributarioDashboardDto.builder()
                .nombreCiudadano(usuario.getNombreCompleto())
                .contribuyenteActivo(ctxDto)
                .totalPredios(totalPredios)
                .totalObligacionesPendientes(pendientesCount)
                .saldoPendienteTotal(saldoPendiente)
                .saldoVencido(saldoVencido)
                .totalPagadoUltimoAnio(totalPagadoAnio)
                .totalConveniosVigentes(conveniosVigentes)
                .totalNotificacionesSinLeer(0)
                .fechaCorte(OffsetDateTime.now())
                .ultimosPagos(ultimosPagos)
                .proximosVencimientos(proximosVenc)
                .comunicacionesRecientes(recientes)
                .build();
    }
}


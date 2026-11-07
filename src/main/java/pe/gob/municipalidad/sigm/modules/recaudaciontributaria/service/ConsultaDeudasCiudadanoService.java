package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.DeudaCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.ObligacionCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Consulta y desglose de deudas tributarias pendientes para el ciudadano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaDeudasCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final ObligacionTributariaRepository obligacionRepository;

    @Transactional(readOnly = true)
    public DeudaCiudadanoDto obtenerResumenDeudas() {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return DeudaCiudadanoDto.builder()
                    .deudaTotal(BigDecimal.ZERO)
                    .deudaVigente(BigDecimal.ZERO)
                    .deudaVencida(BigDecimal.ZERO)
                    .deudaExigibleCoactiva(BigDecimal.ZERO)
                    .totalObligacionesDeuda(0)
                    .detalleObligaciones(Collections.emptyList())
                    .build();
        }

        List<ObligacionTributaria> obligaciones = obligacionRepository.findByContribuyenteId(contribOpt.get().getId());
        LocalDate hoy = LocalDate.now();

        BigDecimal deudaTotal = BigDecimal.ZERO;
        BigDecimal deudaVigente = BigDecimal.ZERO;
        BigDecimal deudaVencida = BigDecimal.ZERO;
        BigDecimal deudaExigibleCoactiva = BigDecimal.ZERO;
        List<ObligacionCiudadanoDto> detalle = new ArrayList<>();

        for (ObligacionTributaria o : obligaciones) {
            if (o.getEstado() == EstadoObligacionTributaria.PAGADA ||
                o.getEstado() == EstadoObligacionTributaria.ANULADA) {
                continue;
            }

            BigDecimal saldo = o.getSaldo() != null ? o.getSaldo() : BigDecimal.ZERO;
            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            deudaTotal = deudaTotal.add(saldo);
            boolean vencida = o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy);

            if (o.getEstadoExigibilidad() == EstadoExigibilidadObligacion.EXIGIBLE && vencida) {
                deudaVencida = deudaVencida.add(saldo);
            } else if (vencida) {
                deudaVencida = deudaVencida.add(saldo);
            } else {
                deudaVigente = deudaVigente.add(saldo);
            }

            detalle.add(ObligacionCiudadanoDto.builder()
                    .id(o.getId())
                    .codigoObligacion(o.getCodigoObligacion())
                    .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "Tributo Municipal")
                    .conceptoCodigo(o.getConcepto() != null ? o.getConcepto().getCodigo() : "TRIB")
                    .tipoDeterminacion(o.getTipoDeterminacion() != null ? o.getTipoDeterminacion().name() : "ORDINARIA")
                    .anioPeriodo(o.getPeriodo() != null ? o.getPeriodo().getAnio() : hoy.getYear())
                    .cuotaNumero(o.getCuotaNumero())
                    .totalCuotas(o.getTotalCuotas())
                    .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : null)
                    .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccionCompleta() : "Quinua")
                    .importeOriginal(o.getImporteDeterminado())
                    .importeVigente(o.getImporteVigente())
                    .importePagado(o.getImportePagado())
                    .saldo(saldo)
                    .fechaVencimiento(o.getFechaVencimiento())
                    .estado(o.getEstado().name())
                    .estadoFinanciero(o.getEstadoFinanciero() != null ? o.getEstadoFinanciero().name() : "PENDIENTE")
                    .estadoExigibilidad(o.getEstadoExigibilidad() != null ? o.getEstadoExigibilidad().name() : "NO_EXIGIBLE")
                    .vencida(vencida)
                    .build());
        }

        return DeudaCiudadanoDto.builder()
                .deudaTotal(deudaTotal)
                .deudaVigente(deudaVigente)
                .deudaVencida(deudaVencida)
                .deudaExigibleCoactiva(deudaExigibleCoactiva)
                .totalObligacionesDeuda(detalle.size())
                .detalleObligaciones(detalle)
                .build();
    }
}


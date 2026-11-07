package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.ObligacionCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consulta de obligaciones tributarias para el ciudadano autenticado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaObligacionesCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final ObligacionTributariaRepository obligacionRepository;

    @Transactional(readOnly = true)
    public Page<ObligacionCiudadanoDto> listarMisObligaciones(Pageable pageable) {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<ObligacionTributaria> page = obligacionRepository
                .findByContribuyenteId(contribOpt.get().getId(), pageable);

        return page.map(this::convertirADto);
    }

    @Transactional(readOnly = true)
    public List<ObligacionCiudadanoDto> listarObligacionesPorTipo(String tipoDeterminacion) {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<ObligacionTributaria> list = obligacionRepository.findByContribuyenteId(contribOpt.get().getId());
        return list.stream()
                .filter(o -> tipoDeterminacion == null || (o.getTipoDeterminacion() != null && o.getTipoDeterminacion().name().equalsIgnoreCase(tipoDeterminacion)))
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ObligacionCiudadanoDto obtenerDetalleObligacion(UUID obligacionId) {
        // Valida estrictamente anti-IDOR
        ObligacionTributaria o = contextService.validarAccesoObligacion(obligacionId);
        return convertirADto(o);
    }

    private ObligacionCiudadanoDto convertirADto(ObligacionTributaria o) {
        LocalDate hoy = LocalDate.now();
        boolean vencida = o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy)
                && o.getEstado() != EstadoObligacionTributaria.PAGADA
                && o.getEstado() != EstadoObligacionTributaria.ANULADA;

        return ObligacionCiudadanoDto.builder()
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
                .vencida(vencida)
                .build();
    }
}


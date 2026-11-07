package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.FraccionamientoCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConvenioFraccionamientoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuotaFraccionamientoTributarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consulta de convenios de fraccionamiento y cronogramas de cuotas para el ciudadano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaFraccionamientosCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final CuotaFraccionamientoTributarioRepository cuotaRepository;

    @Transactional(readOnly = true)
    public Page<FraccionamientoCiudadanoDto> listarMisFraccionamientos(Pageable pageable) {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<ConvenioFraccionamientoTributario> page = convenioRepository
                .findByContribuyenteId(contribOpt.get().getId(), pageable);

        return page.map(this::convertirADto);
    }

    @Transactional(readOnly = true)
    public FraccionamientoCiudadanoDto obtenerDetalleFraccionamiento(UUID convenioId) {
        // Valida acceso anti-IDOR
        ConvenioFraccionamientoTributario c = contextService.validarAccesoConvenio(convenioId);
        return convertirADto(c);
    }

    private FraccionamientoCiudadanoDto convertirADto(ConvenioFraccionamientoTributario c) {
        List<FraccionamientoCiudadanoDto.CuotaConvenioCiudadanoDto> cuotasDto = new ArrayList<>();

        if (c.getCronograma() != null) {
            List<CuotaFraccionamientoTributario> cuotas = cuotaRepository
                    .findByCronogramaIdOrderByNumeroCuotaAsc(c.getCronograma().getId());

            LocalDate hoy = LocalDate.now();
            cuotasDto = cuotas.stream().map(cu -> {
                boolean vencida = cu.getFechaVencimiento() != null && cu.getFechaVencimiento().isBefore(hoy)
                        && cu.getEstado() != EstadoCuotaFraccionamiento.PAGADA;

                return FraccionamientoCiudadanoDto.CuotaConvenioCiudadanoDto.builder()
                        .numeroCuota(cu.getNumeroCuota())
                        .fechaVencimiento(cu.getFechaVencimiento())
                        .montoAmortizacion(cu.getImporteCapital())
                        .montoInteres(cu.getImporteInteres())
                        .montoCuotaTotal(cu.getImporteTotal())
                        .montoPagado(cu.getImportePagado())
                        .estadoCuota(cu.getEstado().name())
                        .fechaPago(cu.getFechaPago())
                        .vencida(vencida)
                        .build();
            }).collect(Collectors.toList());
        }

        BigDecimal total = c.getCronograma() != null ? c.getCronograma().getImporteTotal() : BigDecimal.ZERO;
        BigDecimal inicial = c.getCronograma() != null ? c.getCronograma().getImporteCuotaInicial() : BigDecimal.ZERO;
        BigDecimal financiado = c.getCronograma() != null ? c.getCronograma().getImporteFinanciado() : BigDecimal.ZERO;
        int numCuotas = c.getCronograma() != null ? c.getCronograma().getNumeroCuotas() : 0;
        int pagadas = (int) cuotasDto.stream().filter(cu -> "PAGADA".equalsIgnoreCase(cu.getEstadoCuota())).count();
        BigDecimal saldoPendiente = cuotasDto.stream()
                .filter(cu -> !"PAGADA".equalsIgnoreCase(cu.getEstadoCuota()))
                .map(cu -> cu.getMontoCuotaTotal().subtract(cu.getMontoPagado() != null ? cu.getMontoPagado() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FraccionamientoCiudadanoDto.builder()
                .id(c.getId())
                .codigoConvenio(c.getCodigoConvenio())
                .resolucionAprobacion(c.getResolucionAprobacion())
                .fechaAprobacion(c.getFechaAprobacion())
                .montoTotalFraccionado(total)
                .cuotaInicial(inicial)
                .saldoFinanciado(financiado)
                .numeroCuotas(numCuotas)
                .cuotasPagadas(pagadas)
                .saldoPendiente(saldoPendiente)
                .estado(c.getEstado().getDescripcion())
                .programaNombre(c.getPrograma() != null ? c.getPrograma().getNombre() : "Régimen Ordinario")
                .cronogramaCuotas(cuotasDto)
                .build();
    }
}


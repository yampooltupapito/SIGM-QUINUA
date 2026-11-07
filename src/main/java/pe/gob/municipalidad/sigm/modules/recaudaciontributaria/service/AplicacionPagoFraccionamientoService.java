package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoIncidenciaConvenio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de vinculación de pagos confirmados a cuotas de fraccionamiento.
 * Mantiene la conciliación con las obligaciones originales y los estados del convenio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AplicacionPagoFraccionamientoService {

    private final CuotaFraccionamientoTributarioRepository cuotaRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final IncidenciaConvenioFraccionamientoRepository incidenciaRepository;
    private final DetalleSolicitudFraccionamientoRepository detalleRepository;
    private final ObligacionTributariaRepository obligacionRepository;

    @Transactional
    public void aplicarPagoACuota(UUID cuotaId, BigDecimal importePago, Usuario usuario) {
        if (importePago == null || importePago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El importe de pago debe ser mayor a 0");
        }

        CuotaFraccionamientoTributario cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new BusinessException("Cuota de fraccionamiento no encontrada: " + cuotaId));

        if (cuota.getEstado() == EstadoCuotaFraccionamiento.PAGADA) {
            throw new BusinessException("La cuota N° " + cuota.getNumeroCuota() + " ya se encuentra totalmente cancelada");
        }

        if (importePago.compareTo(cuota.getSaldo()) > 0) {
            throw new BusinessException(String.format("El importe aplicado (S/ %s) excede el saldo de la cuota (S/ %s)",
                    importePago, cuota.getSaldo()));
        }

        BigDecimal nuevoPagado = cuota.getImportePagado().add(importePago);
        BigDecimal nuevoSaldo = cuota.getImporteTotal().subtract(nuevoPagado);

        cuota.setImportePagado(nuevoPagado);
        cuota.setSaldo(nuevoSaldo);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            cuota.setEstado(EstadoCuotaFraccionamiento.PAGADA);
            cuota.setFechaPago(LocalDate.now());
        } else {
            cuota.setEstado(EstadoCuotaFraccionamiento.PARCIALMENTE_PAGADA);
        }

        cuotaRepository.save(cuota);

        // Verificar si todas las cuotas del cronograma han sido canceladas
        CronogramaFraccionamientoTributario cronograma = cuota.getCronograma();
        List<CuotaFraccionamientoTributario> todasLasCuotas = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cronograma.getId());
        boolean todasPagadas = todasLasCuotas.stream().allMatch(c -> c.getEstado() == EstadoCuotaFraccionamiento.PAGADA);

        convenioRepository.findByCronogramaId(cronograma.getId())
                .ifPresent(convenio -> {
                    if (todasPagadas) {
                        convenio.setEstado(EstadoConvenioFraccionamiento.CUMPLIDO);
                        convenioRepository.save(convenio);

                        // Actualizar obligaciones tributarias originales a PAGADA
                        List<DetalleSolicitudFraccionamiento> detalles = detalleRepository.findBySolicitudId(convenio.getSolicitud().getId());
                        for (DetalleSolicitudFraccionamiento det : detalles) {
                            ObligacionTributaria obl = det.getObligacion();
                            obl.setEstado(EstadoObligacionTributaria.PAGADA);
                            obl.setSaldo(BigDecimal.ZERO);
                            obligacionRepository.save(obl);
                        }

                        IncidenciaConvenioFraccionamiento incCierre = IncidenciaConvenioFraccionamiento.builder()
                                .convenio(convenio)
                                .tipoIncidencia(TipoIncidenciaConvenio.REVISION_ADMINISTRATIVA)
                                .descripcion("Convenio cumplido en su totalidad. Todas las cuotas y obligaciones originales amortizadas al 100%.")
                                .responsable(usuario)
                                .fechaRegistro(OffsetDateTime.now())
                                .build();
                        incidenciaRepository.save(incCierre);
                        log.info("Convenio de fraccionamiento {} CUMPLIDO en su totalidad", convenio.getCodigoConvenio());
                    } else {
                        IncidenciaConvenioFraccionamiento incPago = IncidenciaConvenioFraccionamiento.builder()
                                .convenio(convenio)
                                .tipoIncidencia(TipoIncidenciaConvenio.REVISION_ADMINISTRATIVA)
                                .descripcion(String.format("Pago aplicado por S/ %s a la cuota N° %d. Saldo restante de cuota: S/ %s",
                                        importePago, cuota.getNumeroCuota(), nuevoSaldo))
                                .responsable(usuario)
                                .fechaRegistro(OffsetDateTime.now())
                                .build();
                        incidenciaRepository.save(incPago);
                    }
                });

        log.info("Pago de S/ {} aplicado a la cuota N° {} del cronograma {}", importePago, cuota.getNumeroCuota(), cronograma.getId());
    }
}

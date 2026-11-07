package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.CuentaCorrienteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MovimientoCuentaCorrienteDto;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ObligacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MovimientoCuentaCorrienteMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ObligacionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MovimientoCuentaCorrienteMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ObligacionMercadoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuentaCorrienteMercadoService {

    private final ComercianteRepository comercianteRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository;
    private final ObligacionMercadoService obligacionService;

    public CuentaCorrienteMercadoService(ComercianteRepository comercianteRepository,
                                         ObligacionMercadoRepository obligacionRepository,
                                         MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository,
                                         ObligacionMercadoService obligacionService) {
        this.comercianteRepository = comercianteRepository;
        this.obligacionRepository = obligacionRepository;
        this.movimientoCcRepository = movimientoCcRepository;
        this.obligacionService = obligacionService;
    }

    @Transactional(readOnly = true)
    public CuentaCorrienteResponse obtenerCuentaCorrienteComerciante(UUID comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + comercianteId));

        BigDecimal saldoTotal = obligacionRepository.sumarDeudaTotalComerciante(comercianteId);
        if (saldoTotal == null) saldoTotal = BigDecimal.ZERO;

        List<ObligacionMercado> exigibles = obligacionRepository.buscarExigiblesPorComerciante(comercianteId);
        List<ObligacionResponse> obligacionResponses = exigibles.stream()
                .map(obligacionService::toResponse)
                .collect(Collectors.toList());

        List<MovimientoCuentaCorrienteMercado> movimientos = movimientoCcRepository.findByComercianteIdOrderByFechaMovimientoAsc(comercianteId);
        List<MovimientoCuentaCorrienteDto> movimientoDtos = movimientos.stream()
                .map(this::toMovimientoDto)
                .collect(Collectors.toList());

        return CuentaCorrienteResponse.builder()
                .comercianteId(comerciante.getId())
                .comercianteNombre(comerciante.getPersona().getNombreCompleto())
                .comercianteDocumento(comerciante.getPersona().getNumeroDocumento())
                .comercianteCodigo(comerciante.getCodigoComerciante())
                .saldoTotalPendiente(saldoTotal)
                .obligacionesExigibles(obligacionResponses)
                .movimientos(movimientoDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<MovimientoCuentaCorrienteDto> buscarMovimientosComerciante(UUID comercianteId, Pageable pageable) {
        return movimientoCcRepository.buscarPaginadoPorComerciante(comercianteId, pageable)
                .map(this::toMovimientoDto);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoCuentaCorrienteDto> buscarMovimientosPuesto(UUID puestoId, Pageable pageable) {
        return movimientoCcRepository.buscarPaginadoPorPuesto(puestoId, pageable)
                .map(this::toMovimientoDto);
    }

    private MovimientoCuentaCorrienteDto toMovimientoDto(MovimientoCuentaCorrienteMercado m) {
        return MovimientoCuentaCorrienteDto.builder()
                .id(m.getId())
                .tipoMovimiento(m.getTipoMovimiento())
                .tipoMovimientoDescripcion(m.getTipoMovimiento() != null ? m.getTipoMovimiento().getDescripcion() : null)
                .concepto(m.getConcepto())
                .monto(m.getMonto())
                .saldoResultante(m.getSaldoResultante())
                .referenciaId(m.getReferenciaId())
                .tipoReferencia(m.getTipoReferencia())
                .fechaMovimiento(m.getFechaMovimiento())
                .usuario(m.getUsuario())
                .build();
    }
}

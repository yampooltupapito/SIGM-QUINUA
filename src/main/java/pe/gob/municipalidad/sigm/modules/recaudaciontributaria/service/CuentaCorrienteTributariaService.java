package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.MovimientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MovimientoCuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.util.UUID;

public interface CuentaCorrienteTributariaService {

    CuentaCorrienteTributaria asegurarCuentaContribuyente(UUID contribuyenteId, String usuario);

    CuentaCorrienteDetailResponse obtenerPorId(UUID cuentaId);

    CuentaCorrienteDetailResponse obtenerPorContribuyenteId(UUID contribuyenteId);

    Page<CuentaCorrienteSummaryDto> listarCuentas(String query, EstadoCuentaCorriente estado, Boolean conDeuda, Pageable pageable);

    MovimientoCuentaCorrienteTributaria registrarMovimiento(
            UUID cuentaCorrienteId,
            ObligacionTributaria obligacion,
            CuotaObligacionTributaria cuota,
            TipoMovimientoCuentaCorriente tipoMovimiento,
            BigDecimal importe,
            String referenciaOrigen,
            String documentoSustento,
            String descripcion,
            String usuarioResponsable
    );

    Page<MovimientoResponse> listarMovimientos(UUID cuentaId, Pageable pageable);
}

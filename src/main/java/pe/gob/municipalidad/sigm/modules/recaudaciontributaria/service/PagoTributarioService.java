package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PagoTributarioService {

    List<ObligacionPagoItemResponse> buscarObligacionesParaCobro(UUID contribuyenteId);

    PagoResponse registrarPago(RegistrarPagoRequest request, String usuario);

    PagoResponse confirmarPago(UUID pagoId, ConfirmarPagoRequest request, String usuario);

    PagoResponse revertirPago(UUID pagoId, RevertirPagoRequest request, String usuario);

    PagoResponse anularPago(UUID pagoId, String motivo, String usuario);

    PagoResponse obtenerPorId(UUID id);

    PagoResponse obtenerPorCodigo(String codigoPago);

    Page<PagoResponse> listarPagos(String busqueda,
                                   UUID contribuyenteId,
                                   UUID sesionCajaId,
                                   MedioPagoTributario medioPago,
                                   EstadoPagoTributario estado,
                                   OffsetDateTime fechaDesde,
                                   OffsetDateTime fechaHasta,
                                   Pageable pageable);
}

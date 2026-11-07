package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TramoAntiguedadDeuda;

import java.time.LocalDate;
import java.util.UUID;

public interface DeudaTributariaService {

    Page<CarteraItemDto> listarCartera(
            String query,
            UUID contribuyenteId,
            UUID predioId,
            UUID conceptoId,
            UUID periodoId,
            EstadoObligacionTributaria estado,
            EstadoExigibilidadObligacion exigibilidad,
            Boolean soloConSaldo,
            LocalDate fechaCorte,
            TramoAntiguedadDeuda tramo,
            Pageable pageable);

    DeudaDetalleResponse obtenerFichaDeuda(UUID obligacionId, LocalDate fechaCorte);

    DashboardDeudasResponse obtenerDashboardDeudas(LocalDate fechaCorte);

    DeudaDetalleResponse clasificarObligacion(UUID obligacionId, ClasificarDeudaRequest request, String username);

    DeudaDetalleResponse asignarResponsable(UUID obligacionId, AsignarResponsableRequest request, String username);

    DeudaDetalleResponse registrarGestion(UUID obligacionId, RegistrarGestionDeudaRequest request, String username);

    DeudaDetalleResponse registrarCompromiso(UUID obligacionId, RegistrarCompromisoDeudaRequest request, String username);
}

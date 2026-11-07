package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteDashboardResponse {
    private UUID comercianteId;
    private String codigoComerciante;
    private String nombreTitular;
    private TipoComerciante tipoComerciante;
    private TipoVinculoComerciante tipoVinculo;
    private int puestosActivos;
    private int asignacionesVigentes;
    private int contratosVigentes;
    private int autorizacionesVigentes;
    private BigDecimal deudaPendienteTotal;
    private int obligacionesPendientesCount;
    private BigDecimal totalPagosRealizados;
    private int pagosRealizadosCount;
    private int solicitudesEnTramite;
    private long notificacionesNoLeidas;
}

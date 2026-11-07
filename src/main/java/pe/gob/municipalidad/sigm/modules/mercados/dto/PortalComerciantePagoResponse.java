package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.MedioPagoMercado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComerciantePagoResponse {
    private UUID pagoId;
    private String codigoPago;
    private String codigoSeguro;
    private OffsetDateTime fechaPago;
    private BigDecimal montoRecibido;
    private BigDecimal montoAplicado;
    private String moneda;
    private MedioPagoMercado medioPago;
    private String medioPagoDescripcion;
    private String referenciaOperacion;
    private EstadoPagoMercado estado;
    private String estadoDescripcion;
    private String hashComprobante;
    private String urlConstanciaPdf;
}

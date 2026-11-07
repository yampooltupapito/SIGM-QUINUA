package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.MedioPagoMercado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {
    private UUID id;
    private String codigoPago;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private OffsetDateTime fechaPago;
    private BigDecimal montoRecibido;
    private BigDecimal montoAplicado;
    private String moneda;
    private MedioPagoMercado medioPago;
    private String medioPagoDescripcion;
    private String referenciaOperacion;
    private EstadoPagoMercado estado;
    private String estadoDescripcion;
    private UUID cajaSesionId;
    private String cajaCodigo;
    private String cajeroUsername;
    private String codigoSeguro;
    private String hashComprobante;
    private String observaciones;
    @Builder.Default
    private List<AplicacionResponse> aplicaciones = new ArrayList<>();
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

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
    private UUID contribuyenteId;
    private String contribuyenteDocumento;
    private String contribuyenteNombre;
    private UUID sesionCajaId;
    private String sesionCajaCodigo;
    private String cajaNombre;
    private String cajeroNombre;
    private BigDecimal importe;
    private String moneda;
    private MedioPagoTributario medioPago;
    private String medioPagoDescripcion;
    private String referenciaExterna;
    private String numeroOperacionBancaria;
    private String bancoOrigen;
    private String idempotencyKey;
    private EstadoPagoTributario estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private OffsetDateTime fechaHoraPago;
    private OffsetDateTime fechaConfirmacion;
    private String usuarioConfirmacionNombre;
    private OffsetDateTime fechaReversion;
    private String usuarioReversionNombre;
    private String motivoReversion;
    private String observaciones;
    @Builder.Default
    private List<AplicacionPagoResponse> aplicaciones = new ArrayList<>();
}

package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmitirDocumentoRequest {

    @NotNull(message = "El identificador del pago confirmado es obligatorio")
    private UUID pagoId;

    private String tipoDocumentoCodigo; // por defecto RECIBO_PAGO

    private String serieCodigo; // opcional, si no se envía se toma la serie activa

    private String idempotencyKey;
}

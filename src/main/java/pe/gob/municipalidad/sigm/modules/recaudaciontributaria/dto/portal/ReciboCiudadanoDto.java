package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReciboCiudadanoDto {
    private UUID id;
    private String codigoDocumento;
    private String serie;
    private String numero;
    private String numeroCompleto;
    private String tipoDocumentoNombre;
    private OffsetDateTime fechaEmision;
    private BigDecimal importeTotal;
    private String estado;
    private String codigoVerificacion;
    private String hashDocumento;
    private String urlVerificacion;
    private boolean anulado;
}


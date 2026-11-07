package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO para la respuesta pública de verificación documental con estricta protección de datos personales.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionPublicaDocumentoResponse {

    private boolean existe;
    private boolean valido;
    private String codigoDocumento;
    private String tipoDocumentoNombre;
    private OffsetDateTime fechaEmision;
    private String contribuyenteEnmascarado;
    private BigDecimal importeTotal;
    private String moneda;
    private EstadoDocumentoPagoTributario estado;
    private String hashDocumento;
    private String codigoVerificacion;
    private List<String> conceptosPagados;
    private String mensajeResultado;
}

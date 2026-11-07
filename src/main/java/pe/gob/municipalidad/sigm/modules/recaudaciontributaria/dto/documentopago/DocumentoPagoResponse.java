package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPagoResponse {

    private UUID id;
    private String codigoDocumento;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private String serieCodigo;
    private Long numeroCorrelativo;

    private UUID pagoId;
    private String codigoPago;
    private String medioPago;

    private UUID contribuyenteId;
    private String codigoContribuyente;
    private String nombreContribuyente;
    private String tipoDocumentoContribuyente;
    private String numeroDocumentoContribuyente;

    private OffsetDateTime fechaEmision;
    private BigDecimal importeTotal;
    private String moneda;
    private EstadoDocumentoPagoTributario estado;

    private String usuarioEmisorNombre;
    private String oficinaEmisoraNombre;

    private String nombreArchivo;
    private String hashDocumento;
    private String codigoVerificacion;
    private String urlVerificacion;

    private String motivoAnulacion;
    private OffsetDateTime fechaAnulacion;
    private String usuarioAnulacionNombre;

    private UUID documentoSustitutorioId;
    private String documentoSustitutorioCodigo;

    private Integer numeroReimpresiones;
}

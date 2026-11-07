package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionActaPublicaResponse {

    private boolean valida;
    private String codigoActa;
    private String codigoInspeccion;
    private String tipoDocumento;
    private String entidadEmisora;
    private String mercadoNombre;
    private String puestoCodigo;
    private OffsetDateTime fechaEmision;
    private String estadoActa;
    private String hashIntegridad;
    private String mensaje;
}

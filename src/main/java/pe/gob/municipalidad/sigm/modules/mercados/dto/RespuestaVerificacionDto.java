package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.ResultadoVerificacion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaVerificacionDto {

    private UUID id;
    private UUID itemVerificacionId;
    private String itemCodigo;
    private String itemCategoria;
    private String itemDescripcion;
    private boolean itemObligatorio;
    private String itemReferenciaNormativa;

    private ResultadoVerificacion resultado;
    private String resultadoDescripcion;
    private String observaciones;
    private OffsetDateTime fechaRegistro;
    private String inspectorRegistroNombre;
}

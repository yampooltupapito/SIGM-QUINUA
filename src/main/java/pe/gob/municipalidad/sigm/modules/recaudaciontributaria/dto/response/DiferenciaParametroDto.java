package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiferenciaParametroDto {
    private String codigo;
    private String nombre;
    private String tipoDiferencia; // MODIFICADO, AGREGADO, ELIMINADO, SIN_CAMBIO
    private String valorVersionAnterior;
    private String valorVersionNueva;
    private String unidad;
    private String origenNormativo;
}

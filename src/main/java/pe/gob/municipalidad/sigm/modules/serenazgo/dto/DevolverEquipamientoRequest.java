package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevolverEquipamientoRequest {

    private Integer cantidadDevuelta;
    private String estadoBienDevolucion;
    private String observaciones;
}

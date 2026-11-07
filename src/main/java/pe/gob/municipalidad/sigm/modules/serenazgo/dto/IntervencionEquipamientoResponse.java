package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionEquipamientoResponse {

    private UUID id;
    private UUID equipamientoId;
    private String equipamientoNombre;
    private String equipamientoCodigo;
    private String equipamientoTipo;
    private Integer cantidad;
    private String estadoUso;
    private String observaciones;
}

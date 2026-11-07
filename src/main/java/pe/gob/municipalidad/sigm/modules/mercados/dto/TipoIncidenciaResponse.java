package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoIncidenciaResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private PrioridadIncidencia prioridadSugerida;
    private Boolean requierePuesto;
    private Boolean activo;
}

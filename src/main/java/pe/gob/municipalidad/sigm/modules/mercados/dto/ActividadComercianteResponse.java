package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActividadComercianteResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String descripcion;
    private boolean principal;
    private String estado;
}

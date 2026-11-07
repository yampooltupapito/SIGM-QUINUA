package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoControlResponse {
    private UUID id;
    private Integer orden;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String referenciaUbicacion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer tiempoEstimadoDesdeInicioMin;
    private Boolean activo;
}

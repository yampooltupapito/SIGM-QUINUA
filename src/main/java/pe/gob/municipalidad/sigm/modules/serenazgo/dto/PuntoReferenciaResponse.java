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
public class PuntoReferenciaResponse {
    private UUID id;
    private UUID sectorId;
    private UUID zonaId;
    private String nombre;
    private String descripcion;
    private String tipo;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estado;
}

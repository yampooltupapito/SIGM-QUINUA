package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private Long oficinaId;
    private String ubicacion;
    private Long cajeroAsignadoId;
    private String cajeroAsignadoNombre;
    private EstadoCajaMunicipal estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private boolean tieneSesionAbierta;
    private UUID sesionActivaId;
    private String sesionActivaCodigo;
    private String observaciones;
}

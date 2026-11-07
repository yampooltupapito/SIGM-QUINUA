package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallazgoResponse {

    private UUID id;
    private UUID inspeccionId;
    private String codigo;
    private String categoria;
    private String descripcion;
    private GravedadHallazgo gravedadTecnica;
    private String gravedadTecnicaDescripcion;
    private String estado;
    private OffsetDateTime fechaRegistro;
    private String responsableRegistroNombre;
    private int totalMedidas;
    private int totalEvidencias;
}

package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionPuestoResponse {

    private Long id;
    private UUID asignacionId;
    private UUID puestoId;
    private OffsetDateTime fechaDevolucion;
    private String responsableRecepcion;
    private String estadoFisicoPuesto;
    private String observaciones;
    private Long documentoActaId;
    private String documentoActaNombre;
}

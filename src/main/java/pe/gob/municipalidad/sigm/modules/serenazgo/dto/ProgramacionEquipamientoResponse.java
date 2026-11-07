package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionEquipamientoResponse {

    private UUID id;
    private UUID equipamientoId;
    private String codigo;
    private String tipoEquipamientoDesc;
    private String marcaModelo;
    private String numeroSerie;
    private Integer cantidad;
    private EstadoReservaRecurso estadoReserva;
    private String estadoReservaDesc;
    private String observaciones;
    private OffsetDateTime createdAt;
}

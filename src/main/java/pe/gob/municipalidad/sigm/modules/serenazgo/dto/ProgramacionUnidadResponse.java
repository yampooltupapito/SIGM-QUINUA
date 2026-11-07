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
public class ProgramacionUnidadResponse {

    private UUID id;
    private UUID unidadId;
    private String codigo;
    private String tipoUnidadDesc;
    private String placa;
    private String marcaModelo;
    private EstadoReservaRecurso estadoReserva;
    private String estadoReservaDesc;
    private String observaciones;
    private OffsetDateTime createdAt;
}

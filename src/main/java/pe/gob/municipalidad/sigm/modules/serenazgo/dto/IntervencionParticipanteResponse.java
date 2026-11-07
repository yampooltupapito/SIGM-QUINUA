package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionParticipanteResponse {

    private UUID id;
    private UUID personalId;
    private String personalNombre;
    private String personalCodigo;
    private String personalCargo;
    private String funcionDesempenada;
    private OffsetDateTime fechaRegistro;
    private String fechaRegistroFormateada;
    private String observaciones;
}

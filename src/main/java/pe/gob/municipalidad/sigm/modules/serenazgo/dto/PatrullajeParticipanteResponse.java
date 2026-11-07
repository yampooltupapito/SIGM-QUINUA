package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeParticipanteResponse {
    private UUID id;
    private UUID personalId;
    private String codigoInterno;
    private String nombreCompleto;
    private String cargo;
    private String funcionDesempenada;
    private OffsetDateTime fechaIncorporacion;
    private OffsetDateTime fechaRetiro;
}

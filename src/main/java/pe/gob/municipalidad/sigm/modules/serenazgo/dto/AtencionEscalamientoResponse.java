package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadEscalamiento;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEscalamientoResponse {

    private UUID id;
    private EntidadEscalamiento entidadDestinataria;
    private String entidadDestinatariaDesc;
    private String motivo;
    private String canalComunicacion;
    private String referenciaComunicacion;
    private String contactoNombre;
    private OffsetDateTime fechaEscalamiento;
    private Boolean confirmacionRecepcion;
    private OffsetDateTime fechaConfirmacion;
    private String usuarioNombre;
    private String observaciones;

    public EntidadEscalamiento getEntidad() {
        return entidadDestinataria;
    }

    public String getNumeroReferencia() {
        return referenciaComunicacion;
    }
}

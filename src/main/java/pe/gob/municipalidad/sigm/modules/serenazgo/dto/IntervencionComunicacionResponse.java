package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadComunicacion;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoComunicacionIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionComunicacionResponse {

    private UUID id;
    private EntidadComunicacion entidadDestinataria;
    private String entidadDestinatariaDesc;
    private String motivo;
    private OffsetDateTime fechaComunicacion;
    private String fechaComunicacionFormateada;
    private CanalComunicacionDespacho canal;
    private String canalDesc;
    private UUID personalComunicadorId;
    private String personalComunicadorNombre;
    private String numeroReferencia;
    private EstadoComunicacionIntervencion estadoComunicacion;
    private String estadoComunicacionDesc;
    private String confirmadoPor;
    private String resultadoComunicacion;
    private String createdBy;
}

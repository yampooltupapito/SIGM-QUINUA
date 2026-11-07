package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadComunicacion;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoComunicacionIntervencion;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionComunicacionRequest {

    @NotNull(message = "Debe indicar la entidad destinataria de la comunicación")
    private EntidadComunicacion entidadDestinataria;

    @NotBlank(message = "Debe fundamentar el motivo de la comunicación")
    @Size(min = 5, max = 2000)
    private String motivo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaComunicacion;

    @NotNull(message = "Debe seleccionar el canal operativo utilizado")
    private CanalComunicacionDespacho canal;

    private UUID personalComunicadorId;

    private String numeroReferencia;

    @Builder.Default
    private EstadoComunicacionIntervencion estadoComunicacion = EstadoComunicacionIntervencion.ENVIADA;

    private String confirmadoPor;

    private String resultadoComunicacion;
}

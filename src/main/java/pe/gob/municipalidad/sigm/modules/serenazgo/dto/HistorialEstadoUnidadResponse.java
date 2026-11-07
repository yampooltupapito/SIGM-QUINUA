package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoUnidadResponse {

    private UUID id;
    private EstadoUnidadMovil estadoAnterior;
    private String estadoAnteriorDesc;
    private EstadoUnidadMovil estadoNuevo;
    private String estadoNuevoDesc;
    private OffsetDateTime fechaEfectiva;
    private String motivo;
    private String usuarioRegistro;
}

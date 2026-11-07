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
public class PatrullajeHistorialResponse {
    private UUID id;
    private String accion;
    private String estadoAnterior;
    private String estadoNuevo;
    private String descripcion;
    private String username;
    private OffsetDateTime fecha;
}

package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoResponse {
    private UUID id;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDate fechaEfectiva;
    private String motivo;
    private Long documentoSustentoId;
    private String documentoNombre;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}

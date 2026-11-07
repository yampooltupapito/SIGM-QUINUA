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
public class HistorialCargoResponse {
    private UUID id;
    private String cargoAnterior;
    private String cargoNuevo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private Long documentoSustentoId;
    private String documentoNombre;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}

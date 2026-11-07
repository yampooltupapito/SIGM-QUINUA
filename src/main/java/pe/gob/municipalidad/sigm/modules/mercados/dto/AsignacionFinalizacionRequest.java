package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionFinalizacionRequest {

    @NotBlank(message = "El motivo de finalización es obligatorio")
    @Size(max = 2000, message = "El motivo no puede exceder 2000 caracteres")
    private String motivoFinalizacion;

    private Long documentoSustentoId;

    private LocalDate fechaEfectivaCierre;

    @Builder.Default
    private boolean registrarDevolucionInmediata = false;

    private String estadoFisicoPuesto; // BUENO, REGULAR, MALO, REQUIERE_MANTENIMIENTO
    private String observacionesDevolucion;
    private Long documentoActaId;
}

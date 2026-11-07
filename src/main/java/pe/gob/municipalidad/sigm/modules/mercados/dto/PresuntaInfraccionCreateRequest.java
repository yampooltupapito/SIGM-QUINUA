package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresuntaInfraccionCreateRequest {

    @NotNull(message = "El mercado municipal es obligatorio")
    private UUID mercadoId;

    private UUID sectorId;
    private UUID pabellonId;
    private UUID puestoId;
    private UUID comercianteId;

    private UUID incidenciaId;
    private UUID inspeccionId;
    private UUID hallazgoId;

    private UUID tipificacionId;

    @NotBlank(message = "La descripción de los hechos constatados es obligatoria")
    @Size(min = 10, max = 3000, message = "La descripción debe tener entre 10 y 3000 caracteres")
    private String descripcionHechos;

    @NotNull(message = "La fecha de detección es obligatoria")
    private LocalDate fechaDeteccion;

    private UUID expedienteId;
    private String expedienteNumero;
}

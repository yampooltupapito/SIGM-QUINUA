package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutorizacionCreateRequest {

    private UUID asignacionId;

    private UUID comercianteId;

    private UUID puestoId;

    private Long tipoAutorizacionId;

    @Builder.Default
    private String tipoAutorizacion = "PADRON_MUNICIPAL";

    @Builder.Default
    private LocalDate fechaSolicitud = LocalDate.now();

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long expedienteId;

    private Long oficinaEmisoraId;

    private Long autoridadCompetenteId;

    private String observaciones;
}

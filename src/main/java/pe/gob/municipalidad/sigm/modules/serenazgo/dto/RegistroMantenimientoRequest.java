package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoMantenimientoRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRecursoSeguridad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroMantenimientoRequest {

    @NotNull(message = "El tipo de recurso es obligatorio")
    private TipoRecursoSeguridad tipoRecurso;

    private UUID unidadId;
    private UUID equipamientoId;

    @NotNull(message = "El tipo de mantenimiento es obligatorio")
    private TipoMantenimientoRecurso tipoMantenimiento;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate fechaProgramada;

    private BigDecimal kilometrajeMantenimiento;

    @NotBlank(message = "La descripción del trabajo técnico es obligatoria")
    private String descripcion;

    private String tallerProveedor;
    private BigDecimal costo;
}

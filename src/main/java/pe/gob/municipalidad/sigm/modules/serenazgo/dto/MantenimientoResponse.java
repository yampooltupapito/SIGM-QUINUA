package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoMantenimientoRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoMantenimientoRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRecursoSeguridad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MantenimientoResponse {

    private UUID id;
    private String codigo;
    private TipoRecursoSeguridad tipoRecurso;
    private String tipoRecursoDesc;
    private UUID unidadId;
    private String unidadInfo;
    private UUID equipamientoId;
    private String equipamientoInfo;
    private TipoMantenimientoRecurso tipoMantenimiento;
    private String tipoMantenimientoDesc;
    private LocalDate fechaProgramada;
    private LocalDate fechaRealizada;
    private BigDecimal kilometrajeMantenimiento;
    private String descripcion;
    private String tallerProveedor;
    private BigDecimal costo;
    private EstadoMantenimientoRecurso estado;
    private String estadoDesc;
    private String motivoCancelacion;
    private String resultadoTecnico;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}

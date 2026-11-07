package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionUnidadResponse {

    private UUID id;
    private UUID unidadId;
    private String unidadCodigo;
    private String unidadPlaca;
    private UUID personalId;
    private String personalNombreCompleto;
    private String personalCodigo;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFinProgramada;
    private OffsetDateTime fechaDevolucionReal;
    private BigDecimal kilometrajeSalida;
    private BigDecimal kilometrajeRetorno;
    private String motivoServicio;
    private EstadoAsignacionRecurso estadoAsignacion;
    private String estadoAsignacionDesc;
    private String estadoInicialUnidad;
    private String estadoRetornoUnidad;
    private String observacionesEntrega;
    private String observacionesDevolucion;
    private String usuarioEntrega;
    private String usuarioRecepcion;
}

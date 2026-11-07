package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudGestionOcupacionResponse {

    private UUID id;
    private String codigoSolicitud;
    private TipoSolicitudGestion tipoSolicitud;
    private String tipoSolicitudLabel;
    private Long procedimientoId;
    private String procedimientoNombre;

    private UUID mercadoId;
    private String mercadoNombre;
    private UUID puestoId;
    private String puestoNumero;

    private UUID asignacionOrigenId;
    private String asignacionOrigenCodigo;
    private UUID contratoOrigenId;
    private String contratoOrigenCodigo;
    private UUID autorizacionOrigenId;
    private String autorizacionOrigenCodigo;

    private UUID comercianteActualId;
    private String comercianteActualNombre;
    private String comercianteActualDocumento;

    private UUID comerciantePropuestoId;
    private String comerciantePropuestoNombre;
    private String comerciantePropuestoDocumento;

    private LocalDate fechaPresentacion;
    private LocalDate fechaInicioSolicitada;
    private LocalDate fechaFinSolicitada;
    private String motivo;

    private EstadoSolicitudGestion estado;
    private String estadoLabel;
    private String estadoBadgeClass;

    private Long expedienteId;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;

    private String usuarioRegistro;
    private String usuarioEvaluacion;
    private OffsetDateTime fechaEvaluacion;
    private String informeEvaluacion;

    private String usuarioDecision;
    private OffsetDateTime fechaDecision;
    private String motivoDecision;

    private String usuarioFormalizacion;
    private OffsetDateTime fechaFormalizacion;

    private UUID asignacionNuevaId;
    private String asignacionNuevaCodigo;
    private UUID contratoNuevoId;
    private UUID autorizacionNuevaId;

    private String tokenVerificacionQr;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

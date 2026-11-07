package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionEquipamientoResponse {

    private UUID id;
    private UUID equipamientoId;
    private String equipamientoCodigo;
    private String equipamientoDescripcion;
    private UUID personalId;
    private String personalNombre;
    private String personalCodigo;
    private UUID unidadId;
    private String unidadInfo;
    private OffsetDateTime fechaEntrega;
    private OffsetDateTime fechaDevolucion;
    private Integer cantidad;
    private Integer cantidadDevuelta;
    private EstadoAsignacionRecurso estadoAsignacion;
    private String estadoAsignacionDesc;
    private String estadoBienEntrega;
    private String estadoBienDevolucion;
    private String motivo;
    private String observaciones;
    private String usuarioEntrega;
    private String usuarioRecepcion;
}
